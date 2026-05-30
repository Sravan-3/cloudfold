package com.cloudfold.dto;

public class ChunkCompleteRequest {

    private String chunkHash;

    public String getChunkHash()          { return chunkHash; }
    public void setChunkHash(String hash) { this.chunkHash = hash; }
}