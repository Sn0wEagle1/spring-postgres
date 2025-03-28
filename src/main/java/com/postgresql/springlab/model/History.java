package com.postgresql.springlab.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @Column(nullable = false)
    private UUID signatureId;

    @Column(nullable = false)
    private LocalDateTime versionCreatedAt;

    @Column(nullable = false)
    private String threatName;

    @Column(nullable = false)
    private byte[] firstBytes;

    @Column(nullable = false)
    private String remainderHash;

    @Column(nullable = false)
    private int remainderLength;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private int offsetStart;

    @Column(nullable = false)
    private int offsetEnd;

    @Column(nullable = false)
    private byte[] digitalSignature;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Геттеры
    public Long getHistoryId() {
        return historyId;
    }

    public UUID getSignatureId() {
        return signatureId;
    }

    public LocalDateTime getVersionCreatedAt() {
        return versionCreatedAt;
    }

    public String getThreatName() {
        return threatName;
    }

    public byte[] getFirstBytes() {
        return firstBytes;
    }

    public String getRemainderHash() {
        return remainderHash;
    }

    public int getRemainderLength() {
        return remainderLength;
    }

    public String getFileType() {
        return fileType;
    }

    public int getOffsetStart() {
        return offsetStart;
    }

    public int getOffsetEnd() {
        return offsetEnd;
    }

    public byte[] getDigitalSignature() {
        return digitalSignature;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Сеттеры
    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public void setSignatureId(UUID signatureId) {
        this.signatureId = signatureId;
    }

    public void setVersionCreatedAt(LocalDateTime versionCreatedAt) {
        this.versionCreatedAt = versionCreatedAt;
    }

    public void setThreatName(String threatName) {
        this.threatName = threatName;
    }

    public void setFirstBytes(byte[] firstBytes) {
        this.firstBytes = firstBytes;
    }

    public void setRemainderHash(String remainderHash) {
        this.remainderHash = remainderHash;
    }

    public void setRemainderLength(int remainderLength) {
        this.remainderLength = remainderLength;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setOffsetStart(int offsetStart) {
        this.offsetStart = offsetStart;
    }

    public void setOffsetEnd(int offsetEnd) {
        this.offsetEnd = offsetEnd;
    }

    public void setDigitalSignature(byte[] digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

