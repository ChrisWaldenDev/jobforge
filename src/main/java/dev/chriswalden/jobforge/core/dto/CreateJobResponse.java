package dev.chriswalden.jobforge.core.dto;

import dev.chriswalden.jobforge.core.domain.JobStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateJobResponse {
    private UUID jobId;
    private JobStatus status;
}
