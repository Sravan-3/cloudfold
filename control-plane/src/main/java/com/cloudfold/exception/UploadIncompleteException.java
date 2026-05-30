// src/main/java/com/cloudfold/exception/UploadIncompleteException.java

package com.cloudfold.exception;

import java.util.List;

public class UploadIncompleteException extends RuntimeException {

    private final List<Integer> missingChunks;

    public UploadIncompleteException(String message, List<Integer> missingChunks) {
        super(message);
        this.missingChunks = missingChunks;
    }

    public List<Integer> getMissingChunks() { return missingChunks; }
}