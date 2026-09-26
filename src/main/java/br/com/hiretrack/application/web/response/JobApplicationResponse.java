package br.com.hiretrack.application.web.response;

import br.com.hiretrack.application.domain.ApplicationStatus;

import java.time.Instant;
import java.util.UUID;

public record JobApplicationResponse(UUID id, UUID jobId, UUID candidateId, ApplicationStatus status,
                                     Instant createdAt, Instant updatedAt) {
}
