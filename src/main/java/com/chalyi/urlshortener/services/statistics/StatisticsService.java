package com.chalyi.urlshortener.services.statistics;

import com.chalyi.urlshortener.model.OverallStatistics;
import com.chalyi.urlshortener.model.ShortUrl;
import com.chalyi.urlshortener.model.ShortUrlWithStatistics;
import com.chalyi.urlshortener.model.responses.MostUsedUserAgents;

import java.util.List;

public interface StatisticsService {

    List<ShortUrl> getMostViewed(int count);

    List<MostUsedUserAgents> getMostUsedUserAgents(int count);

    OverallStatistics overallTimeStatistics();

    ShortUrlWithStatistics getShortUrlWithStatistics(String shortUrl);
}
