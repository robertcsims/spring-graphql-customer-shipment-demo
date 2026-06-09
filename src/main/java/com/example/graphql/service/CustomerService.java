package com.example.graphql.service;

import com.example.graphql.domain.Customer;
import com.example.graphql.domain.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CustomerService {

    Customer createCustomer(String entityName, CustomerType type);

    Optional<Customer> getCustomerById(Long id);

    List<Customer> getAllCustomers();

    Page<Customer> getCustomersFiltered(CustomerType type, String entityNameContains, Pageable pageable);

    Customer updateCustomer(Long id, String entityName, CustomerType type);

    void deleteCustomer(Long id);

    // For GraphQL nested + seeding helpers
    Customer save(Customer customer);

    /**
     * Returns customer with contacts, locations and shipments initialized (avoids LazyInitializationException in GraphQL nested resolvers).
     */
    Customer getCustomerWithAllRelations(Long id);
}
