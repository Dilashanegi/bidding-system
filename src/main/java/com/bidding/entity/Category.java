package com.bidding.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;        // Category ka naam (e.g. Electronics, Clothes)
    private String description; // Category ki description
    private String status;      // "Active" ya "Deleted"
}
