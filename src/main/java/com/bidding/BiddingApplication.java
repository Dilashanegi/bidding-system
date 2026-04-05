package com.bidding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling = Scheduler on karta hai (AuctionScheduler ke liye zaroori)
@SpringBootApplication
@EnableScheduling
public class BiddingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BiddingApplication.class, args);
        System.out.println("Bidding System Server Started! Port: 8080");
    }
}
