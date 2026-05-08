package com.cloudfold.service;

import com.cloudfold.dto.ChunkDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChunkingServiceTest {

    private ChunkingService chunkingService;

    @BeforeEach
    void setUp() {
        chunkingService = new ChunkingService();
        ReflectionTestUtils.setField(chunkingService, "chunkSize", 5_242_880L);
    }

    @Test
    void twelveMbFileProducesThreeChunks() {
        List<ChunkDescriptor> chunks = chunkingService.plan(12_000_000L);

        assertEquals(3, chunks.size());

        assertEquals(0,         chunks.get(0).getOffset());
        assertEquals(5_242_880, chunks.get(0).getSize());
        assertEquals(0,         chunks.get(0).getChunkNumber());

        assertEquals(5_242_880, chunks.get(1).getOffset());
        assertEquals(5_242_880, chunks.get(1).getSize());

        assertEquals(10_485_760, chunks.get(2).getOffset());
        assertEquals(1_514_240,  chunks.get(2).getSize());
    }

    @Test
    void exactlyOnChunkBoundaryProducesNoRemainder() {
        List<ChunkDescriptor> chunks = chunkingService.plan(10_485_760L);

        assertEquals(2, chunks.size());
        assertEquals(5_242_880, chunks.get(0).getSize());
        assertEquals(5_242_880, chunks.get(1).getSize());
    }

    @Test
    void fileSmallerThanChunkSizeProducesOneChunk() {
        List<ChunkDescriptor> chunks = chunkingService.plan(1_000_000L);

        assertEquals(1, chunks.size());
        assertEquals(0,         chunks.get(0).getOffset());
        assertEquals(1_000_000, chunks.get(0).getSize());
    }

    @Test
    void chunkNumbersAreSequential() {
        List<ChunkDescriptor> chunks = chunkingService.plan(12_000_000L);

        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).getChunkNumber());
        }
    }

    @Test
    void sha256IsDeterministic() {
        byte[] data = "hello cloudfold".getBytes();

        String hash1 = chunkingService.sha256(data);
        String hash2 = chunkingService.sha256(data);

        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length());
    }

    @Test
    void sha256ChangesOnOneByteChange() {
        byte[] data1 = "hello".getBytes();
        byte[] data2 = "hellp".getBytes();

        assertNotEquals(chunkingService.sha256(data1), chunkingService.sha256(data2));
    }

    @Test
    void zeroOrNegativeSizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> chunkingService.plan(0));
        assertThrows(IllegalArgumentException.class, () -> chunkingService.plan(-1));
    }
}