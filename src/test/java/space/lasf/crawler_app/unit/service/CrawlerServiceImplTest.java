package space.lasf.crawler_app.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.handler.CrawlerHandler;
import space.lasf.crawler_app.service.CrawlerServiceImpl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CrawlerServiceImplTest {

    @Mock
    private CrawlerHandler crawlerHandler;

    @InjectMocks
    private CrawlerServiceImpl crawlerService;

    private final String testUrl = "http://test-crawler.com";

    @BeforeEach
    void setUp() {
        // Use ReflectionTestUtils to set the value of the @Value annotated field
        ReflectionTestUtils.setField(crawlerService, "crawlerUrl", testUrl);
    }

    @Test
    @DisplayName("Should call CrawlerHandler with correct URL and request object")
    void crawlResource_shouldDelegateToHandler() {
        // Arrange
        Crawler request = new Crawler();
        request.setSearchKey("test-key");

        // Act
        crawlerService.crawlResource(request);

        // Assert
        // Verify that the handler's crawlResource method was called exactly once
        // with the configured URL and the provided request object.
        verify(crawlerHandler, times(1)).crawlResource(testUrl, request);
    }
}