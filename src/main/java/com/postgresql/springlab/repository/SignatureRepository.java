package com.postgresql.springlab.repository;

import com.postgresql.springlab.model.Signature;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;


public interface SignatureRepository extends JpaRepository<Signature, UUID> {
    List<Signature> findByUpdatedAtAfter(LocalDateTime date);

}