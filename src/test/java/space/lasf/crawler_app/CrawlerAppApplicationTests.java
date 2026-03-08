package space.lasf.crawler_app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"CRAWLER_URL=http://localhost:8081",
	"DATABASE_DB=testdb",
	"DATABASE_USER=sa",
	"DATABASE_PWD="
})
class CrawlerAppApplicationTests {

	@Test
	void contextLoads() {
	}

}
