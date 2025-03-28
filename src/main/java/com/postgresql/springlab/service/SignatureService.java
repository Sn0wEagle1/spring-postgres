package com.postgresql.springlab.service;

import com.postgresql.springlab.model.Signature;
import com.postgresql.springlab.repository.SignatureRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SignatureService {

    private final SignatureRepository signatureRepository;
    private final CryptoService cryptoService;

    public SignatureService(SignatureRepository signatureRepository, CryptoService cryptoService) {
        this.signatureRepository = signatureRepository;
        this.cryptoService = cryptoService;
    }

    public Signature createSignature(Signature signature) throws Exception {
        signature.setUpdatedAt(LocalDateTime.now());
        String dataToSign = signature.getThreatName() + signature.getRemainderHash();
        signature.setDigitalSignature(cryptoService.signData(dataToSign)); // Теперь это byte[]

        String remainderHash = cryptoService.calculateHash(signature.getRemainderBytes());
        signature.setRemainderHash(remainderHash);

        signature.setDigitalSignature(cryptoService.signData(dataToSign));
        return signatureRepository.save(signature);
    }


    public Optional<Signature> getSignature(UUID id) {
        return signatureRepository.findById(id);
    }

    @Transactional
    public List<Signature> getSignatures(LocalDateTime since) {
        return (since != null)
                ? signatureRepository.findByUpdatedAtAfter(since)
                : signatureRepository.findAll().stream()
                .filter(s -> "ACTUAL".equals(s.getStatus()))
                .toList();
    }

    public void deleteSignature(UUID id) {
        signatureRepository.findById(id).ifPresent(signature -> {
            signature.setStatus("DELETED");
            signature.setUpdatedAt(LocalDateTime.now());
            signatureRepository.save(signature);
        });
    }

}
