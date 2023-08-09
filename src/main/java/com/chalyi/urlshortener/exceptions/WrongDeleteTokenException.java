package com.chalyi.urlshortener.exceptions;

public class WrongDeleteTokenException extends RuntimeException {

    public WrongDeleteTokenException(String message) {
        super(message);
    }
}
