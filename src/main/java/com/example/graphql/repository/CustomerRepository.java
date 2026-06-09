package com.example.graphql.repository;

import com.example.graphql.domain.Customer;
import com.example.graphql.domain.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    List<Customer> findByType(CustomerType type);

    Page<Customer> findByType(CustomerType type, Pageable pageable);

    List<Customer> findByEntityNameContainingIgnoreCase(String namePart);
}
