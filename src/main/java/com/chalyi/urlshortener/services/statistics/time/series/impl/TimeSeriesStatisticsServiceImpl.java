package com.chalyi.urlshortener.services.statistics.time.series.impl;

import com.chalyi.urlshortener.model.TimeStatistics;
import com.chalyi.urlshortener.services.statistics.time.series.TimeSeriesParamsService;
import com.chalyi.urlshortener.services.statistics.time.series.TimeSeriesStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.timeseries.AggregationType;
import redis.clients.jedis.timeseries.TSElement;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSeriesStatisticsServiceImpl implements TimeSeriesStatisticsService {

    private final UnifiedJedis unifiedJedis;
    private final TimeSeriesParamsService timeSeriesParamsService;

    @Override
    public TimeStatistics getTimeStatistics(String key, AggregationType aggregationType) {
        Response<List<TSElement>> lastWeek;
        Response<List<TSElement>> thisWeek;
        Response<List<TSElement>> lastMonth;
        Response<List<TSElement>> thisMonth;
        Response<List<TSElement>> lastHour;
        Response<List<TSElement>> yesterday;
        Response<List<TSElement>> today;

        try (Pipeline pipeline = (Pipeline) unifiedJedis.pipelined()) {
            lastWeek = pipeline.tsRange(key, timeSeriesParamsService.getLastWeekParams(aggregationType));
            thisWeek = pipeline.tsRange(key, timeSeriesParamsService.getThisWeekParams(aggregationType));
            lastMonth = pipeline.tsRange(key, timeSeriesParamsService.getLastMonthParams(aggregationType));
            thisMonth = pipeline.tsRange(key, timeSeriesParamsService.getThisMonthParams(aggregationType));
            lastHour = pipeline.tsRange(key, timeSeriesParamsService.getLastHourParams(aggregationType));
            yesterday = pipeline.tsRange(key, timeSeriesParamsService.getYesterdayParams(aggregationType));
            today = pipeline.tsRange(key, timeSeriesParamsService.getTodayParams(aggregationType));
        }

        return TimeStatistics.builder()
                .lastWeek(getVisitedOrZero(lastWeek.get()))
                .thisWeek(getVisitedOrZero(thisWeek.get()))
                .lastMonth(getVisitedOrZero(lastMonth.get()))
                .thisMonth(getVisitedOrZero(thisMonth.get()))
                .yesterday(getVisitedOrZero(yesterday.get()))
                .today(getVisitedOrZero(today.get()))
                .lastHour(getVisitedOrZero(lastHour.get()))
                .build();
    }


    private int getVisitedOrZero(List<TSElement> visited) {
        if (visited == null || visited.size() == 0) {
            return 0;
        }
        return visited.stream()
                .mapToInt(v -> Double.valueOf(v.getValue()).intValue())
                .sum();
    }
}
