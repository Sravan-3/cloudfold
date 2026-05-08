package com.cloudfold.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashRing {

    /*
    * We use TreeMap as all the keys are in sorted order finding any element takes O(logn).
    * Why not use HashMap ? While reaching a Node on a ring we usally need a ceilingKey or floorKey
    * HashMap make this very inefficent so we use TreeMap
    */
    private final TreeMap<Long, String> ring = new TreeMap<>();

    private static final int VIRTUAL_NODES = 150;

    public void addNode(String nodeName) {
        for (int i = 0; i < VIRTUAL_NODES; i++) {
            long position = hash(nodeName + "-" + i);
            ring.put(position, nodeName);
        }
    }

    public void removeNode(String nodeName) {
        for (int i = 0; i < VIRTUAL_NODES; i++) {
            long position = hash(nodeName + "-" + i);
            ring.remove(position);
        }
    }

    /*
    * This method is to get next clockwise node on hash ring of a give chuckHash.
    * First this chuckHash in converted into Position
    * ring.tailMap(position), returns all the elements greater than this position.
    * Example Elements in tree { (Left) 10 -> A | (Root) 30 -> B | (Right) 70 -> C }
    * ring.tailMap(25) => { (Root) 30 -> B | (Right) 70 -> C } - return all node greater than or equals 25
     * We return first vnode from here which is B
    * ring.tailMap(75) => {} - is empty so, return ring.get(ring.firstKey()) - We need to wrap again to 0 / first node
    */

    public String getNode(String chunkHash) {
        if (ring.isEmpty()) {
            throw new IllegalStateException("No nodes in ring");
        }

        long position = hash(chunkHash);

        SortedMap<Long, String> tail = ring.tailMap(position);

        if (tail.isEmpty()) {
            return ring.get(ring.firstKey());
        }

        return ring.get(tail.firstKey());
    }

    public Collection<String> getNodes() {
        return new java.util.HashSet<>(ring.values());
    }

    /*
    * This method uses MD5 to convert a string into 16 random-looking bytes, takes the first 4 bytes,
    * combines them into a 32-bit positive number using bit operations,
    * and returns that as the node’s position on the consistent hashing ring.
    */
    private long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));

            long result = ((long)(digest[3] & 0xFF) << 24)
                    | ((long)(digest[2] & 0xFF) << 16)
                    | ((long)(digest[1] & 0xFF) << 8)
                    | ((long)(digest[0] & 0xFF));

            return result & 0xFFFFFFFFL;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }

}
