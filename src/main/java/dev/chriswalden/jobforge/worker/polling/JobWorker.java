package dev.chriswalden.jobforge.worker.polling;

import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.worker.claiming.JobClaimer;
import dev.chriswalden.jobforge.worker.execution.JobExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobWorker {

    private static final Logger log = LoggerFactory.getLogger(JobWorker.class);

    private final JobClaimer jobClaimer;
    private final JobExecutor jobExecutor;

    public JobWorker(JobClaimer jobClaimer, JobExecutor jobExecutor) {
        this.jobClaimer = jobClaimer;
        this.jobExecutor = jobExecutor;
    }

    @Scheduled(fixedDelayString = "${jobforge.worker.pollDelayMs:1000}")
    public void pollAndWork() {
        int limit = 1;

        List<Job> claimed = jobClaimer.claimNext(limit);

        if (claimed.isEmpty()) return;

        log.info("Claimed {} job(s)", claimed.size());

        for (Job job : claimed) {
            try {
                jobExecutor.execute(job);
            } catch (Exception e) {
                log.error("Unexpected error executing job {}", job.getId(), e);
            }
        }
    }
}
