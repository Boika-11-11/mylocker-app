package com.boika.mylocker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository repository;
    private final PasswordEncoder encoder;

    public DataSeeder(AppUserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {

        if (repository.findByUsername("boika").isEmpty()) {

            AppUser admin = new AppUser();
            admin.setUsername("boika");
            admin.setPassword(encoder.encode("Locker@2026"));
            admin.setRole("ADMIN");
            admin.setApproved(true);

            repository.save(admin);

            System.out.println(">>> Created ADMIN user: boika");
        }
    }
}