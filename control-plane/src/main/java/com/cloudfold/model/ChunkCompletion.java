// src/main/java/com/cloudfold/model/ChunkCompletion.java

package com.cloudfold.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chunk_completions")
public class ChunkCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID uploadId;

    @Column(nullable = false)
    private int chunkNumber;

    @Column(nullable = false, length = 64)
    private String chunkHash;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime completedAt;

    @PrePersist
    public void prePersist() {
        if (completedAt == null) completedAt = OffsetDateTime.now();
    }

    public UUID getId()                        { return id; }
    public UUID getUploadId()                  { return uploadId; }
    public void setUploadId(UUID uploadId)     { this.uploadId = uploadId; }
    public int getChunkNumber()                { return chunkNumber; }
    public void setChunkNumber(int n)          { this.chunkNumber = n; }
    public String getChunkHash()               { return chunkHash; }
    public void setChunkHash(String h)         { this.chunkHash = h; }
    public OffsetDateTime getCompletedAt()     { return completedAt; }
}