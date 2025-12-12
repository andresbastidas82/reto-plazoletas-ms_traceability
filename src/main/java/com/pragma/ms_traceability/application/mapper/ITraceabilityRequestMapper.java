package com.pragma.ms_traceability.application.mapper;

import com.pragma.ms_traceability.application.dto.LogsResponse;
import com.pragma.ms_traceability.application.dto.TraceabilityRequest;
import com.pragma.ms_traceability.domain.model.Traceability;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface ITraceabilityRequestMapper {

    Traceability toTraceability(TraceabilityRequest traceabilityRequest);

    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    LogsResponse toLogsResponse(Traceability traceability);
}
