package com.chalyi.urlshortener.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateShortUrlRequest {
    /**
     * The original url that needs to be shortened.
     */
    private String originalUrl;

    /**
     * Expire in seconds. If no expiration date is required then 0
     */
    private long expire;

    /**
     * Client's user agent for statistics purpose
     */
    private String userAgent;
}
