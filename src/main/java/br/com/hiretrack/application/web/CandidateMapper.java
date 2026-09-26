package br.com.hiretrack.application.web;

import br.com.hiretrack.application.domain.Candidate;
import br.com.hiretrack.application.web.request.CreateCandidateRequest;
import br.com.hiretrack.application.web.response.CandidateResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface CandidateMapper {

    Candidate toEntity(CreateCandidateRequest request);

    CandidateResponse toResponse(Candidate candidate);
}
