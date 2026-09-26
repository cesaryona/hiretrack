package br.com.hiretrack.application.web.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateJobApplicationRequest(@NotNull UUID jobId, @NotNull UUID candidateId) {
}
