package com.chalyi.urlshortener.services.statistics;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.TimeMachine;
import com.chalyi.urlshortener.model.ShortUrl;
import com.chalyi.urlshortener.model.ShortUrlWithStatistics;
import com.chalyi.urlshortener.model.TimeStatistics;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.requests.VisitShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.chalyi.urlshortener.services.crud.ShortUrlInfoService;
import com.chalyi.urlshortener.services.crud.ShortUrlVisitService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StatisticsServiceShortUrlWithStatisticsTest extends BaseTest {

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

    private final StatisticsService statisticsService;
    private final ShortUrlCreateService createService;
    private final ShortUrlVisitService visitService;

    private final ShortUrlInfoService infoService;

    @MockBean
    private Clock clock;

    @Autowired
    public StatisticsServiceShortUrlWithStatisticsTest(StatisticsService statisticsService,
                                                       ShortUrlCreateService createService,
                                                       ShortUrlVisitService visitService,
                                                       ShortUrlInfoService infoService) {
        this.statisticsService = statisticsService;
        this.createService = createService;
        this.visitService = visitService;
        this.infoService = infoService;
    }

    @AfterEach
    public void doCleanup() {
        flushAll();
    }

    @BeforeEach
    public void resetClock() {
        new TimeMachine(clock).resetClock();
    }

    @Test
    public void testShortUrlWithStatistics() throws UnknownHostException {
        CreateShortUrlResponse shortUrl = createShortUrl();
        Assertions.assertNotNull(shortUrl);
        ShortUrlWithStatistics statistics = statisticsService.getShortUrlWithStatistics(shortUrl.getShortUrl());
        Assertions.assertNotNull(statistics);

        ShortUrl info = infoService.info(shortUrl.getShortUrl());

        Assertions.assertEquals(shortUrl.getShortUrl(), statistics.shortUrl().shortUrl());
        Assertions.assertEquals(info, statistics.shortUrl());

        TimeStatistics visitedStatistics = statistics.visitedStatistics();

        Assertions.assertEquals(0, visitedStatistics.today());
        Assertions.assertEquals(0, visitedStatistics.yesterday());
        Assertions.assertEquals(0, visitedStatistics.lastHour());
        Assertions.assertEquals(0, visitedStatistics.lastMonth());
        Assertions.assertEquals(0, visitedStatistics.lastWeek());
        Assertions.assertEquals(0, visitedStatistics.thisWeek());
        Assertions.assertEquals(0, visitedStatistics.thisMonth());
    }

    @Test
    public void testShortUrlWithStatistics_visited() throws UnknownHostException {
        TimeMachine timeMachine = new TimeMachine(clock);
        CreateShortUrlResponse shortUrl = createShortUrl();
        Assertions.assertEquals(infoService.info(shortUrl.getShortUrl()), statisticsService.getShortUrlWithStatistics(shortUrl.getShortUrl()).shortUrl());

        for (Map.Entry<LocalDateTime, Integer> visitedDates : DATES_AND_COUNT.entrySet()) {
            for (int i = 0; i < visitedDates.getValue(); i++) {
                timeMachine.setFixedClock(visitedDates.getKey().minusSeconds(i));
                visitService.visitShortUrl(new VisitShortUrlRequest(
                        shortUrl.getShortUrl(),
                        "testUserAgent",
                        InetAddress.getByName("10.10.0.1")
                ));
                Assertions.assertEquals(infoService.info(shortUrl.getShortUrl()), statisticsService.getShortUrlWithStatistics(shortUrl.getShortUrl()).shortUrl());
            }
        }

        timeMachine.setFixedClock(NOW);

        ShortUrlWithStatistics statistics = statisticsService.getShortUrlWithStatistics(shortUrl.getShortUrl());
        Assertions.assertEquals(DATES_AND_COUNT.get(NOW) + DATES_AND_COUNT.get(LAST_HOUR), statistics.visitedStatistics().today());

        Assertions.assertEquals(DATES_AND_COUNT.get(YESTERDAY), statistics.visitedStatistics().yesterday());
        int thisWeekCount = DATES_AND_COUNT.get(NOW) + DATES_AND_COUNT.get(LAST_HOUR) + DATES_AND_COUNT.get(YESTERDAY);
        Assertions.assertEquals(thisWeekCount, statistics.visitedStatistics().thisWeek());

        int thisMonthCount = thisWeekCount + DATES_AND_COUNT.get(LAST_WEEK);
        Assertions.assertEquals(thisMonthCount, statistics.visitedStatistics().thisMonth());

        int lastWeekCount = DATES_AND_COUNT.get(LAST_WEEK);
        Assertions.assertEquals(lastWeekCount, statistics.visitedStatistics().lastWeek());

        int lastMonthCount = DATES_AND_COUNT.get(LAST_MONTH);
        Assertions.assertEquals(lastMonthCount, statistics.visitedStatistics().lastMonth());

        timeMachine.setFixedClock(NOW.minusMinutes(2));
        statistics = statisticsService.getShortUrlWithStatistics(shortUrl.getShortUrl());
        Assertions.assertEquals(DATES_AND_COUNT.get(LAST_HOUR), statistics.visitedStatistics().lastHour());

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
