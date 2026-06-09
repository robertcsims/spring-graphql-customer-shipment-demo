package com.example.graphql.graphql.dto;

import com.example.graphql.domain.ActivityType;

import java.math.BigDecimal;

/**
 * Flexible filter for shipments (demonstrates GraphQL power for complex criteria).
 */
public record ShipmentFilter(
        Long customerId,
        ActivityType activity,
        BigDecimal minWeight,
        String itemDescriptionContains
) {}
