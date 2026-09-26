package br.com.hiretrack.job.infra;

import br.com.hiretrack.job.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
}
