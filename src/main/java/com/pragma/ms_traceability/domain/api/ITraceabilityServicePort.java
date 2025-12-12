package com.pragma.ms_traceability.domain.api;

import com.pragma.ms_traceability.domain.model.Traceability;
import org.springframework.data.domain.Page;

public interface ITraceabilityServicePort {
    Traceability saveTraceability(Traceability traceability);
    Page<Traceability> getLogsByFilters(Traceability traceability, int page, int size);
}
