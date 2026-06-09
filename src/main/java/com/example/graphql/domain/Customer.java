package com.example.graphql.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity-name", nullable = false)
    private String entityName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CustomerType type;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Contact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ShipmentLocation> shipmentLocations = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Shipment> shipments = new ArrayList<>();

    protected Customer() {
    }

    public Customer(String entityName, CustomerType type) {
        this.entityName = entityName;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public CustomerType getType() {
        return type;
    }

    public void setType(CustomerType type) {
        this.type = type;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
        contact.setCustomer(this);
    }

    public List<ShipmentLocation> getShipmentLocations() {
        return shipmentLocations;
    }

    public void addShipmentLocation(ShipmentLocation location) {
        shipmentLocations.add(location);
        location.setCustomer(this);
    }

    public List<Shipment> getShipments() {
        return shipments;
    }

    public void addShipment(Shipment shipment) {
        shipments.add(shipment);
        shipment.setCustomer(this);
    }

    // Convenience helpers for tests / seeding
    public void setId(Long id) {
        this.id = id;
    }
}
