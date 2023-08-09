package com.chalyi.urlshortener.exceptions;

public class NoSuchUrlFound extends RuntimeException {
    public NoSuchUrlFound(String message) {
        super(message);
    }
}
