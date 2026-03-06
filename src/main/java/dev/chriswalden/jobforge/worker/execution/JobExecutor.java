package dev.chriswalden.jobforge.worker.execution;

import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.api.repository.JobRepository;
import dev.chriswalden.jobforge.worker.dispatch.JobDispatcher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Component
public class JobExecutor {

    private final JobRepository jobRepository;
    private final JobDispatcher jobDispatcher;

    public JobExecutor(JobRepository jobRepository, JobDispatcher jobDispatcher) {
        this.jobRepository = jobRepository;
        this.jobDispatcher = jobDispatcher;
    }

    @Transactional
    public void execute(Job job) {
        try {
            String result = jobDispatcher.dispatch(job);

            job.setResult(result);
            job.setError(null);
            job.setStatus(JobStatus.COMPLETED);
            job.setLockedBy(null);
            job.setLockedAt(null);
            job.setNextRunAt(null);

            jobRepository.save(job);
        } catch (Exception e) {
            handleFailure(job, e);
        }
    }

    private void handleFailure(Job job, Exception e) {
        job.setError(summarize(e));

        int nextAttempt = job.getAttempts() + 1;
        job.setAttempts(nextAttempt);

        if (nextAttempt < job.getMaxAttempts()) {
            job.setStatus(JobStatus.QUEUED);
            job.setNextRunAt(Instant.now().plus(backoff(nextAttempt)));

            job.setLockedBy(null);
            job.setLockedAt(null);
        } else {
            job.setStatus(JobStatus.FAILED);
            job.setLockedBy(null);
            job.setLockedAt(null);
            job.setNextRunAt(null);
        }
        jobRepository.save(job);
    }

    private Duration backoff(int attemptNumber) {
        return switch (attemptNumber) {
            case 1 -> Duration.ofSeconds(2);
            case 2 -> Duration.ofSeconds(5);
            case 3 -> Duration.ofSeconds(15);
            case 4 -> Duration.ofSeconds(30);
            default -> Duration.ofSeconds(60);
        };
    }

    private String summarize(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) msg = e.getClass().getSimpleName();
        return msg;
    }
}
