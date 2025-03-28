package com.postgresql.springlab.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit")
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @Column(nullable = false)
    private UUID signatureId;

    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false)
    private String changeType;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    private String fieldsChanged;

    // Геттеры
    public Long getAuditId() {
        return auditId;
    }

    public UUID getSignatureId() {
        return signatureId;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public String getChangeType() {
        return changeType;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public String getFieldsChanged() {
        return fieldsChanged;
    }

    // Сеттеры
    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public void setSignatureId(UUID signatureId) {
        this.signatureId = signatureId;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public void setFieldsChanged(String fieldsChanged) {
        this.fieldsChanged = fieldsChanged;
    }
}

