package dev.chriswalden.jobforge.worker.identity;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WorkerIdentity {

    private final String workerId = "worker-" + UUID.randomUUID();

    public String id() {
        return workerId;
    }
}
