package com.chalyi.urlshortener.services.statistics;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.SpringBootTestWithDirtyContext;
import com.chalyi.urlshortener.TimeMachine;
import com.chalyi.urlshortener.model.OverallStatistics;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.requests.VisitShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.chalyi.urlshortener.services.crud.ShortUrlVisitService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

@SpringBootTestWithDirtyContext
public class StatisticsServiceGetOverallStatisticTest extends BaseTest {

    private final StatisticsService statisticsService;
    private final ShortUrlCreateService createService;
    private final ShortUrlVisitService visitService;

    @MockBean
    private Clock clock;

    @Autowired
    public StatisticsServiceGetOverallStatisticTest(StatisticsService statisticsService, ShortUrlCreateService createService, ShortUrlVisitService visitService) {
        this.statisticsService = statisticsService;
        this.createService = createService;
        this.visitService = visitService;
    }

    @AfterEach
    public void doCleanup() {
        flushAll();
    }

    @BeforeEach
    public void resetClock() {
        new TimeMachine(clock).resetClock();
    }

    private static final LocalDateTime NOW = LocalDateTime.of(2023, 7, 12, 14, 1);
    private static final LocalDateTime LAST_HOUR = NOW.minusMinutes(30);
    private static final LocalDateTime YESTERDAY = NOW.minusDays(1);
    private static final LocalDateTime LAST_WEEK = NOW.minusWeeks(1);
    private static final LocalDateTime LAST_MONTH = NOW.minusMonths(1);

    private final static Map<LocalDateTime, Integer> DATES_AND_COUNT = Map.of(
            NOW, 5,
            LAST_HOUR, 3,
            YESTERDAY, 4,
            LAST_WEEK, 100,
            LAST_MONTH, 150
    );

    @Test
    void overallTimeStatisticsCreatedTest() throws UnknownHostException {
        TimeMachine timeMachine = new TimeMachine(clock);
        for (Map.Entry<LocalDateTime, Integer> createsDates : DATES_AND_COUNT.entrySet()) {
            for (int i = 0; i < createsDates.getValue(); i++) {
                timeMachine.setFixedClock(createsDates.getKey().minusSeconds(i));
                createShortUrl();
            }
        }

        timeMachine.setFixedClock(NOW);

        OverallStatistics overallStatistics = statisticsService.overallTimeStatistics();
        Assertions.assertEquals(DATES_AND_COUNT.get(NOW) + DATES_AND_COUNT.get(LAST_HOUR), overallStatistics.created().today());

        Assertions.assertEquals(DATES_AND_COUNT.get(YESTERDAY), overallStatistics.created().yesterday());
        int thisWeekCount = DATES_AND_COUNT.get(NOW) + DATES_AND_COUNT.get(LAST_HOUR) + DATES_AND_COUNT.get(YESTERDAY);
        Assertions.assertEquals(thisWeekCount, overallStatistics.created().thisWeek());

        int thisMonthCount = thisWeekCount + DATES_AND_COUNT.get(LAST_WEEK);
        Assertions.assertEquals(thisMonthCount, overallStatistics.created().thisMonth());

        int lastWeekCount = DATES_AND_COUNT.get(LAST_WEEK);
        Assertions.assertEquals(lastWeekCount, overallStatistics.created().lastWeek());

        int lastMonthCount = DATES_AND_COUNT.get(LAST_MONTH);
        Assertions.assertEquals(lastMonthCount, overallStatistics.created().lastMonth());

        timeMachine.setFixedClock(NOW.minusMinutes(2));
        overallStatistics = statisticsService.overallTimeStatistics();
        Assertions.assertEquals(DATES_AND_COUNT.get(LAST_HOUR), overallStatistics.created().lastHour());

        int totalCreated = DATES_AND_COUNT.values().stream().mapToInt(i -> i).sum();
        Assertions.assertEquals(totalCreated, statisticsService.overallTimeStatistics().totalCreated());

        Assertions.assertEquals(totalCreated * 1.0 / DAYS.between(LAST_MONTH, NOW), overallStatistics.averageCreatedPerDay());
    }

    @Test
    void overallTimeStatisticsAverage_noCreatedNoVisited(){
        OverallStatistics overallStatistics = statisticsService.overallTimeStatistics();
        Assertions.assertEquals(0.0d, overallStatistics.averageCreatedPerDay());
        Assertions.assertEquals(0.0d, overallStatistics.averageVisitedPerDay());
    }

    @Test
    void overallTimeStatisticsVisitedTest() throws UnknownHostException {
        TimeMachine timeMachine = new TimeMachine(clock);
        for (Map.Entry<LocalDateTime, Integer> createsDates : DATES_AND_COUNT.entrySet()) {
            for (int i = 0; i < createsDates.getValue(); i++) {
                timeMachine.setFixedClock(createsDates.getKey().minusSeconds(i));
                CreateShortUrlResponse shortUrl = createShortUrl();
                visitService.visitShortUrl(new VisitShortUrlRequest(
                        shortUrl.getShortUrl(),
                        "testUserAgent",
                        InetAddress.getByName("10.10.0.1")
                ));
            }
        }

        timeMachine.setFixedClock(NOW);

        OverallStatistics overallStatistics = statisticsService.overallTimeStatistics();
        Assertions.assertEquals(DATES_AND_COUNT.get(NOW) + DATES_AND_COUNT.get(LAST_HOUR), overallStatistics.visited().today());

        Assertions.assertEquals(DATES_AND_COUNT.get(YESTERDAY), overallStatistics.visited().yesterday());
        int thisWeekCount = DATES_AND_COUNT.get(NOW) + DATES_AND_COUNT.get(LAST_HOUR) + DATES_AND_COUNT.get(YESTERDAY);
        Assertions.assertEquals(thisWeekCount, overallStatistics.visited().thisWeek());

        int thisMonthCount = thisWeekCount + DATES_AND_COUNT.get(LAST_WEEK);
        Assertions.assertEquals(thisMonthCount, overallStatistics.visited().thisMonth());

        int lastWeekCount = DATES_AND_COUNT.get(LAST_WEEK);
        Assertions.assertEquals(lastWeekCount, overallStatistics.visited().lastWeek());

        int lastMonthCount = DATES_AND_COUNT.get(LAST_MONTH);
        Assertions.assertEquals(lastMonthCount, overallStatistics.visited().lastMonth());

        int totalVisited = DATES_AND_COUNT.values().stream().mapToInt(i -> i).sum();
        Assertions.assertEquals(totalVisited, overallStatistics.totalVisited());

        Assertions.assertEquals(totalVisited * 1.0 / DAYS.between(LAST_MONTH, NOW), overallStatistics.averageVisitedPerDay());

        timeMachine.setFixedClock(NOW.minusMinutes(2));
        overallStatistics = statisticsService.overallTimeStatistics();
        Assertions.assertEquals(DATES_AND_COUNT.get(LAST_HOUR), overallStatistics.visited().lastHour());
    }

    private CreateShortUrlResponse createShortUrl() {
        CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                "http://test.com",
                0,
                "testUserAgent"
        );
        return createService.create(createShortUrlRequest);
    }
}
