package dev.chriswalden.jobforge.worker.claiming;

import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.api.repository.JobRepository;
import dev.chriswalden.jobforge.worker.identity.WorkerIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class JobClaimer {

    private static final Logger log = LoggerFactory.getLogger(JobClaimer.class);

    private final JobRepository jobRepository;
    private final WorkerIdentity workerIdentity;

    public JobClaimer(JobRepository jobRepository, WorkerIdentity workerIdentity) {
        this.jobRepository = jobRepository;
        this.workerIdentity = workerIdentity;
    }

    @Transactional
    public List<Job> claimNext(int limit) {
        Instant now = Instant.now();

        List<Job> jobs = jobRepository.findClaimCandidates(
                JobStatus.QUEUED,
                now,
                PageRequest.of(0, limit)
        );

        if (jobs.isEmpty()) return jobs;

        for (Job job : jobs) {
            job.setStatus(JobStatus.RUNNING);
            job.setLockedBy(workerIdentity.id());
            job.setLockedAt(now);
            log.info("[job={}] Claimed by worker={}", job.getId(), workerIdentity.id());
        }

        return jobRepository.saveAll(jobs);
    }
}
