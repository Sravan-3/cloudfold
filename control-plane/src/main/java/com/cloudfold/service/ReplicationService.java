package com.cloudfold.service;

import com.cloudfold.client.StorageNodeClient;
import com.cloudfold.model.ReplicationStatus;
import com.cloudfold.repository.ReplicationStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReplicationService {

    @Value("${app.storage.node.url:http://localhost:9001}")
    private String primaryNodeUrl;

    private final ReplicationStatusRepository replicationRepo;
    private final StorageNodeClient storageNodeClient;
    private static final Logger log =
            LoggerFactory.getLogger(ReplicationService.class);

    public ReplicationService(ReplicationStatusRepository replicationRepo,
                              StorageNodeClient storageNodeClient) {
        this.replicationRepo = replicationRepo;
        this.storageNodeClient = storageNodeClient;
    }

    @Transactional
    public void recordChunkOnNode(String chunkHash, String nodeId) {
        ReplicationStatus rs = replicationRepo
            .findByChunkHashAndNodeId(chunkHash, nodeId)
            .orElse(new ReplicationStatus());

        rs.setChunkHash(chunkHash);
        rs.setNodeId(nodeId);
        rs.setStatus("HEALTHY");
        replicationRepo.save(rs);
    }

    @Async
    @Transactional
    public void replicateChunk(String chunkHash, String sourceNode) {
        List<ReplicationStatus> existing =
            replicationRepo.findByChunkHash(chunkHash);

        String targetNodeUrl = "http://localhost:9002"; // second node in Day 27
        String targetNodeId  = "node-2";

        boolean success = storageNodeClient.replicateChunk(
                chunkHash, primaryNodeUrl, targetNodeUrl
        );

        if (success) {
            recordChunkOnNode(chunkHash, targetNodeId);
            log.info("Chunk {} replicated to {}", chunkHash.substring(0, 8), targetNodeId);
        } else {
            log.error("Failed to replicate chunk {} to {}",
                    chunkHash.substring(0, 8), targetNodeId);
        }
    }
}
