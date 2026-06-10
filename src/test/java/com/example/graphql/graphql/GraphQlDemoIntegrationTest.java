package com.example.graphql.graphql;

import com.example.graphql.domain.*;
import com.example.graphql.service.CustomerService;
import com.example.graphql.service.ServiceOfferingService;
import com.example.graphql.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureHttpGraphQlTester
@ActiveProfiles("test")
@Transactional
class GraphQlDemoIntegrationTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ServiceOfferingService serviceOfferingService;

    private Long acmeId;
    private Long airOfferingId;

    @BeforeEach
    void seedRichCustomer() {
        ServiceOffering ground = serviceOfferingService.create("Standard Ground Shipping", "STD");
        ServiceOffering express = serviceOfferingService.create("Next Business Day Express", "EXP");
        ServiceOffering freight = serviceOfferingService.create("LTL Freight", "LTL");
        ServiceOffering air = serviceOfferingService.create("Air Express Overnight", "AIR");
        airOfferingId = air.getId();

        Customer acme = customerService.createCustomer("Acme Manufacturing Inc.", CustomerType.BUSINESS);
        Contact alice = new Contact("Alice", "Johnson", "555-0100", "alice.johnson@acme.example");
        Contact bob = new Contact("Bob", "Smith", "555-0101", "bob.smith@acme.example");
        acme.addContact(alice);
        acme.addContact(bob);

        ShipmentLocation main = new ShipmentLocation("Main Warehouse", "100 Industrial Parkway", "Suite 200",
                "Detroit", "MI", "48201", "Dock #12", "GATE-8842");
        ShipmentLocation hq = new ShipmentLocation("Headquarters Receiving", "500 Corporate Blvd", null,
                "Detroit", "MI", "48226", "Security desk", null);
        acme.addShipmentLocation(main);
        acme.addShipmentLocation(hq);
        customerService.save(acme);
        acmeId = acme.getId();

        shipmentService.createShipment(acmeId, alice.getId(), main.getId(), ground.getId(),
                "Steel brackets - box of 500", new BigDecimal("48.75"), "24x18x12", ActivityType.PICKUP);
        shipmentService.createShipment(acmeId, bob.getId(), hq.getId(), express.getId(),
                "Prototype circuit boards", new BigDecimal("2.1"), "12x8x3", ActivityType.DELIVERY);
        shipmentService.createShipment(acmeId, alice.getId(), main.getId(), freight.getId(),
                "Pallet of raw aluminum stock", new BigDecimal("412.0"), "48x40x36", ActivityType.PICKUP);
        shipmentService.createShipment(acmeId, bob.getId(), hq.getId(), air.getId(),
                "Urgent CNC machine parts", new BigDecimal("87.5"), "36x24x18", ActivityType.DELIVERY);
    }

    @Test
    void fullRelationalHierarchy_returnsLiveAcmeGraphFromDerby() {
        graphQlTester.document("""
                query FullRelationalHierarchy {
                  customers(filter: { entityNameContains: "Acme" }, page: 0, size: 1) {
                    content {
                      entityName
                      type
                      contacts { firstName lastName email }
                      shipmentLocations { name city gateCode }
                      shipments {
                        itemDescription
                        weight
                        contact { firstName }
                        shipmentLocation { name }
                        serviceOffering { typeCd }
                      }
                    }
                    totalElements
                  }
                }
                """)
                .execute()
                .path("customers.content[0].entityName").entity(String.class).isEqualTo("Acme Manufacturing Inc.")
                .path("customers.content[0].contacts").entityList(Object.class).hasSize(2)
                .path("customers.content[0].shipmentLocations").entityList(Object.class).hasSize(2)
                .path("customers.content[0].shipments").entityList(Object.class).hasSize(4)
                .path("customers.totalElements").entity(Integer.class).isEqualTo(1);
    }

    @Test
    void advancedFiltering_returnsPickupShipmentsAboveMinWeight() {
        graphQlTester.document("""
                query AdvancedFiltering {
                  shipments(filter: { activity: PICKUP, minWeight: 10 }, page: 0, size: 5, sort: ["-weight"]) {
                    content { itemDescription weight activity customer { entityName } }
                    totalElements
                  }
                }
                """)
                .execute()
                .path("shipments.content").entityList(Object.class).hasSize(2)
                .path("shipments.content[0].weight").entity(Double.class).isEqualTo(412.0)
                .path("shipments.content[0].activity").entity(String.class).isEqualTo("PICKUP");
    }

    @Test
    void nestedPagination_returnsTopWeightedShipmentsForAcme() {
        graphQlTester.document("""
                query NestedPagination {
                  customers(filter: { entityNameContains: "Acme" }, page: 0, size: 1) {
                    content {
                      entityName
                      shipments(page: 0, size: 2, sort: ["-weight"]) {
                        weight
                        itemDescription
                        serviceOffering { description }
                      }
                    }
                  }
                }
                """)
                .execute()
                .path("customers.content[0].entityName").entity(String.class).isEqualTo("Acme Manufacturing Inc.")
                .path("customers.content[0].shipments").entityList(Object.class).hasSize(2)
                .path("customers.content[0].shipments[0].weight").entity(Double.class).isEqualTo(412.0);
    }

    @Test
    void createShipmentMutation_persistsAndAppearsInSubsequentQuery() {
        String uniqueDesc = "Urgent prototype parts - integration verify";

        graphQlTester.document("""
                mutation CreateShipment($input: CreateShipmentInput!) {
                  createShipment(input: $input) {
                    id
                    itemDescription
                    customer { entityName }
                    serviceOffering { typeCd }
                  }
                }
                """)
                .variable("input", java.util.Map.of(
                        "customerId", acmeId,
                        "serviceOfferingId", airOfferingId,
                        "itemDescription", uniqueDesc,
                        "weight", 14.25,
                        "dimensions", "20x14x9",
                        "activity", "DELIVERY"
                ))
                .execute()
                .path("createShipment.itemDescription").entity(String.class).isEqualTo(uniqueDesc)
                .path("createShipment.customer.entityName").entity(String.class).isEqualTo("Acme Manufacturing Inc.");

        graphQlTester.document("""
                query {
                  shipments(filter: { itemDescriptionContains: "integration verify" }, page: 0, size: 5) {
                    totalElements
                    content { itemDescription customer { entityName } }
                  }
                }
                """)
                .execute()
                .path("shipments.totalElements").entity(Integer.class).isEqualTo(1)
                .path("shipments.content[0].customer.entityName").entity(String.class).isEqualTo("Acme Manufacturing Inc.");
    }

    @Test
    void updateCustomerMutation_persistsAndIsVisibleInFilterQuery() {
        String updatedName = "Acme Manufacturing Inc. — Updated Live!";

        graphQlTester.document("""
                mutation UpdateCustomer($id: ID!, $name: String!) {
                  updateCustomer(id: $id, entityName: $name, type: BUSINESS) {
                    id
                    entityName
                  }
                }
                """)
                .variable("id", acmeId)
                .variable("name", updatedName)
                .execute()
                .path("updateCustomer.entityName").entity(String.class).isEqualTo(updatedName);

        graphQlTester.document("""
                query {
                  customers(filter: { entityNameContains: "Updated Live" }, page: 0, size: 1) {
                    content { entityName contacts { firstName } }
                    totalElements
                  }
                }
                """)
                .execute()
                .path("customers.totalElements").entity(Integer.class).isEqualTo(1)
                .path("customers.content[0].entityName").entity(String.class).isEqualTo(updatedName)
                .path("customers.content[0].contacts").entityList(Object.class).hasSize(2);
    }

    @Test
    void createCustomerMutation_persistsNewCustomer() {
        graphQlTester.document("""
                mutation {
                  createCustomer(input: { entityName: "Quantum Logistics Demo LLC", type: BUSINESS }) {
                    id
                    entityName
                    type
                  }
                }
                """)
                .execute()
                .path("createCustomer.entityName").entity(String.class).isEqualTo("Quantum Logistics Demo LLC")
                .path("createCustomer.type").entity(String.class).isEqualTo("BUSINESS");

        graphQlTester.document("""
                query {
                  customers(filter: { entityNameContains: "Quantum Logistics" }, page: 0, size: 5) {
                    totalElements
                  }
                }
                """)
                .execute()
                .path("customers.totalElements").entity(Integer.class).isEqualTo(1);
    }

    @Test
    void serviceOfferingsAndShipmentCount_returnLiveReferenceAndAggregateData() {
        graphQlTester.document("query { serviceOfferings { typeCd } }")
                .execute()
                .path("serviceOfferings").entityList(Object.class).hasSize(4);

        graphQlTester.document("query { shipments(page: 0, size: 1) { totalElements } }")
                .execute()
                .path("shipments.totalElements").entity(Integer.class).isEqualTo(4);
    }
}