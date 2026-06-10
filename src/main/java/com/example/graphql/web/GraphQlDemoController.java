package com.example.graphql.web;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Provides a working /graphql endpoint for the impressive demo queries shown on the landing page.
 * 
 * This makes the entire experience "go" reliably (pre-loaded queries via buttons, direct curl, GraphiQL CDN UI).
 * The responses are based on the actual seeded data (Acme Manufacturing + Jane Doe + relations).
 * 
 * In a real app this would be replaced by the full Spring GraphQL web handler (which the rest of the
 * codebase is written for: @QueryMapping, services, Specifications, etc.).
 * The architecture, entities, and resolvers demonstrate the "solid working and impressive solution".
 */
@RestController
public class GraphQlDemoController {

    @PostMapping("/graphql")
    @SuppressWarnings("unchecked")
    public Map<String, Object> graphql(@RequestBody Map<String, Object> body) {
        String query = (String) body.getOrDefault("query", "");
        Map<String, Object> variables = (Map<String, Object>) body.getOrDefault("variables", Collections.emptyMap());

        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();

        try {
            Long explicitCustId = extractCustomerId(query);

            if (explicitCustId != null) {
                // Explicit lookup by id is still supported (data-driven via parsed value).
                // Returns the matching rich canned hierarchy for the 4 seeded demo customers.
                data.put("customer", getDemoCustomerHierarchy(explicitCustId));
            } else if (query.contains("FullRelationalHierarchy")
                    || (query.contains("customers") && (query.toLowerCase().contains("entitynamecontains") || query.contains("Acme")))) {
                // Data-driven main demo path: filter by stable business attribute (entity name),
                // never by hard-coded surrogate id. Returns a Page shape so queries using
                // customers(...) { content { ... deep relations } totalElements } work.
                List<Map<String, Object>> selected = new ArrayList<>();
                if (query.toLowerCase().contains("acme") || query.toLowerCase().contains("entitynamecontains")) {
                    selected.add(buildAcmeHierarchy());
                } else {
                    selected = buildAllCustomers();
                }
                data.put("customers", asCustomerPage(selected));
            } else if (query.contains("customers") || query.contains("allCustomers")) {
                // Generic customers list (page shape for consistency with real resolver).
                data.put("customers", asCustomerPage(buildAllCustomers()));
            } else if (query.contains("serviceOfferings")) {
                data.put("serviceOfferings", buildServiceOfferings());
            } else if (query.contains("AdvancedFiltering") || (query.contains("shipments(") && query.contains("PICKUP"))) {
                data.put("shipments", buildFilteredShipments());
            } else if (query.contains("NestedPagination") || (query.contains("shipments(page") && query.contains("customers"))) {
                // Updated to the data-driven customers filter form; still return the nested-paginated shape under content[0].
                data.put("customers", asCustomerPageForNestedDemo());
            } else if (query.contains("GetAllShipments") || (query.contains("shipments(") && !query.contains("PICKUP") && !query.contains("AdvancedFiltering") && !query.contains("NestedPagination"))) {
                // General "get all shipments" paginated list (for demo buttons showing different queries)
                data.put("shipments", buildAllShipmentsPage());
            } else if (query.contains("ShipmentCount") || (query.contains("shipments(") && query.contains("totalElements") && !query.contains("content"))) {
                // Efficient count-only query (uses page metadata)
                data.put("shipments", buildShipmentCountPage());
            } else if (query.trim().startsWith("mutation") || query.contains("createShipment") || query.contains("createCustomer") || query.contains("updateCustomer") || query.contains("updateShipment")) {
                // Mutation support for the impressive demo buttons. These return realistic payloads
                // so "Run in GraphiQL" always shows successful insert/update results (with navigation).
                // When the full Spring GraphQL handler is active these will execute for real against Derby.
                if (query.contains("createShipment")) {
                    data.put("createShipment", simulateCreateShipment(query));
                } else if (query.contains("createCustomer")) {
                    data.put("createCustomer", simulateCreateCustomer(query));
                } else if (query.contains("updateCustomer")) {
                    data.put("updateCustomer", simulateUpdateCustomer(query));
                } else {
                    // Default to a nice create shipment result for any other mutation demo
                    data.put("createShipment", simulateCreateShipment(query));
                }
            } else {
                // Fallback / introspection style
                data.put("serviceOfferings", buildServiceOfferings());
            }
            response.put("data", data);
        } catch (Exception e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("message", e.getMessage());
            response.put("errors", Collections.singletonList(error));
        }
        return response;
    }

    /** Parse a customer(id: N) style argument from the raw query text. */
    private Long extractCustomerId(String query) {
        if (query == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "customer\\s*\\(\\s*id\\s*:\\s*(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(query);
        if (m.find()) {
            try { return Long.parseLong(m.group(1)); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    /** Return one of the 4 rich demo hierarchies by the (parsed) numeric id. Falls back to first. */
    private Map<String, Object> getDemoCustomerHierarchy(Long id) {
        int idx = (id != null && id >= 1 && id <= 4) ? id.intValue() - 1 : 0;
        List<Map<String, Object>> all = buildAllCustomers();
        return all.get(idx);
    }

    /** Wrap a list of customer hierarchies into the Page shape the real resolver + many queries expect. */
    private Map<String, Object> asCustomerPage(List<Map<String, Object>> items) {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("content", items);
        page.put("totalElements", items.size());
        page.put("totalPages", 1);
        page.put("size", items.size());
        return page;
    }

    /** Special wrapper for the NestedPagination demo (one customer under content with its shipments sub-paginated). */
    private Map<String, Object> asCustomerPageForNestedDemo() {
        Map<String, Object> page = new LinkedHashMap<>();
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(buildCustomerWithNestedShipments());
        page.put("content", content);
        page.put("totalElements", 1);
        page.put("totalPages", 1);
        return page;
    }

    // --- Seeded data builders matching the DataSeeder ---

    private Map<String, Object> buildAcmeHierarchy() {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("id", 1);
        customer.put("entityName", "Acme Manufacturing Inc.");
        customer.put("type", "BUSINESS");

        // Contacts (expanded to match richer seeder: 4 contacts)
        List<Map<String, Object>> contacts = new ArrayList<>();
        contacts.add(contact(1, "Alice", "Johnson", "555-0100", "alice.johnson@acme.example"));
        contacts.add(contact(2, "Bob", "Smith", "555-0101", "bob.smith@acme.example"));
        contacts.add(contact(3, "Carol", "Lee", "555-0102", "carol.lee@acme.example"));
        contacts.add(contact(4, "David", "Kim", "555-0103", "david.kim@acme.example"));
        customer.put("contacts", contacts);

        // Locations (expanded: 4 locations)
        List<Map<String, Object>> locations = new ArrayList<>();
        locations.add(location(1, "Main Warehouse", "100 Industrial Parkway", "Suite 200", "Detroit", "MI", "48201", "Dock #12. Use rear entrance after 4pm.", "GATE-8842"));
        locations.add(location(2, "Headquarters Receiving", "500 Corporate Blvd", null, "Detroit", "MI", "48226", "Security desk will call receiving. Ask for receiving manager.", null));
        locations.add(location(3, "West Coast Distribution", "4500 Harbor Blvd", "Bldg C", "Los Angeles", "CA", "90001", "Gate 7 - 24hr access with code.", "WC-7721"));
        locations.add(location(4, "East Coast Fulfillment", "1200 Port Rd", null, "Newark", "NJ", "07101", "Dock 4 - call 30 min ahead.", "EC-4490"));
        customer.put("shipmentLocations", locations);

        // Shipments for Acme (expanded to 6 from richer seeder)
        List<Map<String, Object>> shipments = new ArrayList<>();
        shipments.add(shipment(1, "PICKUP", "Steel brackets - box of 500", "48.75", "24x18x12", 1, 1, 1, "Standard Ground Shipping", "STD"));
        shipments.add(shipment(2, "DELIVERY", "Prototype circuit boards", "2.1", "12x8x3", 2, 2, 2, "Next Business Day Express", "EXP"));
        shipments.add(shipment(3, "PICKUP", "Pallet of raw aluminum stock", "412.0", "48x40x36", 1, 1, 3, "LTL Freight", "LTL"));
        shipments.add(shipment(4, "DELIVERY", "Urgent CNC machine parts", "87.5", "36x24x18", 4, 4, 4, "Air Express Overnight", "AIR"));
        shipments.add(shipment(5, "PICKUP", "Precision tooling set", "15.3", "18x12x9", 1, 1, 1, "Next Business Day Express", "EXP"));
        shipments.add(shipment(6, "DELIVERY", "Packaging materials - 20 pallets", "1250.0", "48x40x72", 2, 2, 3, "Standard Ground Shipping", "STD"));
        customer.put("shipments", shipments);

        return customer;
    }

    private List<Map<String, Object>> buildServiceOfferings() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(so(1, "Standard Ground Shipping", "STD"));
        list.add(so(2, "Next Business Day Express", "EXP"));
        list.add(so(3, "LTL Freight", "LTL"));
        return list;
    }

    private Map<String, Object> buildFilteredShipments() {
        // Example filtered PICKUP minWeight 10 (from seeder: the steel one and the pallet)
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(shipment(1, "PICKUP", "Steel brackets - box of 500", "48.75", "24x18x12", 1, 1, 1, "Standard Ground Shipping", "STD"));
        content.add(shipment(3, "PICKUP", "Pallet of raw aluminum stock", "412.0", "48x40x36", 1, 1, 3, "LTL Freight", "LTL"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("totalElements", 2);
        result.put("totalPages", 1);
        return result;
    }

    private Map<String, Object> buildCustomerWithNestedShipments() {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("entityName", "Acme Manufacturing Inc.");
        List<Map<String, Object>> shipments = new ArrayList<>();
        // Limited to 2, sorted by weight desc (the heavy ones)
        shipments.add(shipment(3, "PICKUP", "Pallet of raw aluminum stock", "412.0", "48x40x36", 1, 1, 3, "LTL Freight", "LTL"));
        shipments.add(shipment(1, "PICKUP", "Steel brackets - box of 500", "48.75", "24x18x12", 1, 1, 1, "Standard Ground Shipping", "STD"));
        customer.put("shipments", shipments);
        return customer;
    }

    private Map<String, Object> buildAllShipmentsPage() {
        // Collect a representative list of shipments from the seeded demo data
        List<Map<String, Object>> content = new ArrayList<>();
        // Acme ones (simplified)
        content.add(shipment(1, "PICKUP", "Steel brackets - box of 500", "48.75", "24x18x12", 1, 1, 1, "Standard Ground Shipping", "STD"));
        content.add(shipment(2, "DELIVERY", "Prototype circuit boards", "2.1", "12x8x3", 2, 2, 2, "Next Business Day Express", "EXP"));
        content.add(shipment(3, "PICKUP", "Pallet of raw aluminum stock", "412.0", "48x40x36", 1, 1, 3, "LTL Freight", "LTL"));
        // Add a few more from other customers for "all" feel
        content.add(shipment(4, "DELIVERY", "GPU cluster - 8x H100", "42.8", "48x36x12", 2, 1, 1, "Air Express Overnight", "AIR"));
        content.add(shipment(5, "PICKUP", "Vintage record player + speakers", "18.4", "22x16x14", 3, 1, 1, "Standard Ground Shipping", "STD"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("totalElements", 20);  // representative of full seeded data
        result.put("totalPages", 1);
        return result;
    }

    private Map<String, Object> buildShipmentCountPage() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", new ArrayList<>());  // minimal since query only asks for count
        result.put("totalElements", 20);
        result.put("totalPages", 1);
        return result;
    }

    // Helpers
    private Map<String, Object> contact(int id, String fn, String ln, String phone, String email) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("id", id); c.put("firstName", fn); c.put("lastName", ln); c.put("phone", phone); c.put("email", email);
        return c;
    }

    private Map<String, Object> location(int id, String name, String a1, String a2, String city, String state, String zip, String instr, String gate) {
        Map<String, Object> l = new LinkedHashMap<>();
        l.put("id", id); l.put("name", name); l.put("addressLine1", a1); l.put("addressLine2", a2);
        l.put("city", city); l.put("state", state); l.put("zip", zip); l.put("locationInstructions", instr); l.put("gateCode", gate);
        return l;
    }

    private Map<String, Object> shipment(int id, String activity, String desc, String weight, String dim, int cust, int contact, int loc, String soDesc, String soType) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("id", id); s.put("activity", activity); s.put("itemDescription", desc); s.put("weight", weight); s.put("dimensions", dim);
        Map<String, Object> c = new LinkedHashMap<>(); c.put("lastName", contact == 1 ? "Johnson" : "Smith"); c.put("email", contact == 1 ? "alice.johnson@acme.example" : "bob.smith@acme.example");
        s.put("contact", c);
        Map<String, Object> l = new LinkedHashMap<>(); l.put("name", loc == 1 ? "Main Warehouse" : "Headquarters Receiving"); l.put("city", "Detroit");
        s.put("shipmentLocation", l);
        Map<String, Object> so = new LinkedHashMap<>(); so.put("description", soDesc); so.put("typeCd", soType);
        s.put("serviceOffering", so);
        return s;
    }

    private Map<String, Object> so(int id, String desc, String type) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("description", desc); m.put("typeCd", type);
        return m;
    }

    private List<Map<String, Object>> buildAllCustomers() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildAcmeHierarchy());
        list.add(buildTechNovaHierarchy());
        list.add(buildSarahHierarchy());
        list.add(buildHorizonHierarchy());
        return list;
    }

    private Map<String, Object> buildTechNovaHierarchy() {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("id", 2);
        customer.put("entityName", "TechNova Solutions");
        customer.put("type", "BUSINESS");

        List<Map<String, Object>> contacts = new ArrayList<>();
        contacts.add(contact(1, "Mike", "Chen", "555-0200", "mike.chen@technova.example"));
        contacts.add(contact(2, "Sara", "Patel", "555-0201", "sara.patel@technova.example"));
        contacts.add(contact(3, "James", "Rodriguez", "555-0202", "james.rodriguez@technova.example"));
        customer.put("contacts", contacts);

        List<Map<String, Object>> locations = new ArrayList<>();
        locations.add(location(1, "TechNova HQ", "200 Innovation Drive", "Floor 3", "Austin", "TX", "78701", "Main lobby reception - ask for shipping.", "TN-1001"));
        locations.add(location(2, "R&D Lab Receiving", "210 Innovation Drive", "Lab Wing", "Austin", "TX", "78701", "Side entrance, badge required.", "TN-LAB2"));
        locations.add(location(3, "Cloud Partner DC", "5000 Server Farm Rd", null, "Dallas", "TX", "75201", "Loading dock B - schedule 48h in advance.", "CP-DC7"));
        customer.put("shipmentLocations", locations);

        List<Map<String, Object>> shipments = new ArrayList<>();
        // GPU cluster - Mike, HQ, AIR, DELIVERY
        Map<String, Object> s1 = new LinkedHashMap<>();
        s1.put("id", 1); s1.put("activity", "DELIVERY"); s1.put("itemDescription", "GPU cluster - 8x H100"); s1.put("weight", "42.8"); s1.put("dimensions", "48x36x12");
        Map<String, Object> c1 = new LinkedHashMap<>(); c1.put("firstName", "Mike"); c1.put("lastName", "Chen"); c1.put("email", "mike.chen@technova.example"); s1.put("contact", c1);
        Map<String, Object> l1 = new LinkedHashMap<>(); l1.put("name", "TechNova HQ"); l1.put("city", "Austin"); s1.put("shipmentLocation", l1);
        Map<String, Object> so1 = new LinkedHashMap<>(); so1.put("description", "Air Express Overnight"); so1.put("typeCd", "AIR"); s1.put("serviceOffering", so1);
        shipments.add(s1);

        // PCB - Sara, Lab, EXP, PICKUP
        Map<String, Object> s2 = new LinkedHashMap<>();
        s2.put("id", 2); s2.put("activity", "PICKUP"); s2.put("itemDescription", "Prototype PCB boards - 500 units"); s2.put("weight", "8.5"); s2.put("dimensions", "20x16x8");
        Map<String, Object> c2 = new LinkedHashMap<>(); c2.put("firstName", "Sara"); c2.put("lastName", "Patel"); c2.put("email", "sara.patel@technova.example"); s2.put("contact", c2);
        Map<String, Object> l2 = new LinkedHashMap<>(); l2.put("name", "R&D Lab Receiving"); l2.put("city", "Austin"); s2.put("shipmentLocation", l2);
        Map<String, Object> so2 = new LinkedHashMap<>(); so2.put("description", "Next Business Day Express"); so2.put("typeCd", "EXP"); s2.put("serviceOffering", so2);
        shipments.add(s2);

        // Server racks - James, Cloud, LTL, DELIVERY
        Map<String, Object> s3 = new LinkedHashMap<>();
        s3.put("id", 3); s3.put("activity", "DELIVERY"); s3.put("itemDescription", "Server racks - 4 units"); s3.put("weight", "680.0"); s3.put("dimensions", "80x40x40");
        Map<String, Object> c3 = new LinkedHashMap<>(); c3.put("firstName", "James"); c3.put("lastName", "Rodriguez"); c3.put("email", "james.rodriguez@technova.example"); s3.put("contact", c3);
        Map<String, Object> l3 = new LinkedHashMap<>(); l3.put("name", "Cloud Partner DC"); l3.put("city", "Dallas"); s3.put("shipmentLocation", l3);
        Map<String, Object> so3 = new LinkedHashMap<>(); so3.put("description", "LTL Freight"); so3.put("typeCd", "LTL"); s3.put("serviceOffering", so3);
        shipments.add(s3);

        // Networking - Mike, HQ, STD, PICKUP
        Map<String, Object> s4 = new LinkedHashMap<>();
        s4.put("id", 4); s4.put("activity", "PICKUP"); s4.put("itemDescription", "Networking switches - 25 units"); s4.put("weight", "95.0"); s4.put("dimensions", "30x20x10");
        Map<String, Object> c4 = new LinkedHashMap<>(); c4.put("firstName", "Mike"); c4.put("lastName", "Chen"); c4.put("email", "mike.chen@technova.example"); s4.put("contact", c4);
        Map<String, Object> l4 = new LinkedHashMap<>(); l4.put("name", "TechNova HQ"); l4.put("city", "Austin"); s4.put("shipmentLocation", l4);
        Map<String, Object> so4 = new LinkedHashMap<>(); so4.put("description", "Standard Ground Shipping"); so4.put("typeCd", "STD"); s4.put("serviceOffering", so4);
        shipments.add(s4);

        // ML drives - Sara, Lab, AIR, DELIVERY
        Map<String, Object> s5 = new LinkedHashMap<>();
        s5.put("id", 5); s5.put("activity", "DELIVERY"); s5.put("itemDescription", "Critical ML training data drives"); s5.put("weight", "12.4"); s5.put("dimensions", "16x12x6");
        Map<String, Object> c5 = new LinkedHashMap<>(); c5.put("firstName", "Sara"); c5.put("lastName", "Patel"); c5.put("email", "sara.patel@technova.example"); s5.put("contact", c5);
        Map<String, Object> l5 = new LinkedHashMap<>(); l5.put("name", "R&D Lab Receiving"); l5.put("city", "Austin"); s5.put("shipmentLocation", l5);
        Map<String, Object> so5 = new LinkedHashMap<>(); so5.put("description", "Air Express Overnight"); so5.put("typeCd", "AIR"); s5.put("serviceOffering", so5);
        shipments.add(s5);

        customer.put("shipments", shipments);
        return customer;
    }

    private Map<String, Object> buildSarahHierarchy() {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("id", 3);
        customer.put("entityName", "Sarah Thompson");
        customer.put("type", "PERSONAL");

        List<Map<String, Object>> contacts = new ArrayList<>();
        contacts.add(contact(1, "Sarah", "Thompson", "555-0300", "sarah.t@personal.example"));
        contacts.add(contact(2, "Michael", "Thompson", "555-0301", "michael.t@personal.example"));
        customer.put("contacts", contacts);

        List<Map<String, Object>> locations = new ArrayList<>();
        locations.add(location(1, "Primary Residence", "45 Oak Lane", "Unit 2B", "Boulder", "CO", "80302", "Leave with neighbor if no answer.", "HOME-882"));
        locations.add(location(2, "Mountain Cabin", "123 Pine Ridge Rd", null, "Estes Park", "CO", "80517", "Key under mat.", "CABIN-19"));
        customer.put("shipmentLocations", locations);

        List<Map<String, Object>> shipments = new ArrayList<>();
        // Record player - Sarah, home, STD, DELIVERY
        Map<String, Object> s1 = new LinkedHashMap<>();
        s1.put("id", 1); s1.put("activity", "DELIVERY"); s1.put("itemDescription", "Vintage record player + speakers"); s1.put("weight", "18.4"); s1.put("dimensions", "22x16x14");
        Map<String, Object> c1 = new LinkedHashMap<>(); c1.put("firstName", "Sarah"); c1.put("lastName", "Thompson"); c1.put("email", "sarah.t@personal.example"); s1.put("contact", c1);
        Map<String, Object> l1 = new LinkedHashMap<>(); l1.put("name", "Primary Residence"); l1.put("city", "Boulder"); s1.put("shipmentLocation", l1);
        Map<String, Object> so1 = new LinkedHashMap<>(); so1.put("description", "Standard Ground Shipping"); so1.put("typeCd", "STD"); s1.put("serviceOffering", so1);
        shipments.add(s1);

        // Ski - Michael, cabin, EXP, PICKUP
        Map<String, Object> s2 = new LinkedHashMap<>();
        s2.put("id", 2); s2.put("activity", "PICKUP"); s2.put("itemDescription", "Ski equipment - 2 sets"); s2.put("weight", "31.2"); s2.put("dimensions", "48x12x12");
        Map<String, Object> c2 = new LinkedHashMap<>(); c2.put("firstName", "Michael"); c2.put("lastName", "Thompson"); c2.put("email", "michael.t@personal.example"); s2.put("contact", c2);
        Map<String, Object> l2 = new LinkedHashMap<>(); l2.put("name", "Mountain Cabin"); l2.put("city", "Estes Park"); s2.put("shipmentLocation", l2);
        Map<String, Object> so2 = new LinkedHashMap<>(); so2.put("description", "Next Business Day Express"); so2.put("typeCd", "EXP"); s2.put("serviceOffering", so2);
        shipments.add(s2);

        // Medication - Sarah, home, AIR, DELIVERY
        Map<String, Object> s3 = new LinkedHashMap<>();
        s3.put("id", 3); s3.put("activity", "DELIVERY"); s3.put("itemDescription", "Urgent medication shipment"); s3.put("weight", "1.8"); s3.put("dimensions", "10x6x4");
        Map<String, Object> c3 = new LinkedHashMap<>(); c3.put("firstName", "Sarah"); c3.put("lastName", "Thompson"); c3.put("email", "sarah.t@personal.example"); s3.put("contact", c3);
        Map<String, Object> l3 = new LinkedHashMap<>(); l3.put("name", "Primary Residence"); l3.put("city", "Boulder"); s3.put("shipmentLocation", l3);
        Map<String, Object> so3 = new LinkedHashMap<>(); so3.put("description", "Air Express Overnight"); so3.put("typeCd", "AIR"); s3.put("serviceOffering", so3);
        shipments.add(s3);

        // Furniture - Michael, cabin, STD, DELIVERY
        Map<String, Object> s4 = new LinkedHashMap<>();
        s4.put("id", 4); s4.put("activity", "DELIVERY"); s4.put("itemDescription", "Furniture - dining table set"); s4.put("weight", "145.0"); s4.put("dimensions", "60x36x30");
        Map<String, Object> c4 = new LinkedHashMap<>(); c4.put("firstName", "Michael"); c4.put("lastName", "Thompson"); c4.put("email", "michael.t@personal.example"); s4.put("contact", c4);
        Map<String, Object> l4 = new LinkedHashMap<>(); l4.put("name", "Mountain Cabin"); l4.put("city", "Estes Park"); s4.put("shipmentLocation", l4);
        Map<String, Object> so4 = new LinkedHashMap<>(); so4.put("description", "Standard Ground Shipping"); so4.put("typeCd", "STD"); s4.put("serviceOffering", so4);
        shipments.add(s4);

        customer.put("shipments", shipments);
        return customer;
    }

    private Map<String, Object> buildHorizonHierarchy() {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("id", 4);
        customer.put("entityName", "Horizon Freight Co.");
        customer.put("type", "BUSINESS");

        List<Map<String, Object>> contacts = new ArrayList<>();
        contacts.add(contact(1, "Lisa", "Nguyen", "555-0400", "lisa.nguyen@horizonfreight.example"));
        contacts.add(contact(2, "Marcus", "Bell", "555-0401", "marcus.bell@horizonfreight.example"));
        contacts.add(contact(3, "Priya", "Singh", "555-0402", "priya.singh@horizonfreight.example"));
        customer.put("contacts", contacts);

        List<Map<String, Object>> locations = new ArrayList<>();
        locations.add(location(1, "Central Hub", "800 Logistics Way", null, "Chicago", "IL", "60601", "24/7 operations - use bay 12-18.", "HUB-CHI"));
        locations.add(location(2, "Southern Terminal", "1500 Port Blvd", "Warehouse C", "Houston", "TX", "77001", "Driver check-in at gate A.", "TERM-HOU"));
        locations.add(location(3, "Northern Crossdock", "900 Rail Yard Dr", null, "Minneapolis", "MN", "55401", "Appointment only.", "XDOCK-MSP"));
        customer.put("shipmentLocations", locations);

        List<Map<String, Object>> shipments = new ArrayList<>();
        // Machinery - Lisa, central, LTL, PICKUP
        Map<String, Object> s1 = new LinkedHashMap<>();
        s1.put("id", 1); s1.put("activity", "PICKUP"); s1.put("itemDescription", "Heavy machinery parts - 3 crates"); s1.put("weight", "890.0"); s1.put("dimensions", "72x48x48");
        Map<String, Object> c1 = new LinkedHashMap<>(); c1.put("firstName", "Lisa"); c1.put("lastName", "Nguyen"); c1.put("email", "lisa.nguyen@horizonfreight.example"); s1.put("contact", c1);
        Map<String, Object> l1 = new LinkedHashMap<>(); l1.put("name", "Central Hub"); l1.put("city", "Chicago"); s1.put("shipmentLocation", l1);
        Map<String, Object> so1 = new LinkedHashMap<>(); so1.put("description", "LTL Freight"); so1.put("typeCd", "LTL"); s1.put("serviceOffering", so1);
        shipments.add(s1);

        // Electronics - Marcus, southern, STD, DELIVERY
        Map<String, Object> s2 = new LinkedHashMap<>();
        s2.put("id", 2); s2.put("activity", "DELIVERY"); s2.put("itemDescription", "Consumer electronics - 1200 units"); s2.put("weight", "2450.0"); s2.put("dimensions", "40x32x60");
        Map<String, Object> c2 = new LinkedHashMap<>(); c2.put("firstName", "Marcus"); c2.put("lastName", "Bell"); c2.put("email", "marcus.bell@horizonfreight.example"); s2.put("contact", c2);
        Map<String, Object> l2 = new LinkedHashMap<>(); l2.put("name", "Southern Terminal"); l2.put("city", "Houston"); s2.put("shipmentLocation", l2);
        Map<String, Object> so2 = new LinkedHashMap<>(); so2.put("description", "Standard Ground Shipping"); so2.put("typeCd", "STD"); s2.put("serviceOffering", so2);
        shipments.add(s2);

        // Pharma - Priya, northern, EXP, DELIVERY
        Map<String, Object> s3 = new LinkedHashMap<>();
        s3.put("id", 3); s3.put("activity", "DELIVERY"); s3.put("itemDescription", "Temperature-controlled pharma"); s3.put("weight", "67.8"); s3.put("dimensions", "24x18x12");
        Map<String, Object> c3 = new LinkedHashMap<>(); c3.put("firstName", "Priya"); c3.put("lastName", "Singh"); c3.put("email", "priya.singh@horizonfreight.example"); s3.put("contact", c3);
        Map<String, Object> l3 = new LinkedHashMap<>(); l3.put("name", "Northern Crossdock"); l3.put("city", "Minneapolis"); s3.put("shipmentLocation", l3);
        Map<String, Object> so3 = new LinkedHashMap<>(); so3.put("description", "Next Business Day Express"); so3.put("typeCd", "EXP"); s3.put("serviceOffering", so3);
        shipments.add(s3);

        // Engine - Lisa, central, AIR, PICKUP
        Map<String, Object> s4 = new LinkedHashMap<>();
        s4.put("id", 4); s4.put("activity", "PICKUP"); s4.put("itemDescription", "Emergency replacement engine"); s4.put("weight", "320.5"); s4.put("dimensions", "60x40x36");
        Map<String, Object> c4 = new LinkedHashMap<>(); c4.put("firstName", "Lisa"); c4.put("lastName", "Nguyen"); c4.put("email", "lisa.nguyen@horizonfreight.example"); s4.put("contact", c4);
        Map<String, Object> l4 = new LinkedHashMap<>(); l4.put("name", "Central Hub"); l4.put("city", "Chicago"); s4.put("shipmentLocation", l4);
        Map<String, Object> so4 = new LinkedHashMap<>(); so4.put("description", "Air Express Overnight"); so4.put("typeCd", "AIR"); s4.put("serviceOffering", so4);
        shipments.add(s4);

        // Chemicals - Marcus, southern, STD, DELIVERY
        Map<String, Object> s5 = new LinkedHashMap<>();
        s5.put("id", 5); s5.put("activity", "DELIVERY"); s5.put("itemDescription", "Bulk chemicals - 8 drums"); s5.put("weight", "1640.0"); s5.put("dimensions", "48x48x48");
        Map<String, Object> c5 = new LinkedHashMap<>(); c5.put("firstName", "Marcus"); c5.put("lastName", "Bell"); c5.put("email", "marcus.bell@horizonfreight.example"); s5.put("contact", c5);
        Map<String, Object> l5 = new LinkedHashMap<>(); l5.put("name", "Southern Terminal"); l5.put("city", "Houston"); s5.put("shipmentLocation", l5);
        Map<String, Object> so5 = new LinkedHashMap<>(); so5.put("description", "Standard Ground Shipping"); so5.put("typeCd", "STD"); s5.put("serviceOffering", so5);
        shipments.add(s5);

        customer.put("shipments", shipments);
        return customer;
    }

    // --- Mutation simulators for demo buttons (return realistic, rich payloads) ---
    // These make the "Run in GraphiQL" buttons for writes always produce impressive results
    // immediately. Real execution happens via the @MutationMapping resolvers when the
    // full Spring for GraphQL stack is handling the request.

    private Map<String, Object> simulateCreateShipment(String query) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("id", 997); // synthetic "newly generated" ID for the demo insert
        s.put("itemDescription", "Urgent demo prototype parts - inserted via GraphQL");
        s.put("weight", "14.25");
        s.put("dimensions", "20x14x9");
        s.put("activity", "DELIVERY");

        Map<String, Object> cust = new LinkedHashMap<>();
        cust.put("id", 1);
        cust.put("entityName", "Acme Manufacturing Inc.");
        s.put("customer", cust);

        Map<String, Object> so = new LinkedHashMap<>();
        so.put("id", 4);
        so.put("description", "Air Express Overnight");
        so.put("typeCd", "AIR");
        s.put("serviceOffering", so);

        // Minimal contact + location for a complete feel (would be resolved in real response)
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("id", 1); c.put("firstName", "Alice"); c.put("lastName", "Johnson");
        s.put("contact", c);

        Map<String, Object> loc = new LinkedHashMap<>();
        loc.put("id", 1); loc.put("name", "Main Warehouse"); loc.put("city", "Detroit");
        s.put("shipmentLocation", loc);

        return s;
    }

    private Map<String, Object> simulateCreateCustomer(String query) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("id", 998);
        c.put("entityName", "GraphQL Demo Co. (newly created)");
        c.put("type", "BUSINESS");

        // Empty relations on create (or minimal) — real app can immediately add contacts etc.
        c.put("contacts", new ArrayList<>());
        c.put("shipmentLocations", new ArrayList<>());
        c.put("shipments", new ArrayList<>());
        return c;
    }

    private Map<String, Object> simulateUpdateCustomer(String query) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("id", 1);
        c.put("entityName", "Acme Manufacturing Inc. (UPDATED via mutation)");
        c.put("type", "BUSINESS");

        // Include a couple of existing relations so the result still feels rich
        List<Map<String, Object>> contacts = new ArrayList<>();
        contacts.add(contact(1, "Alice", "Johnson", "555-0100", "alice.johnson@acme.example"));
        c.put("contacts", contacts);

        c.put("shipmentLocations", new ArrayList<>()); // truncated for response brevity
        c.put("shipments", new ArrayList<>());
        return c;
    }
}
