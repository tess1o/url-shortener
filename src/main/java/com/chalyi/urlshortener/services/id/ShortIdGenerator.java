package com.chalyi.urlshortener.services.id;

/**
 * This service should generate a pseudo-random string with given length.
 * Ideally for short-url purposes it should not contain special symbols.
 */
public interface ShortIdGenerator {

    /**
     * Generate a pseudo-random string with given length. The id will be used as short-url id and delete token.
     * This implementation should be stable for collisions, do not contain special symbols
     * @param length - the length of result string
     * @return pseudo-random alphanumeric string with given length
     */
    String generate(int length);
}
