package br.com.hiretrack.job;

import br.com.hiretrack.job.domain.Job;
import br.com.hiretrack.job.domain.JobStatus;
import br.com.hiretrack.job.infra.JobRepository;
import lombok.RequiredArgsConstructor;
import com.lib.exception.core.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<Job> list(Pageable pageable) {
        return jobRepository.findAll(pageable);
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
    public Job close(UUID id) {
        var job = jobRepository.findById(id).orElseThrow(NotFoundException::new);
        job.close();
        return job;
    }
}
