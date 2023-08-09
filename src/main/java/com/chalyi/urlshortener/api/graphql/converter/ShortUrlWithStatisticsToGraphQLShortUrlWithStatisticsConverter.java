package com.chalyi.urlshortener.api.graphql.converter;

import com.chalyi.urlshortener.converters.AutoRegisteredConverter;
import com.chalyi.urlshortener.graphql.GraphQLShortUrl;
import com.chalyi.urlshortener.graphql.GraphQLShortUrlWithStatistics;
import com.chalyi.urlshortener.graphql.GraphQLTimeStatistics;
import com.chalyi.urlshortener.model.ShortUrlWithStatistics;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.stream.Collectors;

@Component
public class ShortUrlWithStatisticsToGraphQLShortUrlWithStatisticsConverter extends AutoRegisteredConverter<ShortUrlWithStatistics, GraphQLShortUrlWithStatistics> {

    @Override
    public GraphQLShortUrlWithStatistics convert(ShortUrlWithStatistics source) {
        return GraphQLShortUrlWithStatistics.builder()
                .setShortUrl(getConversionService().convert(source.shortUrl(), GraphQLShortUrl.class))
                .setTimeStatistics(getConversionService().convert(source.visitedStatistics(), GraphQLTimeStatistics.class))
                .setLastVisited(source.lastVisitedTime().stream().map(t -> t.atZone(ZoneId.systemDefault()).toOffsetDateTime()).collect(Collectors.toList()))
                .build();
    }
}
