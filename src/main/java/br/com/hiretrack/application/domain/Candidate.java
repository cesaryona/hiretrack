package br.com.hiretrack.application.domain;

import br.com.hiretrack.shared.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Candidate extends BaseEntity {

    private String name;
    private String email;

    public Candidate(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
