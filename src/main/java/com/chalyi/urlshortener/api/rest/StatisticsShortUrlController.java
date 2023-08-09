package com.chalyi.urlshortener.api.rest;

import com.chalyi.urlshortener.model.OverallStatistics;
import com.chalyi.urlshortener.model.ShortUrl;
import com.chalyi.urlshortener.model.responses.MostUsedUserAgents;
import com.chalyi.urlshortener.services.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping(value = "/stats", produces = "application/json")
@RestController
public class StatisticsShortUrlController {
    private final StatisticsService statisticsService;

    @RequestMapping(value = "/mostViewed", method = RequestMethod.GET)
    public List<ShortUrl> getMostViewedShortUrls(@RequestParam(value = "count", defaultValue = "${short-url.statistics.defaultMostViewed}") int count) {
        return statisticsService.getMostViewed(count);
    }

    @RequestMapping(value = "/mostUsedAgents", method = RequestMethod.GET)
    public List<MostUsedUserAgents> getMostUsedAgentsUrls(@RequestParam(value = "count", defaultValue = "${short-url.statistics.defaultMostUsedAgents}") int count) {
        return statisticsService.getMostUsedUserAgents(count);
    }

    @RequestMapping(value = "/overall", method = RequestMethod.GET)
    public OverallStatistics getOverallStatistics() {
        return statisticsService.overallTimeStatistics();
    }
}
