package com.chalyi.urlshortener.services.statistics;

import com.chalyi.urlshortener.TestDirtyContext;
import com.chalyi.urlshortener.TimeMachine;
import com.chalyi.urlshortener.services.statistics.time.series.TimeSeriesParamsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import redis.clients.jedis.CommandArguments;
import redis.clients.jedis.timeseries.AggregationType;
import redis.clients.jedis.timeseries.TSRangeParams;
import redis.clients.jedis.timeseries.TimeSeriesProtocol;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@TestDirtyContext
public class TimeSeriesParamsServiceTest {

    private static final AggregationType AGGREGATION_TYPE = AggregationType.AVG;
    private final TimeSeriesParamsService timeSeriesParamsService;

    @MockBean
    private Clock clock;

    @Autowired
    public TimeSeriesParamsServiceTest(TimeSeriesParamsService timeSeriesParamsService) {
        this.timeSeriesParamsService = timeSeriesParamsService;
    }

    static Stream<Arguments> nowTime() {
        final List<LocalDateTime> baseData = List.of(
                LocalDateTime.of(2023, 7, 12, 14, 0),
                LocalDateTime.of(2023, 7, 14, 23, 59),
                LocalDateTime.of(2023, 7, 13, 0, 1),
                LocalDateTime.of(2023, 7, 16, 12, 25),
                LocalDateTime.of(2023, 7, 16, 0, 0)
        );
        return baseData.stream().map(Arguments::of);
    }

    static Stream<Arguments> lastWeek() {
        final List<LocalDateTime> now = List.of(
                LocalDateTime.of(2023, 7, 10, 14, 0),
                LocalDateTime.of(2023, 7, 11, 23, 59),
                LocalDateTime.of(2023, 7, 12, 0, 1),
                LocalDateTime.of(2023, 7, 13, 12, 25),
                LocalDateTime.of(2023, 7, 14, 5, 15),
                LocalDateTime.of(2023, 7, 15, 17, 7),
                LocalDateTime.of(2023, 7, 16, 11, 58)
        );

        final LocalDate lastMonday = LocalDate.of(2023, 7, 3);
        final LocalDate currentMonday = lastMonday.plusWeeks(1);

        return now.stream().map(d -> Arguments.of(d, lastMonday, currentMonday));
    }

    static Stream<Arguments> thisWeek() {
        final List<LocalDateTime> now = List.of(
                LocalDateTime.of(2023, 7, 10, 14, 0),
                LocalDateTime.of(2023, 7, 11, 23, 59),
                LocalDateTime.of(2023, 7, 12, 0, 1),
                LocalDateTime.of(2023, 7, 13, 12, 25),
                LocalDateTime.of(2023, 7, 14, 5, 15),
                LocalDateTime.of(2023, 7, 15, 17, 7),
                LocalDateTime.of(2023, 7, 16, 11, 58)
        );

        final LocalDate thisMonday = LocalDate.of(2023, 7, 10);
        return now.stream().map(d -> Arguments.of(d, thisMonday));
    }

    static Stream<Arguments> lastMonth() {
        final List<LocalDateTime> now = List.of(
                LocalDateTime.of(2023, 1, 1, 14, 0),
                LocalDateTime.of(2023, 1, 2, 14, 0),
                LocalDateTime.of(2023, 1, 30, 23, 59),
                LocalDateTime.of(2023, 1, 31, 0, 1),
                LocalDateTime.of(2023, 6, 1, 12, 25),
                LocalDateTime.of(2023, 6, 15, 5, 15),
                LocalDateTime.of(2023, 6, 30, 17, 7)
        );
        return now.stream()
                .map(d -> Arguments.of(
                                d,
                                d.minusMonths(1).withDayOfMonth(1).toLocalDate(),
                                d.withDayOfMonth(1).toLocalDate()
                        )
                );
    }

    static Stream<Arguments> thisMonth() {
        final List<LocalDateTime> now = List.of(
                LocalDateTime.of(2023, 1, 1, 14, 0),
                LocalDateTime.of(2023, 1, 2, 14, 0),
                LocalDateTime.of(2023, 1, 30, 23, 59),
                LocalDateTime.of(2023, 1, 31, 0, 1),
                LocalDateTime.of(2023, 6, 1, 12, 25),
                LocalDateTime.of(2023, 6, 15, 5, 15),
                LocalDateTime.of(2023, 6, 30, 17, 7)
        );
        return now.stream()
                .map(d -> Arguments.of(
                                d,
                                d.withDayOfMonth(1).toLocalDate()
                        )
                );
    }

    private List<String> commonTsRangeParamsTest(TSRangeParams params) {
        TimeSeriesProtocol.TimeSeriesCommand command = TimeSeriesProtocol.TimeSeriesCommand.RANGE;
        CommandArguments commandArgument = new CommandArguments(command);
        params.addParams(commandArgument);
        List<String> commands = new ArrayList<>();
        commandArgument.iterator().forEachRemaining(a -> commands.add(new String(a.getRaw())));

        Assertions.assertEquals(8, commands.size());
        Assertions.assertEquals(new String(command.getRaw()), commands.get(0));
        Assertions.assertEquals("ALIGN", commands.get(3));
        Assertions.assertEquals("-", commands.get(4));
        Assertions.assertEquals(new String(TimeSeriesProtocol.TimeSeriesKeyword.AGGREGATION.getRaw()), commands.get(5));
        Assertions.assertEquals(new String(AGGREGATION_TYPE.getRaw()), commands.get(6));
        return commands;
    }

    @ParameterizedTest
    @MethodSource("nowTime")
    public void testGetTodayParams(LocalDateTime now) {
        new TimeMachine(clock).setFixedClock(now);
        TSRangeParams todayParams = timeSeriesParamsService.getTodayParams(AGGREGATION_TYPE);
        var commands = commonTsRangeParamsTest(todayParams);

        Assertions.assertEquals(getEpochMilli(now.toLocalDate().atStartOfDay()), Long.parseLong(commands.get(1)));
        Assertions.assertEquals(getEpochMilli(now), Long.parseLong(commands.get(2)));
        Assertions.assertEquals(getEpochMilli(now) - getEpochMilli(now.toLocalDate().atStartOfDay()), Long.parseLong(commands.get(7)));
    }


    @ParameterizedTest
    @MethodSource("nowTime")
    public void testGetYesterdayParams(LocalDateTime now) {
        new TimeMachine(clock).setFixedClock(now);
        TSRangeParams yesterdayParams = timeSeriesParamsService.getYesterdayParams(AGGREGATION_TYPE);

        LocalDateTime yesterdayStartOfDay = now.toLocalDate().minusDays(1).atStartOfDay();
        LocalDateTime todayAsStartOfDay = now.toLocalDate().atStartOfDay();

        var commands = commonTsRangeParamsTest(yesterdayParams);

        Assertions.assertEquals(getEpochMilli(yesterdayStartOfDay), Long.parseLong(commands.get(1)));
        Assertions.assertEquals(getEpochMilli(todayAsStartOfDay), Long.parseLong(commands.get(2)));
        Assertions.assertEquals(24 * 3600 * 1000L, Long.parseLong(commands.get(7)));
    }

    @ParameterizedTest
    @MethodSource("nowTime")
    public void testGetLastHourParams(LocalDateTime now) {
        new TimeMachine(clock).setFixedClock(now);
        TSRangeParams lastHourParams = timeSeriesParamsService.getLastHourParams(AGGREGATION_TYPE);

        var commands = commonTsRangeParamsTest(lastHourParams);

        LocalDateTime lastHour = now.minusHours(1);

        Assertions.assertEquals(getEpochMilli(lastHour), Long.parseLong(commands.get(1)));
        Assertions.assertEquals(getEpochMilli(now), Long.parseLong(commands.get(2)));
        Assertions.assertEquals(3600 * 1000L, Long.parseLong(commands.get(7)));
    }

    @ParameterizedTest
    @MethodSource("lastWeek")
    public void testGetLastWeekParams(LocalDateTime now, LocalDate lastMonday, LocalDate currentMonday) {
        new TimeMachine(clock).setFixedClock(now);
        TSRangeParams lastWeekParams = timeSeriesParamsService.getLastWeekParams(AGGREGATION_TYPE);
        var commands = commonTsRangeParamsTest(lastWeekParams);

        Assertions.assertEquals(getEpochMilli(lastMonday), Long.parseLong(commands.get(1)));
        Assertions.assertEquals(getEpochMilli(currentMonday), Long.parseLong(commands.get(2)));
        Assertions.assertEquals(24 * 7 * 3600 * 1000L, Long.parseLong(commands.get(7)));
    }

    @ParameterizedTest
    @MethodSource("thisWeek")
    public void testGetThisWeekParams(LocalDateTime now, LocalDate thisMonday) {
        new TimeMachine(clock).setFixedClock(now);
        TSRangeParams thisWeekParams = timeSeriesParamsService.getThisWeekParams(AGGREGATION_TYPE);
        List<String> commands = commonTsRangeParamsTest(thisWeekParams);

        Assertions.assertEquals(getEpochMilli(thisMonday), Long.parseLong(commands.get(1)));
        Assertions.assertEquals(getEpochMilli(now), Long.parseLong(commands.get(2)));
        Assertions.assertEquals(Duration.between(thisMonday.atStartOfDay(), now).getSeconds() * 1000L, Long.parseLong(commands.get(7)));
    }

    @ParameterizedTest
    @MethodSource("lastMonth")
    public void testGetLastMonthParams(LocalDateTime now, LocalDate firstDayOfLastMonth, LocalDate lastDayOfLastMonth) {
        new TimeMachine(clock).setFixedClock(now);
        TSRangeParams lastMonthParams = timeSeriesParamsService.getLastMonthParams(AGGREGATION_TYPE);

        var commands = commonTsRangeParamsTest(lastMonthParams);

        Assertions.assertEquals(getEpochMilli(firstDayOfLastMonth), Long.parseLong(commands.get(1)));
        Assertions.assertEquals(getEpochMilli(lastDayOfLastMonth), Long.parseLong(commands.get(2)));
        Assertions.assertEquals(Duration.between(firstDayOfLastMonth.atStartOfDay(), lastDayOfLastMonth.atStartOfDay()).getSeconds() * 1000L,
                Long.parseLong(commands.get(7)));
    }

    @ParameterizedTest
    @MethodSource("thisMonth")
    public void testGetThisMonthParams(LocalDateTime now, LocalDate firstDayOfMonth) {
        new TimeMachine(clock).setFixedClock(now);
        TSRangeParams thisMonthParams = timeSeriesParamsService.getThisMonthParams(AGGREGATION_TYPE);

        var commands = commonTsRangeParamsTest(thisMonthParams);

        Assertions.assertEquals(getEpochMilli(firstDayOfMonth), Long.parseLong(commands.get(1)));
        Assertions.assertEquals(getEpochMilli(now), Long.parseLong(commands.get(2)));
        Assertions.assertEquals(Duration.between(firstDayOfMonth.atStartOfDay(), now).getSeconds() * 1000L, Long.parseLong(commands.get(7)));
    }

    private static long getEpochMilli(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private static long getEpochMilli(LocalDate localDate) {
        return getEpochMilli(localDate.atStartOfDay());
    }
}
