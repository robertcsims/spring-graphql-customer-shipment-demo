package com.example.graphql.graphql.dto;

import com.example.graphql.domain.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Input for creating a customer via GraphQL mutation.
 */
public record CreateCustomerInput(
        @NotBlank String entityName,
        @NotNull CustomerType type
) {}
