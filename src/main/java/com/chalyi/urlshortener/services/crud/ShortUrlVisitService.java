package com.chalyi.urlshortener.services.crud;

import com.chalyi.urlshortener.model.requests.VisitShortUrlRequest;

/**
 * This is the service to visit (redirect) to the original url.
 * It makes sense to use it for REST API controllers only
 */
public interface ShortUrlVisitService {

    /**
     * A method to visit an original url by providing corresponding short url. If short url can't be found then NoSuchUrlFound exception is thrown.
     * It could be thrown when either short url is wrong or it's already expired.
     * When valid short url is found then additional statistics should be calculated:
     * 1. Url's user agents - the list of user agents visited the given url
     * 2. Most used visitors user agents
     * 3. Total number of visited urls (global)
     * 4. Unique visitors for given url (based on IP address)
     * 5. Url visitors time series
     * 6. Overall visitors time series
     * 7. Timestamps for last 10 visitors
     *
     * @throws com.chalyi.urlshortener.exceptions.NoSuchUrlFound when no original url is found for given short url.
     * @param request - the visit request with short url, client's ip address and user agent. This data is used for statistics purpose
     * @return the original url if it exists
     */
    String visitShortUrl(VisitShortUrlRequest request);
}
