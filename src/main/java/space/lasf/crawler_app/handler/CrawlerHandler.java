package space.lasf.crawler_app.handler;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.repository.CrawlerRepository;


@Component
public class CrawlerHandler {
	
    private final Logger logger = LoggerFactory.getLogger(getClass());
	//REGEX to filter the resources like Pdf, gif, xls, etc
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz|pdf|xls|xlsx|doc|docx))$");
	
	private final Set<String> internalLinks = new TreeSet<>();
	private final Set<String> resultLinks = new TreeSet<>();
	private Crawler processEntity;
	private Integer count = 0;

    @Autowired
    private CrawlerRepository crawlerRepository;
	
	/**
	 * This method does the operation to crawl the respective link and collect all the links from the page 
	 * to make set for the next set of URL to be crawled. Thus each and every url in the set needs to called
	 *  again and new set of links needs to created. While goes on we have use a way to avoid calling the same 
	 *  again if it had been already visited. In the course of extracting links, any Web crawler will encounter 
	 *  multiple links to the same document. To avoid downloading and processing a document multiple times, 
	 *  a URL-seen test must be performed on each extracted link before adding it to the URL frontier

	 * @param crawlingUrl
	 * @param url
	 * @param urls
	 */
	public void getLinks(String crawlingUrl, String url, Set<String> urls) {
		
		//checks if the same url is already visited
		if(!urls.add(url) || (count++>=100))
			return;
		try {
			Document doc = Jsoup.connect(url).get();
        	findKeywordInText(url, doc.html());
			Elements elements = doc.select("a");
			elements.stream().map(element -> element.absUrl("href"))
				//checks if the url is empty or starts '#'
				//and check if the url have the pattern matching resources
				//and check if the url is external or internal
				.filter((nextUrl) -> (StringUtils.hasLength(nextUrl) && !nextUrl.contains("#")) 
									&& !FILTERS.matcher(nextUrl).matches()
									&& nextUrl.startsWith(crawlingUrl))
				.forEach(nextUrl -> {
					logger.info("internal link found: [" + nextUrl + "]");
					internalLinks.add(nextUrl);
					getLinks(crawlingUrl, nextUrl, urls);
				});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void persistChanges() {
		processEntity.setLastUpdate(LocalDateTime.now());
        crawlerRepository.save(processEntity);
    }

	private void findKeywordInText(String url, String text) {
        Boolean isPresent = text.contains(processEntity.getKeyword());
		count++;
		if (isPresent){
			logger.info("Keyword encontrada na URL: [" + url + "]");
			resultLinks.add(url);
			persistChanges();
		}
        logger.info(count + "-> is Present ? : " + isPresent + "-> URL: " + url);
    }

	public Crawler crawlResource(String link, Crawler request) {
		this.processEntity = request;
		this.processEntity.setUrls(resultLinks);
		getLinks(link, link, new HashSet<>());
		this.processEntity.endProcess();
		persistChanges();
        return this.processEntity;
    }
}
