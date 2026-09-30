package br.com.hiretrack.application;

import br.com.hiretrack.TestcontainersConfiguration;
import br.com.hiretrack.application.domain.ApplicationStatus;
import br.com.hiretrack.application.domain.BusinessRuleException;
import br.com.hiretrack.application.domain.Candidate;
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

import java.util.UUID;

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
    void rejectsClosedJob() {
        var jobId = openJob();
        jobService.close(jobId);

        assertThatThrownBy(() -> applicationService.apply(jobId, newCandidate()))
                .isInstanceOf(BusinessRuleException.class);
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
    void changingStatusOfUnknownApplicationThrowsNotFound() {
        assertThatThrownBy(() -> applicationService.changeStatus(UUID.randomUUID(), ApplicationStatus.UNDER_REVIEW))
                .isInstanceOf(NotFoundException.class);
    }

    private UUID openJob() {
        return jobService.create(new Job("Backend Developer", "Xpto", "Java + Spring")).getId();
    }

    private UUID newCandidate() {
        return applicationService.createCandidate(new Candidate("Maria", UUID.randomUUID() + "@mail.com")).getId();
    }
}
