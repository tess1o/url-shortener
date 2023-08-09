package com.chalyi.urlshortener.api.grpc.converters;

import com.google.protobuf.Timestamp;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class LocalDateTimeToTimestampConverter implements Converter<LocalDateTime, Timestamp> {
    @Override
    public Timestamp convert(LocalDateTime source) {
        return Timestamp.newBuilder().setSeconds(source.toEpochSecond(ZoneOffset.UTC)).build();
    }
}
