package dev.chriswalden.jobforge.integration;

import dev.chriswalden.jobforge.api.messaging.JobPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class JobApiIntegrationTest {

    @Autowired
    WebApplicationContext wac;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    JobPublisher jobPublisher;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    private static final String CREATE_JOB_JSON = "{\"type\":\"CSV_PROCESS\",\"maxAttempts\":3}";

    @Test
    void postJob_returns201WithQueuedStatus() throws Exception {
        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JOB_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").exists())
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void getJob_returns200WithJobData() throws Exception {
        String response = mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JOB_JSON))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        String jobId = node.get("jobId").asString();

        mockMvc.perform(get("/api/v1/jobs/" + jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobId))
                .andExpect(jsonPath("$.status").value("QUEUED"))
                .andExpect(jsonPath("$.type").value("CSV_PROCESS"));
    }

    @Test
    void getJobsByStatus_returnsQueuedJobs() throws Exception {
        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JOB_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/jobs").param("status", "QUEUED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    @Test
    void cancelJob_returns204_thenGetReturns404() throws Exception {
        String response = mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JOB_JSON))
                .andReturn().getResponse().getContentAsString();

        String jobId = objectMapper.readTree(response).get("jobId").asString();

        mockMvc.perform(post("/api/v1/jobs/" + jobId + "/cancel"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/jobs/" + jobId))
                .andExpect(status().isNotFound());
    }
}