package com.postgresql.springlab.service;

import com.postgresql.springlab.model.Signature;
import com.postgresql.springlab.repository.SignatureRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignatureService {

    private final SignatureRepository signatureRepository;

    public SignatureService(SignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    public Signature createSignature(Signature signature) {
        return signatureRepository.save(signature);
    }

    public Signature getSignatureById(Long id) {
        return signatureRepository.findById(id).orElseThrow(() -> new RuntimeException("Signature not found"));
    }

    public List<Signature> getAllSignatures() {
        return signatureRepository.findAll();
    }

    public Signature updateSignature(Long id, Signature newSignature) {
        Signature signature = getSignatureById(id);
        signature.setObjectName(newSignature.getObjectName());
        signature.setFirst8Bytes(newSignature.getFirst8Bytes());
        signature.setHashTail(newSignature.getHashTail());
        signature.setTailLength(newSignature.getTailLength());
        signature.setFileType(newSignature.getFileType());
        signature.setStartOffset(newSignature.getStartOffset());
        signature.setEndOffset(newSignature.getEndOffset());
        return signatureRepository.save(signature);
    }

    public void deleteSignature(Long id) {
        signatureRepository.deleteById(id);
    }
}
