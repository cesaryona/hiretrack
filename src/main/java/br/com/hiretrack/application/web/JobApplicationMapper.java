package br.com.hiretrack.application.web;

import br.com.hiretrack.application.domain.JobApplication;
import br.com.hiretrack.application.web.response.JobApplicationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface JobApplicationMapper {

    JobApplicationResponse toResponse(JobApplication application);
}
