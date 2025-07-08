package space.lasf.crawler_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import space.lasf.crawler_app.entity.Crawler;

/**
 * Repositório para a entidade Crawl.
 */
public interface CrawlerRepository extends JpaRepository<Crawler, String> {
    @Query("select c from Crawler c where c.searchKey = ?1")
    Optional<Crawler> findBySearchKey(String searchKey);

}