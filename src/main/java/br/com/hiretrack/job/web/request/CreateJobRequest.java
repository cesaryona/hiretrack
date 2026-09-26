package br.com.hiretrack.job.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJobRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 150) String company,
        @NotBlank String description) {
}
