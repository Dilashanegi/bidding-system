package com.bidding.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Status: "Placed", "Delivered"
    private String status;

    private String orderDate;

    // Yeh order kisne diya (Customer)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Yeh order kis product ka hai
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Customer ki winning bid
    @ManyToOne
    @JoinColumn(name = "offer_id")
    private ProductOffer offer;
}
