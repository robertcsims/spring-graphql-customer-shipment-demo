package com.example.graphql.graphql.dto;

import com.example.graphql.domain.Customer;
import org.springframework.data.domain.Page;

import java.util.List;

public record CustomerPage(List<Customer> content, long totalElements, int totalPages) {

    public static CustomerPage from(Page<Customer> page) {
        return new CustomerPage(page.getContent(), page.getTotalElements(), page.getTotalPages());
    }
}