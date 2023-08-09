package com.chalyi.urlshortener.api.graphql.converter;

import com.chalyi.urlshortener.graphql.GraphQLTimeStatistics;
import com.chalyi.urlshortener.model.TimeStatistics;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TimeStatisticsToGraphqlTimeStatisticsConverter implements Converter<TimeStatistics, GraphQLTimeStatistics> {
    @Override
    public GraphQLTimeStatistics convert(TimeStatistics statistics) {
        return GraphQLTimeStatistics.builder()
                .setToday(statistics.today())
                .setYesterday(statistics.yesterday())
                .setLastHour(statistics.lastHour())
                .setLastWeek(statistics.lastWeek())
                .setLastMonth(statistics.lastMonth())
                .setThisWeek(statistics.thisWeek())
                .setThisMonth(statistics.thisMonth())
                .build();
    }
}
