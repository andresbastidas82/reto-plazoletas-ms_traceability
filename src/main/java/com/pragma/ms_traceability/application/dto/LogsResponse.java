package com.pragma.ms_traceability.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogsResponse {
    private Long orderId;
    private String previousState;
    private String newState;
    private String datePreviousState;
    private String dateNewState;
    private BigDecimal attentionTime;
    private Long employeeId;
    private Long customerId;
}
