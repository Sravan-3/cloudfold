package com.cloudfold.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConsistentHashRingTest {

    private ConsistentHashRing ring;

    @BeforeEach
    void setUp() {
        ring = new ConsistentHashRing();
    }

    @Test
    void singleNodeGetsAllChunks() {
        ring.addNode("node-A");

        assertEquals("node-A", ring.getNode("any-chunk-hash"));
        assertEquals("node-A", ring.getNode("another-chunk"));
        assertEquals("node-A", ring.getNode("yet-another"));
    }

    @Test
    void assignmentIsConsistent() {
        ring.addNode("node-A");
        ring.addNode("node-B");
        ring.addNode("node-C");

        String firstResult = ring.getNode("chunk-sha256-abc");

        for (int i = 0; i < 100; i++) {
            assertEquals(firstResult, ring.getNode("chunk-sha256-abc"));
        }
    }

    @Test
    void distributionIsRoughlyEven() {
        ring.addNode("node-A");
        ring.addNode("node-B");
        ring.addNode("node-C");

        Map<String, Integer> counts = new HashMap<>();
        counts.put("node-A", 0);
        counts.put("node-B", 0);
        counts.put("node-C", 0);

        for (int i = 0; i < 1000; i++) {
            String node = ring.getNode("chunk-" + i);
            counts.put(node, counts.get(node) + 1);
        }

        System.out.println("Distribution: " + counts);

        for (int count : counts.values()) {
            assertTrue(count >= 250, "Node got too few chunks: " + count);
            assertTrue(count <= 450, "Node got too many chunks: " + count);
        }
    }

    @Test
    void addingNodeOnlyMovesOneThirdOfChunks() {
        ring.addNode("node-A");
        ring.addNode("node-B");
        ring.addNode("node-C");

        Map<String, String> before = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            before.put("chunk-" + i, ring.getNode("chunk-" + i));
        }

        ring.addNode("node-D");


        int moved = 0;
        for (int i = 0; i < 1000; i++) {
            String after = ring.getNode("chunk-" + i);
            if (!after.equals(before.get("chunk-" + i))) {
                moved++;
            }
        }

        System.out.println("Chunks moved after adding node-D: " + moved + "/1000");

        assertTrue(moved < 400, "Too many chunks moved: " + moved);
        assertTrue(moved > 150, "Too few chunks moved (suspiciously low): " + moved);
    }

    @Test
    void removingNodeReassignsItsChunks() {
        ring.addNode("node-A");
        ring.addNode("node-B");
        ring.addNode("node-C");


        String targetChunk = null;
        for (int i = 0; i < 1000; i++) {
            if ("node-B".equals(ring.getNode("chunk-" + i))) {
                targetChunk = "chunk-" + i;
                break;
            }
        }

        assertNotNull(targetChunk, "Couldn't find a chunk on node-B");

        ring.removeNode("node-B");


        String newNode = ring.getNode(targetChunk);
        assertNotEquals("node-B", newNode);
    }

    @Test
    void emptyRingThrows() {
        assertThrows(IllegalStateException.class, () -> ring.getNode("any-chunk"));
    }
}