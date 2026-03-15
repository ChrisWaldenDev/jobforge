package dev.chriswalden.jobforge.core.dto;

import dev.chriswalden.jobforge.core.domain.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Schema(description = "Request body for submitting a new background job")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateJobRequest {

    @Schema(description = "Type of job to run", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private JobType type;

    @Schema(description = "Optional job-specific payload (any JSON-serializable object)")
    private Object payload;

    @Schema(description = "Schedule the job to run no earlier than this time. Null means run immediately.")
    private Instant scheduledFor;

    @Schema(description = "Maximum number of attempts before marking the job FAILED", minimum = "1", maximum = "20", defaultValue = "3")
    @Min(1)
    @Max(20)
    private int maxAttempts = 3;
}
