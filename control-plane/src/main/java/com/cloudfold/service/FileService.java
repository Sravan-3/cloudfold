package com.cloudfold.service;

import com.cloudfold.dto.ChunkDescriptor;
import com.cloudfold.dto.InitUploadRequest;
import com.cloudfold.dto.InitUploadResponse;
import com.cloudfold.model.FileMetadata;
import com.cloudfold.model.FileStatus;
import com.cloudfold.model.Upload;
import com.cloudfold.model.UploadStatus;
import com.cloudfold.repository.FileMetadataRepository;
import com.cloudfold.repository.UploadRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    private final FileMetadataRepository repo;
    private final UploadRepository uploadRepo;
    private final UploadSessionService uploadSessionService;
    private final ChunkingService chunkingService;

    @Value("${app.chunk-size-bytes:5242880}")
    private long chunkSize;

    public FileService(FileMetadataRepository repo, UploadRepository uploadRepo, UploadSessionService uploadSessionService, ChunkingService chunkingService) {
        this.repo = repo;
        this.uploadRepo = uploadRepo;
        this.uploadSessionService = uploadSessionService;
        this.chunkingService = chunkingService;
    }

    @Transactional
    public InitUploadResponse initUpload(InitUploadRequest request){

        /* This functions checks if the filename exits */
        if(request.getFilename() == null || request.getFilename().isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }

        /* This functions checks if the file size is null or -ve */
        if (request.getSize() == null || request.getSize() <= 0) {
            throw new IllegalArgumentException("Invalid file size");
        }

        /* This functions checks if the file exceeds 5GB */
        if (request.getSize() > 5L * 1024 * 1024 * 1024) {
            throw new IllegalArgumentException("File too large");
        }

        FileMetadata file = new FileMetadata();
        file.setFilename(request.getFilename());
        file.setSize(request.getSize());
        file.setStatus(FileStatus.PENDING);

        file = repo.save(file);

        List<ChunkDescriptor> chunkPlan = chunkingService.plan(request.getSize());

        Upload upload = new Upload();
        upload.setFileId(file.getId());
        upload.setStatus(UploadStatus.PENDING);
        upload.setTotalChunks(chunkPlan.size());
        upload.setCompletedChunks(0);

        upload = uploadRepo.save(upload);
        uploadSessionService.cacheUploadSession(upload);

        return new InitUploadResponse(upload.getUploadId(), chunkPlan);
    }

}
