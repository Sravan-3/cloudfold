package com.cloudfold.dto;

import java.util.List;
import java.util.UUID;

public class InitUploadResponse {

    private UUID uploadId;
    private List<Long> chunks;

    public InitUploadResponse(UUID uploadId, List<Long> chunks) {
        this.uploadId = uploadId;
        this.chunks = chunks;
    }

    public UUID getUploadId() {
        return uploadId;
    }

    public List<Long> getChunks() {
        return chunks;
    }
}