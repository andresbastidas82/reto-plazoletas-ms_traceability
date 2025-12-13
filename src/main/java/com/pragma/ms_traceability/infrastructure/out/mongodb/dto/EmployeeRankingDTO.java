package com.pragma.ms_traceability.infrastructure.out.mongodb.dto;

import java.util.List;

public record EmployeeRankingDTO(
        Long employeeId,
        Integer ranking,
        Double averageTime,
        List<OrderRankingDTO> orders
) {}
