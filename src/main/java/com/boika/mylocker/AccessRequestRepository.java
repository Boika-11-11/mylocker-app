package com.boika.mylocker;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {

    List<AccessRequest> findAllByOrderByRequestedAtDesc();

    boolean existsByEmail(String email);
}