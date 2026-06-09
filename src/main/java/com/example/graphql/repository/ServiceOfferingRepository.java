package com.example.graphql.repository;

import com.example.graphql.domain.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {

    List<ServiceOffering> findAll();

    Optional<ServiceOffering> findByTypeCd(String typeCd);
}
