package com.cloudfold.dto;

import java.util.List;
import java.util.UUID;

public class UploadCompleteResponse {

    private UUID fileId;
    private String status;
    private int totalChunks;
    private List<Integer> missingChunks;

    public UploadCompleteResponse(UUID fileId, String status,
                                  int totalChunks, List<Integer> missingChunks) {
        this.fileId = fileId;
        this.status = status;
        this.totalChunks = totalChunks;
        this.missingChunks = missingChunks;
    }

    public UUID getFileId()                  { return fileId; }
    public String getStatus()                { return status; }
    public int getTotalChunks()              { return totalChunks; }
    public List<Integer> getMissingChunks()  { return missingChunks; }
}