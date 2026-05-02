package com.cloudfold.service;

import com.cloudfold.model.Upload;
import com.cloudfold.model.UploadStatus;
import com.cloudfold.repository.UploadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UploadSessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UploadRepository uploadRepository;

    @Value("${app.upload.session.ttl-seconds:86400}")
    private long sessionTtlSeconds;

    public UploadSessionService(RedisTemplate<String, Object> redisTemplate, UploadRepository uploadRepository){
        this.redisTemplate = redisTemplate;
        this.uploadRepository = uploadRepository;
    }

    public void cacheUploadSession(Upload upload){

        String key = "upload:" + upload.getUploadId();

        Map<String, Object> sessionData = Map.of(
                "fileId", upload.getFileId().toString(),
                "status",  upload.getStatus().name(),
                "totalChunks", String.valueOf(upload.getTotalChunks()),
                "completedChunks", String.valueOf(upload.getCompletedChunks())
        );

        redisTemplate.opsForHash().putAll(key, sessionData);
        redisTemplate.expire(key, sessionTtlSeconds, TimeUnit.SECONDS);
    }

    public Upload getUploadSession(UUID uploadId){

        String key = "upload:"+ uploadId;

        Map<Object, Object> cached = redisTemplate.opsForHash().entries(key);

        if(!cached.isEmpty()){

            // Cache HIT

            Upload upload = new Upload();
            upload.setUploadId(uploadId);
            upload.setFileId(UUID.fromString((String) cached.get("fileId")));
            upload.setStatus(UploadStatus.valueOf((String) cached.get("status")));
            upload.setTotalChunks(Integer.parseInt((String) cached.get("totalChunks")));
            upload.setCompletedChunks(Integer.parseInt((String) cached.get("completedChunks")));

            return upload;
        }

        // Cache MISS
        Upload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found: " + uploadId));

        cacheUploadSession(upload);

        return upload;
    }
}
