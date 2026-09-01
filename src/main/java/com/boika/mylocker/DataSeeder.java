package com.boika.mylocker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppUserRepository repository;
    private final PasswordEncoder encoder;

    @Value("${mylocker.admin.username:}")
    private String adminUsername;

    @Value("${mylocker.admin.password:}")
    private String adminPassword;

    public DataSeeder(AppUserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {

        if (adminUsername == null || adminUsername.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            log.warn(">>> No admin credentials configured. Skipping admin creation.");
            return;
        }

        if (repository.findByUsername(adminUsername).isPresent()) {
            return;
        }

        AppUser admin = new AppUser();
        admin.setUsername(adminUsername);
        admin.setPassword(encoder.encode(adminPassword));
        admin.setRole("ADMIN");
        admin.setApproved(true);

        repository.save(admin);

        log.info(">>> Created ADMIN user: {}", adminUsername);
    }
}