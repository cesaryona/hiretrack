package br.com.hiretrack.application.web;

import br.com.hiretrack.application.ApplicationService;
import br.com.hiretrack.application.web.request.CreateCandidateRequest;
import br.com.hiretrack.application.web.response.CandidateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/candidates")
@RequiredArgsConstructor
class CandidateController {

    private final CandidateMapper candidateMapper;
    private final ApplicationService applicationService;

    @PostMapping
    ResponseEntity<CandidateResponse> create(@Valid @RequestBody CreateCandidateRequest request) {
        var candidate = candidateMapper.toResponse(applicationService.createCandidate(candidateMapper.toEntity(request)));
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(candidate.id()).toUri();
        return ResponseEntity.created(location).body(candidate);
    }

    @GetMapping("/{id}")
    ResponseEntity<CandidateResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.of(applicationService.findCandidateById(id).map(candidateMapper::toResponse));
    }
}
