package space.lasf.crawler_app.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import space.lasf.crawler_app.controller.CrawlerController;
import space.lasf.crawler_app.dto.CrawlDto;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.service.ICrawlerService;
import space.lasf.crawler_app.service.IRequestService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CrawlerController.class)
class CrawlerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IRequestService requestService;

    @MockitoBean
    private ICrawlerService crawlerService;

    @Test
    @DisplayName("GET /crawl - Should return all requests")
    void getAllRequests_shouldReturnListOfDtos() throws Exception {
        // Arrange
        CrawlDto dto = CrawlDto.builder().id("123").status("DONE").build();
        when(requestService.findAllRequests()).thenReturn(List.of(dto));

        // Act & Assert
        mockMvc.perform(get("/crawl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("123")));
    }

    @Test
    @DisplayName("GET /crawl/{id} - Should return request when found")
    void getRequestById_whenFound_shouldReturnDto() throws Exception {
        // Arrange
        CrawlDto dto = CrawlDto.builder().id("abc").status("ACTIVE").build();
        when(requestService.findRequestByKey("abc")).thenReturn(Optional.of(dto));

        // Act & Assert
        mockMvc.perform(get("/crawl/{id}", "abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("abc")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("GET /crawl/{id} - Should return 404 when not found")
    void getRequestById_whenNotFound_shouldReturnNotFound() throws Exception {
        // Arrange
        when(requestService.findRequestByKey(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/crawl/{id}", "xyz"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /crawl - Should create request and return ID")
    void createRequest_withValidBody_shouldReturnCreated() throws Exception {
        // Arrange
        String keyword = "java testing";
        Map<String, String> requestBody = Map.of("keyword", keyword);

        Crawler createdCrawler = new Crawler();
        createdCrawler.setSearchKey("new-id");
        createdCrawler.setKeyword(keyword);

        when(requestService.createRequest(keyword)).thenReturn(Optional.of(createdCrawler));
        doNothing().when(crawlerService).crawlResource(any(Crawler.class));

        // Act & Assert
        mockMvc.perform(post("/crawl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("new-id")));
    }

    @Test
    @DisplayName("POST /crawl - Should return 400 for request without keyword")
    void createRequest_withoutKeyword_shouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, String> requestBody = Collections.emptyMap();

        // Act & Assert
        mockMvc.perform(post("/crawl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }
}