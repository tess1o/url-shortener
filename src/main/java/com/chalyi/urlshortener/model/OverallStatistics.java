package com.chalyi.urlshortener.model;

import com.chalyi.urlshortener.model.responses.MostUsedUserAgents;
import lombok.Builder;

import java.util.List;

/**
 * Overall statistics for the short urls
 * @param created - time statistics for created urls
 * @param visited - time statistics for visited urls
 * @param averageCreatedPerDay - average number of short urls created per day
 * @param averageVisitedPerDay - average number of short urls visited per day
 * @param totalCreated - total number of created short urls
 * @param totalVisited - total number of visited short urls
 * @param mostUsedUserAgents - most used user agents for visited short urls
 * @param mostViewed - most viewed short urls
 */
@Builder
public record OverallStatistics(TimeStatistics created,
                                TimeStatistics visited,
                                double averageCreatedPerDay,
                                double averageVisitedPerDay,
                                long totalCreated,
                                long totalVisited,
                                List<MostUsedUserAgents> mostUsedUserAgents,
                                List<ShortUrl> mostViewed
                                ) {
}
