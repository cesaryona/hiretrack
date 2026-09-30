package br.com.hiretrack.application;

import br.com.hiretrack.TestcontainersConfiguration;
import br.com.hiretrack.application.domain.ApplicationStatus;
import br.com.hiretrack.application.domain.BusinessRuleException;
import br.com.hiretrack.application.domain.Candidate;
import br.com.hiretrack.application.infra.JobApplicationRepository;
import br.com.hiretrack.job.JobService;
import br.com.hiretrack.job.domain.Job;
import com.lib.exception.core.ApiException;
import com.lib.exception.core.NotFoundException;
import com.lib.exception.enums.ExceptionEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@RecordApplicationEvents
@Import(TestcontainersConfiguration.class)
class ApplicationServiceTests {

    @Autowired
    ApplicationService applicationService;

    @Autowired
    JobService jobService;

    @Autowired
    ApplicationEvents events;

    @Autowired
    JobApplicationRepository jobApplicationRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Test
    void appliesAndChangesStatus() {
        var application = applicationService.apply(openJob(), newCandidate());
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.RECEIVED);

        applicationService.changeStatus(application.getId(), ApplicationStatus.UNDER_REVIEW);

        assertThat(applicationService.findById(application.getId()).getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
        assertThat(events.stream(ApplicationStatusChangedEvent.class)).singleElement().satisfies(event -> {
            assertThat(event.previousStatus()).isEqualTo("RECEIVED");
            assertThat(event.newStatus()).isEqualTo("UNDER_REVIEW");
        });
    }

    @Test
    void rejectsInvalidTransition() {
        var application = applicationService.apply(openJob(), newCandidate());

        assertThatThrownBy(() -> applicationService.changeStatus(application.getId(), ApplicationStatus.APPROVED))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void concurrentStatusChangeWaitsForLockAndRevalidatesTransition() throws Exception {
        var application = applicationService.apply(openJob(), newCandidate());
        applicationService.changeStatus(application.getId(), ApplicationStatus.UNDER_REVIEW);
        applicationService.changeStatus(application.getId(), ApplicationStatus.INTERVIEW);

        var locked = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var approve = CompletableFuture.runAsync(() -> transactionTemplate.executeWithoutResult(status -> {
            jobApplicationRepository.findByIdForUpdate(application.getId());
            locked.countDown();
            await(release);
            applicationService.changeStatus(application.getId(), ApplicationStatus.APPROVED);
        }));
        assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();

        var reject = CompletableFuture.runAsync(() ->
                applicationService.changeStatus(application.getId(), ApplicationStatus.REJECTED));

        assertThatThrownBy(() -> reject.get(1, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);

        release.countDown();
        approve.get(5, TimeUnit.SECONDS);

        assertThatThrownBy(() -> reject.get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .cause().isInstanceOf(BusinessRuleException.class);
        assertThat(applicationService.findById(application.getId()).getStatus()).isEqualTo(ApplicationStatus.APPROVED);
    }

    @Test
    void rejectsClosedJob() {
        var jobId = openJob();
        jobService.close(jobId);

        assertThatThrownBy(() -> applicationService.apply(jobId, newCandidate()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void applyingToUnknownJobThrowsNotFound() {
        var candidateId = newCandidate();

        assertThatThrownBy(() -> applicationService.apply(UUID.randomUUID(), candidateId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void applyingWithUnknownCandidateThrowsNotFound() {
        var jobId = openJob();

        assertThatThrownBy(() -> applicationService.apply(jobId, UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void rejectsDuplicateApplication() {
        var jobId = openJob();
        var candidateId = newCandidate();
        applicationService.apply(jobId, candidateId);

        assertThatThrownBy(() -> applicationService.apply(jobId, candidateId))
                .isInstanceOf(ApiException.class)
                .extracting("type").isEqualTo(ExceptionEnum.CONFLICT);
    }

    @Test
    void findingUnknownApplicationThrowsNotFound() {
        assertThatThrownBy(() -> applicationService.findById(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findingUnknownCandidateThrowsNotFound() {
        assertThatThrownBy(() -> applicationService.findCandidateById(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findingHistoryOfUnknownApplicationThrowsNotFound() {
        assertThatThrownBy(() -> applicationService.findHistoryByApplicationId(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void changingStatusOfUnknownApplicationThrowsNotFound() {
        assertThatThrownBy(() -> applicationService.changeStatus(UUID.randomUUID(), ApplicationStatus.UNDER_REVIEW))
                .isInstanceOf(NotFoundException.class);
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private UUID openJob() {
        return jobService.create(new Job("Backend Developer", "Xpto", "Java + Spring")).getId();
    }

    private UUID newCandidate() {
        return applicationService.createCandidate(new Candidate("Maria", UUID.randomUUID() + "@mail.com")).getId();
    }
}
