package com.bidding.entity;

import jakarta.persistence.*;
import lombok.Data;

// @Data = Lombok - automatically getters, setters, toString banata hai
// @Entity = yeh class database table hai
@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String firstName;
    private String lastName;
    private String emailId;
    private String password;
    private String phoneNo;

    // Role: "Admin" ya "Customer"
    private String role;

    // Status: "Active" ya "Inactive"
    private String status;

    // Customer ka wallet balance
    private double walletAmount;
}
