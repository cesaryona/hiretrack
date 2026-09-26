package br.com.hiretrack.job;

import br.com.hiretrack.TestcontainersConfiguration;
import br.com.hiretrack.job.domain.Job;
import br.com.hiretrack.job.domain.JobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(jobService.list()).extracting("title").contains("Backend Developer");
        assertThat(jobService.findById(job.getId())).isPresent();
    }

    @Test
    void closesJob() {
        var job = jobService.create(new Job("Frontend Developer", "Xpto", "React"));

        jobService.close(job.getId());

        assertThat(jobService.findById(job.getId())).get().extracting(Job::getStatus).isEqualTo(JobStatus.CLOSED);
    }

    @Test
    void closingUnknownJobReturnsEmpty() {
        assertThat(jobService.close(UUID.randomUUID())).isEmpty();
    }
}
