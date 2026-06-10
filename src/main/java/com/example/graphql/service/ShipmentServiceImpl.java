package com.example.graphql.service;

import com.example.graphql.domain.*;
import com.example.graphql.repository.*;
import com.example.graphql.repository.ShipmentSpecifications;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final CustomerRepository customerRepository;
    private final ContactRepository contactRepository;
    private final ShipmentLocationRepository locationRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;

    public ShipmentServiceImpl(ShipmentRepository shipmentRepository,
                               CustomerRepository customerRepository,
                               ContactRepository contactRepository,
                               ShipmentLocationRepository locationRepository,
                               ServiceOfferingRepository serviceOfferingRepository) {
        this.shipmentRepository = shipmentRepository;
        this.customerRepository = customerRepository;
        this.contactRepository = contactRepository;
        this.locationRepository = locationRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
    }

    @Override
    @Transactional
    public Shipment createShipment(Long customerId, Long contactId, Long locationId, Long serviceOfferingId,
                                   String itemDescription, BigDecimal weight, String dimensions, ActivityType activity) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        Shipment shipment = new Shipment(itemDescription, weight, dimensions, activity);
        shipment.setCustomer(customer);

        if (contactId != null) {
            Contact contact = contactRepository.findById(contactId)
                    .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + contactId));
            shipment.setContact(contact);
        }
        if (locationId != null) {
            ShipmentLocation loc = locationRepository.findById(locationId)
                    .orElseThrow(() -> new IllegalArgumentException("ShipmentLocation not found: " + locationId));
            shipment.setShipmentLocation(loc);
        }
        if (serviceOfferingId != null) {
            ServiceOffering offering = serviceOfferingRepository.findById(serviceOfferingId)
                    .orElseThrow(() -> new IllegalArgumentException("ServiceOffering not found: " + serviceOfferingId));
            shipment.setServiceOffering(offering);
        }

        customer.addShipment(shipment);
        Shipment saved = shipmentRepository.save(shipment);
        initializeShipmentGraph(saved);
        return saved;
    }

    @Override
    public Optional<Shipment> getShipmentById(Long id) {
        return shipmentRepository.findById(id);
    }

    @Override
    public List<Shipment> getShipmentsByCustomer(Long customerId) {
        return shipmentRepository.findByCustomerId(customerId);
    }

    @Override
    public Page<Shipment> getShipmentsFiltered(Long customerId, ActivityType activity, BigDecimal minWeight,
                                               String itemDescriptionContains, Pageable pageable) {
        Specification<Shipment> spec = Specification.where(ShipmentSpecifications.hasCustomerId(customerId))
                .and(ShipmentSpecifications.hasActivity(activity))
                .and(ShipmentSpecifications.weightGreaterThanOrEqual(minWeight))
                .and(ShipmentSpecifications.itemDescriptionContains(itemDescriptionContains));
        return shipmentRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public Shipment updateShipment(Long id, String itemDescription, BigDecimal weight, String dimensions, ActivityType activity) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + id));
        if (itemDescription != null) shipment.setItemDescription(itemDescription);
        if (weight != null) shipment.setWeight(weight);
        if (dimensions != null) shipment.setDimensions(dimensions);
        if (activity != null) shipment.setActivity(activity);
        return shipmentRepository.save(shipment);
    }

    @Override
    @Transactional
    public void deleteShipment(Long id) {
        shipmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public void initializeShipmentGraph(Shipment shipment) {
        if (shipment == null) {
            return;
        }
        Hibernate.initialize(shipment.getCustomer());
        Hibernate.initialize(shipment.getContact());
        Hibernate.initialize(shipment.getShipmentLocation());
        Hibernate.initialize(shipment.getServiceOffering());
    }
}
