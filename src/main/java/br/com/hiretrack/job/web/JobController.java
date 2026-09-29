package br.com.hiretrack.job.web;

import br.com.hiretrack.job.JobService;
import br.com.hiretrack.job.web.request.CreateJobRequest;
import br.com.hiretrack.job.web.response.JobResponse;
import com.lib.exception.core.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    Page<JobResponse> list(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return jobService.list(pageable).map(jobMapper::toResponse);
    }

    @GetMapping("/{id}")
    JobResponse findById(@PathVariable UUID id) {
        return jobMapper.toResponse(jobService.findById(id).orElseThrow(NotFoundException::new));
    }

    @PatchMapping("/{id}/close")
    JobResponse close(@PathVariable UUID id) {
        return jobMapper.toResponse(jobService.close(id));
    }
}
