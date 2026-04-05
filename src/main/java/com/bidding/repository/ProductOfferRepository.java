package com.bidding.repository;

import com.bidding.entity.ProductOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductOfferRepository extends JpaRepository<ProductOffer, Integer> {
    List<ProductOffer> findByProductId(int productId);
    List<ProductOffer> findByUserId(int userId);
    List<ProductOffer> findByProductIdOrderByAmountDesc(int productId);
}
