package com.postgresql.springlab.controller;

import com.postgresql.springlab.model.Signature;
import com.postgresql.springlab.service.SignatureService;
import com.postgresql.springlab.service.CryptoService;
import com.postgresql.springlab.service.VersioningService;
import com.postgresql.springlab.repository.SignatureRepository;
import jakarta.transaction.Transactional;
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
    private final CryptoService cryptoService;
    private final SignatureRepository signatureRepository;
    private final VersioningService versioningService;

    public SignatureController(SignatureService signatureService, CryptoService cryptoService, SignatureRepository signatureRepository, VersioningService versioningService) {
        this.signatureService = signatureService;
        this.cryptoService = cryptoService;
        this.signatureRepository = signatureRepository;
        this.versioningService = versioningService;
    }

    // Создание сигнатуры (только для администратора)
    @PostMapping
    public ResponseEntity<Signature> createSignature(@RequestBody Signature signature, @RequestHeader("role") String role, @RequestHeader("username") String username) {
        if (role == null || !role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(null); // 403 Forbidden для не-администраторов
        }

        try {
            if (signature.getStatus() == null || signature.getStatus().isEmpty()) {
                signature.setStatus("ACTIVE");
            }
            Signature createdSignature = signatureService.createSignature(signature);
            versioningService.saveVersion(createdSignature, "CREATED", username, "{}");  // Логируем действие с пустыми изменениями для новой записи
            return ResponseEntity.ok(createdSignature);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Получение сигнатуры по ID (для всех пользователей)
    @GetMapping("/{id}")
    public ResponseEntity<Signature> getSignature(@PathVariable UUID id,
                                                  @RequestHeader("username") String username,
                                                  @RequestHeader("role") String role) {

        if (role == null || username == null) {
            return ResponseEntity.badRequest().body(null);  // Если не переданы username или role
        }

        Optional<Signature> optionalSignature = signatureService.getSignature(id);
        if (optionalSignature.isEmpty()) {
            return ResponseEntity.notFound().build();  // Если сигнатуры нет, возвращаем 404
        }

        // Логика проверки подписи
        try {
            String data = optionalSignature.get().getThreatName() + optionalSignature.get().getRemainderHash();
            boolean isValid = cryptoService.verifySignature(data, optionalSignature.get().getDigitalSignature());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(optionalSignature.get());  // Возвращаем саму сигнатуру с её данными
    }

    // Получение всех сигнатур (для всех пользователей, но с разными правами)
    @Transactional
    @GetMapping
    public ResponseEntity<List<Signature>> getSignatures(@RequestParam(required = false) LocalDateTime since,
                                                         @RequestParam(required = false) String status,
                                                         @RequestHeader("role") String role) {

        if (role == null) {
            return ResponseEntity.badRequest().build();  // Если не передана роль
        }

        List<Signature> result;

        // Если роль администратора — можно просматривать все записи
        if (role.equals("ADMIN")) {
            if (since != null && status != null) {
                result = signatureRepository.findByUpdatedAtAfterAndStatusIgnoreCase(since, status);
            } else if (since != null) {
                result = signatureRepository.findByUpdatedAtAfterAndActiveStatus(since);
            } else if (status != null) {
                result = signatureRepository.findByStatusIgnoreCase(status);
            } else {
                result = signatureRepository.findAllActive();
            }
        } else {
            // Для остальных ролей доступны только активные записи
            result = signatureRepository.findAllActive();
        }

        return ResponseEntity.ok(result);
    }

    // Обновление сигнатуры (только для администратора)
    @PutMapping("/{id}")
    public ResponseEntity<Signature> updateSignature(@PathVariable("id") UUID id,
                                                     @RequestBody Signature updatedSignature,
                                                     @RequestHeader("role") String role,
                                                     @RequestHeader("username") String username) {

        if (role == null || !role.equals("ADMIN")) {
            return ResponseEntity.status(403).body(null); // 403 Forbidden для не-администраторов
        }

        Signature existingSignature = signatureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Signature not found"));

        // Создаём строку JSON с изменёнными полями
        StringBuilder fieldsChanged = new StringBuilder("{");

        if (!existingSignature.getThreatName().equals(updatedSignature.getThreatName())) {
            fieldsChanged.append("\"threatName\": \"").append(updatedSignature.getThreatName()).append("\", ");
            existingSignature.setThreatName(updatedSignature.getThreatName());
        }
        if (!existingSignature.getFirstBytes().equals(updatedSignature.getFirstBytes())) {
            fieldsChanged.append("\"firstBytes\": \"").append(updatedSignature.getFirstBytes()).append("\", ");
            existingSignature.setFirstBytes(updatedSignature.getFirstBytes());
        }
        if (existingSignature.getRemainderLength() != updatedSignature.getRemainderLength()) {
            fieldsChanged.append("\"remainderLength\": ").append(updatedSignature.getRemainderLength()).append(", ");
            existingSignature.setRemainderLength(updatedSignature.getRemainderLength());
        }
        if (!existingSignature.getFileType().equals(updatedSignature.getFileType())) {
            fieldsChanged.append("\"fileType\": \"").append(updatedSignature.getFileType()).append("\", ");
            existingSignature.setFileType(updatedSignature.getFileType());
        }
        if (existingSignature.getOffsetStart() != updatedSignature.getOffsetStart()) {
            fieldsChanged.append("\"offsetStart\": ").append(updatedSignature.getOffsetStart()).append(", ");
            existingSignature.setOffsetStart(updatedSignature.getOffsetStart());
        }
        if (existingSignature.getOffsetEnd() != updatedSignature.getOffsetEnd()) {
            fieldsChanged.append("\"offsetEnd\": ").append(updatedSignature.getOffsetEnd()).append(", ");
            existingSignature.setOffsetEnd(updatedSignature.getOffsetEnd());
        }

        // Убираем последнюю запятую и пробел, если есть изменения
        if (fieldsChanged.length() > 1) {
            fieldsChanged.setLength(fieldsChanged.length() - 2); // Убираем ", "
        }
        fieldsChanged.append("}");

        // Проверяем, были ли изменения
        if (fieldsChanged.length() == 2) { // "{}" означает, что изменений нет
            return ResponseEntity.ok(existingSignature);
        }

        // Обновляем данные в репозитории
        signatureRepository.save(existingSignature);

        // Вызываем сервис для сохранения истории и аудита
        versioningService.saveVersion(existingSignature, "UPDATED", username, fieldsChanged.toString());

        return ResponseEntity.ok(existingSignature);
    }

    // Удаление сигнатуры (только для администраторов)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSignature(@PathVariable UUID id,
                                                  @RequestHeader("role") String role,
                                                  @RequestHeader("username") String username) {

        if (role == null || !role.equals("ADMIN")) {
            return ResponseEntity.status(403).body("Forbidden: Only admins can delete signatures.");
        }

        Optional<Signature> signatureOptional = signatureService.findById(id);

        if (!signatureOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Signature signature = signatureOptional.get();
        signature.setStatus("DELETED");
        signatureService.save(signature);

        // Логируем удаление в аудит
        versioningService.saveVersion(signature, "DELETED", username, "{}");  // Логируем удаление

        return ResponseEntity.ok("Signature deleted and status updated to DELETED");
    }

    @ControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler(Exception.class)
        public ResponseEntity<String> handleException(Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
