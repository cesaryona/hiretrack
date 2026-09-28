package br.com.hiretrack.application;

import br.com.hiretrack.application.domain.ApplicationStatus;
import br.com.hiretrack.application.domain.Candidate;
import br.com.hiretrack.application.domain.JobApplication;

import java.util.UUID;

public record ApplicationStatusChangedEvent(UUID applicationId, UUID jobId, String candidateName,
                                            String candidateEmail, String previousStatus, String newStatus) {

    public static ApplicationStatusChangedEvent of(JobApplication application, Candidate candidate,
                                                   ApplicationStatus previousStatus, ApplicationStatus newStatus) {
        return new ApplicationStatusChangedEvent(application.getId(), application.getJobId(),
                candidate.getName(), candidate.getEmail(), previousStatus.name(), newStatus.name());
    }
}
