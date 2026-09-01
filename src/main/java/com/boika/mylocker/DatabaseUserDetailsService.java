package com.boika.mylocker;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository repository;
    private final LoginAttemptService attemptService;

    public DatabaseUserDetailsService(AppUserRepository repository,
                                      LoginAttemptService attemptService) {
        this.repository = repository;
        this.attemptService = attemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {

        String cleanEmail = email == null ? "" : email.trim().toLowerCase();

        if (attemptService.isBlocked(cleanEmail)) {
            throw new LockedException("Too many failed attempts. Try again in "
                    + attemptService.minutesRemaining(cleanEmail) + " minute(s).");
        }

        AppUser found = repository.findByEmail(cleanEmail)
                .orElseThrow(() -> new UsernameNotFoundException("No such user"));

        return User.builder()
                .username(found.getEmail())
                .password(found.getPassword())
                .roles(found.getRole())
                .disabled(!found.isApproved())
                .build();
    }
}