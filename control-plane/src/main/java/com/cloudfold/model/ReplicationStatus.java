package com.cloudfold.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "replication_status")
public class ReplicationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String chunkHash;

    @Column(nullable = false)
    private String nodeId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private OffsetDateTime lastChecked;

    @PrePersist
    @PreUpdate
    public void touch() {
        lastChecked = OffsetDateTime.now();
    }

    public UUID getId()                        { return id; }
    public String getChunkHash()               { return chunkHash; }
    public void setChunkHash(String h)         { this.chunkHash = h; }
    public String getNodeId()                  { return nodeId; }
    public void setNodeId(String n)            { this.nodeId = n; }
    public String getStatus()                  { return status; }
    public void setStatus(String s)            { this.status = s; }
    public OffsetDateTime getLastChecked()     { return lastChecked; }
}
