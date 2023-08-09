package com.chalyi.urlshortener.api.grpc;

import com.chalyi.urlshortener.SpringBootTestWithDirtyContext;
import com.chalyi.urlshortener.grpc.*;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.requests.VisitShortUrlRequest;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.chalyi.urlshortener.services.crud.ShortUrlVisitService;
import com.google.protobuf.Empty;
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
public class GrpcStatisticsTest extends BaseGrpcTest {

    @Value("${short-url.statistics.defaultMostViewed}")
    private int defaultMostViewed;

    @Value("${short-url.statistics.defaultMostUsedAgents}")
    private int defaultMostUsedAgents;

    @Autowired
    private ShortUrlCreateService createService;

    @Autowired
    private ShortUrlVisitService visitService;

    @AfterEach
    public void cleanup() {
        flushAll();
    }

    @Test
    public void testMostViewedUrls() throws UnknownHostException {
        UrlStatisticsServiceGrpc.UrlStatisticsServiceBlockingStub stub = UrlStatisticsServiceGrpc.newBlockingStub(getChannel());

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

            Iterator<ShortUrl> grpcResult = stub.getMostViewedShortUrls(MostViewedUrlRequest.newBuilder()
                    .setCount(iterations)
                    .build());

            List<ShortUrl> shortUrls = new ArrayList<>();
            grpcResult.forEachRemaining(shortUrls::add);

            Set<String> shortUrlsFromGrpc = shortUrls.stream().map(ShortUrl::getShortUrl).collect(Collectors.toSet());

            Assertions.assertEquals(createdShortUrls, shortUrlsFromGrpc);
        }
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

        UrlStatisticsServiceGrpc.UrlStatisticsServiceBlockingStub stub = UrlStatisticsServiceGrpc.newBlockingStub(getChannel());

        Iterator<MostUserAgentsResponse> userAgents = stub.getMostUsedUserAgents(MostUserAgentsRequest.newBuilder()
                .setCount(iterations)
                .build());

        Set<String> grpcUserAgents = new HashSet<>();
        while (userAgents.hasNext()) {
            grpcUserAgents.add(userAgents.next().getUserAgent());
        }

        Set<String> expectedUserAgents = Set.of(visitUserAgent, createUserAgent);

        Assertions.assertEquals(expectedUserAgents, grpcUserAgents);
    }

    @Test
    public void testOverallStatistics() throws UnknownHostException {
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

        UrlStatisticsServiceGrpc.UrlStatisticsServiceBlockingStub stub = UrlStatisticsServiceGrpc.newBlockingStub(getChannel());

        OverallStatistics statistics = stub.getOverallStatistics(Empty.newBuilder().build());

        Assertions.assertEquals(iterations, statistics.getCreated().getThisWeek());
        Assertions.assertEquals(iterations, statistics.getCreated().getThisMonth());
        Assertions.assertEquals(iterations, statistics.getCreated().getLastHour());
        Assertions.assertEquals(0, statistics.getCreated().getYesterday());
        Assertions.assertEquals(iterations, statistics.getTotalCreated());
        Assertions.assertEquals(iterations, statistics.getAverageCreatedPerDay());

        Assertions.assertEquals(iterations, statistics.getVisited().getThisWeek());
        Assertions.assertEquals(iterations, statistics.getVisited().getThisMonth());
        Assertions.assertEquals(iterations, statistics.getVisited().getLastHour());
        Assertions.assertEquals(0, statistics.getVisited().getYesterday());
        Assertions.assertEquals(iterations, statistics.getTotalVisited());
        Assertions.assertEquals(iterations, statistics.getAverageVisitedPerDay());
    }
}
