package com.chalyi.urlshortener.converters;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class EpochMilliConverterImpl implements EpochMilliConverter {
    @Override
    public long localDateToEpochMilli(LocalDate date, ZoneId zoneId) {
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli();
    }

    @Override
    public long localDateTimeToEpochMilli(LocalDateTime date, ZoneId zoneId) {
        return date.atZone(zoneId).toInstant().toEpochMilli();
    }
}
