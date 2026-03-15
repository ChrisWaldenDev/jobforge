package dev.chriswalden.jobforge.api.controller;

import dev.chriswalden.jobforge.api.service.JobService;
import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.core.domain.JobType;
import dev.chriswalden.jobforge.core.dto.CreateJobRequest;
import dev.chriswalden.jobforge.core.dto.CreateJobResponse;
import dev.chriswalden.jobforge.core.dto.JobView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Jobs", description = "Submit and manage background jobs")
@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @Operation(summary = "Submit a new job", responses = {
            @ApiResponse(responseCode = "201", description = "Job created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateJobResponse postJob(@Valid @RequestBody CreateJobRequest job) {
        return jobService.submit(job);
    }

    @Operation(summary = "Get a job by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Job found"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{id}")
    @ResponseBody
    public JobView getJob(@PathVariable String id) {
        return jobService.getJob(id);
    }

    @Operation(summary = "List jobs", description = "Returns all jobs, optionally filtered by status and/or type")
    @GetMapping
    @ResponseBody
    public List<JobView> getJobsByStatusAndType(@RequestParam(name = "status", required = false) String jobStatus, @RequestParam(name = "type", required = false) String jobType) {
        JobStatus status = (jobStatus != null) ? JobStatus.valueOf(jobStatus.toUpperCase()) : null;
        JobType type = (jobType != null) ? JobType.valueOf(jobType.toUpperCase()) : null;
        return jobService.getJobsByStatusAndType(status, type);
    }

    @Operation(summary = "Cancel a queued job", responses = {
            @ApiResponse(responseCode = "204", description = "Job cancelled"),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "409", description = "Job cannot be cancelled in its current state")
    })
    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelJob(@PathVariable String id) {
        jobService.cancelJob(id);
    }
}
