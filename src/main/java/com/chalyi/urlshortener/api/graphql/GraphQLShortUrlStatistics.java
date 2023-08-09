package com.chalyi.urlshortener.api.graphql;

import com.chalyi.urlshortener.graphql.*;
import com.chalyi.urlshortener.model.OverallStatistics;
import com.chalyi.urlshortener.model.ShortUrl;
import com.chalyi.urlshortener.model.ShortUrlWithStatistics;
import com.chalyi.urlshortener.model.responses.MostUsedUserAgents;
import com.chalyi.urlshortener.services.statistics.StatisticsService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DgsComponent
@RequiredArgsConstructor
public class GraphQLShortUrlStatistics {

    private final StatisticsService statisticsService;
    private final ConversionService conversionService;

    @Value("${short-url.statistics.defaultMostViewed}")
    private int defaultMostViewed;

    @Value("${short-url.statistics.defaultMostUsedAgents}")
    private int defaultMostUsedAgents;


    @DgsQuery
    public GraphQLShortUrlWithStatistics info(@InputArgument GraphQLShortUrlRequest request) {
        ShortUrlWithStatistics shortUrl = statisticsService.getShortUrlWithStatistics(request.getShortUrl());
        return conversionService.convert(shortUrl, GraphQLShortUrlWithStatistics.class);
    }

    @DgsQuery
    public List<GraphQLMostUserAgentsResponse> mostUsedUserAgents(@InputArgument("request") Optional<GraphQLMostUserAgentsRequest> request) {
        int count = request.isPresent() ? request.get().getCount() : defaultMostUsedAgents;
        List<MostUsedUserAgents> mostUsedUserAgents = statisticsService.getMostUsedUserAgents(count);
        return mostUsedUserAgents.stream()
                .map(m -> conversionService.convert(m, GraphQLMostUserAgentsResponse.class))
                .collect(Collectors.toList());
    }

    @DgsQuery
    public List<GraphQLShortUrl> mostViewedShortUrls(@InputArgument("request") Optional<GraphQLMostViewedUrlRequest> request) {
        int count = request.isPresent() ? request.get().getCount() : defaultMostViewed;

        List<ShortUrl> mostViewed = statisticsService.getMostViewed(count);
        return mostViewed.stream()
                .map(url -> conversionService.convert(url, GraphQLShortUrl.class))
                .collect(Collectors.toList());
    }

    @DgsQuery
    public GraphQLOverallStatistics overallStatistics() {
        OverallStatistics statistics = statisticsService.overallTimeStatistics();
        return conversionService.convert(statistics, GraphQLOverallStatistics.class);
    }
}
