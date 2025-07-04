package space.lasf.crawler_app.mapper;


import java.util.ArrayList;

import org.springframework.stereotype.Component;

import space.lasf.crawler_app.dto.CrawlDto;
import space.lasf.crawler_app.entity.Crawler;



@Component
public class CrawlerMapper {
    public static CrawlDto toCrawlDto(Crawler response) {

		if (response == null)
			return null;

		return CrawlDto.builder()
                .id(response.getSearchKey())
				.status(response.getStatus().toLowerCase())
				.urls(new ArrayList<>(response.getUrls()))
                .build();
	}

}