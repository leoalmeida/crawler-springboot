package space.lasf.crawler_app.controller;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import space.lasf.crawler_app.dto.CrawlDto;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.service.ICrawlerService;
import space.lasf.crawler_app.service.RequestService;

/**
 * Controller para gerenciamento de requests.
 */
@RestController
@RequestMapping("/crawl")
public class CrawlerController {

    Logger LOG = LoggerFactory.getLogger(CrawlerController.class);

    @Autowired
    private RequestService requestService;

    @Autowired
	private ICrawlerService crawlerService;

    @GetMapping
    public ResponseEntity<List<CrawlDto>> getAllRequests() {
        List<CrawlDto> requests = requestService.findAllRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrawlDto> getRequestById(@PathVariable String id) {
        return requestService.findRequestByKey(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, String>> createRequest(@RequestBody Map<String, String> request) {
        if (null == request || null == request.get("keyword") ){
            throw new IllegalArgumentException("The request should have a valid keyword");
        }
        Crawler result = requestService.createRequest(request.get("keyword"))
                                        .orElseThrow();
        crawlerService.crawlResource(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", result.getSearchKey()));
    }

}