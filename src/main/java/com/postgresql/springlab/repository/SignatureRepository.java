package com.postgresql.springlab.repository;

import com.postgresql.springlab.model.Signature;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;


public interface SignatureRepository extends JpaRepository<Signature, UUID> {
    List<Signature> findByUpdatedAtAfter(LocalDateTime since);

    @Query("SELECT s FROM Signature s WHERE s.status != 'DELETED'")
    List<Signature> findAllActive();

    // Метод для поиска записей, обновленных после указанной даты, и со статусом не DELETED
    @Query("SELECT s FROM Signature s WHERE s.updatedAt > :since")
    List<Signature> findByUpdatedAtAfterAndActiveStatus(LocalDateTime since);

    List<Signature> findByStatusIgnoreCase(String status);
    List<Signature> findByUpdatedAtAfterAndStatusIgnoreCase(LocalDateTime updatedAt, String status);

}