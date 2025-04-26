package com.postgresql.springlab.model;
import java.util.UUID;

public class SignatureScanResult {

    private UUID signatureId;
    private String threatName;
    private long offsetFromStart;
    private long offsetFromEnd;
    private boolean matched;

    public SignatureScanResult(UUID signatureId, String threatName, long offsetFromStart, long offsetFromEnd, boolean matched) {
        this.signatureId = signatureId;
        this.threatName = threatName;
        this.offsetFromStart = offsetFromStart;
        this.offsetFromEnd = offsetFromEnd;
        this.matched = matched;
    }

    public UUID getSignatureId() {
        return signatureId;
    }

    public String getThreatName() {
        return threatName;
    }

    public long getOffsetFromStart() {
        return offsetFromStart;
    }

    public long getOffsetFromEnd() {
        return offsetFromEnd;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setSignatureId(UUID signatureId) {
        this.signatureId = signatureId;
    }

    public void setThreatName(String threatName) {
        this.threatName = threatName;
    }

    public void setOffsetFromStart(long offsetFromStart) {
        this.offsetFromStart = offsetFromStart;
    }

    public void setOffsetFromEnd(long offsetFromEnd) {
        this.offsetFromEnd = offsetFromEnd;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }
}