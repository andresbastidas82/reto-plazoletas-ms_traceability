package com.pragma.ms_traceability.infrastructure.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class PrincipalProvider {
    private UserPrincipal getPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Long getUserId() {
        return getPrincipal().getId();
    }


    public String getUserName() {
        return getPrincipal().getName();
    }

    public String getUserEmail() {
        return getPrincipal().getEmail();
    }

    public Long getRestaurantId() {
        return getPrincipal().getRestaurantId();
    }
}
