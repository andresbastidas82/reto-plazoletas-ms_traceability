package com.pragma.ms_traceability.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TraceabilityRequest {
    private Long orderId;
    private Long employeeId;
    private Long customerId;
    private String previousState;
    private String newState;
    private LocalDateTime dateNewState;
}
