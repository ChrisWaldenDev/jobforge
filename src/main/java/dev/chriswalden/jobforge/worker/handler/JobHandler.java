package dev.chriswalden.jobforge.worker.handler;

import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobType;

public interface JobHandler {
    JobType supports();
    String handle(Job job) throws Exception;
}
