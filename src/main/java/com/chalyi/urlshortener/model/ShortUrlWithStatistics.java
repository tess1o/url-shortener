package com.chalyi.urlshortener.model;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ShortUrlWithStatistics(
        ShortUrl shortUrl,
        List<LocalDateTime> lastVisitedTime,
        TimeStatistics visitedStatistics) {
}
