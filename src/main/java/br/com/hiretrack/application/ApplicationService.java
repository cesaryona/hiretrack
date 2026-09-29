package br.com.hiretrack.application;

import br.com.hiretrack.application.domain.ApplicationStatus;
import br.com.hiretrack.application.domain.ApplicationStatusHistory;
import br.com.hiretrack.application.domain.BusinessRuleException;
import br.com.hiretrack.application.domain.Candidate;
import br.com.hiretrack.application.domain.JobApplication;
import br.com.hiretrack.application.infra.ApplicationStatusHistoryRepository;
import br.com.hiretrack.application.infra.CandidateRepository;
import br.com.hiretrack.application.infra.JobApplicationRepository;
import br.com.hiretrack.job.JobService;
import com.lib.exception.core.NotFoundException;
import com.lib.exception.enums.ExceptionEnum;
import com.lib.exception.core.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final CandidateRepository candidateRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
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
    public Candidate findCandidateById(UUID id) {
        return candidateRepository.findById(id).orElseThrow(NotFoundException::new);
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
            throw new ApiException(ExceptionEnum.CONFLICT, "Candidate already applied to this job");
        }
        return jobApplicationRepository.save(new JobApplication(jobId, candidateId));
    }

    @Transactional(readOnly = true)
    public JobApplication findById(UUID id) {
        return jobApplicationRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<ApplicationStatusHistory> findHistoryByApplicationId(UUID applicationId) {
        return statusHistoryRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId);
    }

    @Transactional
    public JobApplication changeStatus(UUID id, ApplicationStatus newStatus) {
        var application = jobApplicationRepository.findById(id).orElseThrow(NotFoundException::new);
        var previousStatus = application.getStatus();
        application.changeStatus(newStatus);
        var candidate = candidateRepository.getReferenceById(application.getCandidateId());
        events.publishEvent(ApplicationStatusChangedEvent.of(application, candidate, previousStatus, newStatus));
        return application;
    }
}
