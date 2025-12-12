package com.pragma.ms_traceability.domain.spi;

public interface IAuthenticationServicePort {
    Long getUserIdOfToken();
    String getNameOfToken();
    String getEmailOfToken();
    Long getRestaurantIdOfToken();
}
