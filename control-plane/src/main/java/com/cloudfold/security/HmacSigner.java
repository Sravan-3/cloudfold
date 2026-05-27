// src/main/java/com/cloudfold/security/HmacSigner.java

package com.cloudfold.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Component
public class HmacSigner {

    @Value("${app.presign.secret}")
    private String secret;

    // Presigned URL is valid (5 minutes = 300 seconds)
    @Value("${app.presign.ttl-seconds:300}")
    private long ttlSeconds;

    public String generateUploadUrl(String chunkHash, String storageNodeBaseUrl) {

        long expires = System.currentTimeMillis() / 1000 + ttlSeconds;


        String dataToSign = chunkHash + ":" + expires;
        String signature = sign(dataToSign);

        return storageNodeBaseUrl
                + "/chunks/" + chunkHash
                + "?expires=" + expires
                + "&sig=" + signature;
    }

    public boolean verify(String chunkHash, long expires, String sig) {

        long nowSeconds = System.currentTimeMillis() / 1000;
        if (nowSeconds > expires) {
            return false;
        }

        String dataToSign = chunkHash + ":" + expires;
        String expectedSig = sign(dataToSign);

        return constantTimeEquals(expectedSig, sig);
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);

        } catch (Exception e) {
            throw new IllegalStateException("HMAC signing failed", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {

        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }

        return result == 0;
    }
}