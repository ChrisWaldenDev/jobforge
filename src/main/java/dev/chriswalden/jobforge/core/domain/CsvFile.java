package dev.chriswalden.jobforge.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "csv_files")
@Getter
@Setter
@NoArgsConstructor
public class CsvFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "original_name")
    private String originalName;
    @Column(name = "content_type")
    private String contentType;

    @Column(nullable = false)
    private byte[] data;

    @Column(nullable = false, updatable = false, name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

}
