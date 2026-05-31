package com.cloudfold.service;

import com.cloudfold.model.ReplicationStatus;
import com.cloudfold.repository.ReplicationStatusRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReplicationService {

    private final ReplicationStatusRepository replicationRepo;


    // Need to add real HTTP client — for now we simulate it
    public ReplicationService(ReplicationStatusRepository replicationRepo) {
        this.replicationRepo = replicationRepo;
    }

    // Records that a chunk exists on a node
    // Called when a chunk upload completes successfully
    @Transactional
    public void recordChunkOnNode(String chunkHash, String nodeId) {
        // Idempotent — if record exists, update it; if not, create it
        ReplicationStatus rs = replicationRepo
            .findByChunkHashAndNodeId(chunkHash, nodeId)
            .orElse(new ReplicationStatus());

        rs.setChunkHash(chunkHash);
        rs.setNodeId(nodeId);
        rs.setStatus("HEALTHY");
        replicationRepo.save(rs);
    }

    // @Async — this runs on a background thread from Spring's thread pool
    // The caller (ReplicationJob) gets control back immediately
    // The actual replication work happens concurrently
    @Async
    @Transactional
    public void replicateChunk(String chunkHash, String sourceNode) {
        List<ReplicationStatus> existing =
            replicationRepo.findByChunkHash(chunkHash);

        // Find a target node that doesn't already have this chunk
        //  this uses ConsistentHashRing + health-checked nodes
        // For now, use a hardcoded second node
        String targetNode = existing.stream()
            .map(ReplicationStatus::getNodeId)
            .noneMatch("node-2"::equals) ? "node-2" : "node-3";

        System.out.println(
            "Replicating chunk " + chunkHash.substring(0, 8) +
            "... from " + sourceNode + " to " + targetNode
        );

        // TODO: actual HTTP call to copy chunk between nodes
        // For now simulate success and record it
        recordChunkOnNode(chunkHash, targetNode);

        System.out.println(
            "Replication complete for chunk " + chunkHash.substring(0, 8)
        );
    }
}
