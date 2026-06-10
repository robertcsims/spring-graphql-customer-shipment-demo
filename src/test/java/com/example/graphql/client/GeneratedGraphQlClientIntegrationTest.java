package com.example.graphql.client;

import com.example.graphql.domain.*;
import com.example.graphql.service.CustomerService;
import com.example.graphql.service.ServiceOfferingService;
import com.example.graphql.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url=jdbc:derby:memory:graphqlClientTest;create=true")
class GeneratedGraphQlClientIntegrationTest {

    @Autowired
    private CustomerShipmentGraphQlDemoService demoService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ServiceOfferingService serviceOfferingService;

    @BeforeEach
    void seedData() {
        ServiceOffering ground = serviceOfferingService.create("Standard Ground Shipping", "STD");
        ServiceOffering express = serviceOfferingService.create("Next Business Day Express", "EXP");
        ServiceOffering freight = serviceOfferingService.create("LTL Freight", "LTL");
        ServiceOffering air = serviceOfferingService.create("Air Express Overnight", "AIR");

        Customer acme = customerService.createCustomer("Acme Manufacturing Inc.", CustomerType.BUSINESS);
        Contact alice = new Contact("Alice", "Johnson", "555-0100", "alice.johnson@acme.example");
        Contact bob = new Contact("Bob", "Smith", "555-0101", "bob.smith@acme.example");
        acme.addContact(alice);
        acme.addContact(bob);
        ShipmentLocation main = new ShipmentLocation("Main Warehouse", "100 Industrial Parkway", null,
                "Detroit", "MI", "48201", "Dock #12", "GATE-8842");
        acme.addShipmentLocation(main);
        customerService.save(acme);

        shipmentService.createShipment(acme.getId(), alice.getId(), main.getId(), ground.getId(),
                "Steel brackets - box of 500", new BigDecimal("48.75"), "24x18x12", ActivityType.PICKUP);
        shipmentService.createShipment(acme.getId(), bob.getId(), main.getId(), express.getId(),
                "Prototype circuit boards", new BigDecimal("2.1"), "12x8x3", ActivityType.DELIVERY);
        shipmentService.createShipment(acme.getId(), alice.getId(), main.getId(), freight.getId(),
                "Pallet of raw aluminum stock", new BigDecimal("412.0"), "48x40x36", ActivityType.PICKUP);
        shipmentService.createShipment(acme.getId(), bob.getId(), main.getId(), air.getId(),
                "Urgent CNC machine parts", new BigDecimal("87.5"), "36x24x18", ActivityType.DELIVERY);
    }

    @Test
    void typedClient_runsAllDemoOperationsAgainstLiveEndpoint() {
        GraphQlClientDemoReport report = demoService.runAllDemoOperations();

        assertThat(report.total()).isEqualTo(10);
        assertThat(report.passed()).isEqualTo(10);
        assertThat(report.allPassed()).isTrue();
        assertThat(report.operations())
                .extracting(GraphQlClientOperationResult::operation)
                .contains(
                        "FullRelationalHierarchy",
                        "AdvancedFiltering",
                        "NestedPagination",
                        "CreateShipment",
                        "UpdateCustomer"
                );
    }
}