package dev.chriswalden.jobforge.core.dto;

import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.core.domain.JobType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class JobView {
    private UUID id;
    private JobType type;
    private JobStatus status;
    private int attempts;
    private int maxAttempts;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant scheduledFor;
    private String result;
    private String error;
}
