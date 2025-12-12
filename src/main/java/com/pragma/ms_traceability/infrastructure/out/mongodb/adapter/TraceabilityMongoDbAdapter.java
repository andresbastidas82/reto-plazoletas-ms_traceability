package com.pragma.ms_traceability.infrastructure.out.mongodb.adapter;

import com.pragma.ms_traceability.domain.model.Traceability;
import com.pragma.ms_traceability.domain.spi.ITraceabilityPersistencePort;
import com.pragma.ms_traceability.infrastructure.out.mongodb.document.TraceabilityDocument;
import com.pragma.ms_traceability.infrastructure.out.mongodb.mapper.ITraceabilityDocumentMapper;
import com.pragma.ms_traceability.infrastructure.out.mongodb.repository.ITraceabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

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
                .toList();
        return new PageImpl<>(traceabilityContent, pageable, total);
    }
}
