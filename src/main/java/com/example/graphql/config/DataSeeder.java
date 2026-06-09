package com.example.graphql.config;

import com.example.graphql.domain.*;
import com.example.graphql.service.CustomerService;
import com.example.graphql.service.ServiceOfferingService;
import com.example.graphql.service.ShipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Seeds realistic relational data on startup.
 *
 * This allows an immediate, compelling demo of deep GraphQL queries
 * that traverse the full customer → contact → location → shipment → serviceOffering graph.
 *
 * Because we use file-based Derby, the seeded data persists across application restarts.
 */
@Configuration
@org.springframework.context.annotation.Profile("!test")
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedData(CustomerService customerService,
                               ServiceOfferingService serviceOfferingService,
                               ShipmentService shipmentService) {
        return args -> {
            if (!customerService.getAllCustomers().isEmpty()) {
                log.info("Database already contains data - skipping seed.");
                return;
            }

            log.info("=== Seeding demo data for GraphQL relational prototype ===");

            // Service Offerings
            ServiceOffering ground = serviceOfferingService.create("Standard Ground Shipping", "STD");
            ServiceOffering express = serviceOfferingService.create("Next Business Day Express", "EXP");
            ServiceOffering freight = serviceOfferingService.create("LTL Freight", "LTL");

            // === Business Customer: Acme Manufacturing ===
            Customer acme = customerService.createCustomer("Acme Manufacturing Inc.", CustomerType.BUSINESS);

            // Contacts for Acme
            Contact alice = new Contact("Alice", "Johnson", "555-0100", "alice.johnson@acme.example");
            Contact bob = new Contact("Bob", "Smith", "555-0101", "bob.smith@acme.example");
            acme.addContact(alice);
            acme.addContact(bob);

            // Locations
            ShipmentLocation warehouse = new ShipmentLocation(
                    "Main Warehouse",
                    "100 Industrial Parkway",
                    "Suite 200",
                    "Detroit",
                    "MI",
                    "48201",
                    "Dock #12. Use rear entrance after 4pm.",
                    "GATE-8842"
            );
            ShipmentLocation hq = new ShipmentLocation(
                    "Headquarters Receiving",
                    "500 Corporate Blvd",
                    null,
                    "Detroit",
                    "MI",
                    "48226",
                    "Security desk will call receiving. Ask for receiving manager.",
                    null
            );
            acme.addShipmentLocation(warehouse);
            acme.addShipmentLocation(hq);

            // Persist customer with relations (service handles cascade via save)
            customerService.save(acme);

            // Shipments for Acme
            shipmentService.createShipment(acme.getId(), alice.getId(), warehouse.getId(), ground.getId(),
                    "Steel brackets - box of 500", new BigDecimal("48.75"), "24x18x12", ActivityType.PICKUP);

            shipmentService.createShipment(acme.getId(), bob.getId(), hq.getId(), express.getId(),
                    "Prototype circuit boards", new BigDecimal("2.1"), "12x8x3", ActivityType.DELIVERY);

            shipmentService.createShipment(acme.getId(), alice.getId(), warehouse.getId(), freight.getId(),
                    "Pallet of raw aluminum stock", new BigDecimal("412.0"), "48x40x36", ActivityType.PICKUP);

            // === Personal Customer ===
            Customer jane = customerService.createCustomer("Jane Doe", CustomerType.PERSONAL);

            Contact janeContact = new Contact("Jane", "Doe", "555-9876", "jane.doe.personal@example.com");
            jane.addContact(janeContact);

            ShipmentLocation home = new ShipmentLocation(
                    "Home",
                    "123 Maple Street",
                    "Apt 4B",
                    "Ann Arbor",
                    "MI",
                    "48103",
                    "Leave at front door if no answer. Ring bell twice.",
                    "CODE-3319"
            );
            jane.addShipmentLocation(home);
            customerService.save(jane);

            shipmentService.createShipment(jane.getId(), janeContact.getId(), home.getId(), ground.getId(),
                    "Vintage record player + speakers", new BigDecimal("18.4"), "22x16x14", ActivityType.DELIVERY);

            log.info("=== Seeding complete. Customers: Acme Manufacturing + Jane Doe ===");
            log.info("Use GraphiQL at http://localhost:8080/graphiql (login: admin/admin123 or viewer/viewer123)");
        };
    }
}
