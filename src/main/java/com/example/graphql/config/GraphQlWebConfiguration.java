package com.example.graphql.config;

/**
 * GraphQL HTTP handling is provided by Spring for GraphQL ({@code spring.graphql.path=/graphql}).
 * Resolvers in {@link com.example.graphql.graphql.resolver.CustomerGraphQlResolver} delegate to
 * JPA services backed by embedded Derby.
 */
public class GraphQlWebConfiguration {}