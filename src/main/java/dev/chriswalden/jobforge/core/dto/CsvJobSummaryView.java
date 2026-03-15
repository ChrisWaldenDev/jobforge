package dev.chriswalden.jobforge.core.dto;

import dev.chriswalden.jobforge.core.domain.JobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Summary of a completed CSV processing job")
@Getter
@Setter
public class CsvJobSummaryView {
    @Schema(description = "Job ID")
    private UUID jobId;

    @Schema(description = "Current job status")
    private JobStatus status;

    @Schema(description = "Number of rows successfully processed")
    private int rowsProcessed;

    @Schema(description = "Number of rows that failed to parse")
    private int rowsFailed;

    @Schema(description = "Number of columns detected in the CSV")
    private int columns;

    @Schema(description = "Header row values, if the CSV had a header")
    private List<String> header;

    @Schema(description = "When processing completed")
    private Instant processedAt;

    @Schema(description = "Original filename of the uploaded CSV")
    private String originalName;
}
