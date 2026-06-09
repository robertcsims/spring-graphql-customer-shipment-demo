package com.example.graphql.service;

import com.example.graphql.domain.ServiceOffering;
import com.example.graphql.repository.ServiceOfferingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ServiceOfferingServiceImpl implements ServiceOfferingService {

    private final ServiceOfferingRepository serviceOfferingRepository;

    public ServiceOfferingServiceImpl(ServiceOfferingRepository serviceOfferingRepository) {
        this.serviceOfferingRepository = serviceOfferingRepository;
    }

    @Override
    public List<ServiceOffering> getAllServiceOfferings() {
        return serviceOfferingRepository.findAll();
    }

    @Override
    public Optional<ServiceOffering> getById(Long id) {
        return serviceOfferingRepository.findById(id);
    }

    @Override
    @Transactional
    public ServiceOffering create(String description, String typeCd) {
        return serviceOfferingRepository.save(new ServiceOffering(description, typeCd));
    }
}
