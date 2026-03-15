package dev.chriswalden.jobforge.core.dto;

import dev.chriswalden.jobforge.core.domain.JobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Schema(description = "Response returned when a job is successfully submitted")
@Getter
@Setter
public class CreateJobResponse {
    @Schema(description = "Unique ID of the created job")
    private UUID jobId;

    @Schema(description = "Initial status of the job (always QUEUED on creation)")
    private JobStatus status;
}
