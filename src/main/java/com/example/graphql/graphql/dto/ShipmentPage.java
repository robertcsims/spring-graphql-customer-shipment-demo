package com.example.graphql.graphql.dto;

import com.example.graphql.domain.Shipment;
import org.springframework.data.domain.Page;

import java.util.List;

public record ShipmentPage(List<Shipment> content, long totalElements, int totalPages) {

    public static ShipmentPage from(Page<Shipment> page) {
        return new ShipmentPage(page.getContent(), page.getTotalElements(), page.getTotalPages());
    }
}