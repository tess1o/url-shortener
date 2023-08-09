package com.chalyi.urlshortener.services.statistics.time.series;

import redis.clients.jedis.timeseries.AggregationType;
import redis.clients.jedis.timeseries.TSRangeParams;

public interface TimeSeriesParamsService {
    TSRangeParams getTodayParams(AggregationType aggregationType);

    TSRangeParams getYesterdayParams(AggregationType aggregationType);

    TSRangeParams getLastWeekParams(AggregationType aggregationType);

    TSRangeParams getThisWeekParams(AggregationType aggregationType);

    TSRangeParams getLastMonthParams(AggregationType aggregationType);

    TSRangeParams getThisMonthParams(AggregationType aggregationType);

    TSRangeParams getLastHourParams(AggregationType aggregationType);
}
