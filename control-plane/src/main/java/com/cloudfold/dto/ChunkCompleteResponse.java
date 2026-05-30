// src/main/java/com/cloudfold/dto/ChunkCompleteResponse.java

package com.cloudfold.dto;

public class ChunkCompleteResponse {

    private boolean cached;
    private int completedChunks;
    private int totalChunks;

    public ChunkCompleteResponse(boolean cached, int completedChunks, int totalChunks) {
        this.cached = cached;
        this.completedChunks = completedChunks;
        this.totalChunks = totalChunks;
    }

    public boolean isCached()          { return cached; }
    public int getCompletedChunks()    { return completedChunks; }
    public int getTotalChunks()        { return totalChunks; }
}