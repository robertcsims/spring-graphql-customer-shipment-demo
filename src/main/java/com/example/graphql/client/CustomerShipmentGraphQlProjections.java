package com.example.graphql.client;

import com.example.graphql.client.generated.model.ContactResponseProjection;
import com.example.graphql.client.generated.model.CustomerPageResponseProjection;
import com.example.graphql.client.generated.model.CustomerResponseProjection;
import com.example.graphql.client.generated.model.ServiceOfferingResponseProjection;
import com.example.graphql.client.generated.model.ShipmentLocationResponseProjection;
import com.example.graphql.client.generated.model.ShipmentPageResponseProjection;
import com.example.graphql.client.generated.model.ShipmentResponseProjection;

import java.util.List;

/**
 * Response projections for the demo operations — mirrors the selections in
 * {@code src/main/resources/graphql-client/operations/*.graphql}.
 */
public final class CustomerShipmentGraphQlProjections {

    private CustomerShipmentGraphQlProjections() {
    }

    public static CustomerPageResponseProjection fullRelationalHierarchy() {
        ContactResponseProjection contact = new ContactResponseProjection()
                .id().firstName().lastName().email().phone();
        ShipmentLocationResponseProjection location = new ShipmentLocationResponseProjection()
                .id().name().addressLine1().city().state().zip().gateCode();
        CustomerResponseProjection customer = new CustomerResponseProjection()
                .id().entityName().type()
                .contacts(contact)
                .shipmentLocations(location);
        return new CustomerPageResponseProjection()
                .content(customer)
                .totalElements()
                .totalPages();
    }

    public static ShipmentPageResponseProjection customerShipmentsSummary() {
        return new ShipmentPageResponseProjection()
                .content(new ShipmentResponseProjection()
                        .id().activity().itemDescription().weight()
                        .contact(new ContactResponseProjection().firstName())
                        .serviceOffering(new ServiceOfferingResponseProjection().typeCd()))
                .totalElements();
    }

    public static ShipmentPageResponseProjection nestedCustomerShipments() {
        return new ShipmentPageResponseProjection()
                .content(new ShipmentResponseProjection()
                        .id().weight().itemDescription()
                        .serviceOffering(new ServiceOfferingResponseProjection().description()));
    }

    public static ShipmentPageResponseProjection advancedFiltering() {
        return new ShipmentPageResponseProjection()
                .content(new ShipmentResponseProjection()
                        .id().itemDescription().weight().activity()
                        .customer(new CustomerResponseProjection().entityName()))
                .totalElements()
                .totalPages();
    }



    public static CustomerPageResponseProjection allCustomers() {
        return new CustomerPageResponseProjection()
                .content(new CustomerResponseProjection().id().entityName().type())
                .totalElements()
                .totalPages();
    }

    public static ShipmentPageResponseProjection allShipments() {
        return new ShipmentPageResponseProjection()
                .content(new ShipmentResponseProjection()
                        .id().itemDescription().weight().activity()
                        .customer(new CustomerResponseProjection().entityName()))
                .totalElements()
                .totalPages();
    }

    public static ShipmentPageResponseProjection shipmentCount() {
        return new ShipmentPageResponseProjection().totalElements();
    }

    public static CustomerPageResponseProjection findCustomerSummary() {
        return new CustomerPageResponseProjection()
                .content(new CustomerResponseProjection()
                        .id().entityName()
                        .contacts(new ContactResponseProjection().id().firstName())
                        .shipmentLocations(new ShipmentLocationResponseProjection().id().name()));
    }
}