package com.chalyi.urlshortener.api.graphql;

import com.chalyi.urlshortener.graphql.GraphQLShortUrlCreateRequest;
import com.chalyi.urlshortener.graphql.GraphQLShortUrlCreateResponse;
import com.chalyi.urlshortener.graphql.GraphQLShortUrlDeleteRequest;
import com.chalyi.urlshortener.graphql.GraphQLShortUrlDeleteResponse;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.chalyi.urlshortener.services.crud.ShortUrlDeleteService;
import com.chalyi.urlshortener.services.net.HttpRequestIpAddressService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.context.request.ServletWebRequest;

@DgsComponent
@RequiredArgsConstructor
@Slf4j
public class GraphQLShortUrlMutation {

    private final ShortUrlCreateService createService;
    private final ShortUrlDeleteService deleteService;
    private final HttpRequestIpAddressService httpRequestIpAddressService;

    @DgsMutation
    public GraphQLShortUrlCreateResponse create(@InputArgument GraphQLShortUrlCreateRequest request,
                                                @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
                                                DgsDataFetchingEnvironment dfe) {
        int expire = request.getExpire() == null ? 0 : request.getExpire();

        HttpServletRequest httpServletRequest = getHttpServletRequest(dfe);

        CreateShortUrlRequest createRequest = new CreateShortUrlRequest(
                request.getOriginalUrl(),
                expire,
                userAgent,
                httpRequestIpAddressService.getIpAddress(httpServletRequest)
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

    @NotNull
    private HttpServletRequest getHttpServletRequest(DgsDataFetchingEnvironment dfe) {
        DgsWebMvcRequestData requestData = (DgsWebMvcRequestData) dfe.getDgsContext().getRequestData();
        ServletWebRequest webRequest = (ServletWebRequest) requestData.getWebRequest();
        return webRequest.getRequest();
    }
}
