package com.pragma.ms_traceability.infrastructure.out.mongodb.mapper;

import com.pragma.ms_traceability.domain.model.Traceability;
import com.pragma.ms_traceability.infrastructure.out.mongodb.document.TraceabilityDocument;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface ITraceabilityDocumentMapper {

    Traceability toModel(TraceabilityDocument traceabilityDocument);

    TraceabilityDocument toDocument(Traceability traceability);
}
