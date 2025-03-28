package com.postgresql.springlab.repository;

import com.postgresql.springlab.model.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    History findBySignatureId(UUID signatureId);
}
