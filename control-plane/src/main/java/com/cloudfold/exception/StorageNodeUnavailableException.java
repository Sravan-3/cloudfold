package com.cloudfold.exception;

public class StorageNodeUnavailableException extends RuntimeException {

    public StorageNodeUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}