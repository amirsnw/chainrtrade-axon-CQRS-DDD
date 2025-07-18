package com.chaintrade.orderservice.core.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderLookupRepository extends JpaRepository<OrderLookupEntity, String> {

    OrderLookupEntity findFirstByCustomerIdAndStatusOrderByDateCreatedDesc(String customerId, OrderStatus status);
} 