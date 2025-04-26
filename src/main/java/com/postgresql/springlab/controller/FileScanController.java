package com.postgresql.springlab.controller;

import com.postgresql.springlab.model.SignatureScanResult;
import com.postgresql.springlab.service.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileScanController {

    private final ScanService scanService;

    @PostMapping("/upload")
    public ResponseEntity<List<SignatureScanResult>> uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        List<SignatureScanResult> result = scanService.scanFile(file);
        return ResponseEntity.ok(result);
    }
}

