package com.postgresql.springlab.repository;

import com.postgresql.springlab.model.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    List<Audit> findBySignatureIdOrderByChangedAtDesc(UUID signatureId);
}

