package com.cloudfold.service;

import com.cloudfold.dto.ChunkCompleteRequest;
import com.cloudfold.dto.ChunkCompleteResponse;
import com.cloudfold.model.ChunkCompletion;
import com.cloudfold.model.Upload;
import com.cloudfold.repository.ChunkCompletionRepository;
import com.cloudfold.repository.UploadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChunkCompletionService {

    private final ChunkCompletionRepository completionRepo;
    private final UploadRepository uploadRepo;
    private final UploadSessionService uploadSessionService;
    private final ReplicationService replicationService;

    public ChunkCompletionService(ChunkCompletionRepository completionRepo,
                                  UploadRepository uploadRepo,
                                  UploadSessionService uploadSessionService,
                                  ReplicationService replicationService) {
        this.completionRepo = completionRepo;
        this.uploadRepo = uploadRepo;
        this.uploadSessionService = uploadSessionService;
        this.replicationService = replicationService;
    }

    @Transactional
    public ChunkCompleteResponse completeChunk(
            UUID uploadId,
            int chunkNumber,
            ChunkCompleteRequest request) {

        Upload upload = uploadSessionService.getUploadSession(uploadId);


        // chunkNumber is 0-indexed, so valid range is 0 to totalChunks-1
        if (chunkNumber < 0 || chunkNumber >= upload.getTotalChunks()) {
            throw new IllegalArgumentException(
                    "Chunk number " + chunkNumber +
                            " is out of range. Upload has " + upload.getTotalChunks() + " chunks."
            );
        }

        // Idempotency check — has this chunk been completed before?
        Optional<ChunkCompletion> existing =
                completionRepo.findByUploadIdAndChunkNumber(uploadId, chunkNumber);

        if (existing.isPresent()) {
            // DUPLICATE REQUEST — return same response as original, don't re-process tells the client "I already had this, no action taken"
            return new ChunkCompleteResponse(
                    true,
                    (int) completionRepo.countByUploadId(uploadId),
                    upload.getTotalChunks()
            );
        }

        /*
        This adds a record to the completion table indicating that a
        specific chunk has been completed.

        Each row is uniquely identified by the combination of
        uploadId and chunkNumber.

        We use a completion table instead of incrementing a counter
        because concurrent requests can cause race conditions.
        Counting completed chunks from this table ensures that the
        metadata remains accurate and idempotent.
        */
        ChunkCompletion completion = new ChunkCompletion();
        completion.setUploadId(uploadId);
        completion.setChunkNumber(chunkNumber);
        completion.setChunkHash(request.getChunkHash());
        completionRepo.save(completion);
        replicationService.recordChunkOnNode(request.getChunkHash(), "node-1");

        /*
        As mentioned above, we treat the database as the source of truth.

        We query the row count to determine the total number of completed
        chunk uploads and persist that value in the metadata table.
        */
        int completedSoFar = (int) completionRepo.countByUploadId(uploadId);

        upload.setCompletedChunks(completedSoFar);
        uploadRepo.save(upload);

        return new ChunkCompleteResponse(false, completedSoFar, upload.getTotalChunks());
    }
}