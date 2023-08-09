package com.chalyi.urlshortener.converters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public interface EpochMilliConverter {

    long localDateToEpochMilli(LocalDate date, ZoneId zoneId);

    long localDateTimeToEpochMilli(LocalDateTime date, ZoneId zoneId);

    default long localDateToEpochMilli(LocalDate date) {
        return localDateToEpochMilli(date, ZoneId.systemDefault());
    }

    default long localDateTimeToEpochMilli(LocalDateTime date) {
        return localDateTimeToEpochMilli(date, ZoneId.systemDefault());
    }
}
