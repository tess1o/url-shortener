package com.chalyi.urlshortener.services;

import org.springframework.stereotype.Service;

@Service
public class RedisUrlKeys {

    public String urlHashKey(String shortUrl) {
        return "url#" + shortUrl;
    }

    public String mostUsedUserAgentsKey() {
        return "most_used_user_agents";
    }

    public String mostViewedUrlSortedSetKey() {
        return "url:most_viewed";
    }

    public String getTotalCountKey() {
        return "total_urls";
    }

    public String getTotalVisitedCountKey(){
        return "total_visited_urls_count";
    }

    public String getUniqueVisitorsKey(String shortUrl) {
        return "url:unique_visitors#" + shortUrl;
    }

    public String urlUserAgentsSet(String shortUrl) {
        return "url:user_agents#" + shortUrl;
    }

    public String urlCreatedTimeSeriesKey() {
        return "url:created";
    }

    public String urlVisitedTimeSeriesKey() {
        return "url:visited";
    }

    public String urlVisitedTimeSeriesKey(String shortUrl) {
        return "url:visited#" + shortUrl;
    }

    public String urlLastVisitedKey(String shortUrl) {
        return "url:last_visited#" + shortUrl;
    }

    public String urlCreatedTimeSeries1DayKey() {
        return "url:created:1_day";
    }

    public String urlVisitedTimeSeries1DayKey() {
        return "url:visited:1_day";
    }
}
