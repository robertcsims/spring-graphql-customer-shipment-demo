package com.example.graphql.repository;

import com.example.graphql.domain.ActivityType;
import com.example.graphql.domain.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long>, JpaSpecificationExecutor<Shipment> {

    List<Shipment> findByCustomerId(Long customerId);

    Page<Shipment> findByCustomerId(Long customerId, Pageable pageable);

    List<Shipment> findByActivity(ActivityType activity);
}
