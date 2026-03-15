package dev.chriswalden.jobforge.api.service;

import dev.chriswalden.jobforge.api.messaging.JobPublisher;
import dev.chriswalden.jobforge.api.repository.JobRepository;
import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.core.domain.JobType;
import dev.chriswalden.jobforge.core.dto.CreateJobRequest;
import dev.chriswalden.jobforge.core.dto.CreateJobResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobPublisher jobPublisher;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        // Use real ObjectMapper (tools.jackson) — no Spring context needed
        ObjectMapper objectMapper = new ObjectMapper();
        jobService = new JobService(jobRepository, objectMapper, jobPublisher);
    }

    // -----------------------------------------------------------------------
    // submit()
    // -----------------------------------------------------------------------

    @Test
    void submit_savesJobWithQueuedStatus() {
        Job savedJob = new Job();
        savedJob.setId(UUID.randomUUID());
        savedJob.setType(JobType.CSV_PROCESS);
        savedJob.setStatus(JobStatus.QUEUED);
        savedJob.setAttempts(0);
        savedJob.setMaxAttempts(3);

        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        CreateJobRequest req = new CreateJobRequest();
        req.setType(JobType.CSV_PROCESS);
        req.setMaxAttempts(3);

        CreateJobResponse resp = jobService.submit(req);

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        Job persisted = captor.getValue();

        assertThat(persisted.getType()).isEqualTo(JobType.CSV_PROCESS);
        assertThat(persisted.getStatus()).isEqualTo(JobStatus.QUEUED);
        assertThat(persisted.getAttempts()).isEqualTo(0);
        assertThat(persisted.getMaxAttempts()).isEqualTo(3);

        assertThat(resp.getJobId()).isEqualTo(savedJob.getId());
        assertThat(resp.getStatus()).isEqualTo(JobStatus.QUEUED);
    }

    @Test
    void submit_callsPublisherAfterSave() {
        Job savedJob = new Job();
        savedJob.setId(UUID.randomUUID());
        savedJob.setType(JobType.CSV_PROCESS);
        savedJob.setStatus(JobStatus.QUEUED);
        savedJob.setAttempts(0);
        savedJob.setMaxAttempts(3);

        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        CreateJobRequest req = new CreateJobRequest();
        req.setType(JobType.CSV_PROCESS);
        req.setMaxAttempts(3);

        jobService.submit(req);

        verify(jobPublisher).publish(savedJob);
    }

    @Test
    void submit_withNullPayload_doesNotSerialize() {
        Job savedJob = new Job();
        savedJob.setId(UUID.randomUUID());
        savedJob.setType(JobType.CSV_PROCESS);
        savedJob.setStatus(JobStatus.QUEUED);
        savedJob.setAttempts(0);
        savedJob.setMaxAttempts(3);

        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        CreateJobRequest req = new CreateJobRequest();
        req.setType(JobType.CSV_PROCESS);
        req.setMaxAttempts(3);
        req.setPayload(null);

        jobService.submit(req);

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        assertThat(captor.getValue().getPayload()).isNull();
    }

    // -----------------------------------------------------------------------
    // getJob()
    // -----------------------------------------------------------------------

    @Test
    void getJob_throwsNoSuchElementException_whenNotFound() {
        String id = UUID.randomUUID().toString();
        when(jobRepository.findById(UUID.fromString(id))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJob(id))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Job not found");
    }

    @Test
    void getJob_returnsJobView_whenFound() {
        UUID id = UUID.randomUUID();
        Job job = new Job();
        job.setId(id);
        job.setType(JobType.CSV_PROCESS);
        job.setStatus(JobStatus.QUEUED);
        job.setAttempts(0);
        job.setMaxAttempts(3);

        when(jobRepository.findById(id)).thenReturn(Optional.of(job));

        var view = jobService.getJob(id.toString());

        assertThat(view).isNotNull();
        assertThat(view.getId()).isEqualTo(id);
        assertThat(view.getStatus()).isEqualTo(JobStatus.QUEUED);
    }

    // -----------------------------------------------------------------------
    // cancelJob()
    // -----------------------------------------------------------------------

    @Test
    void cancelJob_removesJob_whenQueued() {
        UUID id = UUID.randomUUID();
        Job job = new Job();
        job.setId(id);
        job.setStatus(JobStatus.QUEUED);

        when(jobRepository.findById(id)).thenReturn(Optional.of(job));

        jobService.cancelJob(id.toString());

        verify(jobRepository).removeJobById(id);
    }

    @Test
    void cancelJob_throwsIllegalStateException_whenNotQueued() {
        UUID id = UUID.randomUUID();
        Job job = new Job();
        job.setId(id);
        job.setStatus(JobStatus.RUNNING);

        when(jobRepository.findById(id)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.cancelJob(id.toString()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("QUEUED");
    }

    @Test
    void cancelJob_throwsNoSuchElementException_whenNotFound() {
        String id = UUID.randomUUID().toString();
        when(jobRepository.findById(UUID.fromString(id))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.cancelJob(id))
                .isInstanceOf(NoSuchElementException.class);
    }
}