package com.example.graphql.client;

public class GraphQlClientException extends RuntimeException {

    public GraphQlClientException(String message) {
        super(message);
    }

    public GraphQlClientException(String message, Throwable cause) {
        super(message, cause);
    }
}