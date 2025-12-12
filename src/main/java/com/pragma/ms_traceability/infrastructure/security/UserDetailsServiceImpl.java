package com.pragma.ms_traceability.infrastructure.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // En un escenario real, no necesitamos cargar nada de la BD.
        // El rol se validará directamente en SecurityConfig.
        // Creamos un UserDetails "dummy" con un rol temporal.
        // Lo importante es que el username coincida.
        return new User(username, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEMP")));
    }
}
