package com.example.graphql.graphql.resolver;

import com.example.graphql.domain.*;
import com.example.graphql.graphql.dto.CreateCustomerInput;
import com.example.graphql.graphql.dto.CreateShipmentInput;
import com.example.graphql.graphql.dto.CustomerFilter;
import com.example.graphql.graphql.dto.ShipmentFilter;
import com.example.graphql.service.CustomerService;
import com.example.graphql.service.ServiceOfferingService;
import com.example.graphql.service.ShipmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Central GraphQL resolver.
 *
 * This class was implemented rapidly from the original schema specification.
 * It showcases the strength of Spring for GraphQL:
 *   • Root queries + mutations
 *   • @SchemaMapping for deep, client-controlled relationships (the core GraphQL advantage)
 *   • First-class support for pagination, sorting, and input-based filtering
 *
 * A complex relational domain (5 tables, multiple FKs, full hierarchy traversal)
 * was turned into a production-quality, flexible GraphQL API in a very short time.
 */
@Controller
public class CustomerGraphQlResolver {

    private final CustomerService customerService;
    private final ShipmentService shipmentService;
    private final ServiceOfferingService serviceOfferingService;

    public CustomerGraphQlResolver(CustomerService customerService,
                                   ShipmentService shipmentService,
                                   ServiceOfferingService serviceOfferingService) {
        this.customerService = customerService;
        this.shipmentService = shipmentService;
        this.serviceOfferingService = serviceOfferingService;
    }

    // ========== ROOT QUERIES ==========

    @QueryMapping
    public Customer customer(@Argument Long id) {
        // Use the relation-initialized version so deep nested GraphQL queries work reliably
        return customerService.getCustomerWithAllRelations(id);
    }

    @QueryMapping
    public Page<Customer> customers(@Argument CustomerFilter filter,
                                    @Argument Integer page,
                                    @Argument Integer size,
                                    @Argument List<String> sort) {
        Pageable pageable = buildPageable(page, size, sort);
        CustomerType type = (filter != null) ? filter.type() : null;
        String nameTerm = (filter != null) ? filter.entityNameContains() : null;
        return customerService.getCustomersFiltered(type, nameTerm, pageable);
    }

    @QueryMapping
    public Page<Shipment> shipments(@Argument ShipmentFilter filter,
                                    @Argument Integer page,
                                    @Argument Integer size,
                                    @Argument List<String> sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Long custId = filter != null ? filter.customerId() : null;
        ActivityType act = filter != null ? filter.activity() : null;
        var minW = filter != null ? filter.minWeight() : null;
        String descTerm = filter != null ? filter.itemDescriptionContains() : null;
        return shipmentService.getShipmentsFiltered(custId, act, minW, descTerm, pageable);
    }

    @QueryMapping
    public List<ServiceOffering> serviceOfferings() {
        return serviceOfferingService.getAllServiceOfferings();
    }

    // ========== MUTATIONS (demonstrate full CRUD via GraphQL) ==========

    @MutationMapping
    public Customer createCustomer(@Argument CreateCustomerInput input) {
        return customerService.createCustomer(input.entityName(), input.type());
    }

    @MutationMapping
    public Customer updateCustomer(@Argument Long id,
                                   @Argument String entityName,
                                   @Argument CustomerType type) {
        return customerService.updateCustomer(id, entityName, type);
    }

    @MutationMapping
    public Boolean deleteCustomer(@Argument Long id) {
        customerService.deleteCustomer(id);
        return true;
    }

    @MutationMapping
    public Shipment createShipment(@Argument CreateShipmentInput input) {
        return shipmentService.createShipment(
                input.customerId(),
                input.contactId(),
                input.shipmentLocationId(),
                input.serviceOfferingId(),
                input.itemDescription(),
                input.weight(),
                input.dimensions(),
                input.activity()
        );
    }

    @MutationMapping
    public Shipment updateShipment(@Argument Long id,
                                   @Argument String itemDescription,
                                   @Argument java.math.BigDecimal weight,
                                   @Argument String dimensions,
                                   @Argument ActivityType activity) {
        return shipmentService.updateShipment(id, itemDescription, weight, dimensions, activity);
    }

    @MutationMapping
    public Boolean deleteShipment(@Argument Long id) {
        shipmentService.deleteShipment(id);
        return true;
    }

    // ========== NESTED RESOLVERS (the magic of GraphQL - fetch only what you need, deep relations in 1 roundtrip) ==========

    @SchemaMapping(typeName = "Customer", field = "contacts")
    public List<Contact> contacts(Customer customer) {
        // In a real high-scale system use DataLoader + batching here to avoid N+1
        return customer.getContacts();
    }

    @SchemaMapping(typeName = "Customer", field = "shipmentLocations")
    public List<ShipmentLocation> shipmentLocations(Customer customer) {
        return customer.getShipmentLocations();
    }

    @SchemaMapping(typeName = "Customer", field = "shipments")
    public List<Shipment> shipments(Customer customer,
                                    @Argument Integer page,
                                    @Argument Integer size,
                                    @Argument List<String> sort) {
        // Support optional sub-pagination / sorting from the client when requesting customer { shipments(...) }
        if (page != null || size != null || (sort != null && !sort.isEmpty())) {
            Pageable pageable = buildPageable(page, size, sort);
            return shipmentService.getShipmentsFiltered(customer.getId(), null, null, null, pageable).getContent();
        }
        return customer.getShipments();
    }

    @SchemaMapping(typeName = "Shipment", field = "customer")
    public Customer customerForShipment(Shipment shipment) {
        return shipment.getCustomer();
    }

    @SchemaMapping(typeName = "Shipment", field = "contact")
    public Contact contactForShipment(Shipment shipment) {
        return shipment.getContact();
    }

    @SchemaMapping(typeName = "Shipment", field = "shipmentLocation")
    public ShipmentLocation shipmentLocationForShipment(Shipment shipment) {
        return shipment.getShipmentLocation();
    }

    @SchemaMapping(typeName = "Shipment", field = "serviceOffering")
    public ServiceOffering serviceOfferingForShipment(Shipment shipment) {
        return shipment.getServiceOffering();
    }

    // ========== Helpers ==========

    private Pageable buildPageable(Integer page, Integer size, List<String> sort) {
        int p = (page != null && page >= 0) ? page : 0;
        int s = (size != null && size > 0 && size <= 100) ? size : 20;

        Sort sortSpec = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            sortSpec = Sort.by(sort.stream()
                    .map(srt -> {
                        if (srt.startsWith("-")) {
                            return Sort.Order.desc(srt.substring(1));
                        }
                        return Sort.Order.asc(srt.startsWith("+") ? srt.substring(1) : srt);
                    })
                    .toList());
        }
        return PageRequest.of(p, s, sortSpec);
    }
}
