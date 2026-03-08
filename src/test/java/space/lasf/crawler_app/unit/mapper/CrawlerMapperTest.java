package space.lasf.crawler_app.unit.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import space.lasf.crawler_app.dto.CrawlDto;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.mapper.CrawlerMapper;

class CrawlerMapperTest {

    @Test
    void toCrawlDtoShouldReturnNullWhenEntityIsNull() {
        assertNull(CrawlerMapper.toCrawlDto(null));
    }

    @Test
    void toCrawlDtoShouldMapFieldsAndLowercaseStatus() {
        Crawler crawler = new Crawler();
        crawler.setSearchKey("ABCD1234");
        crawler.setKeyword("java");
        crawler.startProcess();
        crawler.addLink("https://example.com/page");

        CrawlDto dto = CrawlerMapper.toCrawlDto(crawler);

        assertNotNull(dto);
        assertEquals("ABCD1234", dto.getId());
        assertEquals("active", dto.getStatus());
        assertEquals(1, dto.getUrls().size());
        assertTrue(dto.getUrls().contains("https://example.com/page"));
    }
}
