package com.example.graphql.client;

public record GraphQlClientOperationResult(
        String operation,
        boolean success,
        String summary
) {
    public static GraphQlClientOperationResult ok(String operation, String summary) {
        return new GraphQlClientOperationResult(operation, true, summary);
    }

    public static GraphQlClientOperationResult failed(String operation, String summary) {
        return new GraphQlClientOperationResult(operation, false, summary);
    }
}