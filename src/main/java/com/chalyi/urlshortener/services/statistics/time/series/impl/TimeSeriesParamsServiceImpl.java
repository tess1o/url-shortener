package com.chalyi.urlshortener.services.statistics.time.series.impl;

import com.chalyi.urlshortener.converters.EpochMilliConverter;
import com.chalyi.urlshortener.services.statistics.time.series.TimeSeriesParamsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.timeseries.AggregationType;
import redis.clients.jedis.timeseries.TSRangeParams;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

@RequiredArgsConstructor
@Service
public class TimeSeriesParamsServiceImpl implements TimeSeriesParamsService {

    private final EpochMilliConverter converter;
    private final Clock clock;

    public static final int ONE_DAY_MILLISECONDS = 24 * 60 * 60 * 1000;

    @Override
    public TSRangeParams getTodayParams(AggregationType aggregationType) {
        LocalDateTime localDateTimeNow = LocalDateTime.now(clock);
        return new TSRangeParams(
                converter.localDateTimeToEpochMilli(localDateTimeNow.toLocalDate().atStartOfDay()),
                converter.localDateTimeToEpochMilli(localDateTimeNow)
        )
                .aggregation(aggregationType, localDateTimeNow.toLocalTime().toSecondOfDay() * 1000L)
                .alignStart();
    }

    @Override
    public TSRangeParams getYesterdayParams(AggregationType aggregationType) {
        LocalDate localDateNow = LocalDate.now(clock);
        return new TSRangeParams(converter.localDateToEpochMilli(localDateNow.minusDays(1)), converter.localDateToEpochMilli(localDateNow))
                .aggregation(aggregationType, ONE_DAY_MILLISECONDS)
                .alignStart();
    }

    @Override
    public TSRangeParams getLastWeekParams(AggregationType aggregationType) {
        LocalDate localDateNow = LocalDate.now(clock);

        LocalDateTime lastMonday = localDateNow.minusWeeks(1).with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime lastSunday = localDateNow.minusWeeks(1).with(DayOfWeek.SUNDAY).atStartOfDay().plusDays(1);

        return new TSRangeParams(converter.localDateTimeToEpochMilli(lastMonday), converter.localDateTimeToEpochMilli(lastSunday))
                .aggregation(aggregationType, Duration.between(lastMonday, lastSunday).getSeconds() * 1000L)
                .alignStart();
    }

    @Override
    public TSRangeParams getThisWeekParams(AggregationType aggregationType) {
        LocalDateTime localDateTimeNow = LocalDateTime.now(clock);
        LocalDate localDateNow = LocalDate.now(clock);

        LocalDateTime thisMonday = localDateNow.with(DayOfWeek.MONDAY).atStartOfDay();

        return new TSRangeParams(converter.localDateTimeToEpochMilli(thisMonday), converter.localDateTimeToEpochMilli(localDateTimeNow))
                .aggregation(aggregationType, Duration.between(thisMonday, localDateTimeNow).getSeconds() * 1000L)
                .alignStart();
    }

    @Override
    public TSRangeParams getLastMonthParams(AggregationType aggregationType) {
        LocalDate localDateNow = LocalDate.now(clock);
        LocalDateTime firstDateOfLastMonth = localDateNow.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime lastDateOfLastMonth = localDateNow.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();

        return new TSRangeParams(converter.localDateTimeToEpochMilli(firstDateOfLastMonth), converter.localDateTimeToEpochMilli(lastDateOfLastMonth))
                .aggregation(aggregationType, Duration.between(firstDateOfLastMonth, lastDateOfLastMonth).getSeconds() * 1000L)
                .alignStart();
    }

    @Override
    public TSRangeParams getThisMonthParams(AggregationType aggregationType) {
        LocalDateTime localDateTimeNow = LocalDateTime.now(clock);

        LocalDateTime firstDateOfThisMonth = localDateTimeNow.toLocalDate().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();

        return new TSRangeParams(converter.localDateTimeToEpochMilli(firstDateOfThisMonth), converter.localDateTimeToEpochMilli(localDateTimeNow))
                .aggregation(aggregationType, Duration.between(firstDateOfThisMonth, localDateTimeNow).getSeconds() * 1000L)
                .alignStart();
    }

    @Override
    public TSRangeParams getLastHourParams(AggregationType aggregationType) {
        LocalDateTime localDateTimeNow = LocalDateTime.now(clock);

        return new TSRangeParams(converter.localDateTimeToEpochMilli(localDateTimeNow.minusHours(1)), converter.localDateTimeToEpochMilli(localDateTimeNow))
                .aggregation(aggregationType, Duration.between(localDateTimeNow.minusHours(1), localDateTimeNow).getSeconds() * 1000L)
                .alignStart();
    }
}
