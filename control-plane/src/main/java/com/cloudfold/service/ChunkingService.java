// src/main/java/com/cloudfold/service/ChunkingService.java

package com.cloudfold.service;

import com.cloudfold.dto.ChunkDescriptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class ChunkingService {

    @Value("${app.chunk-size-bytes:5242880}")
    private long chunkSize;

    /*
           Below function Creates Chunk Plan
           If file size = 15MB, chunk size = 5MB
           Generated chunks: [0, 5MB, 10MB]
           Each value represents: Start uploading from this byte offset

        */
    public List<ChunkDescriptor> plan(long fileSizeBytes) {

        if (fileSizeBytes <= 0) {
            throw new IllegalArgumentException("File size must be positive");
        }

        List<ChunkDescriptor> chunks = new ArrayList<>();
        long offset = 0;
        int chunkNumber = 0;

        while (offset < fileSizeBytes) {
            long thisChunkSize = Math.min(chunkSize, fileSizeBytes - offset);

            chunks.add(new ChunkDescriptor(chunkNumber, offset, thisChunkSize));

            offset += thisChunkSize;
            chunkNumber++;
        }

        return chunks;
    }


    public String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(data);

            return HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}