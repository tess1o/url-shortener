package com.chalyi.urlshortener.model;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record ShortUrl(
        String shortUrl,
        String originalUrl,
        long visitors,
        long uniqueVisitors,
        Set<String> userAgents,
        LocalDateTime created,
        LocalDateTime expire
) {
}
