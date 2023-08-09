package com.chalyi.urlshortener.model;

import com.chalyi.urlshortener.model.responses.MostUsedUserAgents;
import lombok.Builder;

import java.util.List;

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
