package com.cloudfold.repository;

import com.cloudfold.model.ReplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReplicationStatusRepository extends JpaRepository<ReplicationStatus, UUID> {


    List<ReplicationStatus> findByChunkHash(String chunkHash);


    Optional<ReplicationStatus> findByChunkHashAndNodeId(String chunkHash, String nodeId);

    @Query("""
        SELECT r.chunkHash
        FROM ReplicationStatus r
        WHERE r.status = 'HEALTHY'
        GROUP BY r.chunkHash
        HAVING COUNT(r.id) < 2
        """)
    List<String> findUnderReplicatedChunkHashes();
}
