package dev.chriswalden.jobforge.core.mapper;

import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.dto.JobView;

public class JobMapper {

    public static JobView toJobView(Job job) {
        JobView jobView = new JobView();
        jobView.setId(job.getId());
        jobView.setType(job.getType());
        jobView.setStatus(job.getStatus());
        jobView.setAttempts(job.getAttempts());
        jobView.setMaxAttempts(job.getMaxAttempts());
        jobView.setCreatedAt(job.getCreatedAt());
        jobView.setUpdatedAt(job.getUpdatedAt());
        jobView.setScheduledFor(job.getScheduledFor());
        jobView.setResult(job.getResult());
        jobView.setError(job.getError());

        return jobView;
    }
}
