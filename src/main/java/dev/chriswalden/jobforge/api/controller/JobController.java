package dev.chriswalden.jobforge.api.controller;

import dev.chriswalden.jobforge.api.service.JobService;
import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.core.domain.JobType;
import dev.chriswalden.jobforge.core.dto.CreateJobRequest;
import dev.chriswalden.jobforge.core.dto.CreateJobResponse;
import dev.chriswalden.jobforge.core.dto.JobView;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public CreateJobResponse postJob(@Valid @RequestBody CreateJobRequest job) {
        return jobService.submit(job);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public JobView getJob(@PathVariable String id) {
        return jobService.getJob(id);
    }

    @GetMapping
    @ResponseBody
    public List<JobView> getJobsByStatusAndType(@RequestParam(name = "status", required = false) String jobStatus, @RequestParam(name = "type", required = false) String jobType) {
        JobStatus status = (jobStatus != null) ? JobStatus.valueOf(jobStatus.toUpperCase()) : null;
        JobType type = (jobType != null) ? JobType.valueOf(jobType.toUpperCase()) : null;
        return jobService.getJobsByStatusAndType(status, type);
    }
}
