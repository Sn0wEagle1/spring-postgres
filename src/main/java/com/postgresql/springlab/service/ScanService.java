package com.postgresql.springlab.service;

import com.postgresql.springlab.model.Signature;
import com.postgresql.springlab.model.SignatureScanResult;
import com.postgresql.springlab.repository.SignatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.Base64;

@Service
public class ScanService {

    private final SignatureRepository signatureRepository;
    private static final Logger logger = LoggerFactory.getLogger(ScanService.class);

    private static final int WINDOW_SIZE = 8;
    private static final int BASE = 256;
    private static final long MOD = 1_000_000_007;
    private static final long MAX_FILE_SIZE = 1_000_000_000; // 1GB

    @Value("${scanner.chunk-size:8192}")
    private int chunkSize;

    public ScanService(SignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    public List<SignatureScanResult> scanFile(MultipartFile file) throws Exception {
        // Валидация входного файла
        if (file == null || file.isEmpty()) {
            logger.error("Uploaded file is empty or null");
            throw new IllegalArgumentException("Uploaded file is empty or null");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            logger.error("File size exceeds maximum limit of {} bytes", MAX_FILE_SIZE);
            throw new IllegalArgumentException("File size exceeds maximum limit");
        }

        logger.info("Starting scan for file: {}", file.getOriginalFilename());

        // Создание временного файла
        File tempFile = File.createTempFile("upload_", ".tmp");
        try {
            file.transferTo(tempFile);
            logger.info("Temporary file created: {}", tempFile.getAbsolutePath());
        } catch (IOException e) {
            tempFile.delete();
            logger.error("Error while creating temporary file", e);
            throw new IOException("Failed to create temporary file", e);
        }

        // Загрузка активных сигнатур
        List<Signature> signatures = signatureRepository.findAll().stream()
                .filter(s -> "ACTUAL".equalsIgnoreCase(s.getStatus()))
                .toList();
        logger.info("Loaded {} active signatures from the database", signatures.size());

        // Создание хэш-таблицы для быстрого поиска сигнатур по хэшу firstBytes
        Map<Long, List<Signature>> hashMap = new HashMap<>();
        for (Signature sig : signatures) {
            if (sig.getFirstBytes() == null || sig.getFirstBytes().length != WINDOW_SIZE) {
                logger.warn("Invalid signature {}: firstBytes is null or incorrect length", sig.getThreatName());
                continue;
            }
            long hash = hashBytes(sig.getFirstBytes());
            hashMap.computeIfAbsent(hash, k -> new ArrayList<>()).add(sig);
            logger.debug("Signature {} firstBytes (hex): {}, hash: {}",
                    sig.getThreatName(), toHex(sig.getFirstBytes()), hash);
        }

        List<SignatureScanResult> results = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
            byte[] fileContent = new byte[(int) raf.length()];
            raf.readFully(fileContent);
            logger.debug("File content (hex): {}", toHex(fileContent));
            raf.seek(0);

            Deque<Byte> window = new ArrayDeque<>();
            long rollingHash = 0;
            long power = 1;
            for (int i = 0; i < WINDOW_SIZE - 1; i++) {
                power = (power * BASE) % MOD;
            }

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            long position = -1;

            while ((bytesRead = raf.read(buffer)) != -1) {
                logger.debug("Reading {} bytes from file at position {}", bytesRead, position + 1);
                for (int i = 0; i < bytesRead; i++) {
                    byte b = buffer[i];
                    position++;
                    logger.debug("Processing byte at position {}: {} (hex: {:02x})", position, (char)b, b);
                    window.addLast(b);

                    if (window.size() == WINDOW_SIZE) {
                        // Формируем окно
                        byte[] windowBytes = new byte[WINDOW_SIZE];
                        int j = 0;
                        for (byte wb : window) windowBytes[j++] = wb;
                        rollingHash = hashBytes(windowBytes);

                        logger.debug("Window bytes at position {} (hex): {}, rolling hash: {}",
                                position - WINDOW_SIZE + 1, toHex(windowBytes), rollingHash);

                        List<Signature> potential = hashMap.get(rollingHash);
                        if (potential != null) {
                            for (Signature sig : potential) {
                                logger.debug("Matching signature: {}", sig.getThreatName());
                                if (matchSignature(raf, position, sig, windowBytes, sha256, fileContent)) {
                                    long start = position - WINDOW_SIZE + 1;
                                    logger.info("Match found for signature: {} at position {}", sig.getThreatName(), start);
                                    results.add(new SignatureScanResult(
                                            sig.getId(),
                                            sig.getThreatName(),
                                            start,
                                            start + WINDOW_SIZE + sig.getRemainderLength() - 1,
                                            true
                                    ));
                                }
                            }
                        }

                        // Удаляем старый байт и обновляем хэш
                        byte removed = window.removeFirst();
                        rollingHash = (rollingHash - (power * (removed & 0xFF)) % MOD + MOD) % MOD;
                        rollingHash = (rollingHash * BASE + (b & 0xFF)) % MOD;
                    }
                }
            }
        } finally {
            if (tempFile.delete()) {
                logger.info("Temporary file deleted");
            } else {
                logger.warn("Could not delete temporary file: {}", tempFile.getAbsolutePath());
            }
        }

        logger.info("Scan finished with {} matches found", results.size());
        return results;
    }

    private boolean matchSignature(RandomAccessFile raf, long position, Signature sig, byte[] windowBytes, MessageDigest sha256, byte[] fileContent) throws Exception {
        logger.debug("Matching signature at position {}: threatName={}", position, sig.getThreatName());
        logger.debug("Signature firstBytes (Base64): {}", Base64.getEncoder().encodeToString(sig.getFirstBytes()));
        logger.debug("Signature firstBytes (hex): {}", toHex(sig.getFirstBytes()));
        logger.debug("Signature remainderBytes (Base64): {}", Base64.getEncoder().encodeToString(sig.getRemainderBytes()));
        logger.debug("Signature remainderBytes (hex): {}", toHex(sig.getRemainderBytes()));
        logger.debug("Signature remainderBytes (text): {}", new String(sig.getRemainderBytes(), StandardCharsets.US_ASCII));
        logger.debug("Signature remainderLength: {}", sig.getRemainderLength());
        logger.debug("Signature remainderHash (Base64): {}", sig.getRemainderHash());

        // Проверка первых 8 байт
        if (!Arrays.equals(sig.getFirstBytes(), windowBytes)) {
            logger.debug("First bytes do not match for signature: {}", sig.getThreatName());
            return false;
        }

        // Вычисление позиций
        long start = position - WINDOW_SIZE + 1; // Начало firstBytes
        long remainderStart = start + WINDOW_SIZE; // Начало remainderBytes
        long fileLength = fileContent.length;
        logger.debug("position: {}, WINDOW_SIZE: {}, start: {}, remainderStart: {}, remainderLength: {}, fileLength: {}",
                position, WINDOW_SIZE, start, remainderStart, sig.getRemainderLength(), fileLength);

        if (remainderStart < 0 || remainderStart >= fileLength) {
            logger.warn("Invalid remainderStart {} for signature {}", remainderStart, sig.getThreatName());
            return false;
        }
        if (sig.getRemainderLength() <= 0 || remainderStart + sig.getRemainderLength() > fileLength) {
            logger.warn("Not enough bytes in file for signature {} (expected {} bytes from position {}, but file length is {})",
                    sig.getThreatName(), sig.getRemainderLength(), remainderStart, fileLength);
            return false;
        }

        // Чтение remainderBytes из fileContent
        byte[] remainder = new byte[sig.getRemainderLength()];
        try {
            System.arraycopy(fileContent, (int) remainderStart, remainder, 0, sig.getRemainderLength());
        } catch (Exception e) {
            logger.error("Error copying remainder bytes at position {} for signature {}: {}",
                    remainderStart, sig.getThreatName(), e.getMessage());
            return false;
        }
        logger.debug("remainderStart: {}", remainderStart);
        logger.debug("File content at remainderStart {} (hex): {}", remainderStart,
                toHex(Arrays.copyOfRange(fileContent, (int) remainderStart, (int) Math.min(fileLength, remainderStart + sig.getRemainderLength()))));
        logger.debug("Read remainder bytes (hex): {}", toHex(remainder));
        logger.debug("Read remainder bytes (text): {}", new String(remainder, StandardCharsets.US_ASCII));

        // Вычисление SHA-256 хэша
        sha256.reset();
        byte[] hashBytes = sha256.digest(remainder);
        String calculatedHash = toHex(hashBytes);
        logger.debug("Calculated SHA-256 hash: {}", calculatedHash);

        // Конвертация remainderHash из Base64 в hex
        byte[] expectedHashBytes;
        try {
            expectedHashBytes = Base64.getDecoder().decode(sig.getRemainderHash());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid Base64 remainderHash for signature {}: {}", sig.getThreatName(), sig.getRemainderHash());
            return false;
        }
        String expectedHash = toHex(expectedHashBytes);
        logger.debug("Expected SHA-256 hash: {}", expectedHash);

        // Проверка хэша remainderBytes сигнатуры
        sha256.reset();
        byte[] sigRemainderHashBytes = sha256.digest(sig.getRemainderBytes());
        String sigRemainderHash = toHex(sigRemainderHashBytes);
        logger.debug("SHA-256 hash of signature remainderBytes: {}", sigRemainderHash);

        if (!calculatedHash.equalsIgnoreCase(expectedHash)) {
            logger.debug("Remainder hash mismatch for signature: {}, expected: {}, calculated: {}",
                    sig.getThreatName(), expectedHash, calculatedHash);
            return false;
        }

        // Проверка смещений
        if (sig.getOffsetStart() != null && start < sig.getOffsetStart()) {
            logger.debug("Start offset is less than the minimum offset for signature: {}", sig.getThreatName());
            return false;
        }
        long signatureEnd = start + WINDOW_SIZE + sig.getRemainderLength() - 1;
        if (sig.getOffsetEnd() != null && signatureEnd > sig.getOffsetEnd()) {
            logger.debug("Signature end offset exceeds maximum offset for signature: {}", sig.getThreatName());
            return false;
        }

        logger.debug("Signature matched successfully: {}", sig.getThreatName());
        return true;
    }

    private long hashBytes(byte[] bytes) {
        long hash = 0;
        for (byte b : bytes) {
            hash = (hash * BASE + (b & 0xFF)) % MOD;
        }
        return hash;
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }
}