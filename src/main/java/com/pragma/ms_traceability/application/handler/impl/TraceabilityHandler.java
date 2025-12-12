package com.pragma.ms_traceability.application.handler.impl;

import com.pragma.ms_traceability.application.dto.GenericResponse;
import com.pragma.ms_traceability.application.dto.LogsResponse;
import com.pragma.ms_traceability.application.dto.TraceabilityRequest;
import com.pragma.ms_traceability.application.handler.ITraceabilityHandler;
import com.pragma.ms_traceability.application.mapper.ITraceabilityRequestMapper;
import com.pragma.ms_traceability.domain.api.ITraceabilityServicePort;
import com.pragma.ms_traceability.domain.model.Traceability;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TraceabilityHandler implements ITraceabilityHandler {

    private final ITraceabilityServicePort traceabilityServicePort;
    private final ITraceabilityRequestMapper traceabilityRequestMapper;


    @Override
    public GenericResponse saveTraceability(TraceabilityRequest traceabilityRequest) {
        Traceability traceability = traceabilityRequestMapper.toTraceability(traceabilityRequest);
        traceabilityServicePort.saveTraceability(traceability);
        return GenericResponse.builder()
                .code("CREATED")
                .httpStatus(HttpStatus.CREATED.value())
                .message("Traceability saved successfully")
                .build();
    }

    @Override
    public Page<LogsResponse> getLogsByOrderId(TraceabilityRequest traceabilityRequest, int page, int size) {
        Traceability traceability = traceabilityRequestMapper.toTraceability(traceabilityRequest);
        return traceabilityServicePort.getLogsByFilters(traceability, page, size)
                .map(traceabilityRequestMapper::toLogsResponse);
    }
}
