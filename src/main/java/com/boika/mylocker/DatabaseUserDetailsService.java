package com.boika.mylocker;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository repository;

    public DatabaseUserDetailsService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser found = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No such user"));

        return User.builder()
                .username(found.getUsername())
                .password(found.getPassword())
                .roles(found.getRole())
                .disabled(!found.isApproved())
                .build();
    }
}