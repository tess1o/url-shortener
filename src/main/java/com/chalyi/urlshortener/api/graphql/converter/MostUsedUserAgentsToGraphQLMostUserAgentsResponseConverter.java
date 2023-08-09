package com.chalyi.urlshortener.api.graphql.converter;

import com.chalyi.urlshortener.graphql.GraphQLMostUserAgentsResponse;
import com.chalyi.urlshortener.model.responses.MostUsedUserAgents;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MostUsedUserAgentsToGraphQLMostUserAgentsResponseConverter
        implements Converter<MostUsedUserAgents, GraphQLMostUserAgentsResponse> {
    @Override
    public GraphQLMostUserAgentsResponse convert(MostUsedUserAgents source) {
        return GraphQLMostUserAgentsResponse.builder()
                .setUserAgent(source.userAgent())
                .setOccurrences(source.occurrences())
                .build();
    }
}
