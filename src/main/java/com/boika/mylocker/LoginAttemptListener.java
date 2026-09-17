package com.boika.mylocker;

import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptListener {

    private final LoginAttemptService attemptService;
    private final AppUserRepository userRepository;

    public LoginAttemptListener(LoginAttemptService attemptService,
                                AppUserRepository userRepository) {
        this.attemptService = attemptService;
        this.userRepository = userRepository;
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String email = String.valueOf(event.getAuthentication().getName());
        attemptService.loginFailed(email);
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {

        String email = String.valueOf(event.getAuthentication().getName());

        attemptService.loginSucceeded(email);

        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}