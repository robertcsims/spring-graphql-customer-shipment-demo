package com.example.graphql.service;

import com.example.graphql.domain.ServiceOffering;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceOfferingServiceImplTest {

    @Autowired
    private ServiceOfferingService serviceOfferingService;

    @Test
    void createAndRetrieveServiceOffering() {
        ServiceOffering created = serviceOfferingService.create("Next Day Air", "EXP");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Next Day Air");
        assertThat(created.getTypeCd()).isEqualTo("EXP");

        List<ServiceOffering> all = serviceOfferingService.getAllServiceOfferings();
        assertThat(all).extracting(ServiceOffering::getTypeCd).contains("EXP");
    }
}
