package com.chalyi.urlshortener.api.graphql.config;

import graphql.GraphQLError;
import graphql.execution.ResultPath;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.fieldvalidation.FieldAndArguments;
import graphql.execution.instrumentation.fieldvalidation.FieldValidationEnvironment;
import graphql.execution.instrumentation.fieldvalidation.FieldValidationInstrumentation;
import graphql.execution.instrumentation.fieldvalidation.SimpleFieldValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

@Slf4j
@Configuration
public class InstrumentationConfig {

    private BiFunction<FieldAndArguments, FieldValidationEnvironment, Optional<GraphQLError>>
    originalUrlIsUrl() {
        return (fieldAndArguments, fieldValidationEnvironment) -> {
            Map<String, Object> request = fieldAndArguments.getArgumentValue("request");
            var originalUrl = (String) request.get("originalUrl");

            return isValidURL(originalUrl)
                    ? Optional.empty()
                    : Optional.of(fieldValidationEnvironment.mkError("The `originalUrl` parameter should be a valid URL"));
        };
    }

    private boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }

        return true;
    }

    @Bean
    Instrumentation originalUrlValidation() {
        var pathAddNewCustomer = ResultPath.parse("/" + "create");
        var fieldValidation = new SimpleFieldValidation();

        fieldValidation.addRule(pathAddNewCustomer, originalUrlIsUrl());

        return new FieldValidationInstrumentation(fieldValidation);
    }
}
