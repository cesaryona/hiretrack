package br.com.hiretrack.application.web.response;

import java.time.Instant;
import java.util.UUID;

public record ApplicationStatusHistoryResponse(UUID id, String previousStatus, String newStatus, Instant changedAt) {
}
