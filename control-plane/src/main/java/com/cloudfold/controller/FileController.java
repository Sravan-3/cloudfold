package com.cloudfold.controller;

import com.cloudfold.dto.InitUploadRequest;
import com.cloudfold.dto.InitUploadResponse;
import com.cloudfold.security.HmacSigner;
import com.cloudfold.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService service;
    private final HmacSigner hmacSigner;

    @Value("${app.storage.node.url}")
    private String storageNodeUrl;

    public FileController(FileService service, HmacSigner hmacSigner){
        this.service = service;
        this.hmacSigner = hmacSigner;
    }

    @GetMapping("/upload-url")
    public Map<String, String> getUploadUrl(
            @RequestParam String chunkHash) {

        if (chunkHash == null || chunkHash.length() != 64) {
            throw new IllegalArgumentException("Invalid chunk hash");
        }

        String url = hmacSigner.generateUploadUrl(chunkHash, storageNodeUrl);
        return Map.of("url", url);
    }

    @PostMapping("/init-upload")
    public InitUploadResponse initUpload(@RequestBody InitUploadRequest request){
        return service.initUpload(request);
    }
}
