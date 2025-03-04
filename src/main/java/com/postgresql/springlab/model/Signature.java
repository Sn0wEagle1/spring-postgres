package com.postgresql.springlab.model;

import jakarta.persistence.*;

@Entity
@Table(name = "signatures")
public class Signature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String objectName;
    private byte[] first8Bytes;
    private String hashTail;
    private int tailLength;
    private String fileType;
    private int startOffset;
    private int endOffset;

    public Signature() {
    }

    public Signature(String objectName, byte[] first8Bytes, String hashTail, int tailLength, String fileType, int startOffset, int endOffset) {
        this.objectName = objectName;
        this.first8Bytes = first8Bytes;
        this.hashTail = hashTail;
        this.tailLength = tailLength;
        this.fileType = fileType;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public Long getId() {
        return id;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public byte[] getFirst8Bytes() {
        return first8Bytes;
    }

    public void setFirst8Bytes(byte[] first8Bytes) {
        this.first8Bytes = first8Bytes;
    }

    public String getHashTail() {
        return hashTail;
    }

    public void setHashTail(String hashTail) {
        this.hashTail = hashTail;
    }

    public int getTailLength() {
        return tailLength;
    }

    public void setTailLength(int tailLength) {
        this.tailLength = tailLength;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }
}
