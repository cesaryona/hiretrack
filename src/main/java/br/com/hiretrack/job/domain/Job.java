package br.com.hiretrack.job.domain;

import br.com.hiretrack.shared.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Job extends BaseEntity {

    private String title;
    private String company;
    private String description;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.OPEN;

    public Job(String title, String company, String description) {
        this.title = title;
        this.company = company;
        this.description = description;
    }

    public void close() {
        this.status = JobStatus.CLOSED;
    }
}
