package com.chalyi.urlshortener.api.graphql;

import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.exceptions.WrongDeleteTokenException;
import com.netflix.graphql.types.errors.ErrorType;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class GraphQLErrorHandling implements DataFetcherExceptionHandler {

    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(DataFetcherExceptionHandlerParameters handlerParameters) {
        if (handlerParameters.getException() instanceof WrongDeleteTokenException) {
            return handleWrongDeleteTokenException(handlerParameters);
        }

        if (handlerParameters.getException() instanceof NoSuchUrlFound) {
            return handleNoSuchUrlFound(handlerParameters);
        }

        return DataFetcherExceptionHandler.super.handleException(handlerParameters);

    }

    private CompletableFuture<DataFetcherExceptionHandlerResult> handleNoSuchUrlFound(DataFetcherExceptionHandlerParameters handlerParameters) {
        Map<String, Object> debugInfo = (Map<String, Object>) handlerParameters.getArgumentValues().get("request");

        GraphQLError graphqlError = TypedGraphQLError.newInternalErrorBuilder()
                .message("shortUrl `" + debugInfo.get("shortUrl") + "` is not found")
                .debugInfo(debugInfo)
                .errorType(ErrorType.NOT_FOUND)
                .path(handlerParameters.getPath()).build();

        DataFetcherExceptionHandlerResult result = DataFetcherExceptionHandlerResult.newResult()
                .error(graphqlError)
                .build();

        return CompletableFuture.completedFuture(result);
    }

    @NotNull
    private CompletableFuture<DataFetcherExceptionHandlerResult> handleWrongDeleteTokenException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Map<String, Object> debugInfo = (Map<String, Object>) handlerParameters.getArgumentValues().get("request");

        GraphQLError graphqlError = TypedGraphQLError.newInternalErrorBuilder()
                .message("Wrong deleteToken")
                .debugInfo(debugInfo)
                .errorType(ErrorType.BAD_REQUEST)
                .path(handlerParameters.getPath()).build();

        DataFetcherExceptionHandlerResult result = DataFetcherExceptionHandlerResult.newResult()
                .error(graphqlError)
                .build();

        return CompletableFuture.completedFuture(result);
    }
}
