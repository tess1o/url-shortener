package com.chalyi.urlshortener.services.crud;

/**
 * Service to delete a short url. A valid delete token is required.
 * The implementation should handle cases when a wrong short url or delete token is provided
 */
public interface ShortUrlDeleteService {
    /**
     * Delete short url from the storage. If no exception is thrown then the operation should be
     * considered as successful.
     * @throws com.chalyi.urlshortener.exceptions.NoSuchUrlFound when non-existing short url is provided
     * @throws com.chalyi.urlshortener.exceptions.WrongDeleteTokenException when an invalid delete-token is provided
     * @param shortUrl - short url to be deleted
     * @param deleteToken - delete token
     */
    void delete(String shortUrl, String deleteToken);
}
