package br.com.hiretrack.job.web;

import br.com.hiretrack.job.domain.Job;
import br.com.hiretrack.job.web.request.CreateJobRequest;
import br.com.hiretrack.job.web.response.JobResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface JobMapper {

    Job toEntity(CreateJobRequest request);

    JobResponse toResponse(Job job);
}
