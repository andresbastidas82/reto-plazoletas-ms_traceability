package com.pragma.ms_traceability.infrastructure.out.mongodb.adapter;

import com.mongodb.BasicDBObject;
import com.pragma.ms_traceability.domain.model.Traceability;
import com.pragma.ms_traceability.domain.spi.ITraceabilityPersistencePort;
import com.pragma.ms_traceability.infrastructure.out.mongodb.document.TraceabilityDocument;
import com.pragma.ms_traceability.infrastructure.out.mongodb.dto.EmployeeRankingDTO;
import com.pragma.ms_traceability.infrastructure.out.mongodb.mapper.ITraceabilityDocumentMapper;
import com.pragma.ms_traceability.infrastructure.out.mongodb.repository.ITraceabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;



@Component
@RequiredArgsConstructor
public class TraceabilityMongoDbAdapter implements ITraceabilityPersistencePort {

    private final ITraceabilityRepository traceabilityRepository;
    private final ITraceabilityDocumentMapper traceabilityDocumentMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public Traceability saveTraceability(Traceability traceability) {
        TraceabilityDocument traceabilityDocument = traceabilityDocumentMapper.toDocument(traceability);

        return traceabilityDocumentMapper.toModel(traceabilityRepository.save(traceabilityDocument));
    }

    @Override
    public Traceability getLastTraceabilityByOrderId(Long orderId) {
        TraceabilityDocument traceabilityDocument = traceabilityRepository.findTopByOrderIdOrderByDateNewStateDesc(orderId)
                                                                            .orElse(null);
        return traceabilityDocumentMapper.toModel(traceabilityDocument);
    }

    @Override
    public Page<Traceability> getLogsByFilters(Traceability traceability, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "order_id");

        Pageable pageable = PageRequest.of(page, size, sort);
        Query query = new Query();
        if (traceability.getOrderId() != null) {
            query.addCriteria(Criteria.where("order_id").is(traceability.getOrderId()));
        }
        if (traceability.getEmployeeId() != null) {
            query.addCriteria(Criteria.where("employee_id").is(traceability.getEmployeeId()));
        }
        if (traceability.getCustomerId() != null) {
            query.addCriteria(Criteria.where("customer_id").is(traceability.getCustomerId()));
        }
        if (traceability.getNewState() != null && !traceability.getNewState().isEmpty()) {
            query.addCriteria(Criteria.where("new_state").is(traceability.getNewState()));
        }

        long total = mongoTemplate.count(query, TraceabilityDocument.class);
        query.with(pageable);
        List<TraceabilityDocument> documents = mongoTemplate.find(query, TraceabilityDocument.class);

        List<Traceability> traceabilityContent = documents.stream()
                .map(traceabilityDocumentMapper::toModel)
                .collect(Collectors.toList());
        return new PageImpl<>(traceabilityContent, pageable, total);
    }

    @Override
    public List<EmployeeRankingDTO> getEmployeePerformanceRanking(int cantidadPedidos){
        // 1️⃣ Match: solo PENDING y DELIVERED con employee
        MatchOperation matchStates = match(
                new Criteria().andOperator(
                        Criteria.where("employee_id").exists(true),
                        new Criteria().orOperator(
                                Criteria.where("previous_state").is("PENDING"),
                                Criteria.where("new_state").is("DELIVERED")
                        )
                )
        );

        // 2️⃣ Group por order + employee
        GroupOperation groupByOrderAndEmployee = group(
                Fields.from(
                        Fields.field("orderId", "order_id"),
                        Fields.field("employeeId", "employee_id")
                )
        )
                .min(
                        ConditionalOperators.when(Criteria.where("previous_state").is("PENDING"))
                                .thenValueOf("date_previous_state")
                                .otherwiseValueOf("$$REMOVE")
                ).as("pendingDate")
                .max(
                        ConditionalOperators.when(Criteria.where("new_state").is("DELIVERED"))
                                .thenValueOf("date_new_state")
                                .otherwiseValueOf("$$REMOVE")
                ).as("deliveredDate");

        // 3️⃣ Match pedidos válidos
        MatchOperation validOrders = match(
                Criteria.where("pendingDate").ne(null)
                        .and("deliveredDate").ne(null)
        );

        // 4️⃣ Project: calcular tiempo en minutos
        ProjectionOperation calculateAttentionTime = project()
                .and("_id.employeeId").as("employeeId")
                .and("_id.orderId").as("orderId")
                .and(
                        ArithmeticOperators.Divide.valueOf(
                                ArithmeticOperators.Subtract.valueOf("deliveredDate")
                                        .subtract("pendingDate")
                        ).divideBy(1000 * 60)
                ).as("attentionTime")
                .and("deliveredDate").as("deliveredDate");

        // 5️⃣ Sort por pedidos más recientes
        SortOperation sortByRecentOrders =
                sort(Sort.Direction.DESC, "deliveredDate");

        // 6️⃣ Group por empleado
        GroupOperation groupByEmployee = group("employeeId")
                .push(
                        new BasicDBObject("orderId", "$orderId")
                                .append("attentionTime",
                                        new BasicDBObject("$round", List.of("$attentionTime", 2)))
                ).as("orders");

        // 7️⃣ Limitar cantidad de pedidos
        ProjectionOperation limitOrders = project()
                .and("_id").as("employeeId")
                .and(
                        ArrayOperators.Slice.sliceArrayOf("orders")
                                .itemCount(cantidadPedidos)
                ).as("orders");

        // 8️⃣ Calcular promedio
        AddFieldsOperation calculateAverage = addFields()
                .addField("averageTime")
                .withValue(
                        new BasicDBObject("$round",
                                List.of(new BasicDBObject("$avg", "$orders.attentionTime"), 2))
                )
                .build();

        // 9️⃣ Sort por mejor promedio
        SortOperation sortByAverage =
                sort(Sort.Direction.ASC, "averageTime");

        Aggregation aggregation = Aggregation.newAggregation(
                matchStates,
                groupByOrderAndEmployee,
                validOrders,
                calculateAttentionTime,
                sortByRecentOrders,
                groupByEmployee,
                limitOrders,
                calculateAverage,
                sortByAverage
        );

        List<EmployeeRankingDTO> results =
                mongoTemplate.aggregate(
                        aggregation,
                        "traceability_logs",
                        EmployeeRankingDTO.class
                ).getMappedResults();

        // Ranking en Java
        AtomicInteger rank = new AtomicInteger(1);

        return results.stream()
                .map(r -> new EmployeeRankingDTO(
                        r.employeeId(),
                        rank.getAndIncrement(),
                        r.averageTime(),
                        r.orders()
                ))
                .toList();
        }
}
