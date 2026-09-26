package br.com.hiretrack.job.web.response;

import br.com.hiretrack.job.domain.JobStatus;

import java.time.Instant;
import java.util.UUID;

public record JobResponse(UUID id, String title, String company, String description, JobStatus status,
                          Instant createdAt, Instant updatedAt) {
}
