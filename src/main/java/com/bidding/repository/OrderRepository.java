package com.bidding.repository;

import com.bidding.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Integer> {
    List<Orders> findByUserId(int userId);
}
