package br.com.hiretrack.application;

import java.util.UUID;

public record ApplicationStatusChangedEvent(UUID applicationId, UUID jobId, String candidateName,
                                            String candidateEmail, String previousStatus, String newStatus) {
}
