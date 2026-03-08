package space.lasf.crawler_app.unit.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.lasf.crawler_app.component.JsoupConnectionWrapper;
import space.lasf.crawler_app.entity.CrawlStatus;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.handler.CrawlerHandler;
import space.lasf.crawler_app.repository.CrawlerRepository;

@ExtendWith(MockitoExtension.class)
class CrawlerHandlerTest {

    @Mock
    private JsoupConnectionWrapper jsoupConnectionWrapper;

    @Mock
    private CrawlerRepository crawlerRepository;

    @InjectMocks
    private CrawlerHandler crawlerHandler;

    private Crawler testRequest;
    private final String baseUrl = "https://example.com";

    @BeforeEach
    void setUp() {
        testRequest = new Crawler();
        testRequest.setKeyword("java");
        testRequest.setSearchKey("test-key");
        testRequest.startProcess();
        when(crawlerRepository.save(any(Crawler.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should crawl internal links and add urls containing keyword")
    void crawlResource_shouldCrawlAndCollectMatchingUrls() throws IOException {
        String url1 = baseUrl;
        String url2 = baseUrl + "/page2";
        String url3 = baseUrl + "/page3";

        Document doc1 = parseHtml(
                url1,
                "<html><body>java keyword <a href='/page2'>Page2</a><a href='https://external.com/out'>External</a></body></html>");
        Document doc2 = parseHtml(url2, "<html><body>no match <a href='/page3'>Page3</a></body></html>");
        Document doc3 = parseHtml(url3, "<html><body>another java hit</body></html>");

        when(jsoupConnectionWrapper.connect(url1)).thenReturn(doc1);
        when(jsoupConnectionWrapper.connect(url2)).thenReturn(doc2);
        when(jsoupConnectionWrapper.connect(url3)).thenReturn(doc3);

        Crawler result = crawlerHandler.crawlResource(baseUrl, testRequest);

        assertEquals(CrawlStatus.DONE.name(), result.getStatus());
        Set<String> urls = result.getUrls();
        assertEquals(2, urls.size());
        assertTrue(urls.contains(url1));
        assertTrue(urls.contains(url3));
        verify(crawlerRepository, atLeast(3)).save(any(Crawler.class));
    }

    @Test
    @DisplayName("Should end request even if first page fetch fails")
    void crawlResource_whenFetchFails_shouldStillEndRequest() throws IOException {
        when(jsoupConnectionWrapper.connect(baseUrl)).thenThrow(new IOException("network error"));

        Crawler result = crawlerHandler.crawlResource(baseUrl, testRequest);

        assertEquals(CrawlStatus.DONE.name(), result.getStatus());
        assertTrue(result.getUrls().isEmpty());
        verify(crawlerRepository, atLeast(1)).save(any(Crawler.class));
    }

    private static Document parseHtml(String baseUri, String html) {
        return Parser.parse(html, baseUri);
    }
}
