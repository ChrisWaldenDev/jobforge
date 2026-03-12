package dev.chriswalden.jobforge.worker.handler;

import dev.chriswalden.jobforge.core.domain.CsvFile;
import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobType;
import dev.chriswalden.jobforge.api.repository.CsvFileRepository;
import dev.chriswalden.jobforge.worker.handler.csv.CsvProcessPayload;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class CsvProcessJobHandler implements JobHandler {

    private final ObjectMapper mapper;
    private final CsvFileRepository csvFileRepository;

    public CsvProcessJobHandler(final ObjectMapper mapper, final CsvFileRepository csvFileRepository) {
        this.mapper = mapper;
        this.csvFileRepository = csvFileRepository;
    }

    @Override
    public JobType supports() {
        return JobType.CSV_PROCESS;
    }

    @Override
    public String handle(Job job) throws Exception {
        CsvProcessPayload payload = mapper.readValue(job.getPayload(), CsvProcessPayload.class);

        UUID fileId = UUID.fromString(payload.fileId());
        String delimiter = (payload.delimiter() == null || payload.delimiter().isBlank()) ? "," : payload.delimiter();
        boolean hasHeader = payload.hasHeader();

        CsvFile csv = csvFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        int rowsProcessed = 0;
        int rowsFailed = 0;
        int columns = -1;
        List<String> headerCols = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csv.getData()), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] parts = line.split(Pattern.quote(delimiter), -1);

                if (first) {
                    first = false;
                    columns = parts.length;

                    if (hasHeader) {
                        headerCols.addAll(Arrays.asList(parts));
                        continue;
                    }

                    // No header: generate positional keys column_0, column_1, ...
                    for (int i = 0; i < columns; i++) {
                        headerCols.add("column_" + i);
                    }
                }

                if (parts.length != columns) {
                    rowsFailed++;
                    continue;
                }

                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < columns; i++) {
                    row.put(headerCols.get(i), parts[i]);
                }
                rows.add(row);
                rowsProcessed++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileId", fileId.toString());
        result.put("originalName", csv.getOriginalName());
        result.put("rowsProcessed", rowsProcessed);
        result.put("rowsFailed", rowsFailed);
        result.put("columns", columns);
        result.put("hasHeader", hasHeader);
        result.put("header", headerCols);
        result.put("processedAt", Instant.now().toString());
        result.put("rows", rows);

        return mapper.writeValueAsString(result);
    }
}
