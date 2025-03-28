package com.postgresql.springlab.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signatures")
public class Signature {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String threatName;

    @Column(nullable = false, length = 8)
    private byte[] firstBytes;

    @Lob
    private byte[] remainderBytes;

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

    @Lob
    private byte[] digitalSignature;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String status; // ACTUAL, DELETED, CORRUPTED

    // Геттеры и сеттеры
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getThreatName() {
        return threatName;
    }

    public void setThreatName(String threatName) {
        this.threatName = threatName;
    }

    public byte[] getRemainderBytes() {
        return remainderBytes;
    }

    public void setRemainderBytes(byte[] remainderBytes) {
        this.remainderBytes = remainderBytes;
    }

    public byte[] getFirstBytes() {
        return firstBytes;
    }

    public void setFirstBytes(byte[] firstBytes) {
        this.firstBytes = firstBytes;
    }

    public String getRemainderHash() {
        return remainderHash;
    }

    public void setRemainderHash(String remainderHash) {
        this.remainderHash = remainderHash;
    }

    public int getRemainderLength() {
        return remainderLength;
    }

    public void setRemainderLength(int remainderLength) {
        this.remainderLength = remainderLength;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getOffsetStart() {
        return offsetStart;
    }

    public void setOffsetStart(int offsetStart) {
        this.offsetStart = offsetStart;
    }

    public int getOffsetEnd() {
        return offsetEnd;
    }

    public void setOffsetEnd(int offsetEnd) {
        this.offsetEnd = offsetEnd;
    }

    public byte[] getDigitalSignature() {
        return digitalSignature;
    }

    public void setDigitalSignature(byte[] digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

