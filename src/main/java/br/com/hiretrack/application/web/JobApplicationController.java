package br.com.hiretrack.application.web;

import br.com.hiretrack.application.ApplicationService;
import br.com.hiretrack.application.web.request.ChangeStatusRequest;
import br.com.hiretrack.application.web.request.CreateJobApplicationRequest;
import br.com.hiretrack.application.web.response.JobApplicationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
class JobApplicationController {

    private final JobApplicationMapper jobApplicationMapper;
    private final ApplicationService applicationService;

    @PostMapping
    ResponseEntity<JobApplicationResponse> apply(@Valid @RequestBody CreateJobApplicationRequest request) {
        var application = jobApplicationMapper.toResponse(applicationService.apply(request.jobId(), request.candidateId()));
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(application.id()).toUri();
        return ResponseEntity.created(location).body(application);
    }

    @GetMapping("/{id}")
    ResponseEntity<JobApplicationResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.of(applicationService.findById(id).map(jobApplicationMapper::toResponse));
    }

    @PatchMapping("/{id}/status")
    ResponseEntity<JobApplicationResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.of(applicationService.changeStatus(id, request.status()).map(jobApplicationMapper::toResponse));
    }
}
