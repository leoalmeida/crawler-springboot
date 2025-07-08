package space.lasf.crawler_app.unit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.lasf.crawler_app.component.CodeGenerator;
import space.lasf.crawler_app.dto.CrawlDto;
import space.lasf.crawler_app.entity.CrawlStatus;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.repository.CrawlerRepository;
import space.lasf.crawler_app.service.RequestServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private CrawlerRepository crawlerRepository;

    @Mock
    private CodeGenerator codeGenerator;

    @InjectMocks
    private RequestServiceImpl requestService;

    @Nested
    @DisplayName("createRequest Tests")
    class CreateRequestTests {

        @Test
        @DisplayName("Should create and return request for a valid keyword")
        void createRequest_withValidKeyword_shouldCreateAndReturnRequest() {
            // Arrange
            String keyword = "valid keyword";
            String expectedCode = "mocked12";
            when(codeGenerator.generateRandomCode()).thenReturn(expectedCode);
            when(crawlerRepository.save(any(Crawler.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Optional<Crawler> result = requestService.createRequest(keyword);

            // Assert
            assertTrue(result.isPresent());
            Crawler savedCrawler = result.get();

            assertEquals(keyword, savedCrawler.getKeyword());
            assertEquals(expectedCode, savedCrawler.getSearchKey());
            assertEquals(CrawlStatus.ACTIVE, savedCrawler.getStatus());
            verify(codeGenerator).generateRandomCode();
            verify(crawlerRepository).save(any(Crawler.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null keyword")
        void createRequest_withNullKeyword_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () -> requestService.createRequest(null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for empty keyword")
        void createRequest_withEmptyKeyword_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () -> requestService.createRequest("   "));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for short keyword")
        void createRequest_withShortKeyword_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () -> requestService.createRequest("abc"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for long keyword")
        void createRequest_withLongKeyword_shouldThrowException() {
            String longKeyword = "a".repeat(33);
            assertThrows(IllegalArgumentException.class, () -> requestService.createRequest(longKeyword));
        }
    }

    @Nested
    @DisplayName("endRequest Tests")
    class EndRequestTests {
        @Test
        @DisplayName("Should set status to DONE and save")
        void endRequest_withValidRequest_shouldEndProcessAndSave() {
            // Arrange
            Crawler crawler = new Crawler();
            crawler.startProcess();
            when(crawlerRepository.save(any(Crawler.class))).thenReturn(crawler);

            // Act
            Crawler result = requestService.endRequest(crawler);

            // Assert
            assertEquals(CrawlStatus.DONE, result.getStatus());
            assertNotNull(result.getLastUpdate());
            verify(crawlerRepository).save(crawler);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null request")
        void endRequest_withNullRequest_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () -> requestService.endRequest(null));
        }
    }

    @Test
    @DisplayName("findRequestByKey should return DTO when key exists")
    void findRequestByKey_whenExists_shouldReturnDto() {
        // Arrange
        String key = "somekey";
        Crawler crawler = new Crawler();
        crawler.setSearchKey(key);
        crawler.setKeyword("test");
        crawler.setStatus(CrawlStatus.ACTIVE.name());
        when(crawlerRepository.findBySearchKey(key)).thenReturn(Optional.of(crawler));

        // Act
        Optional<CrawlDto> result = requestService.findRequestByKey(key);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(key, result.get().getId());
        assertEquals("ACTIVE", result.get().getStatus());
    }

    @Test
    @DisplayName("findAllRequests should return a list of DTOs")
    void findAllRequests_shouldReturnListOfDtos() {
        // Arrange
        Crawler crawler1 = new Crawler();
        crawler1.setSearchKey("key1");
        Crawler crawler2 = new Crawler();
        crawler2.setSearchKey("key2");
        when(crawlerRepository.findAll()).thenReturn(List.of(crawler1, crawler2));

        // Act
        List<CrawlDto> result = requestService.findAllRequests();

        // Assert
        assertEquals(2, result.size());
        assertEquals("key1", result.get(0).getId());
        assertEquals("key2", result.get(1).getId());
    }
}