package com.example.graphql.service;

import com.example.graphql.domain.ActivityType;
import com.example.graphql.domain.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ShipmentService {

    Shipment createShipment(Long customerId, Long contactId, Long locationId, Long serviceOfferingId,
                            String itemDescription, BigDecimal weight, String dimensions, ActivityType activity);

    Optional<Shipment> getShipmentById(Long id);

    List<Shipment> getShipmentsByCustomer(Long customerId);

    Page<Shipment> getShipmentsFiltered(Long customerId, ActivityType activity, BigDecimal minWeight,
                                        String itemDescriptionContains, Pageable pageable);

    Shipment updateShipment(Long id, String itemDescription, BigDecimal weight, String dimensions, ActivityType activity);

    void deleteShipment(Long id);
}
