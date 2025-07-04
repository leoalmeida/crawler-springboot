package space.lasf.crawler_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "space.lasf.crawler_app.repository")
@EntityScan("space.lasf.crawler_app.entity")
@EnableAsync
public class CrawlerAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrawlerAppApplication.class, args);
	}

}
