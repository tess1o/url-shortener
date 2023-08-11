package com.chalyi.urlshortener.services.crud;

import com.chalyi.urlshortener.model.ShortUrl;

/**
 * This service is used to return information about the short url including some basic statistics.
 * More statistics should be handled in separate service
 */
public interface ShortUrlInfoService {
    ShortUrl info(String shortUrl);
}
