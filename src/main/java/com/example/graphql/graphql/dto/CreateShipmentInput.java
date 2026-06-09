package com.example.graphql.graphql.dto;

import com.example.graphql.domain.ActivityType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Mutation input for creating a shipment. References are by ID (typical in GraphQL).
 */
public record CreateShipmentInput(
        @NotNull Long customerId,
        Long contactId,
        Long shipmentLocationId,
        Long serviceOfferingId,
        String itemDescription,
        BigDecimal weight,
        String dimensions,
        @NotNull ActivityType activity
) {}
