package com.boika.mylocker;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginFailureHandler.class);

    private final LoginAttemptService attemptService;

    public LoginFailureHandler(LoginAttemptService attemptService) {
        this.attemptService = attemptService;
        setDefaultFailureUrl("/login?error");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String email = request.getParameter("username");

        if (attemptService.isBlocked(email)) {
            log.warn("Account temporarily locked after repeated failed sign-in attempts: {}", email);
        }

        super.onAuthenticationFailure(request, response, exception);
    }
}