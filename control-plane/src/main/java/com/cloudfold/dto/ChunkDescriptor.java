package com.cloudfold.dto;

public class ChunkDescriptor {

    private final int chunkNumber;
    private final long offset;
    private final long size;

    // Hash is empty at plan time — filled when chunk data actually arrives
    private String hash;

    public ChunkDescriptor(int chunkNumber, long offset, long size) {
        this.chunkNumber = chunkNumber;
        this.offset = offset;
        this.size = size;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public long getOffset() {
        return offset;
    }

    public long getSize() {
        return size;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

}
