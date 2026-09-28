package br.com.hiretrack.application.web.response;

import br.com.hiretrack.application.domain.ApplicationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JobApplicationResponse(UUID id, UUID jobId, UUID candidateId, ApplicationStatus status,
                                     List<ApplicationStatus> availableStatuses, Instant createdAt, Instant updatedAt) {
}
