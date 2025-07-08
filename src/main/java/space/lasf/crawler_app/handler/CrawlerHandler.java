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
	
    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlerHandler.class);
	//REGEX to filter the resources like Pdf, gif, xls, etc
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz|pdf|xls|xlsx|doc|docx))$");

    @Autowired
    private CrawlerRepository crawlerRepository;

    @Autowired
    private JsoupConnectionWrapper jsoupConnectionWrapper;

	private Crawler persistChanges(Crawler processEntity) {
        return crawlerRepository.save(processEntity);
    }

	private Crawler findKeywordInText(String url, String text, Crawler processEntity) {
        Crawler result = (!text.contains(processEntity.getKeyword()))
							?processEntity
							:persistChanges(processEntity.addLink(url));
		LOGGER.info(MessageFormat.format("found: [{0}]", result.getUrls().size()));
		//LOGGER.info(MessageFormat.format("Keyword encontrada: [{0}]", url));	
		return result;
	
    }

    @Transactional
	public Crawler crawlResource(String link, Crawler request) {
		Set<String> visitedUrls = new HashSet<>();
		Queue<String> urlsToVisit = new LinkedList<>();

		urlsToVisit.add(link);
		visitedUrls.add(link);

		Crawler currentRequestState = request;

		while (!urlsToVisit.isEmpty() && visitedUrls.size() <= 1000) {
			String currentUrl = urlsToVisit.poll();
			LOGGER.info(MessageFormat.format("Crawling URL: [{0}]", currentUrl));

			try {
				Document doc = jsoupConnectionWrapper.connect(currentUrl);
				currentRequestState = findKeywordInText(currentUrl, doc.html(), currentRequestState);

				Elements elements = doc.select("a");
				for (Element element : elements) {
					String nextUrl = element.absUrl("href");

					if (!StringUtils.hasLength(nextUrl) || nextUrl.contains("#") || FILTERS.matcher(nextUrl).matches()) {
						continue;
					}

					// Check if it's an internal link and not visited yet
					if (nextUrl.startsWith(link) && visitedUrls.add(nextUrl)) {
						urlsToVisit.add(nextUrl);
					}
				}
			} catch (IOException e) {
				LOGGER.error(MessageFormat.format("IOException Error while crawling [{0}]: [{1}]", currentUrl, e.getMessage()));
				// Continue to the next URL in the queue
			}
		}

        return persistChanges(currentRequestState.endProcess());
    }
}
