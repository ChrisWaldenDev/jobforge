package dev.chriswalden.jobforge.worker.handler;

import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

@Component
public class NoopJobHandler implements JobHandler {

    private final ObjectMapper mapper;

    public NoopJobHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public JobType supports() {
        return JobType.NOOP;
    }

    @Override
    public String handle(Job job) throws InterruptedException {
        // "Work"
        Thread.sleep(300);

        Map<String, Object> result = Map.of(
                "message", "NOOP job executed successfully",
                "jobId", job.getId().toString(),
                "timestamp", Instant.now().toString()
        );

        return mapper.writeValueAsString(result);
    }
}
