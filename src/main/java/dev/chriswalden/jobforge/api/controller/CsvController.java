package dev.chriswalden.jobforge.api.controller;

import dev.chriswalden.jobforge.api.service.CsvService;
import dev.chriswalden.jobforge.core.dto.CreateJobResponse;
import dev.chriswalden.jobforge.core.dto.CsvJobSummaryView;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/csv")
public class CsvController {

    private final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

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

    @GetMapping(value = "/jobs/{id}/summary")
    public CsvJobSummaryView getCsvSummary(@PathVariable UUID id) {
        return csvService.getSummary(id);
    }

    @GetMapping(value = "/jobs/{id}/result", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCsvResult(@PathVariable UUID id) {
        String result = csvService.getResult(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }
}
