package com.cloudfold.repository;

import com.cloudfold.model.ChunkCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChunkCompletionRepository extends JpaRepository<ChunkCompletion, UUID> {

    Optional<ChunkCompletion> findByUploadIdAndChunkNumber(UUID uploadId, int chunkNumber);
    long countByUploadId(UUID uploadId);

    @Query("SELECT c.chunkNumber FROM ChunkCompletion c WHERE c.uploadId = :uploadId")
    List<Integer> findCompletedChunkNumbers(@Param("uploadId") UUID uploadId);
}