package br.com.hiretrack.application.infra;

import br.com.hiretrack.application.domain.JobApplication;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    boolean existsByJobIdAndCandidateId(UUID jobId, UUID candidateId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from JobApplication a where a.id = :id")
    Optional<JobApplication> findByIdForUpdate(UUID id);
}
