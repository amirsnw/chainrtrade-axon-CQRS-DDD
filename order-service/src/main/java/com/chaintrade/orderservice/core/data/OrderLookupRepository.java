package com.chaintrade.orderservice.core.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderLookupRepository extends JpaRepository<OrderLookupEntity, String> {

    Optional<OrderLookupEntity> findFirstByCustomerIdAndStatusOrderByDateCreatedDesc(String customerId, OrderStatus status);
} 