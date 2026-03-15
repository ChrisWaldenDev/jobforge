package dev.chriswalden.jobforge.core.dto;

import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.core.domain.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "A background job and its current state")
@Getter
@Setter
public class JobView {
    @Schema(description = "Unique job ID")
    private UUID id;

    @Schema(description = "Job type")
    private JobType type;

    @Schema(description = "Current job status")
    private JobStatus status;

    @Schema(description = "Number of execution attempts so far")
    private int attempts;

    @Schema(description = "Maximum allowed attempts before marking FAILED")
    private int maxAttempts;

    @Schema(description = "When the job was created")
    private Instant createdAt;

    @Schema(description = "When the job was last updated")
    private Instant updatedAt;

    @Schema(description = "Earliest time the job may be picked up. Null means no delay.")
    private Instant scheduledFor;

    @Schema(description = "JSON result produced by the job handler (populated on COMPLETED)")
    private String result;

    @Schema(description = "Error message from the last failed attempt")
    private String error;
}
