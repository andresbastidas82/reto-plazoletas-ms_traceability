package com.pragma.ms_traceability.application.mapper;

import com.pragma.ms_traceability.application.dto.LogsResponse;
import com.pragma.ms_traceability.application.dto.TraceabilityRequest;
import com.pragma.ms_traceability.domain.model.Traceability;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface ITraceabilityRequestMapper {

    Traceability toTraceability(TraceabilityRequest traceabilityRequest);

    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "attentionTime", source = ".", qualifiedByName = "getAttentionTime")
    LogsResponse toLogsResponse(Traceability traceability);

    @Named("getAttentionTime")
    default BigDecimal getAttentionTime(Traceability traceability) {
        LocalDateTime startDate = traceability.getDatePreviousState();
        LocalDateTime finishDate = traceability.getDateNewState();
        if (startDate != null && finishDate != null) {
            long seconds = Duration.between(startDate, finishDate).getSeconds();
            return BigDecimal.valueOf(seconds)
                    .divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
        }
        return null;
    }
}
