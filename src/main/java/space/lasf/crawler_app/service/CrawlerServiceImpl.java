package space.lasf.crawler_app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.handler.CrawlerHandler;

/**
 * Implementação do serviço para gerenciamento de clientes.
 */
@Service
public class CrawlerServiceImpl implements ICrawlerService {
    Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	CrawlerHandler crawlerHandler;
    
    @Value("${app.crawler.url}")
    private String crawlerUrl;
	
	@Override
    @Async
	public void crawlResource(Crawler request) {
        logger.info("Starting request..." + request.getSearchKey());
        crawlerHandler.crawlResource(crawlerUrl,request);
        logger.info("End of processing request..." + request.getSearchKey());
	}

}