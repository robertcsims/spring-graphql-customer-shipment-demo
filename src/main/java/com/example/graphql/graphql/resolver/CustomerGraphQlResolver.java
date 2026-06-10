package com.example.graphql.graphql.resolver;

import com.example.graphql.domain.*;
import com.example.graphql.graphql.dto.CreateCustomerInput;
import com.example.graphql.graphql.dto.CreateShipmentInput;
import com.example.graphql.graphql.dto.CustomerFilter;
import com.example.graphql.graphql.dto.CustomerPage;
import com.example.graphql.graphql.dto.ShipmentFilter;
import com.example.graphql.graphql.dto.ShipmentPage;
import com.example.graphql.service.CustomerService;
import com.example.graphql.service.ServiceOfferingService;
import com.example.graphql.service.ShipmentService;
import org.hibernate.Hibernate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Central GraphQL resolver backed by Derby persistence via JPA services.
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
    @Transactional(readOnly = true)
    public Customer customer(@Argument Long id) {
        return customerService.getCustomerWithAllRelations(id);
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public CustomerPage customers(@Argument CustomerFilter filter,
                                  @Argument Integer page,
                                  @Argument Integer size,
                                  @Argument List<String> sort) {
        Pageable pageable = buildPageable(page, size, sort);
        CustomerType type = (filter != null) ? filter.type() : null;
        String nameTerm = (filter != null) ? filter.entityNameContains() : null;
        var result = customerService.getCustomersFiltered(type, nameTerm, pageable);
        // Eagerly initialize nested collections while the session is open so deep
        // GraphQL selections work with open-in-view disabled.
        result.getContent().forEach(customerService::initializeCustomerGraph);
        return CustomerPage.from(result);
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public ShipmentPage shipments(@Argument ShipmentFilter filter,
                                  @Argument Integer page,
                                  @Argument Integer size,
                                  @Argument List<String> sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Long custId = filter != null ? filter.customerId() : null;
        ActivityType act = filter != null ? filter.activity() : null;
        var minW = filter != null ? filter.minWeight() : null;
        String descTerm = filter != null ? filter.itemDescriptionContains() : null;
        var result = shipmentService.getShipmentsFiltered(custId, act, minW, descTerm, pageable);
        result.getContent().forEach(shipmentService::initializeShipmentGraph);
        return ShipmentPage.from(result);
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public List<ServiceOffering> serviceOfferings() {
        return serviceOfferingService.getAllServiceOfferings();
    }

    // ========== MUTATIONS ==========

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

    // ========== NESTED RESOLVERS ==========

    @SchemaMapping(typeName = "Customer", field = "contacts")
    @Transactional(readOnly = true)
    public List<Contact> contacts(Customer customer) {
        Hibernate.initialize(customer.getContacts());
        return customer.getContacts();
    }

    @SchemaMapping(typeName = "Customer", field = "shipmentLocations")
    @Transactional(readOnly = true)
    public List<ShipmentLocation> shipmentLocations(Customer customer) {
        Hibernate.initialize(customer.getShipmentLocations());
        return customer.getShipmentLocations();
    }

    @SchemaMapping(typeName = "Customer", field = "shipments")
    @Transactional(readOnly = true)
    public List<Shipment> shipments(Customer customer,
                                    @Argument Integer page,
                                    @Argument Integer size,
                                    @Argument List<String> sort) {
        if (page != null || size != null || (sort != null && !sort.isEmpty())) {
            Pageable pageable = buildPageable(page, size, sort);
            return shipmentService.getShipmentsFiltered(customer.getId(), null, null, null, pageable).getContent();
        }
        customerService.initializeCustomerGraph(customer);
        return customer.getShipments();
    }

    @SchemaMapping(typeName = "Shipment", field = "customer")
    @Transactional(readOnly = true)
    public Customer customerForShipment(Shipment shipment) {
        Hibernate.initialize(shipment.getCustomer());
        return shipment.getCustomer();
    }

    @SchemaMapping(typeName = "Shipment", field = "contact")
    @Transactional(readOnly = true)
    public Contact contactForShipment(Shipment shipment) {
        Hibernate.initialize(shipment.getContact());
        return shipment.getContact();
    }

    @SchemaMapping(typeName = "Shipment", field = "shipmentLocation")
    @Transactional(readOnly = true)
    public ShipmentLocation shipmentLocationForShipment(Shipment shipment) {
        Hibernate.initialize(shipment.getShipmentLocation());
        return shipment.getShipmentLocation();
    }

    @SchemaMapping(typeName = "Shipment", field = "serviceOffering")
    @Transactional(readOnly = true)
    public ServiceOffering serviceOfferingForShipment(Shipment shipment) {
        Hibernate.initialize(shipment.getServiceOffering());
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