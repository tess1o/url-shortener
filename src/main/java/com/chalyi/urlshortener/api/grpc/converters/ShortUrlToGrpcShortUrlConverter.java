package com.chalyi.urlshortener.api.grpc.converters;

import com.chalyi.urlshortener.model.ShortUrl;
import com.google.protobuf.Timestamp;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class ShortUrlToGrpcShortUrlConverter implements Converter<ShortUrl, com.chalyi.urlshortener.grpc.ShortUrl> {
    @Override
    public com.chalyi.urlshortener.grpc.ShortUrl convert(ShortUrl shortUrl) {
        return com.chalyi.urlshortener.grpc.ShortUrl.newBuilder()
                .setShortUrl(shortUrl.shortUrl())
                .setOriginalUrl(shortUrl.originalUrl())
                .setVisitors(shortUrl.visitors())
                .setUniqueVisitors(shortUrl.uniqueVisitors())
                .addAllUserAgents(shortUrl.userAgents())
                .setCreated(Timestamp.newBuilder().setSeconds(shortUrl.created().toEpochSecond(ZoneOffset.UTC)).build())
                .setExpire(Timestamp.newBuilder().setSeconds(shortUrl.expire().toEpochSecond(ZoneOffset.UTC)).build())
                .build();
    }
}
