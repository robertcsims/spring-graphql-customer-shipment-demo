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
import org.springframework.context.annotation.Profile;

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
@Profile("!test")
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

            // Service Offerings (shared across all customers)
            ServiceOffering ground = serviceOfferingService.create("Standard Ground Shipping", "STD");
            ServiceOffering express = serviceOfferingService.create("Next Business Day Express", "EXP");
            ServiceOffering freight = serviceOfferingService.create("LTL Freight", "LTL");
            ServiceOffering air = serviceOfferingService.create("Air Express Overnight", "AIR");

            // ============================================
            // CUSTOMER 1: Acme Manufacturing Inc. (BUSINESS) - Expanded
            // ============================================
            Customer acme = customerService.createCustomer("Acme Manufacturing Inc.", CustomerType.BUSINESS);

            Contact acmeAlice = new Contact("Alice", "Johnson", "555-0100", "alice.johnson@acme.example");
            Contact acmeBob = new Contact("Bob", "Smith", "555-0101", "bob.smith@acme.example");
            Contact acmeCarol = new Contact("Carol", "Lee", "555-0102", "carol.lee@acme.example");
            Contact acmeDavid = new Contact("David", "Kim", "555-0103", "david.kim@acme.example");
            acme.addContact(acmeAlice);
            acme.addContact(acmeBob);
            acme.addContact(acmeCarol);
            acme.addContact(acmeDavid);

            ShipmentLocation acmeMain = new ShipmentLocation("Main Warehouse", "100 Industrial Parkway", "Suite 200", "Detroit", "MI", "48201", "Dock #12. Use rear entrance after 4pm.", "GATE-8842");
            ShipmentLocation acmeHq = new ShipmentLocation("Headquarters Receiving", "500 Corporate Blvd", null, "Detroit", "MI", "48226", "Security desk will call receiving. Ask for receiving manager.", null);
            ShipmentLocation acmeWest = new ShipmentLocation("West Coast Distribution", "4500 Harbor Blvd", "Bldg C", "Los Angeles", "CA", "90001", "Gate 7 - 24hr access with code.", "WC-7721");
            ShipmentLocation acmeEast = new ShipmentLocation("East Coast Fulfillment", "1200 Port Rd", null, "Newark", "NJ", "07101", "Dock 4 - call 30 min ahead.", "EC-4490");
            acme.addShipmentLocation(acmeMain);
            acme.addShipmentLocation(acmeHq);
            acme.addShipmentLocation(acmeWest);
            acme.addShipmentLocation(acmeEast);

            customerService.save(acme);

            // 6 shipments for Acme (rich data)
            shipmentService.createShipment(acme.getId(), acmeAlice.getId(), acmeMain.getId(), ground.getId(), "Steel brackets - box of 500", new BigDecimal("48.75"), "24x18x12", ActivityType.PICKUP);
            shipmentService.createShipment(acme.getId(), acmeBob.getId(), acmeHq.getId(), express.getId(), "Prototype circuit boards", new BigDecimal("2.1"), "12x8x3", ActivityType.DELIVERY);
            shipmentService.createShipment(acme.getId(), acmeCarol.getId(), acmeWest.getId(), freight.getId(), "Pallet of raw aluminum stock", new BigDecimal("412.0"), "48x40x36", ActivityType.PICKUP);
            shipmentService.createShipment(acme.getId(), acmeDavid.getId(), acmeEast.getId(), air.getId(), "Urgent CNC machine parts", new BigDecimal("87.5"), "36x24x18", ActivityType.DELIVERY);
            shipmentService.createShipment(acme.getId(), acmeAlice.getId(), acmeMain.getId(), express.getId(), "Precision tooling set", new BigDecimal("15.3"), "18x12x9", ActivityType.PICKUP);
            shipmentService.createShipment(acme.getId(), acmeBob.getId(), acmeWest.getId(), ground.getId(), "Packaging materials - 20 pallets", new BigDecimal("1250.0"), "48x40x72", ActivityType.DELIVERY);

            // ============================================
            // CUSTOMER 2: TechNova Solutions (BUSINESS)
            // ============================================
            Customer technova = customerService.createCustomer("TechNova Solutions", CustomerType.BUSINESS);

            Contact tnMike = new Contact("Mike", "Chen", "555-0200", "mike.chen@technova.example");
            Contact tnSara = new Contact("Sara", "Patel", "555-0201", "sara.patel@technova.example");
            Contact tnJames = new Contact("James", "Rodriguez", "555-0202", "james.rodriguez@technova.example");
            technova.addContact(tnMike);
            technova.addContact(tnSara);
            technova.addContact(tnJames);

            ShipmentLocation tnHq = new ShipmentLocation("TechNova HQ", "200 Innovation Drive", "Floor 3", "Austin", "TX", "78701", "Main lobby reception - ask for shipping.", "TN-1001");
            ShipmentLocation tnLab = new ShipmentLocation("R&D Lab Receiving", "210 Innovation Drive", "Lab Wing", "Austin", "TX", "78701", "Side entrance, badge required.", "TN-LAB2");
            ShipmentLocation tnCloud = new ShipmentLocation("Cloud Partner DC", "5000 Server Farm Rd", null, "Dallas", "TX", "75201", "Loading dock B - schedule 48h in advance.", "CP-DC7");
            technova.addShipmentLocation(tnHq);
            technova.addShipmentLocation(tnLab);
            technova.addShipmentLocation(tnCloud);

            customerService.save(technova);

            shipmentService.createShipment(technova.getId(), tnMike.getId(), tnHq.getId(), air.getId(), "GPU cluster - 8x H100", new BigDecimal("42.8"), "48x36x12", ActivityType.DELIVERY);
            shipmentService.createShipment(technova.getId(), tnSara.getId(), tnLab.getId(), express.getId(), "Prototype PCB boards - 500 units", new BigDecimal("8.5"), "20x16x8", ActivityType.PICKUP);
            shipmentService.createShipment(technova.getId(), tnJames.getId(), tnCloud.getId(), freight.getId(), "Server racks - 4 units", new BigDecimal("680.0"), "80x40x40", ActivityType.DELIVERY);
            shipmentService.createShipment(technova.getId(), tnMike.getId(), tnHq.getId(), ground.getId(), "Networking switches - 25 units", new BigDecimal("95.0"), "30x20x10", ActivityType.PICKUP);
            shipmentService.createShipment(technova.getId(), tnSara.getId(), tnLab.getId(), air.getId(), "Critical ML training data drives", new BigDecimal("12.4"), "16x12x6", ActivityType.DELIVERY);

            // ============================================
            // CUSTOMER 3: Sarah Thompson (PERSONAL)
            // ============================================
            Customer sarah = customerService.createCustomer("Sarah Thompson", CustomerType.PERSONAL);

            Contact sarahSelf = new Contact("Sarah", "Thompson", "555-0300", "sarah.t@personal.example");
            Contact sarahSpouse = new Contact("Michael", "Thompson", "555-0301", "michael.t@personal.example");
            sarah.addContact(sarahSelf);
            sarah.addContact(sarahSpouse);

            ShipmentLocation sarahHome = new ShipmentLocation("Primary Residence", "45 Oak Lane", "Unit 2B", "Boulder", "CO", "80302", "Leave with neighbor if no answer.", "HOME-882");
            ShipmentLocation sarahCabin = new ShipmentLocation("Mountain Cabin", "123 Pine Ridge Rd", null, "Estes Park", "CO", "80517", "Key under mat.", "CABIN-19");
            sarah.addShipmentLocation(sarahHome);
            sarah.addShipmentLocation(sarahCabin);

            customerService.save(sarah);

            shipmentService.createShipment(sarah.getId(), sarahSelf.getId(), sarahHome.getId(), ground.getId(), "Vintage record player + speakers", new BigDecimal("18.4"), "22x16x14", ActivityType.DELIVERY);
            shipmentService.createShipment(sarah.getId(), sarahSpouse.getId(), sarahCabin.getId(), express.getId(), "Ski equipment - 2 sets", new BigDecimal("31.2"), "48x12x12", ActivityType.PICKUP);
            shipmentService.createShipment(sarah.getId(), sarahSelf.getId(), sarahHome.getId(), air.getId(), "Urgent medication shipment", new BigDecimal("1.8"), "10x6x4", ActivityType.DELIVERY);
            shipmentService.createShipment(sarah.getId(), sarahSpouse.getId(), sarahCabin.getId(), ground.getId(), "Furniture - dining table set", new BigDecimal("145.0"), "60x36x30", ActivityType.DELIVERY);

            // ============================================
            // CUSTOMER 4: Horizon Freight Co. (BUSINESS)
            // ============================================
            Customer horizon = customerService.createCustomer("Horizon Freight Co.", CustomerType.BUSINESS);

            Contact horLisa = new Contact("Lisa", "Nguyen", "555-0400", "lisa.nguyen@horizonfreight.example");
            Contact horMarcus = new Contact("Marcus", "Bell", "555-0401", "marcus.bell@horizonfreight.example");
            Contact horPriya = new Contact("Priya", "Singh", "555-0402", "priya.singh@horizonfreight.example");
            horizon.addContact(horLisa);
            horizon.addContact(horMarcus);
            horizon.addContact(horPriya);

            ShipmentLocation horCentral = new ShipmentLocation("Central Hub", "800 Logistics Way", null, "Chicago", "IL", "60601", "24/7 operations - use bay 12-18.", "HUB-CHI");
            ShipmentLocation horSouth = new ShipmentLocation("Southern Terminal", "1500 Port Blvd", "Warehouse C", "Houston", "TX", "77001", "Driver check-in at gate A.", "TERM-HOU");
            ShipmentLocation horNorth = new ShipmentLocation("Northern Crossdock", "900 Rail Yard Dr", null, "Minneapolis", "MN", "55401", "Appointment only.", "XDOCK-MSP");
            horizon.addShipmentLocation(horCentral);
            horizon.addShipmentLocation(horSouth);
            horizon.addShipmentLocation(horNorth);

            customerService.save(horizon);

            shipmentService.createShipment(horizon.getId(), horLisa.getId(), horCentral.getId(), freight.getId(), "Heavy machinery parts - 3 crates", new BigDecimal("890.0"), "72x48x48", ActivityType.PICKUP);
            shipmentService.createShipment(horizon.getId(), horMarcus.getId(), horSouth.getId(), ground.getId(), "Consumer electronics - 1200 units", new BigDecimal("2450.0"), "40x32x60", ActivityType.DELIVERY);
            shipmentService.createShipment(horizon.getId(), horPriya.getId(), horNorth.getId(), express.getId(), "Temperature-controlled pharma", new BigDecimal("67.8"), "24x18x12", ActivityType.DELIVERY);
            shipmentService.createShipment(horizon.getId(), horLisa.getId(), horCentral.getId(), air.getId(), "Emergency replacement engine", new BigDecimal("320.5"), "60x40x36", ActivityType.PICKUP);
            shipmentService.createShipment(horizon.getId(), horMarcus.getId(), horSouth.getId(), ground.getId(), "Bulk chemicals - 8 drums", new BigDecimal("1640.0"), "48x48x48", ActivityType.DELIVERY);

            log.info("=== Seeding complete. 4 rich customers seeded (Acme, TechNova, Sarah Thompson, Horizon Freight) with multiple contacts, locations and shipments each ===");
            log.info("Use GraphiQL at http://localhost:8080/graphiql (login: admin/admin123 or viewer/viewer123)");
        };
    }
}
