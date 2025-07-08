package space.lasf.crawler_app.service;


import java.util.List;
import java.util.Optional;

import space.lasf.crawler_app.dto.CrawlDto;
import space.lasf.crawler_app.entity.Crawler;

/**
 * Serviço para gerenciamento de clientes.
 */
public interface IRequestService {
    
    Optional<Crawler> createRequest(String keyword);
    
    Crawler endRequest(Crawler request);

    Optional<CrawlDto> findRequestByKey(String key);
    
    List<CrawlDto> findAllRequests();
}