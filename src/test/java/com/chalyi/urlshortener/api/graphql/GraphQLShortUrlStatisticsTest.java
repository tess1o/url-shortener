package com.chalyi.urlshortener.api.graphql;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.SpringBootTestWithDirtyContext;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.requests.VisitShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.chalyi.urlshortener.services.crud.ShortUrlVisitService;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTestWithDirtyContext
public class GraphQLShortUrlStatisticsTest extends BaseTest {

    @Autowired
    private ShortUrlCreateService createService;

    @Autowired
    private ShortUrlVisitService visitService;

    @Autowired
    private DgsQueryExecutor dgsQueryExecutor;

    @Value("${short-url.statistics.defaultMostViewed}")
    private int defaultMostViewed;

    @Value("${short-url.statistics.defaultMostUsedAgents}")
    private int defaultMostUsedAgents;

    @AfterEach
    public void cleanup() {
        flushAll();
    }

    @Test
    public void infoTest() throws UnknownHostException {
        String originalUrl = "http://test.com";
        CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                originalUrl,
                0,
                "testUserAgent",
                InetAddress.getByName("10.10.0.0")
        );

        CreateShortUrlResponse response = createService.create(createShortUrlRequest);
        Assertions.assertNotNull(response);

        String graphqlRequest = """
                                
                 query($shortUrl: String!) {
                  info(request: { shortUrl: $shortUrl }) {
                    shortUrl {
                      shortUrl
                      originalUrl
                      visitors
                      uniqueVisitors
                      userAgents
                      created
                      expire
                    }
                    lastVisited
                    timeStatistics {
                      lastWeek
                      thisWeek
                      lastMonth
                      thisMonth
                      lastHour
                      today
                      yesterday
                    }
                  }
                }
                         
                """;
        Map<String, Object> result = dgsQueryExecutor.executeAndExtractJsonPath(graphqlRequest,
                "data.info",
                Map.of("shortUrl", response.getShortUrl()));

        Assertions.assertTrue(((JSONArray) result.get("lastVisited")).isEmpty());
        Assertions.assertTrue(result.get("shortUrl") instanceof Map);
        Map<String, Object> shortUrl = (Map<String, Object>) result.get("shortUrl");
        Assertions.assertEquals(response.getShortUrl(), shortUrl.get("shortUrl"));
        Assertions.assertEquals(originalUrl, shortUrl.get("originalUrl"));
    }

    @Test
    public void testMostViewedShortUrls() throws UnknownHostException {
        final int iterations = defaultMostViewed + 2;

        Set<String> createdShortUrls = new HashSet<>();

        for (int i = 0; i < iterations; i++) {
            CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                    "http://ukr.net",
                    0,
                    "testUserAgent",
                    InetAddress.getByName("10.10.0.0")
            );
            String shortUrl = createService.create(createShortUrlRequest).getShortUrl();
            createdShortUrls.add(shortUrl);
            visitService.visitShortUrl(new VisitShortUrlRequest(
                    shortUrl,
                    "testUserAgent",
                    InetAddress.getByName("10.10.0.0")
            ));
        }

        String graphqlRequest = """
                query($count: NonNegativeInt!){
                  mostViewedShortUrls(request: {count:$count}) {
                    shortUrl
                  }
                }          
                """;

        JSONArray shortUrls = dgsQueryExecutor.executeAndExtractJsonPath(graphqlRequest,
                "data.mostViewedShortUrls",
                Map.of("count", iterations));

        Set<String> shortUrlsFromQuery = shortUrls.stream()
                .map(s -> (String) (((LinkedHashMap) s).get("shortUrl")))
                .collect(Collectors.toSet());

        Assertions.assertEquals(iterations, shortUrlsFromQuery.size());
        Assertions.assertEquals(createdShortUrls, shortUrlsFromQuery);
    }

    @Test
    public void testMostUsedUserAgents() throws UnknownHostException {
        final int iterations = defaultMostUsedAgents + 2;
        String visitUserAgent = "visitUserAgent";
        String createUserAgent = "testUserAgent";

        for (int i = 0; i < iterations; i++) {
            CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                    "http://ukr.net",
                    0,
                    createUserAgent,
                    InetAddress.getByName("10.10.0.0")
            );
            String shortUrl = createService.create(createShortUrlRequest).getShortUrl();
            visitService.visitShortUrl(new VisitShortUrlRequest(
                    shortUrl,
                    visitUserAgent,
                    InetAddress.getByName("10.10.0.0")
            ));
        }

        String graphqlRequest = """
                query($count: NonNegativeInt!){
                  mostUsedUserAgents(request: {count:$count}) {
                    userAgent
                  }
                }          
                """;

        JSONArray userAgents = dgsQueryExecutor.executeAndExtractJsonPath(graphqlRequest,
                "data.mostUsedUserAgents",
                Map.of("count", iterations));

        List<String> expectedUserAgents = List.of(visitUserAgent, createUserAgent);

        Assertions.assertEquals(expectedUserAgents.size(), userAgents.size());

        List<String> responseUserAgents = userAgents.stream()
                .map(a -> (String)((LinkedHashMap) a).get("userAgent"))
                .toList();

        Assertions.assertLinesMatch(expectedUserAgents, responseUserAgents);
    }

    @Test
    public void testOverallStatistics() throws UnknownHostException {
        final int iterations = defaultMostViewed + 2;

        for (int i = 0; i < iterations; i++) {
            CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                    "http://ukr.net",
                    0,
                    "testUserAgent",
                    InetAddress.getByName("10.10.0.0")
            );
            String shortUrl = createService.create(createShortUrlRequest).getShortUrl();
            visitService.visitShortUrl(new VisitShortUrlRequest(
                    shortUrl,
                    "testUserAgent",
                    InetAddress.getByName("10.10.0.0")
            ));
        }

        String graphqlRequest = """
                {
                  overallStatistics {
                    created {
                      lastWeek
                      thisWeek
                      lastMonth
                      thisMonth
                      lastHour
                      today
                      yesterday
                    }
                    visited {
                      lastWeek
                      thisWeek
                      lastMonth
                      thisMonth
                      lastHour
                      today
                      yesterday
                    }
                    averageCreatedPerDay
                    averageVisitedPerDay
                    totalCreated
                    totalVisited
                    mostViewedUrls {
                      shortUrl
                      originalUrl
                      visitors
                      uniqueVisitors
                      userAgents
                      created
                      expire
                    }
                    mostUsedUserAgents {
                      userAgent
                      occurrences
                    }
                  }
                }
                            
                """;

        Map<String, Object> result = dgsQueryExecutor.executeAndExtractJsonPath(graphqlRequest,
                "data.overallStatistics");

        Map<String, Object> created = (Map<String, Object>) result.get("created");
        Assertions.assertEquals(iterations, created.get("thisWeek"));
        Assertions.assertEquals(iterations, created.get("thisMonth"));
        Assertions.assertEquals(iterations, created.get("lastHour"));
        Assertions.assertEquals(0, created.get("yesterday"));
        Assertions.assertEquals(iterations, result.get("totalCreated"));
    }
}
