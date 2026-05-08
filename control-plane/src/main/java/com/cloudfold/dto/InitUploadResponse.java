package com.cloudfold.dto;

import java.util.List;
import java.util.UUID;

public class InitUploadResponse {

    private UUID uploadId;
    private List<ChunkDescriptor> chunks;

    public InitUploadResponse(UUID uploadId, List<ChunkDescriptor> chunks) {
        this.uploadId = uploadId;
        this.chunks = chunks;
    }

    public UUID getUploadId() {
        return uploadId;
    }
    public List<ChunkDescriptor> getChunks() {
        return chunks;
    }
}