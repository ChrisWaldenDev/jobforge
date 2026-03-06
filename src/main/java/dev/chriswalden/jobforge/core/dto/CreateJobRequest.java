package dev.chriswalden.jobforge.core.dto;

import dev.chriswalden.jobforge.core.domain.JobType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateJobRequest {

    @NotNull
    private JobType type;
    private Object payload;

    @Min(1)
    @Max(20)
    private int maxAttempts = 3;
}
