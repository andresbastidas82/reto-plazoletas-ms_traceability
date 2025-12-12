package com.pragma.ms_traceability.domain.spi;

import com.pragma.ms_traceability.domain.model.Traceability;
import org.springframework.data.domain.Page;

public interface ITraceabilityPersistencePort {

    Traceability saveTraceability(Traceability traceability);

    Traceability getLastTraceabilityByOrderId(Long orderId);

    Page<Traceability> getLogsByFilters(Traceability traceability, int page, int size);
}
