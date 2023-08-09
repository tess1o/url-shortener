package com.chalyi.urlshortener.api.graphql.config;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimplePerformantInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationCreateStateParameters;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class TracingInstrumentation extends SimplePerformantInstrumentation {

    @Override
    public @Nullable InstrumentationState createState(InstrumentationCreateStateParameters parameters) {
        return new TracingState();
    }

    @Override
    public @Nullable InstrumentationContext<ExecutionResult> beginExecution(InstrumentationExecutionParameters parameters, InstrumentationState state) {
        TracingState tracingState = (TracingState) state;
        tracingState.startTime = System.currentTimeMillis();
        return super.beginExecution(parameters, tracingState);
    }

    @Override
    public @NotNull DataFetcher<?> instrumentDataFetcher(DataFetcher<?> dataFetcher, InstrumentationFieldFetchParameters parameters, InstrumentationState state) {
        if (parameters.isTrivialDataFetcher()) {
            return dataFetcher;
        }

        return environment -> {
            long startTime = System.currentTimeMillis();
            Object result = dataFetcher.get(environment);
            if (result instanceof CompletableFuture) {
                ((CompletableFuture<?>) result).whenComplete((r, ex) -> {
                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("Async datafetcher {} took {}ms", findDatafetcherTag(parameters), totalTime);
                });
            } else {
                long totalTime = System.currentTimeMillis() - startTime;
                log.info("Datafetcher {} took {}ms", findDatafetcherTag(parameters), totalTime);
            }

            return result;
        };
    }

    @Override
    public @NotNull CompletableFuture<ExecutionResult> instrumentExecutionResult(ExecutionResult executionResult,
                                                                                 InstrumentationExecutionParameters parameters,
                                                                                 InstrumentationState state) {
        TracingState tracingState = (TracingState) state;
        long totalTime = System.currentTimeMillis() - tracingState.startTime;
        log.info("Total execution time: {}ms", totalTime);

        return super.instrumentExecutionResult(executionResult, parameters, state);
    }


    private String findDatafetcherTag(InstrumentationFieldFetchParameters parameters) {
        GraphQLOutputType type = parameters.getExecutionStepInfo().getParent().getType();
        GraphQLObjectType parent;
        if (type instanceof GraphQLNonNull) {
            parent = (GraphQLObjectType) ((GraphQLNonNull) type).getWrappedType();
        } else {
            parent = (GraphQLObjectType) type;
        }

        return parent.getName() + "." + parameters.getExecutionStepInfo().getPath().getSegmentName();
    }

    static class TracingState implements InstrumentationState {
        long startTime;
    }
}
