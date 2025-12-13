package com.pragma.ms_traceability.application.handler;

import com.pragma.ms_traceability.application.dto.GenericResponse;
import com.pragma.ms_traceability.application.dto.LogsResponse;
import com.pragma.ms_traceability.application.dto.TraceabilityRequest;
import com.pragma.ms_traceability.infrastructure.out.mongodb.dto.EmployeeRankingDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ITraceabilityHandler {

    GenericResponse saveTraceability(TraceabilityRequest traceabilityRequest);

    Page<LogsResponse> getLogsByOrderId(TraceabilityRequest traceabilityRequest, int page, int size);

    List<EmployeeRankingDTO> getPerformanceRanking(int size);
}
