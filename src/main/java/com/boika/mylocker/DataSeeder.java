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

    @Value("${mylocker.admin.email-login:}")
    private String adminEmail;

    @Value("${mylocker.admin.username:}")
    private String adminUsername;

    @Value("${mylocker.admin.password:}")
    private String adminPassword;

    @Value("${mylocker.demo.email:demo@hopeconnect.dev}")
    private String demoEmail;

    @Value("${mylocker.demo.password:}")
    private String demoPassword;

    public DataSeeder(AppUserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        createAdmin();
        createDemo();
    }

    private void createAdmin() {

        if (adminEmail == null || adminEmail.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            log.warn(">>> No admin credentials configured. Skipping admin creation.");
            return;
        }

        String cleanEmail = adminEmail.trim().toLowerCase();

        if (repository.findByEmail(cleanEmail).isPresent()) {
            return;
        }

        String displayName = (adminUsername == null || adminUsername.isBlank())
                ? "Admin"
                : capitalise(adminUsername.trim());

        AppUser admin = new AppUser();
        admin.setEmail(cleanEmail);
        admin.setUsername(displayName);
        admin.setPassword(encoder.encode(adminPassword));
        admin.setRole("ADMIN");
        admin.setApproved(true);

        repository.save(admin);

        log.info(">>> Created ADMIN user: {} ({})", displayName, cleanEmail);
    }

    private void createDemo() {

        if (demoPassword == null || demoPassword.isBlank()) {
            log.info(">>> No demo password configured. Skipping demo account.");
            return;
        }

        String cleanEmail = demoEmail.trim().toLowerCase();

        if (repository.findByEmail(cleanEmail).isPresent()) {
            return;
        }

        AppUser demo = new AppUser();
        demo.setEmail(cleanEmail);
        demo.setUsername("Demo");
        demo.setPassword(encoder.encode(demoPassword));
        demo.setRole("DEMO");
        demo.setApproved(true);

        repository.save(demo);

        log.info(">>> Created DEMO user: {}", cleanEmail);
    }

    private String capitalise(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}