package com.chalyi.urlshortener.api.graphql.converter;

import com.chalyi.urlshortener.converters.AutoRegisteredConverter;
import com.chalyi.urlshortener.graphql.GraphQLMostUserAgentsResponse;
import com.chalyi.urlshortener.graphql.GraphQLOverallStatistics;
import com.chalyi.urlshortener.graphql.GraphQLShortUrl;
import com.chalyi.urlshortener.graphql.GraphQLTimeStatistics;
import com.chalyi.urlshortener.model.OverallStatistics;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OverallStatisticsToGraphqlOverallStatistics extends AutoRegisteredConverter<OverallStatistics, GraphQLOverallStatistics> {
    @Override
    public GraphQLOverallStatistics convert(OverallStatistics statistics) {
        return GraphQLOverallStatistics.builder()
                .setCreated(getConversionService().convert(statistics.created(), GraphQLTimeStatistics.class))
                .setVisited(getConversionService().convert(statistics.visited(), GraphQLTimeStatistics.class))
                .setAverageCreatedPerDay(statistics.averageCreatedPerDay())
                .setAverageVisitedPerDay(statistics.averageVisitedPerDay())
                .setTotalCreated(Long.valueOf(statistics.totalCreated()).intValue())
                .setTotalVisited(Long.valueOf(statistics.totalVisited()).intValue())
                .setMostViewedUrls(statistics.mostViewed().stream().map(url -> getConversionService().convert(url, GraphQLShortUrl.class)).collect(Collectors.toList()))
                .setMostUsedUserAgents(statistics.mostUsedUserAgents().stream().map(a -> getConversionService().convert(a, GraphQLMostUserAgentsResponse.class)).collect(Collectors.toList()))
                .build();
    }
}
