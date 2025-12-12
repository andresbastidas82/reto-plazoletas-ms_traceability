package com.pragma.ms_traceability.infrastructure.out.mongodb.document;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "traceability_logs") // Nombre de la colección en MongoDB
@Getter
@Setter
public class TraceabilityDocument {

    @Id
    private String id;

    @Field("order_id")
    private Long orderId;

    @Field("employee_id")
    private Long employeeId;

    @Field("customer_id")
    private Long customerId;

    @Field("previous_state")
    private String previousState;

    @Field("new_state")
    private String newState;

    @Field("date_previous_state")
    private LocalDateTime datePreviousState;

    @Field("date_new_state")
    private LocalDateTime dateNewState;
}
