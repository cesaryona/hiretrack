package br.com.hiretrack.job;

import br.com.hiretrack.TestcontainersConfiguration;
import br.com.hiretrack.job.domain.Job;
import br.com.hiretrack.job.domain.JobStatus;
import com.lib.exception.core.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class JobServiceTests {

    @Autowired
    JobService jobService;

    @Test
    void createsAndListsJobs() {
        var job = jobService.create(new Job("Backend Developer", "Xpto", "Java + Spring"));

        assertThat(job.getId()).isNotNull();
        assertThat(job.getStatus()).isEqualTo(JobStatus.OPEN);
        assertThat(jobService.list(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent())
                .extracting(Job::getId).contains(job.getId());
        assertThat(jobService.findById(job.getId()).getId()).isEqualTo(job.getId());
    }

    @Test
    void closesJob() {
        var job = jobService.create(new Job("Frontend Developer", "Xpto", "React"));

        var closed = jobService.close(job.getId());

        assertThat(closed.getStatus()).isEqualTo(JobStatus.CLOSED);
        assertThat(jobService.findById(job.getId()).getStatus()).isEqualTo(JobStatus.CLOSED);
    }

    @Test
    void findingUnknownJobThrowsNotFound() {
        assertThatThrownBy(() -> jobService.findById(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void closingUnknownJobThrowsNotFound() {
        assertThatThrownBy(() -> jobService.close(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }
}
