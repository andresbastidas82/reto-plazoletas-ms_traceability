package com.pragma.ms_traceability.infrastructure.out.mongodb.dto;

public record OrderRankingDTO(
        Long orderId,
        Double attentionTime
) {}
