package com.example.graphql.service;

import com.example.graphql.domain.Customer;
import com.example.graphql.domain.CustomerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerServiceImplTest {

    @Autowired
    private CustomerService customerService;

    @Test
    void createCustomer_shouldPersistAndReturnEntity() {
        Customer created = customerService.createCustomer("Acme Corp", CustomerType.BUSINESS);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEntityName()).isEqualTo("Acme Corp");
        assertThat(created.getType()).isEqualTo(CustomerType.BUSINESS);
    }

    @Test
    void getCustomerById_shouldReturnWhenExists() {
        Customer c = customerService.createCustomer("Personal User", CustomerType.PERSONAL);

        Optional<Customer> found = customerService.getCustomerById(c.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEntityName()).isEqualTo("Personal User");
    }

    @Test
    void getCustomersFiltered_shouldSupportTypeAndNameFilterAndPagination() {
        customerService.createCustomer("TechStart LLC", CustomerType.BUSINESS);
        customerService.createCustomer("John Doe Consulting", CustomerType.BUSINESS);
        customerService.createCustomer("Jane Smith", CustomerType.PERSONAL);

        Page<Customer> businessPage = customerService.getCustomersFiltered(
                CustomerType.BUSINESS, "tech", PageRequest.of(0, 10));

        assertThat(businessPage.getContent()).hasSize(1);
        assertThat(businessPage.getContent().get(0).getEntityName()).isEqualTo("TechStart LLC");

        Page<Customer> allBusiness = customerService.getCustomersFiltered(
                CustomerType.BUSINESS, null, PageRequest.of(0, 5));
        // In this isolated test we created exactly 2 BUSINESS customers
        assertThat(allBusiness.getTotalElements()).isEqualTo(2);
    }

    @Test
    void updateCustomer_shouldModifyFields() {
        Customer c = customerService.createCustomer("Old Name", CustomerType.PERSONAL);

        Customer updated = customerService.updateCustomer(c.getId(), "New Name LLC", CustomerType.BUSINESS);

        assertThat(updated.getEntityName()).isEqualTo("New Name LLC");
        assertThat(updated.getType()).isEqualTo(CustomerType.BUSINESS);
    }

    @Test
    void deleteCustomer_shouldRemove() {
        Customer c = customerService.createCustomer("To Delete", CustomerType.PERSONAL);
        Long id = c.getId();

        customerService.deleteCustomer(id);

        assertThat(customerService.getCustomerById(id)).isEmpty();
    }

    @Test
    void getAllCustomers_returnsSeededOrCreated() {
        customerService.createCustomer("List Test 1", CustomerType.BUSINESS);
        List<Customer> all = customerService.getAllCustomers();
        assertThat(all).isNotEmpty();
    }

    @Test
    void updateNonExistent_shouldThrow() {
        assertThatThrownBy(() -> customerService.updateCustomer(99999L, "x", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}
