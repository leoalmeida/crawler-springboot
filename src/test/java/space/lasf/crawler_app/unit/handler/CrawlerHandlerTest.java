package space.lasf.crawler_app.unit.handler;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import space.lasf.crawler_app.component.JsoupConnectionWrapper;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.handler.CrawlerHandler;
import space.lasf.crawler_app.repository.CrawlerRepository;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrawlerHandlerTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JsoupConnectionWrapper jsoupConnectionWrapper;
    
    @Mock
    private CrawlerRepository crawlerRepository;

    @InjectMocks
    private CrawlerHandler crawlerHandler;

    private Crawler testRequest;
    private final String baseUrl = "https://example.com";
    private final String keyword = "java";

    @BeforeEach
    void setUp() {
        testRequest = new Crawler();
        testRequest.setKeyword(keyword);
        testRequest.setSearchKey("test-key");
        testRequest.startProcess();
    }

    private Document createMockDocument(String html) {
        return Document.createShell(baseUrl).html(html).ownerDocument();
    }

    @Test
    @DisplayName("Should iteratively crawl internal links and find keywords")
    void crawlResource_shouldCrawlIteratively() throws IOException {
        // Arrange
        String htmlBody = "<html><body>" +
                "<a href='http://page1.com'>Link 1</a>" +
                "<a href='https://page2.com/resource'>Link 2</a>" +
                "<a href='/relative/path'>Relative</a>" +
                "</body></html>";
        String expectedUrl = baseUrl + "?q=test%20keyword";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(htmlBody);
        String url1 = baseUrl + "/";
        String url2 = baseUrl + "/page2";
        String url3 = baseUrl + "/page3";
        String externalUrl = "https://othersite.com";


        when(restTemplate.getForEntity(eq(expectedUrl), eq(String.class))).thenReturn(responseEntity);
        Document doc1 = createMockDocument(String.format("<html><body>Here is the %s keyword. <a href='%s'>Link 2</a> <a href='%s'>External</a></body></html>", keyword, url2, externalUrl));
        Document doc2 = createMockDocument(String.format("<html><body>No keyword here. <a href='%s'>Link 3</a> <a href='%s'>Cycle Link</a></body></html>", url3, url1));
        Document doc3 = createMockDocument(String.format("<html><body>Another %s keyword.</body></html>", keyword));

        when(jsoupConnectionWrapper.connect(url1)).thenReturn(doc1);
        when(jsoupConnectionWrapper.connect(url2)).thenReturn(doc2);
        when(jsoupConnectionWrapper.connect(url3)).thenReturn(doc3);

        // When persistChanges is called, just return the
        when(crawlerRepository.save(any(Crawler.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        crawlerHandler.crawlResource(baseUrl, testRequest);

        // Assert
        ArgumentCaptor<Crawler> crawlerCaptor = ArgumentCaptor.forClass(Crawler.class);
        verify(crawlerRepository, times(3)).save(crawlerCaptor.capture());
        
        Crawler finalizedRequest = crawlerCaptor.getValue();
        Set<String> urls = finalizedRequest.getUrls();

        assertEquals(3, urls.size());
        assertTrue(urls.contains("http://page1.com/"));
        assertTrue(urls.contains("https://page2.com/resource"));
        assertTrue(urls.contains(baseUrl + "/relative/path"));
    }

    @Test
    @DisplayName("Should finalize request with empty URLs when HTTP client fails")
    void crawlResource_onHttpClientError_shouldEndRequest() {
        // Arrange
        String expectedUrl = baseUrl + "?q=test%20keyword";
        when(restTemplate.getForEntity(eq(expectedUrl), eq(String.class)))
                .thenThrow(HttpClientErrorException.class);
        
        // When persistChanges is called, just return the 
        when(crawlerRepository.save(any(Crawler.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        crawlerHandler.crawlResource(baseUrl, testRequest);

        // Assert
        ArgumentCaptor<Crawler> crawlerCaptor = ArgumentCaptor.forClass(Crawler.class);
        verify(crawlerRepository, times(1)).save(crawlerCaptor.capture());

        Crawler finalizedRequest = crawlerCaptor.getValue();
        assertTrue(finalizedRequest.getUrls().isEmpty());
    }
}