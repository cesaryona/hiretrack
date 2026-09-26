package br.com.hiretrack.job;

import br.com.hiretrack.job.domain.Job;
import br.com.hiretrack.job.domain.JobStatus;
import br.com.hiretrack.job.infra.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    @Transactional
    public Job create(Job job) {
        return jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public List<Job> list() {
        return jobRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public Optional<Job> findById(UUID id) {
        return jobRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean isOpen(UUID id) {
        return jobRepository.findById(id).map(job -> job.getStatus() == JobStatus.OPEN).orElse(false);
    }

    @Transactional
    public Optional<Job> close(UUID id) {
        return jobRepository.findById(id).map(job -> {
            job.close();
            return job;
        });
    }
}
