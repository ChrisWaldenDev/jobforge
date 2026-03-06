package dev.chriswalden.jobforge.api.repository;

import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobStatus;
import dev.chriswalden.jobforge.core.domain.JobType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    List<Job> findByStatusAndTypeOrderByCreatedAtDesc(JobStatus status, JobType type, Pageable pageable);

    List<Job> findAllByType(JobType type);

    List<Job> findAllByStatus(JobStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select j
    from Job j
    where j.status = :status
        and (j.nextRunAt is null or j.nextRunAt <= :now)
    order by j.createdAt asc
""")
    List<Job> findClaimCandidates(
            @Param("status") JobStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );
}
