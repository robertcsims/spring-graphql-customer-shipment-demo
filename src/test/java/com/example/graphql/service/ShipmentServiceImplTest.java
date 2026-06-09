package com.example.graphql.service;

import com.example.graphql.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShipmentServiceImplTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ServiceOfferingService serviceOfferingService;

    private Long customerId;
    private Long serviceOfferingId;

    @BeforeEach
    void setup() {
        Customer c = customerService.createCustomer("Ship Test Co", CustomerType.BUSINESS);
        this.customerId = c.getId();

        ServiceOffering offering = serviceOfferingService.create("Ground Standard", "STD");
        this.serviceOfferingId = offering.getId();
    }

    @Test
    void createShipment_withMinimalRefs_shouldPersistHierarchy() {
        Shipment s = shipmentService.createShipment(
                customerId, null, null, serviceOfferingId,
                "Laptop", new BigDecimal("4.5"), "18x12x4", ActivityType.PICKUP);

        assertThat(s.getId()).isNotNull();
        assertThat(s.getCustomer().getId()).isEqualTo(customerId);
        assertThat(s.getActivity()).isEqualTo(ActivityType.PICKUP);
        assertThat(s.getServiceOffering().getTypeCd()).isEqualTo("STD");
    }

    @Test
    void getShipmentsFiltered_byActivityAndCustomer() {
        shipmentService.createShipment(customerId, null, null, serviceOfferingId,
                "Box A", new BigDecimal("10"), null, ActivityType.PICKUP);
        shipmentService.createShipment(customerId, null, null, serviceOfferingId,
                "Box B", new BigDecimal("3"), null, ActivityType.DELIVERY);

        Page<Shipment> pickups = shipmentService.getShipmentsFiltered(
                customerId, ActivityType.PICKUP, null, null, PageRequest.of(0, 10));

        assertThat(pickups.getContent()).hasSize(1);
        assertThat(pickups.getContent().get(0).getItemDescription()).isEqualTo("Box A");
    }

    @Test
    void getShipmentsByCustomer_returnsAllForCustomer() {
        shipmentService.createShipment(customerId, null, null, serviceOfferingId, "Item1", null, null, ActivityType.PICKUP);
        shipmentService.createShipment(customerId, null, null, serviceOfferingId, "Item2", null, null, ActivityType.DELIVERY);

        List<Shipment> shipments = shipmentService.getShipmentsByCustomer(customerId);
        assertThat(shipments).hasSize(2);
    }

    @Test
    void updateShipment_modifiesFields() {
        Shipment s = shipmentService.createShipment(customerId, null, null, serviceOfferingId,
                "Old Item", new BigDecimal("5"), "10x10x10", ActivityType.PICKUP);

        Shipment updated = shipmentService.updateShipment(s.getId(), "New Item", new BigDecimal("7.25"), "12x12x12", ActivityType.DELIVERY);

        assertThat(updated.getItemDescription()).isEqualTo("New Item");
        assertThat(updated.getWeight()).isEqualByComparingTo("7.25");
        assertThat(updated.getActivity()).isEqualTo(ActivityType.DELIVERY);
    }
}
