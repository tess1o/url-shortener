package com.chalyi.urlshortener.api.grpc.converters;

import com.chalyi.urlshortener.grpc.MostUserAgentsResponse;
import com.chalyi.urlshortener.model.responses.MostUsedUserAgents;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MostUserAgentsToMostUserAgentsResponseConverter implements Converter<MostUsedUserAgents, MostUserAgentsResponse> {
    @Override
    public MostUserAgentsResponse convert(MostUsedUserAgents source) {
        return MostUserAgentsResponse.newBuilder()
                .setUserAgent(source.userAgent())
                .setOccurrences(source.occurrences())
                .build();
    }
}
