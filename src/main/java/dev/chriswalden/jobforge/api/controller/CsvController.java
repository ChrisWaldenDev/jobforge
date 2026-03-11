package dev.chriswalden.jobforge.api.controller;

import dev.chriswalden.jobforge.api.service.CsvService;
import dev.chriswalden.jobforge.core.dto.CreateJobResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}
