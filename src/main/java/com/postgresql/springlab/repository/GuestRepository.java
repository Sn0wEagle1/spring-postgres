package com.postgresql.springlab.repository;

import com.postgresql.springlab.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    Optional<Guest> findByName(String name);  // Найти гостя по имени
    Optional<Guest> findByEmail(String email);  // Найти гостя по email
}
