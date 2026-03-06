package dev.chriswalden.jobforge.api.repository;

import dev.chriswalden.jobforge.core.domain.CsvFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CsvFileRepository extends JpaRepository<CsvFile, UUID> {
}
