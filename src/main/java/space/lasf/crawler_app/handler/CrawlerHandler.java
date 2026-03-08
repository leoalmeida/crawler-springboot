package space.lasf.crawler_app.handler;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import space.lasf.crawler_app.component.JsoupConnectionWrapper;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.repository.CrawlerRepository;


@Component
public class CrawlerHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerHandler.class);
	// REGEX to filter resources like PDF, gif, xls, etc.
	private static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz|pdf|xls|xlsx|doc|docx))$");
	private static final int MAX_VISITED_URLS = 1000;

    @Autowired
    private CrawlerRepository crawlerRepository;

    @Autowired
    private JsoupConnectionWrapper jsoupConnectionWrapper;

	private Crawler persistChanges(final Crawler processEntity) {
        return crawlerRepository.save(processEntity);
    }

	private Crawler findKeywordInText(final String url, final String text,
			final Crawler processEntity) {
        Crawler result = (!text.contains(processEntity.getKeyword()))
				? processEntity
				: persistChanges(processEntity.addLink(url));
		LOGGER.info(MessageFormat.format("found: [{0}]", result.getUrls().size()));
		return result;
	}

	private boolean shouldSkipLink(final String nextUrl) {
		return !StringUtils.hasLength(nextUrl)
				|| nextUrl.contains("#")
				|| FILTERS.matcher(nextUrl).matches();
	}

	private void addInternalUnvisitedUrls(final Document doc, final String baseLink,
			final Set<String> visitedUrls, final Queue<String> urlsToVisit) {
		Elements elements = doc.select("a");
		for (Element element : elements) {
			String nextUrl = element.absUrl("href");
			if (shouldSkipLink(nextUrl)) {
				continue;
			}
			if (nextUrl.startsWith(baseLink) && visitedUrls.add(nextUrl)) {
				urlsToVisit.add(nextUrl);
			}
		}
	}

	private Crawler processCurrentUrl(final String currentUrl, final String baseLink,
			final Crawler currentRequestState, final Set<String> visitedUrls,
			final Queue<String> urlsToVisit) throws IOException {
		Document doc = jsoupConnectionWrapper.connect(currentUrl);
		Crawler updatedState = findKeywordInText(currentUrl, doc.html(), currentRequestState);
		addInternalUnvisitedUrls(doc, baseLink, visitedUrls, urlsToVisit);
		return updatedState;
    }

    @Transactional
	public Crawler crawlResource(final String link, final Crawler request) {
		Set<String> visitedUrls = new HashSet<>();
		Queue<String> urlsToVisit = new LinkedList<>();
		urlsToVisit.add(link);
		visitedUrls.add(link);

		Crawler currentRequestState = request;
		while (!urlsToVisit.isEmpty() && visitedUrls.size() <= MAX_VISITED_URLS) {
			String currentUrl = urlsToVisit.poll();
			LOGGER.info(MessageFormat.format("Crawling URL: [{0}]", currentUrl));
			try {
				currentRequestState = processCurrentUrl(
						currentUrl, link, currentRequestState, visitedUrls, urlsToVisit);
			} catch (IOException e) {
				LOGGER.error(
						MessageFormat.format(
								"IOException Error while crawling [{0}]: [{1}]",
								currentUrl,
								e.getMessage()));
			}
		}
        return persistChanges(currentRequestState.endProcess());
    }
}
