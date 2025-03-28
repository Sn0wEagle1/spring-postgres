package com.postgresql.springlab.service;

import com.postgresql.springlab.model.History;
import com.postgresql.springlab.model.Signature;
import com.postgresql.springlab.model.Audit;
import com.postgresql.springlab.repository.HistoryRepository;
import com.postgresql.springlab.repository.AuditRepository;
import com.postgresql.springlab.repository.SignatureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VersioningService {

    private final HistoryRepository historyRepository;
    private final AuditRepository auditRepository;
    private final SignatureRepository signatureRepository;

    public VersioningService(HistoryRepository historyRepository,
                             AuditRepository auditRepository,
                             SignatureRepository signatureRepository) {
        this.historyRepository = historyRepository;
        this.auditRepository = auditRepository;
        this.signatureRepository = signatureRepository;
    }

    @Transactional
    public void saveVersion(Signature signature, String changeType, String changedBy, String fieldsChanged) {
        if (signature.getStatus() == null) {
            signature.setStatus("ACTIVE");
        }

        signatureRepository.save(signature);

        History history = new History();
        history.setSignatureId(signature.getId());
        history.setVersionCreatedAt(LocalDateTime.now());
        history.setThreatName(signature.getThreatName());
        history.setFirstBytes(signature.getFirstBytes());
        history.setRemainderHash(signature.getRemainderHash());
        history.setRemainderLength(signature.getRemainderLength());
        history.setFileType(signature.getFileType());
        history.setOffsetStart(signature.getOffsetStart());
        history.setOffsetEnd(signature.getOffsetEnd());
        history.setDigitalSignature(signature.getDigitalSignature());
        history.setStatus(signature.getStatus());
        history.setUpdatedAt(signature.getUpdatedAt());

        historyRepository.save(history);

        Audit audit = new Audit();
        audit.setSignatureId(signature.getId());
        audit.setChangedBy(changedBy);
        audit.setChangeType(changeType);
        audit.setChangedAt(LocalDateTime.now());
        audit.setFieldsChanged(fieldsChanged);

        auditRepository.save(audit);
    }

    @Transactional
    public void saveVersion(Signature signature, String changeType, String changedBy) {
        if (signature.getStatus() == null) {
            signature.setStatus("ACTIVE");
        }

        signatureRepository.save(signature);

        History history = new History();
        history.setSignatureId(signature.getId());
        history.setVersionCreatedAt(LocalDateTime.now());
        history.setThreatName(signature.getThreatName());
        history.setFirstBytes(signature.getFirstBytes());
        history.setRemainderHash(signature.getRemainderHash());
        history.setRemainderLength(signature.getRemainderLength());
        history.setFileType(signature.getFileType());
        history.setOffsetStart(signature.getOffsetStart());
        history.setOffsetEnd(signature.getOffsetEnd());
        history.setDigitalSignature(signature.getDigitalSignature());
        history.setStatus(signature.getStatus());
        history.setUpdatedAt(signature.getUpdatedAt());

        historyRepository.save(history);

        Audit audit = new Audit();
        audit.setSignatureId(signature.getId());
        audit.setChangedBy(changedBy);
        audit.setChangeType(changeType);
        audit.setChangedAt(LocalDateTime.now());
        audit.setFieldsChanged(null); // Устанавливаем null, если поле fieldsChanged не нужно

        auditRepository.save(audit);
    }


}
