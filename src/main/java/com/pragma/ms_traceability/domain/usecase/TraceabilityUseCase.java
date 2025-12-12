package com.pragma.ms_traceability.domain.usecase;

import com.pragma.ms_traceability.domain.api.ITraceabilityServicePort;
import com.pragma.ms_traceability.domain.model.Traceability;
import com.pragma.ms_traceability.domain.spi.IAuthenticationServicePort;
import com.pragma.ms_traceability.domain.spi.ITraceabilityPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TraceabilityUseCase implements ITraceabilityServicePort {

    private final ITraceabilityPersistencePort traceabilityPersistencePort;
    private final IAuthenticationServicePort authenticationServicePort;

    @Override
    public Traceability saveTraceability(Traceability traceability) {
        if (traceability.getNewState().equalsIgnoreCase("PENDING")) {
            traceability.setDatePreviousState(traceability.getDateNewState());
        } else {
            Traceability lastTraceability = traceabilityPersistencePort.getLastTraceabilityByOrderId(traceability.getOrderId());
            if (lastTraceability != null) {
                traceability.setDatePreviousState(lastTraceability.getDateNewState());
            }
        }
        return traceabilityPersistencePort.saveTraceability(traceability);
    }

    @Override
    public Page<Traceability> getLogsByFilters(Traceability traceability, int page, int size) {
        traceability.setCustomerId(authenticationServicePort.getUserIdOfToken());
        return traceabilityPersistencePort.getLogsByFilters(traceability, page, size);
    }
}
