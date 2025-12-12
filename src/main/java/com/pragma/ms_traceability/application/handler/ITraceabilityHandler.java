package com.pragma.ms_traceability.application.handler;

import com.pragma.ms_traceability.application.dto.GenericResponse;
import com.pragma.ms_traceability.application.dto.LogsResponse;
import com.pragma.ms_traceability.application.dto.TraceabilityRequest;
import org.springframework.data.domain.Page;

public interface ITraceabilityHandler {

    GenericResponse saveTraceability(TraceabilityRequest traceabilityRequest);

    Page<LogsResponse> getLogsByOrderId(TraceabilityRequest traceabilityRequest, int page, int size);
}
