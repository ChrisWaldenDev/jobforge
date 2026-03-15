package dev.chriswalden.jobforge.integration;

import dev.chriswalden.jobforge.api.repository.CsvFileRepository;
import dev.chriswalden.jobforge.api.repository.JobRepository;
import dev.chriswalden.jobforge.core.domain.JobStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CsvJobIntegrationTest {

    @Autowired
    WebApplicationContext wac;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    CsvFileRepository csvFileRepository;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @AfterEach
    void cleanup() {
        jobRepository.deleteAll();
        csvFileRepository.deleteAll();
    }

    @Test
    void submitCsvJob_isProcessedToCompletion() throws Exception {
        String csvContent = "name,score\nAlice,95\nBob,87\nCarol,92";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        String response = mockMvc.perform(multipart("/api/v1/csv/jobs").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").exists())
                .andReturn().getResponse().getContentAsString();

        UUID jobId = UUID.fromString(objectMapper.readTree(response).get("jobId").asString());

        await().atMost(10, SECONDS).until(() ->
                jobRepository.findById(jobId)
                        .map(j -> j.getStatus() == JobStatus.COMPLETED)
                        .orElse(false)
        );

        var job = jobRepository.findById(jobId).orElseThrow();
        assertThat(job.getStatus()).isEqualTo(JobStatus.COMPLETED);
        assertThat(job.getResult()).isNotNull();
        assertThat(job.getError()).isNull();
    }

    @Test
    void getCsvSummary_returnsCorrectStats() throws Exception {
        String csvContent = "name,score\nAlice,95\nBob,87";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        String response = mockMvc.perform(multipart("/api/v1/csv/jobs").file(file))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID jobId = UUID.fromString(objectMapper.readTree(response).get("jobId").asString());

        await().atMost(10, SECONDS).until(() ->
                jobRepository.findById(jobId)
                        .map(j -> j.getStatus() == JobStatus.COMPLETED)
                        .orElse(false)
        );

        mockMvc.perform(get("/api/v1/csv/jobs/" + jobId + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rowsProcessed").value(2))
                .andExpect(jsonPath("$.columns").value(2))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.header[0]").value("name"))
                .andExpect(jsonPath("$.header[1]").value("score"));
    }
}