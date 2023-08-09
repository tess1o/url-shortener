package com.chalyi.urlshortener.services.statistics;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.SpringBootTestWithDirtyContext;
import com.chalyi.urlshortener.TimeMachine;
import com.chalyi.urlshortener.model.ShortUrl;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.requests.VisitShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.model.responses.MostUsedUserAgents;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.chalyi.urlshortener.services.crud.ShortUrlVisitService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

@SpringBootTestWithDirtyContext
public class StatisticsServiceGetMostViewedTest extends BaseTest {
    private final StatisticsService statisticsService;
    private final ShortUrlCreateService createService;
    private final ShortUrlVisitService visitService;

    @MockBean
    private Clock clock;

    @Autowired
    public StatisticsServiceGetMostViewedTest(StatisticsService statisticsService, ShortUrlCreateService createService, ShortUrlVisitService visitService) {
        this.statisticsService = statisticsService;
        this.createService = createService;
        this.visitService = visitService;
    }

    @BeforeEach
    public void doCleanup() {
        flushAll();
    }

    @BeforeEach
    public void resetClock() {
        new TimeMachine(clock).resetClock();
    }

    @Test
    void testGetMostViewed() throws UnknownHostException {
        final int urls = 20;

        List<String> createdShortUrls = new ArrayList<>();

        for (int i = 0; i < urls; i++) {
            CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                    "http://test.com",
                    0,
                    "testUserAgent"
            );

            CreateShortUrlResponse response = createService.create(createShortUrlRequest);

            createdShortUrls.add(response.getShortUrl());

            VisitShortUrlRequest visitShortUrlRequest = new VisitShortUrlRequest(
                    response.getShortUrl(),
                    "testUserAgent",
                    InetAddress.getByName("10.10.0.0")
            );

            visitService.visitShortUrl(visitShortUrlRequest);
            Assertions.assertEquals(i + 1, statisticsService.getMostViewed(urls).size());
        }
        Assertions.assertEquals(urls / 2, statisticsService.getMostViewed(urls / 2).size());
        Assertions.assertTrue(createdShortUrls.containsAll(statisticsService.getMostViewed(urls).stream().map(ShortUrl::shortUrl).toList()));

        String mostVisitedUrl = createdShortUrls.get(0);
        int maxVisited = 5;
        VisitShortUrlRequest visitShortUrlRequest = new VisitShortUrlRequest(
                mostVisitedUrl,
                "testUserAgent",
                InetAddress.getByName("10.10.0.0")
        );
        for (int i = 0; i < maxVisited; i++) {
            visitService.visitShortUrl(visitShortUrlRequest);
        }
        Assertions.assertEquals(mostVisitedUrl, statisticsService.getMostViewed(urls).get(0).shortUrl());

        String secondMostVisitedUrl = createdShortUrls.get(10);
        int secondMostVisitedCount = maxVisited - 1;
        visitShortUrlRequest = new VisitShortUrlRequest(
                secondMostVisitedUrl,
                "testUserAgent",
                InetAddress.getByName("10.10.0.0")
        );
        for (int i = 0; i < secondMostVisitedCount; i++) {
            visitService.visitShortUrl(visitShortUrlRequest);
        }

        Assertions.assertEquals(mostVisitedUrl, statisticsService.getMostViewed(2).get(0).shortUrl());
        Assertions.assertEquals(secondMostVisitedUrl, statisticsService.getMostViewed(2).get(1).shortUrl());
    }

    @Test
    void testMostUsedUserAgent_createUrls() {
        final int urls = 10;
        final String testUserAgent = "testUserAgent";

        for (int i = 0; i < urls; i++) {
            CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                    "http://test.com",
                    0,
                    testUserAgent
            );
            createService.create(createShortUrlRequest);
        }
        List<MostUsedUserAgents> statistics = statisticsService.getMostUsedUserAgents(urls);
        Assertions.assertEquals(1, statistics.size());
        Assertions.assertEquals(urls, statistics.get(0).occurrences());
        Assertions.assertEquals(testUserAgent, statistics.get(0).userAgent());
    }
}
