package com.example.graphql.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "shipment_location")
public class ShipmentLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address-line-1")
    private String addressLine1;

    @Column(name = "address-line-2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip")
    private String zip;

    @Column(name = "location-instructions", length = 1000)
    private String locationInstructions;

    @Column(name = "gate-code")
    private String gateCode;

    protected ShipmentLocation() {
    }

    public ShipmentLocation(String name, String addressLine1, String addressLine2,
                            String city, String state, String zip,
                            String locationInstructions, String gateCode) {
        this.name = name;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.locationInstructions = locationInstructions;
        this.gateCode = gateCode;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getLocationInstructions() {
        return locationInstructions;
    }

    public void setLocationInstructions(String locationInstructions) {
        this.locationInstructions = locationInstructions;
    }

    public String getGateCode() {
        return gateCode;
    }

    public void setGateCode(String gateCode) {
        this.gateCode = gateCode;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
