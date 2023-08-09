package com.chalyi.urlshortener.api.grpc.converters;

import com.chalyi.urlshortener.model.TimeStatistics;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TimeStatisticsToGrpcTimeStatisticsConverter implements Converter<TimeStatistics, com.chalyi.urlshortener.grpc.TimeStatistics> {
    @Override
    public com.chalyi.urlshortener.grpc.TimeStatistics convert(TimeStatistics timeStatistics) {
        return com.chalyi.urlshortener.grpc.TimeStatistics.newBuilder()
                .setLastHour(timeStatistics.lastHour())
                .setLastMonth(timeStatistics.lastMonth())
                .setLastWeek(timeStatistics.lastWeek())
                .setThisMonth(timeStatistics.thisMonth())
                .setThisWeek(timeStatistics.thisWeek())
                .setYesterday(timeStatistics.yesterday())
                .setToday(timeStatistics.today())
                .build();
    }
}
