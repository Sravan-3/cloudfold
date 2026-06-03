package com.cloudfold.client;

import com.cloudfold.exception.StorageNodeUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class StorageNodeClient {

    private static final Logger log =
            LoggerFactory.getLogger(StorageNodeClient.class);

    private final RestTemplate restTemplate;

    @Value("${app.storage.node.url:http://localhost:9001}")
    private String storageNodeUrl;

    public StorageNodeClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean chunkExists(String chunkHash) {
        String url = storageNodeUrl + "/chunks/" + chunkHash;

        try {

            ResponseEntity<Void> response = restTemplate.headForHeaders(url)
                    .toSingleValueMap()
                    .isEmpty()
                    ? ResponseEntity.notFound().build()
                    : ResponseEntity.ok().build();

            return response.getStatusCode() == HttpStatus.OK;

        } catch (ResourceAccessException e) {
            log.error("Storage node unreachable at {}: {}", url, e.getMessage());
            return false;

        } catch (Exception e) {
            log.error("Unexpected error checking chunk {} on storage node: {}",
                    chunkHash, e.getMessage());
            return false;
        }
    }

    public boolean verifyChunk(String chunkHash) {

        String url = storageNodeUrl + "/chunks/" + chunkHash;
        log.info("Verifying chunk at URL: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "*/*");
            var entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            log.info("Response status: {}", response.getStatusCode());
            return response.getStatusCode() == HttpStatus.OK;

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            log.warn("Chunk {} not found — got 404", chunkHash.substring(0, 8));
            return false;

        } catch (ResourceAccessException e) {
            log.error("Storage node unreachable: {}", e.getMessage());
            throw new StorageNodeUnavailableException(
                    "Storage node unreachable: " + e.getMessage(), e
            );

        } catch (Exception e) {
            log.error("Unexpected exception {}: {}", e.getClass().getName(), e.getMessage());
            throw new StorageNodeUnavailableException(
                    "Unexpected error: " + e.getMessage(), e
            );
        }
    }

    public boolean replicateChunk(String chunkHash, String sourceNodeUrl,
                                  String targetNodeUrl) {
        log.info("Replicating chunk {} from {} to {}",
                chunkHash.substring(0, 8), sourceNodeUrl, targetNodeUrl);

        try {
            byte[] chunkBytes = restTemplate.getForObject(
                    sourceNodeUrl + "/chunks/" + chunkHash,
                    byte[].class
            );

            if (chunkBytes == null || chunkBytes.length == 0) {
                log.error("Got empty response fetching chunk {} from source",
                        chunkHash.substring(0, 8));
                return false;
            }

            restTemplate.postForObject(
                    targetNodeUrl + "/chunks/" + chunkHash,
                    chunkBytes,
                    String.class
            );

            log.info("Successfully replicated chunk {} ({} bytes)",
                    chunkHash.substring(0, 8), chunkBytes.length);
            return true;

        } catch (ResourceAccessException e) {
            log.error("Node unreachable during replication of chunk {}: {}",
                    chunkHash.substring(0, 8), e.getMessage());
            return false;

        } catch (Exception e) {
            log.error("Failed to replicate chunk {}: {}",
                    chunkHash.substring(0, 8), e.getMessage());
            return false;
        }
    }
}