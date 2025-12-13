package com.pragma.ms_traceability.domain.api;

import com.pragma.ms_traceability.domain.model.Traceability;
import com.pragma.ms_traceability.infrastructure.out.mongodb.dto.EmployeeRankingDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ITraceabilityServicePort {
    Traceability saveTraceability(Traceability traceability);
    Page<Traceability> getLogsByFilters(Traceability traceability, int page, int size);
    List<EmployeeRankingDTO> getEmployeePerformanceRanking(int limit);
}
