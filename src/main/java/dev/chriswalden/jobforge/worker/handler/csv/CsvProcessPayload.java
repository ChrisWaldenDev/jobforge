package dev.chriswalden.jobforge.worker.handler.csv;

public record CsvProcessPayload (
    String fileId,
    String delimiter,
    boolean hasHeader
) {}
