package dev.chriswalden.jobforge.worker.handler;

import dev.chriswalden.jobforge.api.repository.CsvFileRepository;
import dev.chriswalden.jobforge.core.domain.CsvFile;
import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.core.domain.JobType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvProcessJobHandlerTest {

    @Mock
    private CsvFileRepository csvFileRepository;

    private CsvProcessJobHandler handler;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new CsvProcessJobHandler(mapper, csvFileRepository);
    }

    private Job buildJob(UUID fileId, String delimiter, boolean hasHeader) throws Exception {
        String payload = mapper.writeValueAsString(
                java.util.Map.of("fileId", fileId.toString(), "delimiter", delimiter, "hasHeader", hasHeader)
        );
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setType(JobType.CSV_PROCESS);
        job.setStatus(JobStatus.QUEUED);
        job.setAttempts(0);
        job.setMaxAttempts(3);
        job.setPayload(payload);
        return job;
    }

    private CsvFile buildCsvFile(UUID id, String csvContent) {
        CsvFile csv = new CsvFile();
        csv.setData(csvContent.getBytes(StandardCharsets.UTF_8));
        csv.setOriginalName("test.csv");
        csv.setContentType("text/csv");
        return csv;
    }

    // -----------------------------------------------------------------------
    // Test 1: Valid CSV with header — 2 data rows
    // -----------------------------------------------------------------------

    @Test
    void handle_validCsvWithHeader_returnsCorrectCounts() throws Exception {
        UUID fileId = UUID.randomUUID();
        String csv = "name,age,city\nAlice,30,London\nBob,25,Paris\n";

        CsvFile csvFile = buildCsvFile(fileId, csv);
        when(csvFileRepository.findById(fileId)).thenReturn(Optional.of(csvFile));

        Job job = buildJob(fileId, ",", true);
        String resultJson = handler.handle(job);

        JsonNode node = mapper.readTree(resultJson);
        assertThat(node.get("rowsProcessed").asInt()).isEqualTo(2);
        assertThat(node.get("rowsFailed").asInt()).isEqualTo(0);
        assertThat(node.get("columns").asInt()).isEqualTo(3);
        assertThat(node.get("hasHeader").asBoolean()).isTrue();

        // Verify header names
        JsonNode header = node.get("header");
        assertThat(header.isArray()).isTrue();
        assertThat(header.get(0).asString()).isEqualTo("name");
        assertThat(header.get(1).asString()).isEqualTo("age");
        assertThat(header.get(2).asString()).isEqualTo("city");
    }

    // -----------------------------------------------------------------------
    // Test 2: CSV without header — generates positional keys
    // -----------------------------------------------------------------------

    @Test
    void handle_csvWithoutHeader_generatesPositionalKeys() throws Exception {
        UUID fileId = UUID.randomUUID();
        // No header row — all rows are data rows
        String csv = "Alice,30,London\nBob,25,Paris\n";

        CsvFile csvFile = buildCsvFile(fileId, csv);
        when(csvFileRepository.findById(fileId)).thenReturn(Optional.of(csvFile));

        Job job = buildJob(fileId, ",", false);
        String resultJson = handler.handle(job);

        JsonNode node = mapper.readTree(resultJson);
        assertThat(node.get("rowsProcessed").asInt()).isEqualTo(2);
        assertThat(node.get("rowsFailed").asInt()).isEqualTo(0);
        assertThat(node.get("columns").asInt()).isEqualTo(3);

        // Positional header keys: column_0, column_1, column_2
        JsonNode header = node.get("header");
        assertThat(header.isArray()).isTrue();
        assertThat(header.get(0).asString()).isEqualTo("column_0");
        assertThat(header.get(1).asString()).isEqualTo("column_1");
        assertThat(header.get(2).asString()).isEqualTo("column_2");
    }

    // -----------------------------------------------------------------------
    // Test 3: CSV with an invalid row (wrong column count)
    // -----------------------------------------------------------------------

    @Test
    void handle_csvWithInvalidRow_incrementsRowsFailed() throws Exception {
        UUID fileId = UUID.randomUUID();
        // Row 3 is missing the third column — should be counted as failed
        String csv = "name,age,city\nAlice,30,London\nBob,25\n";

        CsvFile csvFile = buildCsvFile(fileId, csv);
        when(csvFileRepository.findById(fileId)).thenReturn(Optional.of(csvFile));

        Job job = buildJob(fileId, ",", true);
        String resultJson = handler.handle(job);

        JsonNode node = mapper.readTree(resultJson);
        assertThat(node.get("rowsProcessed").asInt()).isEqualTo(1);
        assertThat(node.get("rowsFailed").asInt()).isEqualTo(1);
        assertThat(node.get("columns").asInt()).isEqualTo(3);
    }

    // -----------------------------------------------------------------------
    // Extra: fileId propagated in result
    // -----------------------------------------------------------------------

    @Test
    void handle_resultContainsFileId() throws Exception {
        UUID fileId = UUID.randomUUID();
        String csv = "a,b\n1,2\n";

        CsvFile csvFile = buildCsvFile(fileId, csv);
        when(csvFileRepository.findById(fileId)).thenReturn(Optional.of(csvFile));

        Job job = buildJob(fileId, ",", true);
        String resultJson = handler.handle(job);

        JsonNode node = mapper.readTree(resultJson);
        assertThat(node.get("fileId").asString()).isEqualTo(fileId.toString());
    }
}