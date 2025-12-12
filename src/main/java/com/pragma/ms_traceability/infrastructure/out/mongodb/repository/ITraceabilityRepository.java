package com.pragma.ms_traceability.infrastructure.out.mongodb.repository;

import com.pragma.ms_traceability.infrastructure.out.mongodb.document.TraceabilityDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITraceabilityRepository extends MongoRepository<TraceabilityDocument, String> {

    Optional<TraceabilityDocument> findTopByOrderIdOrderByDateNewStateDesc(Long orderId);
}
