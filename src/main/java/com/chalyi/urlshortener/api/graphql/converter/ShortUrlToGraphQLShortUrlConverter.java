package com.chalyi.urlshortener.api.graphql.converter;

import com.chalyi.urlshortener.graphql.GraphQLShortUrl;
import com.chalyi.urlshortener.model.ShortUrl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.ArrayList;

@Component
public class ShortUrlToGraphQLShortUrlConverter implements Converter<ShortUrl, GraphQLShortUrl> {
    @Override
    public GraphQLShortUrl convert(ShortUrl shortUrl) {
        return GraphQLShortUrl.builder()
                .setOriginalUrl(shortUrl.originalUrl())
                .setShortUrl(shortUrl.shortUrl())
                .setCreated(shortUrl.created().atOffset(ZoneOffset.UTC))
                .setExpire(shortUrl.expire().atOffset(ZoneOffset.UTC))
                .setVisitors(Long.valueOf(shortUrl.visitors()).intValue())
                .setUniqueVisitors(Long.valueOf(shortUrl.uniqueVisitors()).intValue())
                .setUserAgents(new ArrayList<>(shortUrl.userAgents()))
                .build();
    }
}
