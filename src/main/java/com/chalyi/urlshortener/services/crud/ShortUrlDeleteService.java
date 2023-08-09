package com.chalyi.urlshortener.services.crud;

public interface ShortUrlDeleteService {
    void delete(String shortUrl, String deleteToken);
}
