package space.lasf.crawler_app.unit.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import space.lasf.crawler_app.entity.CrawlStatus;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.repository.CrawlerRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CrawlerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CrawlerRepository crawlerRepository;

    @Test
    @DisplayName("findBySearchKey should find a crawler by its search key")
    void findBySearchKey_whenCrawlerExists_shouldReturnCrawler() {
        // Arrange
        Crawler crawler = new Crawler();
        crawler.setSearchKey("test-key");
        crawler.setKeyword("test-keyword");
        crawler.setStatus(CrawlStatus.ACTIVE.name());
        entityManager.persistAndFlush(crawler);

        // Act
        Optional<Crawler> found = crawlerRepository.findBySearchKey("test-key");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getSearchKey()).isEqualTo("test-key");
    }

    @Test
    @DisplayName("findBySearchKey should return empty when no crawler matches the key")
    void findBySearchKey_whenCrawlerDoesNotExist_shouldReturnEmpty() {
        // Act
        Optional<Crawler> found = crawlerRepository.findBySearchKey("non-existent-key");

        // Assert
        assertThat(found).isNotPresent();
    }
}