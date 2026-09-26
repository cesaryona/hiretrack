package br.com.hiretrack.application.web.request;

import br.com.hiretrack.application.domain.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(@NotNull ApplicationStatus status) {
}
