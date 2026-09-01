package com.boika.mylocker;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptListener {

    private final LoginAttemptService attemptService;

    public LoginAttemptListener(LoginAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = String.valueOf(event.getAuthentication().getName());
        attemptService.loginFailed(username);
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = String.valueOf(event.getAuthentication().getName());
        attemptService.loginSucceeded(username);
    }
}