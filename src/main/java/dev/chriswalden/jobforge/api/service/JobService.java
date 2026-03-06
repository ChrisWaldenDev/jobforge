package dev.chriswalden.jobforge.api.service;

import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.core.domain.JobType;
import dev.chriswalden.jobforge.core.dto.CreateJobRequest;
import dev.chriswalden.jobforge.core.dto.CreateJobResponse;
import dev.chriswalden.jobforge.core.dto.JobView;
import dev.chriswalden.jobforge.core.mapper.JobMapper;
import dev.chriswalden.jobforge.api.repository.JobRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    public JobService(JobRepository jobRepository, ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
    }

    public CreateJobResponse submit(CreateJobRequest req) {
        String payloadJson = toJson(req.getPayload());

        Job job = new Job();
        job.setType(req.getType());
        job.setStatus(JobStatus.QUEUED);
        job.setPayload(payloadJson);

        job.setAttempts(0);
        job.setMaxAttempts(req.getMaxAttempts());

        Job saved = jobRepository.save(job);

        CreateJobResponse resp = new CreateJobResponse();
        resp.setJobId(saved.getId());
        resp.setStatus(saved.getStatus());
        return resp;
    }

    public JobView getJob(String id) {
        Job job = jobRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NullPointerException("Job not found"));
        return JobMapper.toJobView(job);
    }

    public List<JobView> getJobsByStatusAndType(JobStatus status, JobType type) {
        ArrayList<JobView> jobViews = new ArrayList<>();
        List<Job> jobs = jobRepository.findByStatusAndTypeOrderByCreatedAtDesc(status, type, Pageable.unpaged());
        for (Job job : jobs) {
            jobViews.add(JobMapper.toJobView(job));
        }
        return jobViews;
    }

    private String toJson(Object payload) {
        if (payload == null) return null;
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Payload must be valid JSON.", e);
        }
    }
}
