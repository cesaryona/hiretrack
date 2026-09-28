package br.com.hiretrack.application.web;

import br.com.hiretrack.application.domain.JobApplication;
import br.com.hiretrack.application.web.response.JobApplicationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
interface JobApplicationMapper {

    @Mapping(target = "availableStatuses", expression = "java(application.getStatus().availableTransitions())")
    JobApplicationResponse toResponse(JobApplication application);
}
