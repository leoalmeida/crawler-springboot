package space.lasf.crawler_app.unit.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import space.lasf.crawler_app.component.ExceptionHandlerConfig;
import space.lasf.crawler_app.controller.CrawlerController;
import space.lasf.crawler_app.dto.CrawlDto;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.service.ICrawlerService;
import space.lasf.crawler_app.service.IRequestService;

@ExtendWith(MockitoExtension.class)
class CrawlerControllerTest {

    @Mock
    private IRequestService requestService;

    @Mock
    private ICrawlerService crawlerService;

    @InjectMocks
    private CrawlerController crawlerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(crawlerController)
                .setControllerAdvice(new ExceptionHandlerConfig())
                .build();
    }

    @Test
    @DisplayName("GET /crawl - Should return all requests")
    void getAllRequests_shouldReturnListOfDtos() throws Exception {
        CrawlDto dto = CrawlDto.builder().id("123").status("done").build();
        when(requestService.findAllRequests()).thenReturn(List.of(dto));

        mockMvc.perform(get("/crawl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("123")));
    }

    @Test
    @DisplayName("GET /crawl/{id} - Should return request when found")
    void getRequestById_whenFound_shouldReturnDto() throws Exception {
        CrawlDto dto = CrawlDto.builder().id("abc").status("active").build();
        when(requestService.findRequestByKey("abc")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/crawl/{id}", "abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("abc")))
                .andExpect(jsonPath("$.status", is("active")));
    }

    @Test
    @DisplayName("GET /crawl/{id} - Should return 404 when not found")
    void getRequestById_whenNotFound_shouldReturnNotFound() throws Exception {
        when(requestService.findRequestByKey(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/crawl/{id}", "xyz")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /crawl - Should create request and return ID")
    void createRequest_withValidBody_shouldReturnCreated() throws Exception {
        String keyword = "java testing";
        Map<String, String> requestBody = Map.of("keyword", keyword);

        Crawler createdCrawler = new Crawler();
        createdCrawler.setSearchKey("new-id1a");
        createdCrawler.setKeyword(keyword);
        createdCrawler.startProcess();

        when(requestService.createRequest(keyword)).thenReturn(Optional.of(createdCrawler));
        doNothing().when(crawlerService).crawlResource(any(Crawler.class));

        mockMvc.perform(post("/crawl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("new-id1a")));
    }

    @Test
    @DisplayName("POST /crawl - Should return 400 for request without keyword")
    void createRequest_withoutKeyword_shouldReturnBadRequest() throws Exception {
        Map<String, String> requestBody = Collections.emptyMap();

        mockMvc.perform(post("/crawl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }
}
