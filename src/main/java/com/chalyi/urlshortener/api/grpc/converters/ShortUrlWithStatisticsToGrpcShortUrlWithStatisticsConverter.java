package com.chalyi.urlshortener.api.grpc.converters;

import com.chalyi.urlshortener.grpc.ShortUrlWithStatistics;
import com.chalyi.urlshortener.grpc.TimeStatistics;
import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShortUrlWithStatisticsToGrpcShortUrlWithStatisticsConverter
        implements Converter<com.chalyi.urlshortener.model.ShortUrlWithStatistics, ShortUrlWithStatistics> {

    private final ShortUrlToGrpcShortUrlConverter shortUrlToGrpcShortUrlConverter;
    private final LocalDateTimeToTimestampConverter timestampConverter;
    private final TimeStatisticsToGrpcTimeStatisticsConverter timeStatisticsToGrpcTimeStatisticsConverter;


    @Override
    public ShortUrlWithStatistics convert(com.chalyi.urlshortener.model.ShortUrlWithStatistics shortUrlWithStatistics) {
        return ShortUrlWithStatistics.newBuilder()
                .setShortUrl(shortUrlToGrpcShortUrlConverter.convert(shortUrlWithStatistics.shortUrl()))
                .addAllLastVisited(getAllVisited(shortUrlWithStatistics.lastVisitedTime()))
                .setVisitedStatistics(getVisitedStatistics(shortUrlWithStatistics.visitedStatistics()))
                .build();
    }

    private TimeStatistics getVisitedStatistics(com.chalyi.urlshortener.model.TimeStatistics timeStatistics) {
        return timeStatisticsToGrpcTimeStatisticsConverter.convert(timeStatistics);
    }

    private Iterable<Timestamp> getAllVisited(List<LocalDateTime> visited) {
        return visited.stream()
                .map(timestampConverter::convert)
                .collect(Collectors.toList());
    }
}
