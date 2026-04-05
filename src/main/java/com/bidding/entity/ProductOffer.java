package com.bidding.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product_offer")
public class ProductOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Customer ne kitna amount bid kiya
    private double amount;

    // Status: "Pending", "Won", "Lost"
    private String status;

    // Yeh bid kisne lagayi (Customer)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Yeh bid kis product par hai
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
