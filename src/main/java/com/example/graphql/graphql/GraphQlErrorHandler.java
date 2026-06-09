package com.example.graphql.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Provides clean, user-friendly GraphQL errors instead of leaking stack traces.
 * Very important for a polished leadership demo.
 */
@Component
public class GraphQlErrorHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        // Return a sanitized error message
        return GraphqlErrorBuilder.newError(env)
                .message("GraphQL error: " + ex.getMessage())
                .build();
    }

    @Override
    protected List<GraphQLError> resolveToMultipleErrors(Throwable ex, DataFetchingEnvironment env) {
        return List.of(resolveToSingleError(ex, env));
    }
}
