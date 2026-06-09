package com.example.graphql.service;

import com.example.graphql.domain.ServiceOffering;

import java.util.List;
import java.util.Optional;

public interface ServiceOfferingService {

    List<ServiceOffering> getAllServiceOfferings();

    Optional<ServiceOffering> getById(Long id);

    ServiceOffering create(String description, String typeCd);
}
