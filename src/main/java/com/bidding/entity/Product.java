package com.bidding.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String description;
    private double price;       // Starting bid price
    private int quantity;
    private String image1;

    // Status: "Available", "Sold", "Unsold", "Deleted"
    private String status;

    // NEW: Auction end date - format: "2025-12-31"
    // Scheduler raat 12 baje check karta hai - agar aaj ya pehle hai to auction band
    private String endDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "added_by_user_id")
    private User addedBy;
}
