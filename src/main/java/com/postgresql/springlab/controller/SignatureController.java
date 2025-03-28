package com.postgresql.springlab.controller;

import com.postgresql.springlab.model.History;
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
    private final CryptoService cryptoService;  // Добавляем зависимость
    private final SignatureRepository signatureRepository;
    private final VersioningService versioningService;

    public SignatureController(SignatureService signatureService, CryptoService cryptoService, SignatureRepository signatureRepository, VersioningService versioningService) {
        this.signatureService = signatureService;
        this.cryptoService = cryptoService;
        this.signatureRepository = signatureRepository;
        this.versioningService = versioningService;
    }

    @PostMapping
    public ResponseEntity<Signature> createSignature(@RequestBody Signature signature) {
        try {
            // Если статус не задан, устанавливаем его в "ACTIVE"
            if (signature.getStatus() == null || signature.getStatus().isEmpty()) {
                signature.setStatus("ACTIVE");
            }
            return ResponseEntity.ok(signatureService.createSignature(signature));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Signature> getSignature(@PathVariable UUID id) {
        Optional<Signature> optionalSignature = signatureService.getSignature(id);  // Получаем сигнатуру по ID
        if (optionalSignature.isEmpty()) {
            return ResponseEntity.notFound().build();  // Если сигнатуры нет, возвращаем 404
        }

        Signature signature = optionalSignature.get();  // Получаем сам объект сигнатуры

        // Логика проверки подписи
        try {
            String data = signature.getThreatName() + signature.getRemainderHash();
            boolean isValid = cryptoService.verifySignature(data, signature.getDigitalSignature());  // Проверка подписи
            // Если нужно, можно логировать или что-то с этим делать, но мы не добавляем в саму сигнатуру
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();  // Ошибка при проверке подписи
        }

        return ResponseEntity.ok(signature);  // Возвращаем саму сигнатуру с её данными
    }

    @Transactional
    @GetMapping
    public List<Signature> getSignatures(@RequestParam(required = false) LocalDateTime since) {
        if (since == null) {
            // Возвращаем все активные записи
            return signatureRepository.findAllActive();
        } else {
            // Фильтруем по дате обновления и статусу не DELETED
            return signatureRepository.findByUpdatedAtAfterAndActiveStatus(since);
        }
    }


    // Обновление сигнатуры
    @PutMapping("/{id}")
    public ResponseEntity<Signature> updateSignature(@PathVariable("id") UUID id, @RequestBody Signature updatedSignature) {
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
        versioningService.saveVersion(existingSignature, "UPDATED", "admin", fieldsChanged.toString());

        return ResponseEntity.ok(existingSignature);
    }


    // Удаление сигнатуры
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSignature(@PathVariable UUID id, @RequestParam String changedBy) {
        Optional<Signature> signatureOptional = signatureService.findById(id);

        if (!signatureOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Signature signature = signatureOptional.get();

        // Обновляем статус на "DELETED"
        signature.setStatus("DELETED");

        // Сохраняем обновленную сигнатуру
        signatureService.save(signature);

        // Добавляем запись в историю и аудит
        versioningService.saveVersion(signature, "DELETE", changedBy);

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
