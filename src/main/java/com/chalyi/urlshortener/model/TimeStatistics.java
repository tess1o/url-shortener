package com.chalyi.urlshortener.model;

import lombok.Builder;

@Builder
public record TimeStatistics(
        int lastWeek,
        int thisWeek,
        int lastMonth,
        int thisMonth,
        int lastHour,
        int today,
        int yesterday) {
}
