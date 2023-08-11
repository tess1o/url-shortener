package com.chalyi.urlshortener.services.crud;

import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;

/**
 * A service to create a short url. The request might come from different sources - Rest API, GraphQL, gRPC
 */
public interface ShortUrlCreateService {

    //TODO: catch an error if something is wrong

    /**
     * This method should create a new short-url and setup all required keys in redis,
     * including the keys used for statistics. Delete token is stored in the short's url hash
     * and it should never be returned to end user.
     *
     * @param request with parameters to create a short url for given original url
     * @return a response with short-url and delete-token
     */
    CreateShortUrlResponse create(CreateShortUrlRequest request);
}
