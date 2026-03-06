package dev.chriswalden.jobforge.api.service;

import dev.chriswalden.jobforge.core.domain.CsvFile;
import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.core.domain.JobType;
import dev.chriswalden.jobforge.core.dto.CreateJobResponse;
import dev.chriswalden.jobforge.api.repository.CsvFileRepository;
import dev.chriswalden.jobforge.api.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CsvService {

    private final CsvFileRepository csvFileRepository;
    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    public CsvService(final CsvFileRepository csvFileRepository, JobRepository jobRepository, ObjectMapper objectMapper) {
        this.csvFileRepository = csvFileRepository;
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
    }

    public CreateJobResponse submit(MultipartFile file, String delimiter, Boolean hasHeader, Integer maxAttempts) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String delim = (delimiter == null || delimiter.isBlank()) ? "," : delimiter;
        boolean header = hasHeader == null || hasHeader;
        int maxAtt = (maxAttempts == null) ? 5 : maxAttempts;

        CsvFile csv = new CsvFile();
        csv.setOriginalName(file.getOriginalFilename());
        csv.setContentType(file.getContentType());

        try {
            csv.setData(file.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read uploaded file bytes", e);
        }

        CsvFile savedFile = csvFileRepository.save(csv);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("fileId", savedFile.getId().toString());
        payload.put("delimiter", delim);
        payload.put("hasHeader", header);

        String payloadJson = toJson(payload);

        Job job = new Job();
        job.setType(JobType.CSV_PROCESS);
        job.setStatus(JobStatus.QUEUED);
        job.setPayload(payloadJson);

        job.setAttempts(0);
        job.setMaxAttempts(maxAtt);

        job.setResult(null);
        job.setError(null);
        job.setLockedBy(null);
        job.setLockedAt(null);

        Job savedJob = jobRepository.save(job);

        CreateJobResponse resp = new CreateJobResponse();
        resp.setJobId(savedJob.getId());
        resp.setStatus(savedJob.getStatus());
        return resp;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException("Could not convert object to JSON", e);
        }
    }
}
