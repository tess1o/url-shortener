package com.chalyi.urlshortener.services.statistics;

import com.chalyi.urlshortener.model.OverallStatistics;
import com.chalyi.urlshortener.model.ShortUrl;
import com.chalyi.urlshortener.model.ShortUrlWithStatistics;
import com.chalyi.urlshortener.model.TimeStatistics;
import com.chalyi.urlshortener.model.responses.MostUsedUserAgents;
import com.chalyi.urlshortener.services.RedisUrlKeys;
import com.chalyi.urlshortener.services.crud.ShortUrlInfoService;
import com.chalyi.urlshortener.services.statistics.time.series.TimeSeriesStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.SortingParams;
import redis.clients.jedis.resps.Tuple;
import redis.clients.jedis.timeseries.AggregationType;
import redis.clients.jedis.timeseries.TSInfo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final int ONE_DAY_MILLISECONDS = 1000 * 24 * 3600;
    private static final int NUMBER_OF_FIELDS_IN_SORT = 5;

    @Value("${short-url.statistics.defaultMostViewed}")
    private int defaultMostViewed;

    @Value("${short-url.statistics.defaultMostUsedAgents}")
    private int defaultMostUsedAgents;

    private final RedisUrlKeys redisUrlKeys;
    private final UnifiedJedis unifiedJedis;
    private final ShortUrlInfoService shortUrlInfoService;
    private final TimeSeriesStatisticsService timeSeriesStatisticsService;

    @Override
    public List<ShortUrl> getMostViewed(int count) {
        SortingParams params = new SortingParams()
                .nosort()
                .desc()
                .limit(0, count)
                .get("#", "url#*->originalUrl", "url#*->visitors", "url#*->created", "url#*->expire");

        List<String> sort = unifiedJedis.sort(redisUrlKeys.mostViewedUrlSortedSetKey(), params);
        List<Response<Set<String>>> userAgents;
        List<Response<Long>> uniqueVisitors;
        int totalResults;
        try (Pipeline pipeline = (Pipeline) unifiedJedis.pipelined()) {
            userAgents = new ArrayList<>();
            uniqueVisitors = new ArrayList<>();

            totalResults = Math.min(count, sort.size() / NUMBER_OF_FIELDS_IN_SORT);

            for (int i = 0; i < totalResults; i++) {
                var index = i * NUMBER_OF_FIELDS_IN_SORT;
                String shortUrl = sort.get(index);
                userAgents.add(pipeline.smembers(redisUrlKeys.urlUserAgentsSet(shortUrl)));
                uniqueVisitors.add(pipeline.pfcount(redisUrlKeys.getUniqueVisitorsKey(shortUrl)));
            }
        }

        List<ShortUrl> results = new ArrayList<>();

        for (int i = 0; i < totalResults; i++) {
            var index = i * NUMBER_OF_FIELDS_IN_SORT;
            Set<String> userAgentsForUrl = userAgents.get(i).get();
            Long uniqueVisitorsForUrl = uniqueVisitors.get(i).get();
            results.add(toShortUrl(sort.subList(index, index + NUMBER_OF_FIELDS_IN_SORT), userAgentsForUrl, uniqueVisitorsForUrl));
        }
        return results;
    }

    @Override
    public List<MostUsedUserAgents> getMostUsedUserAgents(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }
        List<Tuple> scores = unifiedJedis.zrevrangeWithScores(redisUrlKeys.mostUsedUserAgentsKey(), 0, count - 1);
        return scores.stream()
                .map(s -> new MostUsedUserAgents(s.getElement(), Double.valueOf(s.getScore()).intValue()))
                .collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public OverallStatistics overallTimeStatistics() {
        CompletableFuture<TimeStatistics> created = CompletableFuture.supplyAsync(() ->
                timeSeriesStatisticsService.getTimeStatistics(redisUrlKeys.urlCreatedTimeSeriesKey(), AggregationType.SUM));
        CompletableFuture<TimeStatistics> visited = CompletableFuture.supplyAsync(() ->
                timeSeriesStatisticsService.getTimeStatistics(redisUrlKeys.urlVisitedTimeSeriesKey(), AggregationType.SUM));

        CompletableFuture.allOf(created, visited).get();


        double averageCreatedPerDay = getAverageFromTimeSeries(redisUrlKeys.urlCreatedTimeSeriesKey());
        double averageVisitedPerDay = getAverageFromTimeSeries(redisUrlKeys.urlVisitedTimeSeriesKey());

        List<MostUsedUserAgents> mostUsedUserAgents = this.getMostUsedUserAgents(defaultMostUsedAgents);
        List<ShortUrl> mostViewed = this.getMostViewed(defaultMostViewed);

        return OverallStatistics.builder()
                .totalCreated(getLongSafe(redisUrlKeys.getTotalCountKey()))
                .totalVisited(getLongSafe(redisUrlKeys.getTotalVisitedCountKey()))
                .visited(visited.get())
                .created(created.get())
                .averageCreatedPerDay(averageCreatedPerDay)
                .averageVisitedPerDay(averageVisitedPerDay)
                .mostUsedUserAgents(mostUsedUserAgents)
                .mostViewed(mostViewed)
                .build();
    }

    private double getAverageFromTimeSeries(String key) {
        TSInfo info = unifiedJedis.tsInfo(key);

        if (info == null || info.getIntegerProperty("firstTimestamp") == 0) {
            return 0.0d;
        }

        long totalDays = (info.getIntegerProperty("lastTimestamp") - info.getIntegerProperty("firstTimestamp")) / (ONE_DAY_MILLISECONDS);
        if (totalDays == 0) {
            totalDays = 1;
        }
        return info.getIntegerProperty("totalSamples") * 1.0 / totalDays;
    }

    private long getLongSafe(String key) {
        String value = unifiedJedis.get(key);
        if (value == null || value.isEmpty()) {
            return 0L;
        }
        return Long.parseLong(unifiedJedis.get(key));
    }

    @Override
    public ShortUrlWithStatistics getShortUrlWithStatistics(String shortUrl) {
        ShortUrl shortUrlFromRedis = shortUrlInfoService.info(shortUrl);

        List<String> lastVisited = unifiedJedis.lrange(redisUrlKeys.urlLastVisitedKey(shortUrl), -10, -1);

        List<LocalDateTime> lastVisitedTime = lastVisited
                .stream()
                .map(l -> Instant.ofEpochMilli(Long.parseLong(l)).atZone(ZoneId.systemDefault()).toLocalDateTime())
                .collect(Collectors.toList());

        return ShortUrlWithStatistics.builder()
                .shortUrl(shortUrlFromRedis)
                .lastVisitedTime(lastVisitedTime)
                .visitedStatistics(timeSeriesStatisticsService.getTimeStatistics(redisUrlKeys.urlVisitedTimeSeriesKey(shortUrl), AggregationType.SUM))
                .build();
    }

    private ShortUrl toShortUrl(List<String> sortResults, Set<String> userAgents, long uniqueVisitors) {
        return ShortUrl.builder()
                .shortUrl(sortResults.get(0))
                .originalUrl(sortResults.get(1))
                .visitors(Long.parseLong(sortResults.get(2)))
                .created(LocalDateTime.parse(sortResults.get(3), DateTimeFormatter.ISO_DATE_TIME))
                .expire(LocalDateTime.parse(sortResults.get(4), DateTimeFormatter.ISO_DATE_TIME))
                .userAgents(userAgents)
                .uniqueVisitors(uniqueVisitors)
                .build();

    }
}
