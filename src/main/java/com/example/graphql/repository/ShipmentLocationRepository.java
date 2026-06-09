package com.example.graphql.repository;

import com.example.graphql.domain.ShipmentLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentLocationRepository extends JpaRepository<ShipmentLocation, Long> {

    List<ShipmentLocation> findByCustomerId(Long customerId);
}
