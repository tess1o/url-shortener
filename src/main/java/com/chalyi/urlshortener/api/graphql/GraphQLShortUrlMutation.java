package com.chalyi.urlshortener.api.graphql;

import com.chalyi.urlshortener.graphql.GraphQLShortUrlCreateRequest;
import com.chalyi.urlshortener.graphql.GraphQLShortUrlCreateResponse;
import com.chalyi.urlshortener.graphql.GraphQLShortUrlDeleteRequest;
import com.chalyi.urlshortener.graphql.GraphQLShortUrlDeleteResponse;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.chalyi.urlshortener.services.crud.ShortUrlDeleteService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;

@DgsComponent
@RequiredArgsConstructor
@Slf4j
public class GraphQLShortUrlMutation {

    private final ShortUrlCreateService createService;
    private final ShortUrlDeleteService deleteService;

    @DgsMutation
    public GraphQLShortUrlCreateResponse create(@InputArgument GraphQLShortUrlCreateRequest request,
                                                @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        int expire = request.getExpire() == null ? 0 : request.getExpire();

        CreateShortUrlRequest createRequest = new CreateShortUrlRequest(
                request.getOriginalUrl(),
                expire,
                userAgent
        );
        CreateShortUrlResponse response = createService.create(createRequest);
        return GraphQLShortUrlCreateResponse.builder()
                .setShortUrl(response.getShortUrl())
                .setDeleteToken(response.getDeleteToken())
                .build();
    }

    @DgsMutation
    public GraphQLShortUrlDeleteResponse delete(GraphQLShortUrlDeleteRequest request) {
        deleteService.delete(request.getShortUrl(), request.getDeleteToken());
        return GraphQLShortUrlDeleteResponse.builder()
                .setResponse("Deleted")
                .build();
    }
}
