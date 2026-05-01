package com.cloudfold.service;

import com.cloudfold.dto.InitUploadRequest;
import com.cloudfold.dto.InitUploadResponse;
import com.cloudfold.model.FileMetadata;
import com.cloudfold.model.FileStatus;
import com.cloudfold.model.Upload;
import com.cloudfold.model.UploadStatus;
import com.cloudfold.repository.FileMetadataRepository;
import com.cloudfold.repository.UploadRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    private final FileMetadataRepository repo;

    private final UploadRepository uploadRepo;

    private static final long CHUNK_SIZE = 1024 * 1024; // 1MB

    public FileService(FileMetadataRepository repo, UploadRepository uploadRepo) {
        this.repo = repo;
        this.uploadRepo = uploadRepo;
    }

    public InitUploadResponse intiUpload(InitUploadRequest request){


        /* This functions checks if the filename exits */
        if(request.getFilename() == null || request.getFilename().isBlank()) {
            throw new RuntimeException("Filename is required");
        }

        /* This functions checks if the file size is null or -ve */
        if (request.getSize() == null || request.getSize() <= 0) {
            throw new RuntimeException("Invalid file size");
        }

        /* This functions checks if the file exceeds 5GB */
        if (request.getSize() > 5L * 1024 * 1024 * 1024) {
            throw new RuntimeException("File too large");
        }

        FileMetadata file = new FileMetadata();
        file.setFilename(request.getFilename());
        file.setSize(request.getSize());
        file.setStatus(FileStatus.PENDING);

        file = repo.save(file);

        /*
            Below function Creates Chunk Plan
            If file size = 3MB, chunk size = 1MB
            Generated chunks: [0, 1MB, 2MB]
            Each value represents: Start uploading from this byte offset
         */

        List<Long> chunks = new ArrayList<>();

        long size = request.getSize();
        long offset = 0;

        while (offset < size) {
            chunks.add(offset);
            offset += CHUNK_SIZE;
        }

        /*
            Calculate the total number of chucks
        */

        int totalChunks = (int) Math.ceil((double) request.getSize() / CHUNK_SIZE);

        Upload upload = new Upload();
        upload.setFileId(file.getId());
        upload.setStatus(UploadStatus.PENDING);
        upload.setTotalChunks(totalChunks);
        upload.setCompletedChunks(0);

        upload = uploadRepo.save(upload);

        return new InitUploadResponse(upload.getUploadId(), chunks);
    }

}
