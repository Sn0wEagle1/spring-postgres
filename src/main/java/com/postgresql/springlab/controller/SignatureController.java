package com.postgresql.springlab.controller;

import com.postgresql.springlab.model.Signature;
import com.postgresql.springlab.service.SignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/signatures")
public class SignatureController {

    private final SignatureService signatureService;

    public SignatureController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @PostMapping
    public ResponseEntity<Signature> createSignature(@RequestBody Signature signature) {
        return ResponseEntity.ok(signatureService.createSignature(signature));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Signature> getSignature(@PathVariable Long id) {
        return ResponseEntity.ok(signatureService.getSignatureById(id));
    }

    @GetMapping
    public ResponseEntity<List<Signature>> getAllSignatures() {
        return ResponseEntity.ok(signatureService.getAllSignatures());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Signature> updateSignature(@PathVariable Long id, @RequestBody Signature signature) {
        return ResponseEntity.ok(signatureService.updateSignature(id, signature));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSignature(@PathVariable Long id) {
        signatureService.deleteSignature(id);
        return ResponseEntity.noContent().build();
    }
}
