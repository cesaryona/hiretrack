package br.com.hiretrack.application.web.response;

import java.time.Instant;
import java.util.UUID;

public record CandidateResponse(UUID id, String name, String email, Instant createdAt, Instant updatedAt) {
}
