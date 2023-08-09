package com.chalyi.urlshortener;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.doReturn;

@RequiredArgsConstructor
public class TimeMachine {

    private final Clock clock;

    public void setFixedClock(LocalDateTime dateTime) {
        var instant = ZonedDateTime.of(dateTime, ZoneId.systemDefault()).toInstant();
        var fixedClock = Clock.fixed(instant, ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    }

    public void resetClock() {
        var fixedClock = Clock.systemDefaultZone();
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    }
}
