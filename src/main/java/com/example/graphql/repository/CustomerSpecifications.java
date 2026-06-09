package com.example.graphql.repository;

import com.example.graphql.domain.Customer;
import com.example.graphql.domain.CustomerType;
import org.springframework.data.jpa.domain.Specification;

public final class CustomerSpecifications {

    private CustomerSpecifications() {}

    public static Specification<Customer> hasType(CustomerType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Customer> entityNameContains(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isBlank()) return null;
            return cb.like(cb.lower(root.get("entityName")), "%" + term.toLowerCase() + "%");
        };
    }

    public static Specification<Customer> hasId(Long id) {
        return (root, query, cb) -> id == null ? null : cb.equal(root.get("id"), id);
    }
}
