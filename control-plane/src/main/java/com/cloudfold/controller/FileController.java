package com.cloudfold.controller;

import com.cloudfold.client.StorageNodeClient;
import com.cloudfold.config.HttpClientConfig;
import com.cloudfold.dto.*;
import com.cloudfold.security.HmacSigner;
import com.cloudfold.service.ChunkCompletionService;
import com.cloudfold.service.FileService;
import com.cloudfold.service.UploadCompletionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService service;
    private final HmacSigner hmacSigner;
    private final ChunkCompletionService chunkCompletionService;
    private final UploadCompletionService uploadCompletionService;
    private final StorageNodeClient storageNodeClient;

    @Value("${app.storage.node.url}")
    private String storageNodeUrl;

    public FileController(FileService service,
                          HmacSigner hmacSigner,
                          ChunkCompletionService chunkCompletionService,
                          UploadCompletionService uploadCompletionService,
                          StorageNodeClient storageNodeClient){
        this.service = service;
        this.hmacSigner = hmacSigner;
        this.chunkCompletionService = chunkCompletionService;
        this.uploadCompletionService = uploadCompletionService;
        this.storageNodeClient = storageNodeClient;
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

    @PostMapping("/{uploadId}/chunks/{chunkNumber}/complete")
    public ChunkCompleteResponse completeChunk(
            @PathVariable UUID uploadId,
            @PathVariable int chunkNumber,
            @RequestBody ChunkCompleteRequest request) {

        return chunkCompletionService.completeChunk(uploadId, chunkNumber, request);
    }

    @PostMapping("/{uploadId}/complete")
    public UploadCompleteResponse completeUpload(@PathVariable UUID uploadId) {
        return uploadCompletionService.completeUpload(uploadId);
    }

    @GetMapping("/chunks/{chunkHash}/verify")
    public Map<String, Object> verifyChunk(@PathVariable String chunkHash) {

        if (chunkHash.length() != 64) {
            throw new IllegalArgumentException("Invalid chunk hash");
        }

        boolean exists = storageNodeClient.verifyChunk(chunkHash);
        return Map.of(
                "chunkHash", chunkHash,
                "exists", exists,
                "storageNode", storageNodeUrl
        );
    }
}
