package br.com.hiretrack.application;

import br.com.hiretrack.application.domain.ApplicationStatus;
import br.com.hiretrack.application.domain.BusinessRuleException;
import br.com.hiretrack.application.domain.Candidate;
import br.com.hiretrack.application.domain.JobApplication;
import br.com.hiretrack.application.infra.CandidateRepository;
import br.com.hiretrack.application.infra.JobApplicationRepository;
import br.com.hiretrack.job.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final CandidateRepository candidateRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobService jobService;
    private final ApplicationEventPublisher events;

    @Transactional
    public Candidate createCandidate(Candidate candidate) {
        if (candidateRepository.existsByEmail(candidate.getEmail())) {
            throw new BusinessRuleException("Email already registered");
        }
        return candidateRepository.save(candidate);
    }

    @Transactional(readOnly = true)
    public Optional<Candidate> findCandidateById(UUID id) {
        return candidateRepository.findById(id);
    }

    @Transactional
    public JobApplication apply(UUID jobId, UUID candidateId) {
        if (!jobService.isOpen(jobId)) {
            throw new BusinessRuleException("Job not found or closed");
        }
        if (!candidateRepository.existsById(candidateId)) {
            throw new BusinessRuleException("Candidate not found");
        }
        if (jobApplicationRepository.existsByJobIdAndCandidateId(jobId, candidateId)) {
            throw new BusinessRuleException("Candidate already applied to this job");
        }
        return jobApplicationRepository.save(new JobApplication(jobId, candidateId));
    }

    @Transactional(readOnly = true)
    public Optional<JobApplication> findById(UUID id) {
        return jobApplicationRepository.findById(id);
    }

    @Transactional
    public Optional<JobApplication> changeStatus(UUID id, ApplicationStatus newStatus) {
        return jobApplicationRepository.findById(id).map(application -> {
            var previousStatus = application.getStatus();
            application.changeStatus(newStatus);
            var candidate = candidateRepository.getReferenceById(application.getCandidateId());
            events.publishEvent(new ApplicationStatusChangedEvent(application.getId(), application.getJobId(),
                    candidate.getName(), candidate.getEmail(), previousStatus.name(), newStatus.name()));
            return application;
        });
    }
}
