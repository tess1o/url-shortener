package com.chalyi.urlshortener;

import com.chalyi.urlshortener.services.RedisUrlKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.timeseries.AggregationType;
import redis.clients.jedis.timeseries.DuplicatePolicy;
import redis.clients.jedis.timeseries.TSCreateParams;

/**
 * This component is used to initialize redis with required time series.
 * The time series must be created only once.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InitRedisEntities {

    public static final int ONE_DAY_MILLISECONDS = 24 * 3600 * 1000;
    public static final TSCreateParams CREATE_PARAMS = TSCreateParams.createParams().duplicatePolicy(DuplicatePolicy.SUM);

    private final UnifiedJedis unifiedJedis;
    private final RedisUrlKeys redisUrlKeys;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!unifiedJedis.exists(redisUrlKeys.urlCreatedTimeSeriesKey())) {
            log.info("Creating key {}", redisUrlKeys.urlCreatedTimeSeriesKey());
            unifiedJedis.tsCreate(redisUrlKeys.urlCreatedTimeSeriesKey(), CREATE_PARAMS);
        }

        if (!unifiedJedis.exists(redisUrlKeys.urlCreatedTimeSeries1DayKey())) {
            log.info("Creating key {}", redisUrlKeys.urlCreatedTimeSeries1DayKey());
            unifiedJedis.tsCreate(redisUrlKeys.urlCreatedTimeSeries1DayKey(), CREATE_PARAMS);
            unifiedJedis.tsCreateRule(
                    redisUrlKeys.urlCreatedTimeSeriesKey(),
                    redisUrlKeys.urlCreatedTimeSeries1DayKey(),
                    AggregationType.SUM,
                    ONE_DAY_MILLISECONDS
            );
        }

        if (!unifiedJedis.exists(redisUrlKeys.urlVisitedTimeSeriesKey())) {
            log.info("Creating key {}", redisUrlKeys.urlVisitedTimeSeriesKey());
            unifiedJedis.tsCreate(redisUrlKeys.urlVisitedTimeSeriesKey(), CREATE_PARAMS);
        }

        if (!unifiedJedis.exists(redisUrlKeys.urlVisitedTimeSeries1DayKey())) {
            log.info("Creating key {}", redisUrlKeys.urlVisitedTimeSeries1DayKey());
            unifiedJedis.tsCreate(redisUrlKeys.urlVisitedTimeSeries1DayKey(), CREATE_PARAMS);
            unifiedJedis.tsCreateRule(
                    redisUrlKeys.urlVisitedTimeSeriesKey(),
                    redisUrlKeys.urlVisitedTimeSeries1DayKey(),
                    AggregationType.SUM,
                    ONE_DAY_MILLISECONDS
            );
        }
    }
}
