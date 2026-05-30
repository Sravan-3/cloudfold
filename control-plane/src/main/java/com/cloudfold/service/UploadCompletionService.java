package com.cloudfold.service;

import com.cloudfold.dto.UploadCompleteResponse;
import com.cloudfold.exception.UploadIncompleteException;
import com.cloudfold.model.FileMetadata;
import com.cloudfold.model.FileStatus;
import com.cloudfold.model.Upload;
import com.cloudfold.model.UploadStatus;
import com.cloudfold.repository.ChunkCompletionRepository;
import com.cloudfold.repository.FileMetadataRepository;
import com.cloudfold.repository.UploadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UploadCompletionService {

    private final UploadRepository uploadRepo;
    private final FileMetadataRepository fileRepo;
    private final ChunkCompletionRepository completionRepo;
    private final UploadSessionService uploadSessionService;

    public UploadCompletionService(UploadRepository uploadRepo,
                                   FileMetadataRepository fileRepo,
                                   ChunkCompletionRepository completionRepo,
                                   UploadSessionService uploadSessionService) {
        this.uploadRepo = uploadRepo;
        this.fileRepo = fileRepo;
        this.completionRepo = completionRepo;
        this.uploadSessionService = uploadSessionService;
    }

    @Transactional
    public UploadCompleteResponse completeUpload(UUID uploadId) {

        Upload upload = uploadSessionService.getUploadSession(uploadId);

        /*
        This executes a database query that returns a list of
        chunks that were uploaded successfully.
        */
        List<Integer> completedNumbers =
                completionRepo.findCompletedChunkNumbers(uploadId);


        /*
        Store the results in a HashSet to avoid O(n²) lookups,
        since List.contains() performs a linear scan.
        */
        Set<Integer> completedSet = new HashSet<>(completedNumbers);

        List<Integer> missingChunks = new ArrayList<>();

        /*
        Iterate through all chunks and identify the missing ones.
        Store the missing chunks in a separate list to return.
        */
        for (int i = 0; i < upload.getTotalChunks(); i++) {
            if (!completedSet.contains(i)) {
                missingChunks.add(i);
            }
        }

        if (!missingChunks.isEmpty()) {
            throw new UploadIncompleteException(
                    "Upload incomplete. Missing chunks: " + missingChunks,
                    missingChunks
            );
        }

        /*
        Sets the status of the upload session to "COMPLETED".
        */
        upload.setStatus(UploadStatus.COMPLETED);
        uploadRepo.save(upload);

        FileMetadata file = fileRepo.findById(upload.getFileId())
                .orElseThrow(() -> new IllegalStateException(
                        "File not found for upload: " + uploadId));

        /*
        Sets the status of the File Metadata  to "COMPLETED".
        */
        file.setStatus(FileStatus.COMPLETED);
        fileRepo.save(file);

        return new UploadCompleteResponse(
                file.getId(),
                "COMPLETED",
                upload.getTotalChunks(),
                List.of()
        );
    }
}