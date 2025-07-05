package space.lasf.crawler_app.handler;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
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

	public Crawler getLinks(String crawlingUrl, String url, Set<String> urls, Crawler request) {
		LOGGER.info(MessageFormat.format("Quantidade pesquisada: [{0}]", urls.size()));
		LOGGER.info(MessageFormat.format("Quantidade encontrada: [{0}]", request.getUrls().size()));
		//checks if the same url is already visited
		if(!urls.add(url) || urls.size()>1000){
			LOGGER.info(MessageFormat.format("URL já pesquisada: [{0}]", url));
			return request;
		}
		Crawler resultCrawler = request;
		try {
			Document doc = Jsoup.connect(url).get();
        	resultCrawler = findKeywordInText(url, doc.html(), resultCrawler);
			Elements elements = doc.select("a");
			for(Element element : elements){

				String nextUrl = element.absUrl("href");

				//checks if the url is empty or starts '#'
				if(!StringUtils.hasLength(nextUrl) || (StringUtils.hasLength(nextUrl) && ( nextUrl.contains("#"))))
					continue;

				//checks if the url have the pattern matching resources
				if(FILTERS.matcher(nextUrl).matches()){
					continue;
				}
				
				//checks if the url is external or internal
				if(nextUrl.startsWith(crawlingUrl)){
					resultCrawler = getLinks(crawlingUrl, nextUrl, urls, resultCrawler);
				}
			}
			return resultCrawler;
		} catch (IOException e) {
			LOGGER.error(MessageFormat.format("IOException Error: [{0}]", e.getMessage()));
			return resultCrawler.errorProcess();
		}
	}

    @Transactional
	private Crawler persistChanges(Crawler processEntity) {
        return crawlerRepository.save(processEntity);
    }

	private Crawler findKeywordInText(String url, String text, Crawler processEntity) {
        Crawler result = (!text.contains(processEntity.getKeyword()))
							?processEntity
							:persistChanges(processEntity.addLink(url));
		LOGGER.info(MessageFormat.format("Keyword encontrada: [{0}]", url));	
		return result;
	
    }

	public Crawler crawlResource(String link, Crawler request) {
		Crawler result = getLinks(link, link, new HashSet<>(), request);
        return persistChanges(result.endProcess());
    }
}
