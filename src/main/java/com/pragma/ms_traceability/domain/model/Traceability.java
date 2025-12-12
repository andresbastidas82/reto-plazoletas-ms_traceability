package com.pragma.ms_traceability.domain.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Traceability {
    private String id; // MongoDB usa String para los IDs autogenerados
    private Long orderId;
    private Long employeeId;
    private Long customerId;
    private String previousState;
    private String newState;
    private LocalDateTime datePreviousState;
    private LocalDateTime dateNewState;
}
