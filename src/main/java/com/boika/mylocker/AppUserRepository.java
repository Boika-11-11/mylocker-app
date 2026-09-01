package com.boika.mylocker;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByResetToken(String resetToken);

    List<AppUser> findByApprovedFalse();

    List<AppUser> findAllByOrderByUsernameAsc();
}