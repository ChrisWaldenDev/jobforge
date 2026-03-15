package dev.chriswalden.jobforge.integration;

import dev.chriswalden.jobforge.api.repository.CsvFileRepository;
import dev.chriswalden.jobforge.api.repository.JobRepository;
import dev.chriswalden.jobforge.api.service.JobService;
import dev.chriswalden.jobforge.core.domain.CsvFile;
import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.core.domain.JobType;
import dev.chriswalden.jobforge.core.dto.CreateJobRequest;
import dev.chriswalden.jobforge.core.dto.CreateJobResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class WorkerIntegrationTest {

    @Autowired
    JobService jobService;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    CsvFileRepository csvFileRepository;

    @AfterEach
    void cleanup() {
        jobRepository.deleteAll();
        csvFileRepository.deleteAll();
    }

    @Test
    void worker_processesSubmittedCsvJob_toCompletion() {
        CsvFile csvFile = new CsvFile();
        csvFile.setData("col1,col2\nval1,val2\nval3,val4\n".getBytes(StandardCharsets.UTF_8));
        csvFile.setOriginalName("worker-test.csv");
        csvFile.setContentType("text/csv");
        CsvFile saved = csvFileRepository.save(csvFile);

        Map<String, Object> payload = Map.of(
                "fileId", saved.getId().toString(),
                "delimiter", ",",
                "hasHeader", true
        );

        CreateJobRequest req = new CreateJobRequest();
        req.setType(JobType.CSV_PROCESS);
        req.setPayload(payload);
        req.setMaxAttempts(3);

        CreateJobResponse resp = jobService.submit(req);
        UUID jobId = resp.getJobId();

        await().atMost(10, SECONDS).until(() ->
                jobRepository.findById(jobId)
                        .map(j -> j.getStatus() == JobStatus.COMPLETED)
                        .orElse(false)
        );

        Job job = jobRepository.findById(jobId).orElseThrow();
        assertThat(job.getStatus()).isEqualTo(JobStatus.COMPLETED);
        assertThat(job.getResult()).contains("rowsProcessed");
        assertThat(job.getResult()).contains("col1");
        assertThat(job.getAttempts()).isEqualTo(0);
        assertThat(job.getError()).isNull();
    }
}