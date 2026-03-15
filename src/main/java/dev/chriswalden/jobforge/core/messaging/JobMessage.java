package dev.chriswalden.jobforge.core.messaging;

import dev.chriswalden.jobforge.core.domain.JobType;

import java.util.UUID;

public record JobMessage(UUID jobId, JobType jobType) {}