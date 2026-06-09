package com.example.graphql.graphql.dto;

import com.example.graphql.domain.CustomerType;

/**
 * Input filter for customer queries. All fields optional.
 */
public record CustomerFilter(
        CustomerType type,
        String entityNameContains
) {}
