package com.chalyi.urlshortener.api.grpc.converters;

import com.chalyi.urlshortener.grpc.MostUserAgentsResponse;
import com.chalyi.urlshortener.grpc.ShortUrl;
import com.chalyi.urlshortener.model.OverallStatistics;
import com.chalyi.urlshortener.model.responses.MostUsedUserAgents;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OverallStatisticsToGrpcOverallStatisticsConverter implements Converter<OverallStatistics, com.chalyi.urlshortener.grpc.OverallStatistics> {

    private final MostUserAgentsToMostUserAgentsResponseConverter mostUserAgentsToMostUserAgentsResponseConverter;
    private final TimeStatisticsToGrpcTimeStatisticsConverter timeStatisticsToGrpcTimeStatisticsConverter;
    private final ShortUrlToGrpcShortUrlConverter shortUrlToGrpcShortUrlConverter;

    @Override
    public com.chalyi.urlshortener.grpc.OverallStatistics convert(OverallStatistics source) {
       return com.chalyi.urlshortener.grpc.OverallStatistics.newBuilder()
               .setCreated(timeStatisticsToGrpcTimeStatisticsConverter.convert(source.created()))
               .setVisited(timeStatisticsToGrpcTimeStatisticsConverter.convert(source.visited()))
               .setAverageCreatedPerDay(source.averageCreatedPerDay())
               .setAverageVisitedPerDay(source.averageVisitedPerDay())
               .setTotalCreated(source.totalCreated())
               .setTotalVisited(source.totalVisited())
               .addAllMostUserAgentsResponses(getMostUserAgents(source.mostUsedUserAgents()))
               .addAllMostViewedUrls(getMostViewedUrls(source.mostViewed()))
               .build();
    }

    private Iterable<ShortUrl> getMostViewedUrls(List<com.chalyi.urlshortener.model.ShortUrl> shortUrls) {
        return shortUrls.stream()
                .map(shortUrlToGrpcShortUrlConverter::convert)
                .collect(Collectors.toList());
    }

    private Iterable<MostUserAgentsResponse> getMostUserAgents(List<MostUsedUserAgents> mostUsedUserAgents) {
        return mostUsedUserAgents.stream()
                .map(mostUserAgentsToMostUserAgentsResponseConverter::convert)
                .collect(Collectors.toList());
    }
}
