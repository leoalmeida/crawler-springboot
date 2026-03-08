package space.lasf.crawler_app.integration;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.repository.CrawlerRepository;
import space.lasf.crawler_app.service.ICrawlerService;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest(
        properties = {"CRAWLER_URL=http://localhost:8081", "DATABASE_DB=testdb", "DATABASE_USER=sa", "DATABASE_PWD="})
class CrawlerControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CrawlerRepository crawlerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICrawlerService crawlerService;

    @BeforeEach
    void setUp() {
        // Mock the async service call as we are not testing its implementation here
        doNothing().when(crawlerService).crawlResource(any(Crawler.class));
        crawlerRepository.deleteAll();
    }

    @Test
    void createRequest_shouldReturnCreatedWithId() throws Exception {
        Map<String, String> requestBody = Map.of("keyword", "spring boot");

        mockMvc.perform(post("/crawl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.id", hasLength(8)));
    }

    @Test
    void createRequest_withInvalidKeyword_shouldReturnError() throws Exception {
        // Keyword is too short, based on RequestServiceImpl validation
        Map<String, String> requestBody = Map.of("keyword", "abc");

        // The service throws IllegalArgumentException, which is mapped to a 400 Bad Request by ExceptionHandlerConfig.
        mockMvc.perform(post("/crawl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById_whenExists_shouldReturnRequest() throws Exception {
        Crawler crawler = new Crawler();
        crawler.setKeyword("java");
        crawler.setSearchKey("abcdefgh");
        crawler.startProcess();
        crawlerRepository.save(crawler);

        mockMvc.perform(get("/crawl/{id}", "abcdefgh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("abcdefgh")))
                .andExpect(jsonPath("$.status", is("active")));
    }

    @Test
    void getRequestById_whenNotExists_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/crawl/{id}", "nonexistent")).andExpect(status().isNotFound());
    }

    @Test
    void getAllRequests_shouldReturnListOfRequests() throws Exception {
        Crawler crawler1 = new Crawler();
        crawler1.setKeyword("test1");
        crawler1.setSearchKey("11111111");
        crawler1.startProcess();
        crawlerRepository.save(crawler1);

        Crawler crawler2 = new Crawler();
        crawler2.setKeyword("test2");
        crawler2.setSearchKey("22222222");
        crawler2.startProcess();
        crawlerRepository.save(crawler2);

        mockMvc.perform(get("/crawl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", anyOf(is("11111111"), is("22222222"))))
                .andExpect(jsonPath("$[1].id", anyOf(is("11111111"), is("22222222"))));
    }
}
