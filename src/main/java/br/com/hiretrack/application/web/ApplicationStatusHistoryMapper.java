package br.com.hiretrack.application.web;

import br.com.hiretrack.application.domain.ApplicationStatusHistory;
import br.com.hiretrack.application.web.response.ApplicationStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
interface ApplicationStatusHistoryMapper {

    @Mapping(source = "createdAt", target = "changedAt")
    ApplicationStatusHistoryResponse toResponse(ApplicationStatusHistory history);
}
