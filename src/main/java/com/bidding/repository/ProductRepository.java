package com.bidding.repository;

import com.bidding.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByStatus(String status);
    List<Product> findByCategoryIdAndStatus(int categoryId, String status);
    List<Product> findByNameContainingIgnoreCaseAndStatus(String name, String status);
}
