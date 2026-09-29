package br.com.hiretrack.application.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCandidateRequest(
        @NotBlank(message = "must not be blank") @Size(max = 150) String name,
        @NotBlank(message = "must not be blank") @Email @Size(max = 150) String email) {
}
