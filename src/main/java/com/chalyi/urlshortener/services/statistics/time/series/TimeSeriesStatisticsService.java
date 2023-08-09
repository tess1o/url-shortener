package com.chalyi.urlshortener.services.statistics.time.series;

import com.chalyi.urlshortener.model.TimeStatistics;
import redis.clients.jedis.timeseries.AggregationType;

public interface TimeSeriesStatisticsService {
    TimeStatistics getTimeStatistics(String key, AggregationType aggregationType);
}
