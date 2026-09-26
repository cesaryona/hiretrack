package br.com.hiretrack.job.web;

import br.com.hiretrack.job.JobService;
import br.com.hiretrack.job.web.request.CreateJobRequest;
import br.com.hiretrack.job.web.response.JobResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
class JobController {

    private final JobMapper jobMapper;
    private final JobService jobService;

    @PostMapping
    ResponseEntity<JobResponse> create(@Valid @RequestBody CreateJobRequest request) {
        var job = jobMapper.toResponse(jobService.create(jobMapper.toEntity(request)));
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(job.id()).toUri();
        return ResponseEntity.created(location).body(job);
    }

    @GetMapping
    List<JobResponse> list() {
        return jobService.list().stream().map(jobMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    ResponseEntity<JobResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.of(jobService.findById(id).map(jobMapper::toResponse));
    }

    @PatchMapping("/{id}/close")
    ResponseEntity<JobResponse> close(@PathVariable UUID id) {
        return ResponseEntity.of(jobService.close(id).map(jobMapper::toResponse));
    }
}
