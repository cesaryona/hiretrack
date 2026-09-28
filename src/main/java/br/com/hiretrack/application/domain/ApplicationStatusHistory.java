package br.com.hiretrack.application.domain;

import br.com.hiretrack.shared.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationStatusHistory extends BaseEntity {

    private UUID applicationId;
    private String previousStatus;
    private String newStatus;

    public ApplicationStatusHistory(UUID applicationId, String previousStatus, String newStatus) {
        this.applicationId = applicationId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
}
