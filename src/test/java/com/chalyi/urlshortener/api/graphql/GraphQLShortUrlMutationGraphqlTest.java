package com.chalyi.urlshortener.api.graphql;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.SpringBootTestWithDirtyContext;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.types.errors.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@SpringBootTestWithDirtyContext
public class GraphQLShortUrlMutationGraphqlTest extends BaseTest {

    @Autowired
    DgsQueryExecutor dgsQueryExecutor;

    @SpyBean
    private ShortUrlCreateService createService;

    @AfterEach
    public void cleanup() {
        flushAll();
    }

    @Test
    void createShortUrlTest() {

        CreateShortUrlResponse createShortUrlResponse = new CreateShortUrlResponse(
                "some-short-url-here",
                "some-delete-token-here"
        );

        doReturn(createShortUrlResponse).when(createService).create(any());

        Map<String, String> results = dgsQueryExecutor.executeAndExtractJsonPath(
                """
                            mutation {
                            create(request: { originalUrl: "https://ukr.net" }) {
                              shortUrl
                              deleteToken
                            }
                        }
                        """,
                "data.create");

        Assertions.assertEquals(createShortUrlResponse.getShortUrl(), results.get("shortUrl"));
        Assertions.assertEquals(createShortUrlResponse.getDeleteToken(), results.get("deleteToken"));
    }

    @Test
    void createShortUrl_WithExpireTest() {

        CreateShortUrlResponse createShortUrlResponse = new CreateShortUrlResponse(
                "some-short-url-here",
                "some-delete-token-here"
        );

        doReturn(createShortUrlResponse).when(createService).create(any());

        Map<String, String> results = dgsQueryExecutor.executeAndExtractJsonPath(
                """
                            mutation {
                            create(request: { originalUrl: "https://ukr.net", expire: 200 }) {
                              shortUrl
                              deleteToken
                            }
                        }
                        """,
                "data.create");

        Assertions.assertEquals(createShortUrlResponse.getShortUrl(), results.get("shortUrl"));
        Assertions.assertEquals(createShortUrlResponse.getDeleteToken(), results.get("deleteToken"));
    }

    @Test
    void createShortUrl_UnexpectedExceptionTest() {

        doThrow(NullPointerException.class).when(createService).create(any());

        ExecutionResult results = dgsQueryExecutor.execute(
                """
                            mutation {
                            create(request: { originalUrl: "https://ukr.net" }) {
                              shortUrl
                              deleteToken
                            }
                        }
                        """);

        Assertions.assertEquals(1,results.getErrors().size());
        ExceptionWhileDataFetching error = (ExceptionWhileDataFetching) results.getErrors().get(0);
        Assertions.assertTrue(error.getMessage().contains("Exception while fetching data (/create)"));
        Assertions.assertEquals(NullPointerException.class, error.getException().getClass());
    }


    @Test
    void createShortUrl_WrongUrlTest() {
        ExecutionResult results = dgsQueryExecutor.execute(
                """
                            mutation {
                            create(request: { originalUrl: "not-an-url" }) {
                              shortUrl
                              deleteToken
                            }
                        }
                        """);

        Assertions.assertNotNull(results.getErrors());
        Assertions.assertEquals(1, results.getErrors().size());
        GraphQLError error = results.getErrors().get(0);
        Assertions.assertEquals("The `originalUrl` parameter should be a valid URL", error.getMessage());
    }

    @Test
    void deleteShortUrlTest() throws UnknownHostException {
        CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                "http://test.com",
                0,
                "testUserAgent",
                InetAddress.getByName("10.10.0.0")
        );

        CreateShortUrlResponse response = createService.create(createShortUrlRequest);
        Assertions.assertNotNull(response);

        String graphqlResponse = dgsQueryExecutor.executeAndExtractJsonPath(
                """                     
                        mutation($shortUrl: String!, $deleteToken: String!) {
                          delete(request: { shortUrl: $shortUrl, deleteToken: $deleteToken }) {
                            response
                          }
                        }
                                            
                        """,
                "data.delete.response",
                Map.of("shortUrl", response.getShortUrl(),
                        "deleteToken", response.getDeleteToken())
        );
        Assertions.assertEquals("Deleted", graphqlResponse);
    }

    @Test
    void deleteShortUrl_nonExistingUrlTest() {
        ExecutionResult graphqlResponse = dgsQueryExecutor.execute(
                """                     
                        mutation($shortUrl: String!, $deleteToken: String!) {
                          delete(request: { shortUrl: $shortUrl, deleteToken: $deleteToken }) {
                            response
                          }
                        }
                                            
                        """,
                Map.of("shortUrl", "non-existing-url",
                        "deleteToken", "random-delete-token")
        );
        Assertions.assertEquals(1, graphqlResponse.getErrors().size());
        GraphQLError error = graphqlResponse.getErrors().get(0);
        Assertions.assertEquals(ErrorType.NOT_FOUND.name(), error.getExtensions().get("errorType"));
        Assertions.assertTrue(error.getMessage().contains("non-existing-url"));
    }

    @Test
    void deleteShortUrl_wrongDeleteToken() throws UnknownHostException {
        CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                "http://test.com",
                0,
                "testUserAgent",
                InetAddress.getByName("10.10.0.0")
        );

        CreateShortUrlResponse response = createService.create(createShortUrlRequest);
        Assertions.assertNotNull(response);
        ExecutionResult graphqlResponse = dgsQueryExecutor.execute(
                """                     
                        mutation($shortUrl: String!, $deleteToken: String!) {
                          delete(request: { shortUrl: $shortUrl, deleteToken: $deleteToken }) {
                            response
                          }
                        }
                                            
                        """,
                Map.of("shortUrl", response.getShortUrl(),
                        "deleteToken", "random-delete-token")
        );
        Assertions.assertEquals(1, graphqlResponse.getErrors().size());
        GraphQLError error = graphqlResponse.getErrors().get(0);
        Assertions.assertEquals(ErrorType.BAD_REQUEST.name(), error.getExtensions().get("errorType"));
        Assertions.assertEquals("Wrong deleteToken", error.getMessage());
    }
}
