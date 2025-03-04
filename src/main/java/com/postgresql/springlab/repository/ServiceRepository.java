package com.postgresql.springlab.repository;

import com.postgresql.springlab.model.Servicing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Servicing, Long> {
    List<Servicing> findByName(String name);  // Найти услугу по названию
}

