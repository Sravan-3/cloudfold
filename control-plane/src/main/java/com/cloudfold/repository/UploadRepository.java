package com.cloudfold.repository;

import com.cloudfold.model.Upload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UploadRepository extends JpaRepository<Upload, UUID> {

    Optional<Upload> findByUploadIdAndStatus(UUID uploadId, String status);
}