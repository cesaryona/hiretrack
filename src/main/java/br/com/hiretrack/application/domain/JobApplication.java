package br.com.hiretrack.application.domain;

import br.com.hiretrack.shared.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobApplication extends BaseEntity {

    private UUID jobId;
    private UUID candidateId;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.RECEIVED;

    public JobApplication(UUID jobId, UUID candidateId) {
        this.jobId = jobId;
        this.candidateId = candidateId;
    }

    public void changeStatus(ApplicationStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new BusinessRuleException("Cannot change status from %s to %s".formatted(status, newStatus));
        }
        this.status = newStatus;
    }
}
