package com.example.graphql.repository;

import com.example.graphql.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByCustomerId(Long customerId);

    List<Contact> findByLastNameContainingIgnoreCase(String lastName);
}
