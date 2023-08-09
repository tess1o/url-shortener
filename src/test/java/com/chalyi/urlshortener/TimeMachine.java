package com.chalyi.urlshortener;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.doReturn;

/**
 * This class is used to set current clock to specific LocalDateTime.
 * So every time LocalDateTime.now(clock) or LocalDate.now(clock) or Instant.now(clock) is used they
 * will return the same date. It's helpful to move time to past and future as required for tests.
 * It's important that classes that are being tested inject Clock bean and use `now` methods with that clock.
 * Otherwise, the mocks won't work
 */
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
