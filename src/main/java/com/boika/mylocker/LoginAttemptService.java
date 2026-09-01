package com.boika.mylocker;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 15;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    private static class Attempt {
        int count;
        Instant lockedUntil;
    }

    public void loginFailed(String username) {
        if (username == null || username.isBlank()) {
            return;
        }

        String key = username.toLowerCase();
        Attempt attempt = attempts.computeIfAbsent(key, k -> new Attempt());

        attempt.count++;

        if (attempt.count >= MAX_ATTEMPTS) {
            attempt.lockedUntil = Instant.now().plusSeconds(LOCK_MINUTES * 60);
            attempt.count = 0;
        }
    }

    public void loginSucceeded(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        attempts.remove(username.toLowerCase());
    }

    public boolean isBlocked(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        Attempt attempt = attempts.get(username.toLowerCase());

        if (attempt == null || attempt.lockedUntil == null) {
            return false;
        }

        if (Instant.now().isAfter(attempt.lockedUntil)) {
            attempts.remove(username.toLowerCase());
            return false;
        }

        return true;
    }

    public long minutesRemaining(String username) {
        Attempt attempt = attempts.get(username.toLowerCase());

        if (attempt == null || attempt.lockedUntil == null) {
            return 0;
        }

        long seconds = attempt.lockedUntil.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(1, seconds / 60);
    }
}