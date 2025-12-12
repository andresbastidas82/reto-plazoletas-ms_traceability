package com.pragma.ms_traceability.infrastructure.input.rest;

import com.pragma.ms_traceability.application.dto.GenericResponse;
import com.pragma.ms_traceability.application.dto.LogsResponse;
import com.pragma.ms_traceability.application.dto.TraceabilityRequest;
import com.pragma.ms_traceability.application.handler.ITraceabilityHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/traceability")
@RequiredArgsConstructor
public class TraceabilityController {

    private final ITraceabilityHandler traceabilityHandler;

    @PostMapping("/save")
    public ResponseEntity<GenericResponse> saveTraceability(@RequestBody TraceabilityRequest traceabilityRequest) {
        return ResponseEntity.ok(traceabilityHandler.saveTraceability(traceabilityRequest));
    }

    @PostMapping("/logs-customer")
    public ResponseEntity<Page<LogsResponse>> getLogsByOrderId(
            @RequestBody TraceabilityRequest traceabilityRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(traceabilityHandler.getLogsByOrderId(traceabilityRequest, page, size));
    }

}
