package com.example.graphql.repository;

import com.example.graphql.domain.ActivityType;
import com.example.graphql.domain.Shipment;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ShipmentSpecifications {

    private ShipmentSpecifications() {}

    public static Specification<Shipment> hasCustomerId(Long customerId) {
        return (root, query, cb) -> customerId == null ? null : cb.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Shipment> hasActivity(ActivityType activity) {
        return (root, query, cb) -> activity == null ? null : cb.equal(root.get("activity"), activity);
    }

    public static Specification<Shipment> weightGreaterThanOrEqual(BigDecimal minWeight) {
        return (root, query, cb) -> minWeight == null ? null : cb.greaterThanOrEqualTo(root.get("weight"), minWeight);
    }

    public static Specification<Shipment> itemDescriptionContains(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isBlank()) return null;
            return cb.like(cb.lower(root.get("itemDescription")), "%" + term.toLowerCase() + "%");
        };
    }
}
