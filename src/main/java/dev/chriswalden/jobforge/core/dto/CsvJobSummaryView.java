package dev.chriswalden.jobforge.core.dto;

import dev.chriswalden.jobforge.core.domain.JobStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CsvJobSummaryView {
    private UUID jobId;
    private JobStatus status;
    private int rowsProcessed;
    private int rowsFailed;
    private int columns;
    private List<String> header;
    private Instant processedAt;
    private String originalName;
}
