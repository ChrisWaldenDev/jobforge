package dev.chriswalden.jobforge.api.controller;

import dev.chriswalden.jobforge.api.service.CsvService;
import dev.chriswalden.jobforge.core.dto.CreateJobResponse;
import dev.chriswalden.jobforge.core.dto.CsvJobSummaryView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "CSV Jobs", description = "Upload and inspect CSV processing jobs")
@RestController
@RequestMapping("/api/v1/csv")
public class CsvController {

    private final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @Operation(summary = "Submit a CSV processing job", responses = {
            @ApiResponse(responseCode = "201", description = "Job created"),
            @ApiResponse(responseCode = "400", description = "Invalid file or parameters")
    })
    @PostMapping(
            value = "/jobs",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public CreateJobResponse submitCsvJob(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "delimiter", required = false) String delimiter,
            @RequestParam(value = "hasHeader", required = false) Boolean hasHeader,
            @RequestParam(value = "maxAttempts", required = false) Integer maxAttempts
            ) {
        return csvService.submit(file, delimiter, hasHeader, maxAttempts);
    }

    @Operation(summary = "Get CSV job summary", description = "Returns parsed metadata including row counts, column info, and processing status", responses = {
            @ApiResponse(responseCode = "200", description = "Summary returned"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping(value = "/jobs/{id}/summary")
    public CsvJobSummaryView getCsvSummary(@PathVariable UUID id) {
        return csvService.getSummary(id);
    }

    @Operation(summary = "Get raw CSV job result", description = "Returns the raw JSON result stored after processing", responses = {
            @ApiResponse(responseCode = "200", description = "Result returned"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping(value = "/jobs/{id}/result", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCsvResult(@PathVariable UUID id) {
        String result = csvService.getResult(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }
}
