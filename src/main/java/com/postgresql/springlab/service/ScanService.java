package com.postgresql.springlab.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.postgresql.springlab.model.Signature;
import com.postgresql.springlab.model.SignatureScanResult;
import com.postgresql.springlab.repository.SignatureRepository;

@Service
@RequiredArgsConstructor
public class FileScanService {

    private final SignatureRepository signatureRepository;

    private static final int FIRST_BYTES_LENGTH = 16;  // Увеличиваем длину для более точного поиска
    private static final int CHUNK_SIZE = 8192; // 8KB
    private static final long BASE = 256;
    private static final long MOD = 1_000_000_007;

    public List<SignatureScanResult> scanFile(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        File tempFile = File.createTempFile("upload-", ".tmp");
        file.transferTo(tempFile);

        Map<Long, List<Signature>> hashToSignatures = prepareSignatureMap();

        List<SignatureScanResult> results = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {
            Deque<Byte> window = new LinkedList<>();
            long rollingHash = 0;
            long basePower = 1;

            // Устанавливаем basePower для корректного вычисления хеша
            for (int i = 0; i < FIRST_BYTES_LENGTH - 1; i++) {
                basePower = (basePower * BASE) % MOD;
            }

            long filePointer = 0;
            byte[] chunk = new byte[CHUNK_SIZE];
            int bytesRead;

            while ((bytesRead = raf.read(chunk)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    byte currentByte = chunk[i];
                    window.addLast(currentByte);

                    rollingHash = (rollingHash * BASE + (currentByte & 0xFF)) % MOD;

                    if (window.size() > FIRST_BYTES_LENGTH) {
                        byte removed = window.removeFirst();
                        rollingHash = (rollingHash - ((removed & 0xFF) * basePower) % MOD + MOD) % MOD;
                    }

                    if (window.size() == FIRST_BYTES_LENGTH) {
                        long currentOffset = filePointer + i - FIRST_BYTES_LENGTH + 1;
                        List<Signature> potentialMatches = hashToSignatures.get(rollingHash);
                        if (potentialMatches != null) {
                            byte[] firstBytes = new byte[FIRST_BYTES_LENGTH];
                            int j = 0;
                            for (Byte b : window) {
                                firstBytes[j++] = b;
                            }
                            for (Signature signature : potentialMatches) {
                                if (Arrays.equals(firstBytes, signature.getFirstBytes())) {
                                    if (checkTail(raf, signature, currentOffset + FIRST_BYTES_LENGTH)) {
                                        if (isOffsetValid(currentOffset, raf.length(), signature)) {
                                            results.add(buildResult(signature, currentOffset, currentOffset + signature.getRemainderLength() + FIRST_BYTES_LENGTH - 1));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                filePointer += bytesRead;
            }
        } finally {
            tempFile.delete();
        }

        return results;
    }

    private boolean isOffsetValid(long offset, long fileLength, Signature sig) {
        return (sig.getOffsetStart() == null || offset >= sig.getOffsetStart()) &&
                (sig.getOffsetEnd() == null || offset <= sig.getOffsetEnd());
    }

    private boolean checkTail(RandomAccessFile raf, Signature sig, long start)
            throws IOException, NoSuchAlgorithmException {

        raf.seek(start);
        byte[] tailBytes = new byte[sig.getRemainderLength()];
        int read = raf.read(tailBytes);
        if (read < sig.getRemainderLength()) return false;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(tailBytes);

        // Декодируем строку обратно в байты
        byte[] expectedHash = Base64.getDecoder().decode(sig.getRemainderHash());

        return Arrays.equals(hash, expectedHash);
    }

    private SignatureScanResult buildResult(Signature sig, long from, long to) {
        return new SignatureScanResult(sig.getId(), sig.getThreatName(), from, to, true);
    }

    private Map<Long, List<Signature>> prepareSignatureMap() {
        List<Signature> signatures = signatureRepository.findAll();
        Map<Long, List<Signature>> map = new HashMap<>();

        for (Signature sig : signatures) {
            System.out.println("Loaded signature: " + sig.getThreatName() + ", firstBytes: " + Arrays.toString(sig.getFirstBytes()));
            long hash = 0;
            for (byte b : sig.getFirstBytes()) {
                hash = (hash * BASE + (b & 0xFF)) % MOD;
            }
            map.computeIfAbsent(hash, k -> new ArrayList<>()).add(sig);
        }
        return map;
    }

}
