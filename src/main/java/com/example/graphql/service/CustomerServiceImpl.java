package com.example.graphql.service;

import com.example.graphql.domain.Customer;
import com.example.graphql.domain.CustomerType;
import com.example.graphql.repository.CustomerRepository;
import com.example.graphql.repository.CustomerSpecifications;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public Customer createCustomer(String entityName, CustomerType type) {
        Customer customer = new Customer(entityName, type);
        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Page<Customer> getCustomersFiltered(CustomerType type, String entityNameContains, Pageable pageable) {
        Specification<Customer> spec = Specification.where(CustomerSpecifications.hasType(type))
                .and(CustomerSpecifications.entityNameContains(entityNameContains));
        return customerRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public Customer updateCustomer(Long id, String entityName, CustomerType type) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + id));
        if (entityName != null) customer.setEntityName(entityName);
        if (type != null) customer.setType(type);
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerWithAllRelations(Long id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer != null) {
            // Force initialization of lazy collections for GraphQL deep queries
            Hibernate.initialize(customer.getContacts());
            Hibernate.initialize(customer.getShipmentLocations());
            Hibernate.initialize(customer.getShipments());
            // Also initialize nested objects on shipments for full hierarchy
            customer.getShipments().forEach(s -> {
                Hibernate.initialize(s.getContact());
                Hibernate.initialize(s.getShipmentLocation());
                Hibernate.initialize(s.getServiceOffering());
            });
        }
        return customer;
    }
}
