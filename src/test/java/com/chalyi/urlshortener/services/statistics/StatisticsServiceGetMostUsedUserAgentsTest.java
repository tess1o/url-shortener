package com.chalyi.urlshortener.services.statistics;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.SpringBootTestWithDirtyContext;
import com.chalyi.urlshortener.TimeMachine;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.requests.VisitShortUrlRequest;
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
import java.util.Map;

@SpringBootTestWithDirtyContext
public class StatisticsServiceGetMostUsedUserAgentsTest extends BaseTest {

    private final StatisticsService statisticsService;
    private final ShortUrlCreateService createService;
    private final ShortUrlVisitService visitService;

    @MockBean
    private Clock clock;

    @Autowired
    public StatisticsServiceGetMostUsedUserAgentsTest(StatisticsService statisticsService, ShortUrlCreateService createService, ShortUrlVisitService visitService) {
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
    void testMostUsedUserAgent_visitUrls() throws UnknownHostException {
        final int urls = 10;
        final String agent = "testMostUsedUserAgent_visitUrls";
        List<String> createdShortUrls = new ArrayList<>();

        for (int i = 0; i < urls; i++) {
            CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                    "http://test.com",
                    0,
                    null
            );
            createdShortUrls.add(createService.create(createShortUrlRequest).getShortUrl());
        }
        Assertions.assertEquals(0, statisticsService.getMostUsedUserAgents(urls).size());

        for (String createdShortUrl : createdShortUrls) {
            VisitShortUrlRequest visitShortUrlRequest = new VisitShortUrlRequest(
                    createdShortUrl,
                    agent,
                    InetAddress.getByName("10.10.0.0")
            );
            visitService.visitShortUrl(visitShortUrlRequest);
        }
        List<MostUsedUserAgents> statistics = statisticsService.getMostUsedUserAgents(urls);
        Assertions.assertEquals(1, statistics.size());
        Assertions.assertEquals(urls, statistics.get(0).occurrences());
        Assertions.assertEquals(agent, statistics.get(0).userAgent());
    }

    @Test
    void testMostUsedUserAgent_visitDifferentUrls() throws UnknownHostException {
        final int urls = 10;
        List<String> createdShortUrls = new ArrayList<>();

        for (int i = 0; i < urls; i++) {
            CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                    "http://test.com",
                    0,
                    null
            );
            createdShortUrls.add(createService.create(createShortUrlRequest).getShortUrl());
        }

        Map<String, Integer> userAgentAndCount = Map.of(
                "agent", 5,
                "randomAgent", 3,
                "oneMoreAgent", 2
        );

        String shortUrl = createdShortUrls.get(0);

        for (Map.Entry<String, Integer> userAgent : userAgentAndCount.entrySet()) {
            VisitShortUrlRequest visitShortUrlRequest = new VisitShortUrlRequest(
                    shortUrl,
                    userAgent.getKey(),
                    InetAddress.getByName("10.10.0.0")
            );
            for (int i = 0; i < userAgent.getValue(); i++) {
                visitService.visitShortUrl(visitShortUrlRequest);
            }
        }

        List<MostUsedUserAgents> mostUsedUserAgents = statisticsService.getMostUsedUserAgents(userAgentAndCount.size());
        Assertions.assertEquals("agent", mostUsedUserAgents.get(0).userAgent());
        Assertions.assertEquals(5, mostUsedUserAgents.get(0).occurrences());

        Assertions.assertEquals("randomAgent", mostUsedUserAgents.get(1).userAgent());
        Assertions.assertEquals(3, mostUsedUserAgents.get(1).occurrences());
    }

    @Test
    void testMostUsedUserAgent_negativeCount() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> statisticsService.getMostUsedUserAgents(-1));
    }
}
