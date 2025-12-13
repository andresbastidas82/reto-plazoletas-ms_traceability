package com.pragma.ms_traceability.domain.spi;

import com.pragma.ms_traceability.domain.model.Traceability;
import com.pragma.ms_traceability.infrastructure.out.mongodb.dto.EmployeeRankingDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ITraceabilityPersistencePort {

    Traceability saveTraceability(Traceability traceability);

    Traceability getLastTraceabilityByOrderId(Long orderId);

    Page<Traceability> getLogsByFilters(Traceability traceability, int page, int size);

    List<EmployeeRankingDTO> getEmployeePerformanceRanking(int limit);
}
