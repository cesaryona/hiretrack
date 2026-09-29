package br.com.hiretrack.job.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJobRequest(
        @NotBlank(message = "must not be blank") @Size(max = 150) String title,
        @NotBlank(message = "must not be blank") @Size(max = 150) String company,
        @NotBlank(message = "must not be blank") String description) {
}
