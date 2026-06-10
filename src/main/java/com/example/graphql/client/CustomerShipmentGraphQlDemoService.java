package com.example.graphql.client;

import com.example.graphql.client.generated.model.ActivityType;
import com.example.graphql.client.generated.model.CreateCustomerInput;
import com.example.graphql.client.generated.model.CreateCustomerMutationRequest;
import com.example.graphql.client.generated.model.CreateCustomerMutationResponse;
import com.example.graphql.client.generated.model.CreateShipmentInput;
import com.example.graphql.client.generated.model.CreateShipmentMutationRequest;
import com.example.graphql.client.generated.model.CreateShipmentMutationResponse;
import com.example.graphql.client.generated.model.Customer;
import com.example.graphql.client.generated.model.CustomerFilter;
import com.example.graphql.client.generated.model.CustomerPage;
import com.example.graphql.client.generated.model.CustomerResponseProjection;
import com.example.graphql.client.generated.model.CustomerType;
import com.example.graphql.client.generated.model.CustomersQueryRequest;
import com.example.graphql.client.generated.model.CustomersQueryResponse;
import com.example.graphql.client.generated.model.ServiceOffering;
import com.example.graphql.client.generated.model.ServiceOfferingResponseProjection;
import com.example.graphql.client.generated.model.ServiceOfferingsQueryRequest;
import com.example.graphql.client.generated.model.ServiceOfferingsQueryResponse;
import com.example.graphql.client.generated.model.ShipmentFilter;
import com.example.graphql.client.generated.model.ShipmentPage;
import com.example.graphql.client.generated.model.ShipmentResponseProjection;
import com.example.graphql.client.generated.model.ShipmentsQueryRequest;
import com.example.graphql.client.generated.model.ShipmentsQueryResponse;
import com.example.graphql.client.generated.model.UpdateCustomerMutationRequest;
import com.example.graphql.client.generated.model.UpdateCustomerMutationResponse;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerShipmentGraphQlDemoService {

    public static final String ACME_FILTER = "Acme";

    private final CustomerShipmentGraphQlClient graphQlClient;

    public CustomerShipmentGraphQlDemoService(CustomerShipmentGraphQlClient graphQlClient) {
        this.graphQlClient = graphQlClient;
    }

    public GraphQlClientDemoReport runAllDemoOperations() {
        List<GraphQlClientOperationResult> results = new ArrayList<>();
        results.add(runSafely("FullRelationalHierarchy", this::fullRelationalHierarchy));
        results.add(runSafely("AdvancedFiltering", this::advancedFiltering));
        results.add(runSafely("NestedPagination", this::nestedPagination));
        results.add(runSafely("ListServiceOfferings", this::listServiceOfferings));
        results.add(runSafely("GetAllCustomers", this::getAllCustomers));
        results.add(runSafely("GetAllShipments", this::getAllShipments));
        results.add(runSafely("ShipmentCount", this::shipmentCount));
        results.add(runSafely("CreateCustomer", this::createCustomer));
        results.add(runSafely("CreateShipment", this::createShipment));
        results.add(runSafely("UpdateCustomer", this::updateCustomer));

        int passed = (int) results.stream().filter(GraphQlClientOperationResult::success).count();
        return new GraphQlClientDemoReport(
                passed == results.size(),
                passed,
                results.size(),
                "graphql-java-codegen typed client",
                "src/main/resources/graphql-client/operations/*.graphql",
                java.time.Instant.now(),
                results
        );
    }

    private GraphQlClientOperationResult runSafely(String name, OperationRunner runner) {
        try {
            return GraphQlClientOperationResult.ok(name, runner.run());
        } catch (Exception ex) {
            return GraphQlClientOperationResult.failed(name, ex.getMessage());
        }
    }

    private String fullRelationalHierarchy() {
        CustomersQueryResponse response = graphQlClient.execute(
                new GraphQLRequest(
                        "FullRelationalHierarchy",
                        CustomersQueryRequest.builder()
                                .setFilter(CustomerFilter.builder().setEntityNameContains(ACME_FILTER).build())
                                .setPage(0)
                                .setSize(1)
                                .build(),
                        CustomerShipmentGraphQlProjections.fullRelationalHierarchy()
                ),
                CustomersQueryResponse.class
        );
        CustomerPage page = response.customers();
        Customer customer = page.getContent().getFirst();
        ShipmentsQueryResponse shipments = graphQlClient.execute(
                new GraphQLRequest(
                        "FullRelationalHierarchyShipments",
                        ShipmentsQueryRequest.builder()
                                .setFilter(ShipmentFilter.builder().setCustomerId(customer.getId()).build())
                                .setPage(0)
                                .setSize(20)
                                .build(),
                        CustomerShipmentGraphQlProjections.customerShipmentsSummary()
                ),
                ShipmentsQueryResponse.class
        );
        return "customer=%s, contacts=%d, locations=%d, shipments=%d, totalElements=%d"
                .formatted(customer.getEntityName(), customer.getContacts().size(),
                        customer.getShipmentLocations().size(), shipments.shipments().getTotalElements(),
                        page.getTotalElements());
    }

    private String advancedFiltering() {
        ShipmentsQueryResponse response = graphQlClient.execute(
                new GraphQLRequest(
                        "AdvancedFiltering",
                        ShipmentsQueryRequest.builder()
                                .setFilter(ShipmentFilter.builder()
                                        .setActivity(ActivityType.PICKUP)
                                        .setMinWeight(10.0)
                                        .build())
                                .setPage(0)
                                .setSize(5)
                                .setSort(List.of("-weight"))
                                .build(),
                        CustomerShipmentGraphQlProjections.advancedFiltering()
                ),
                ShipmentsQueryResponse.class
        );
        ShipmentPage page = response.shipments();
        return "pickups>=10lb: count=%d, topWeight=%s"
                .formatted(page.getContent().size(), page.getContent().getFirst().getWeight());
    }

    private String nestedPagination() {
        AcmeContext acme = resolveAcmeContext();
        ShipmentsQueryResponse response = graphQlClient.execute(
                new GraphQLRequest(
                        "NestedPagination",
                        ShipmentsQueryRequest.builder()
                                .setFilter(ShipmentFilter.builder().setCustomerId(acme.customerId()).build())
                                .setPage(0)
                                .setSize(2)
                                .setSort(List.of("-weight"))
                                .build(),
                        CustomerShipmentGraphQlProjections.nestedCustomerShipments()
                ),
                ShipmentsQueryResponse.class
        );
        var shipments = response.shipments().getContent();
        return "customerId=%s, topShipments=%d, heaviest=%s"
                .formatted(acme.customerId(), shipments.size(), shipments.getFirst().getWeight());
    }

    private String listServiceOfferings() {
        ServiceOfferingsQueryResponse response = graphQlClient.execute(
                new GraphQLRequest(
                        ServiceOfferingsQueryRequest.builder().build(),
                        new ServiceOfferingResponseProjection().id().description().typeCd()
                ),
                ServiceOfferingsQueryResponse.class
        );
        List<ServiceOffering> offerings = response.serviceOfferings();
        return "offerings=%d, first=%s (%s)"
                .formatted(offerings.size(), offerings.getFirst().getDescription(), offerings.getFirst().getTypeCd());
    }

    private String getAllCustomers() {
        CustomersQueryResponse response = graphQlClient.execute(
                new GraphQLRequest(
                        "GetAllCustomers",
                        CustomersQueryRequest.builder().setPage(0).setSize(20).build(),
                        CustomerShipmentGraphQlProjections.allCustomers()
                ),
                CustomersQueryResponse.class
        );
        return "customers=%d".formatted(response.customers().getTotalElements());
    }

    private String getAllShipments() {
        ShipmentsQueryResponse response = graphQlClient.execute(
                new GraphQLRequest(
                        "GetAllShipments",
                        ShipmentsQueryRequest.builder().setPage(0).setSize(20).build(),
                        CustomerShipmentGraphQlProjections.allShipments()
                ),
                ShipmentsQueryResponse.class
        );
        return "shipments=%d".formatted(response.shipments().getTotalElements());
    }

    private String shipmentCount() {
        ShipmentsQueryResponse response = graphQlClient.execute(
                new GraphQLRequest(
                        "ShipmentCount",
                        ShipmentsQueryRequest.builder().setPage(0).setSize(1).build(),
                        CustomerShipmentGraphQlProjections.shipmentCount()
                ),
                ShipmentsQueryResponse.class
        );
        return "totalElements=%d".formatted(response.shipments().getTotalElements());
    }

    private String createCustomer() {
        String uniqueName = "Quantum Logistics Demo LLC " + UUID.randomUUID().toString().substring(0, 8);
        CreateCustomerMutationResponse response = graphQlClient.execute(
                new GraphQLRequest(
                        "CreateCustomer",
                        CreateCustomerMutationRequest.builder()
                                .setInput(CreateCustomerInput.builder()
                                        .setEntityName(uniqueName)
                                        .setType(CustomerType.BUSINESS)
                                        .build())
                                .build(),
                        new CustomerResponseProjection().id().entityName().type()
                                .contacts(new com.example.graphql.client.generated.model.ContactResponseProjection().firstName())
                ),
                CreateCustomerMutationResponse.class
        );
        Customer created = response.createCustomer();
        return "created id=%s name=%s".formatted(created.getId(), created.getEntityName());
    }

    private String createShipment() {
        AcmeContext acme = resolveAcmeContext();
        String description = "Typed client shipment " + UUID.randomUUID().toString().substring(0, 8);
        CreateShipmentMutationResponse response = graphQlClient.execute(
                new GraphQLRequest(
                        "CreateShipment",
                        CreateShipmentMutationRequest.builder()
                                .setInput(CreateShipmentInput.builder()
                                        .setCustomerId(acme.customerId())
                                        .setContactId(acme.contactId())
                                        .setShipmentLocationId(acme.locationId())
                                        .setServiceOfferingId(acme.airOfferingId())
                                        .setItemDescription(description)
                                        .setWeight(14.25)
                                        .setDimensions("20x14x9")
                                        .setActivity(ActivityType.DELIVERY)
                                        .build())
                                .build(),
                        new ShipmentResponseProjection()
                                .id().itemDescription()
                                .customer(new CustomerResponseProjection().id().entityName())
                                .serviceOffering(new ServiceOfferingResponseProjection().description().typeCd())
                ),
                CreateShipmentMutationResponse.class
        );
        return "created id=%s desc=%s customer=%s"
                .formatted(response.createShipment().getId(), response.createShipment().getItemDescription(),
                        response.createShipment().getCustomer().getEntityName());
    }

    private String updateCustomer() {
        AcmeContext acme = resolveAcmeContext();
        String updatedName = "Acme Manufacturing Inc. — Updated via Typed Client";
        UpdateCustomerMutationResponse response = graphQlClient.execute(
                new GraphQLRequest(
                        "UpdateCustomer",
                        UpdateCustomerMutationRequest.builder()
                                .setId(acme.customerId())
                                .setEntityName(updatedName)
                                .setType(CustomerType.BUSINESS)
                                .build(),
                        new CustomerResponseProjection().id().entityName().type()
                                .contacts(new com.example.graphql.client.generated.model.ContactResponseProjection()
                                        .firstName().lastName().email())
                ),
                UpdateCustomerMutationResponse.class
        );
        return "updated id=%s name=%s contacts=%d"
                .formatted(response.updateCustomer().getId(), response.updateCustomer().getEntityName(),
                        response.updateCustomer().getContacts().size());
    }

    private AcmeContext resolveAcmeContext() {
        CustomersQueryResponse lookup = graphQlClient.execute(
                new GraphQLRequest(
                        "FindAcmeCustomer",
                        CustomersQueryRequest.builder()
                                .setFilter(CustomerFilter.builder().setEntityNameContains(ACME_FILTER).build())
                                .setPage(0)
                                .setSize(1)
                                .build(),
                        CustomerShipmentGraphQlProjections.findCustomerSummary()
                ),
                CustomersQueryResponse.class
        );
        Customer acme = lookup.customers().getContent().getFirst();
        String airOfferingId = graphQlClient.execute(
                new GraphQLRequest(
                        ServiceOfferingsQueryRequest.builder().build(),
                        new ServiceOfferingResponseProjection().id().typeCd()
                ),
                ServiceOfferingsQueryResponse.class
        ).serviceOfferings().stream()
                .filter(o -> "AIR".equals(o.getTypeCd()))
                .map(ServiceOffering::getId)
                .findFirst()
                .orElseThrow(() -> new GraphQlClientException("AIR service offering not found"));

        return new AcmeContext(
                acme.getId(),
                acme.getContacts().getFirst().getId(),
                acme.getShipmentLocations().getFirst().getId(),
                airOfferingId
        );
    }

    private record AcmeContext(String customerId, String contactId, String locationId, String airOfferingId) {
    }

    @FunctionalInterface
    private interface OperationRunner {
        String run();
    }
}