package com.postgresql.springlab.controller;

import com.postgresql.springlab.model.Signature;
import com.postgresql.springlab.service.SignatureService;
import com.postgresql.springlab.service.CryptoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/signatures")
public class SignatureController {

    private final SignatureService signatureService;
    private final CryptoService cryptoService;  // Добавляем зависимость

    public SignatureController(SignatureService signatureService, CryptoService cryptoService) {
        this.signatureService = signatureService;
        this.cryptoService = cryptoService;
    }

    @PostMapping
    public ResponseEntity<Signature> createSignature(@RequestBody Signature signature) {
        try {
            return ResponseEntity.ok(signatureService.createSignature(signature));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Boolean> verifySignature(@PathVariable UUID id) {
        Optional<Signature> optionalSignature = signatureService.getSignature(id);
        if (optionalSignature.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Signature signature = optionalSignature.get();
        try {
            String data = signature.getThreatName() + signature.getRemainderHash();
            boolean isValid = cryptoService.verifySignature(data, signature.getDigitalSignature()); // Используем экземпляр
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Signature>> getSignatures(@RequestParam(required = false) LocalDateTime since) {
        // Если параметр since предоставлен, фильтруем записи с updated_at позже, чем указано в параметре
        List<Signature> signatures = signatureService.getSignatures(since);
        return ResponseEntity.ok(signatures);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSignature(@PathVariable UUID id) {
        signatureService.deleteSignature(id);
        return ResponseEntity.noContent().build();
    }

    @ControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler(Exception.class)
        public ResponseEntity<String> handleException(Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
