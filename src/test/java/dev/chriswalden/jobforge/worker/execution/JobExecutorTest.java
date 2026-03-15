package dev.chriswalden.jobforge.worker.execution;

import dev.chriswalden.jobforge.api.messaging.JobPublisher;
import dev.chriswalden.jobforge.api.repository.JobRepository;
import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.core.domain.JobType;
import dev.chriswalden.jobforge.worker.dispatch.JobDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobExecutorTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobDispatcher jobDispatcher;

    @Mock
    private JobPublisher jobPublisher;

    private JobExecutor jobExecutor;

    @BeforeEach
    void setUp() {
        jobExecutor = new JobExecutor(jobRepository, jobDispatcher, jobPublisher);
    }

    private Job buildJob(int attempts, int maxAttempts) {
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setType(JobType.CSV_PROCESS);
        job.setStatus(JobStatus.QUEUED);
        job.setAttempts(attempts);
        job.setMaxAttempts(maxAttempts);
        return job;
    }

    // -----------------------------------------------------------------------
    // Success path
    // -----------------------------------------------------------------------

    @Test
    void execute_setsCompletedStatus_whenDispatcherSucceeds() throws Exception {
        Job job = buildJob(0, 3);
        when(jobRepository.save(any(Job.class))).thenReturn(job);
        when(jobDispatcher.dispatch(job)).thenReturn("{\"ok\":true}");

        jobExecutor.execute(job);

        assertThat(job.getStatus()).isEqualTo(JobStatus.COMPLETED);
        assertThat(job.getResult()).isEqualTo("{\"ok\":true}");
        assertThat(job.getError()).isNull();
    }

    @Test
    void execute_savesJobTwice_onSuccess() throws Exception {
        Job job = buildJob(0, 3);
        when(jobRepository.save(any(Job.class))).thenReturn(job);
        when(jobDispatcher.dispatch(job)).thenReturn("result");

        jobExecutor.execute(job);

        // First save: RUNNING; second save: COMPLETED
        verify(jobRepository, times(2)).save(job);
        verify(jobPublisher, never()).publish(any());
    }

    // -----------------------------------------------------------------------
    // Retry path (nextAttempt < maxAttempts)
    // -----------------------------------------------------------------------

    @Test
    void execute_requeuesJob_whenDispatcherThrowsAndAttemptsRemain() throws Exception {
        Job job = buildJob(0, 3);
        when(jobRepository.save(any(Job.class))).thenReturn(job);
        when(jobDispatcher.dispatch(job)).thenThrow(new RuntimeException("transient error"));

        jobExecutor.execute(job);

        // nextAttempt = 1, maxAttempts = 3 → 1 < 3 → QUEUED
        assertThat(job.getStatus()).isEqualTo(JobStatus.QUEUED);
        assertThat(job.getAttempts()).isEqualTo(1);
        assertThat(job.getError()).isEqualTo("transient error");
        verify(jobPublisher).publish(job);
    }

    @Test
    void execute_setsNextRunAt_onRetry() throws Exception {
        Job job = buildJob(0, 3);
        when(jobRepository.save(any(Job.class))).thenReturn(job);
        when(jobDispatcher.dispatch(job)).thenThrow(new RuntimeException("err"));

        jobExecutor.execute(job);

        assertThat(job.getNextRunAt()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // Max attempts reached path (nextAttempt >= maxAttempts)
    // -----------------------------------------------------------------------

    @Test
    void execute_setsFailedStatus_whenMaxAttemptsReached() throws Exception {
        // attempts=2, maxAttempts=3 → nextAttempt=3, 3 < 3 is false → FAILED
        Job job = buildJob(2, 3);
        when(jobRepository.save(any(Job.class))).thenReturn(job);
        when(jobDispatcher.dispatch(job)).thenThrow(new RuntimeException("fatal error"));

        jobExecutor.execute(job);

        assertThat(job.getStatus()).isEqualTo(JobStatus.FAILED);
        assertThat(job.getAttempts()).isEqualTo(3);
        assertThat(job.getError()).isEqualTo("fatal error");
    }

    @Test
    void execute_doesNotPublish_whenMaxAttemptsReached() throws Exception {
        Job job = buildJob(2, 3);
        when(jobRepository.save(any(Job.class))).thenReturn(job);
        when(jobDispatcher.dispatch(job)).thenThrow(new RuntimeException("fatal"));

        jobExecutor.execute(job);

        verify(jobPublisher, never()).publish(any());
    }

    @Test
    void execute_clearsLockFields_onFailure() throws Exception {
        Job job = buildJob(2, 3);
        job.setLockedBy("worker-1");
        when(jobRepository.save(any(Job.class))).thenReturn(job);
        when(jobDispatcher.dispatch(job)).thenThrow(new RuntimeException("fatal"));

        jobExecutor.execute(job);

        assertThat(job.getLockedBy()).isNull();
        assertThat(job.getLockedAt()).isNull();
        assertThat(job.getNextRunAt()).isNull();
    }
}