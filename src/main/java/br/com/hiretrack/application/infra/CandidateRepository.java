package br.com.hiretrack.application.infra;

import br.com.hiretrack.application.domain.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CandidateRepository extends JpaRepository<Candidate, UUID> {

    boolean existsByEmail(String email);
}
