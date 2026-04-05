package com.bidding.controller;

import com.bidding.entity.*;
import com.bidding.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOfferRepository offerRepository;

    @Autowired
    private UserRepository userRepository;

    // =============================================
    // CUSTOMER KE ORDERS DEKHO
    // URL: GET /api/order/fetch/user-wise?userId=5
    // =============================================
    @GetMapping("/fetch/user-wise")
    public ResponseEntity<Map<String, Object>> getMyOrders(@RequestParam int userId) {
        Map<String, Object> response = new HashMap<>();

        List<Orders> orders = orderRepository.findByUserId(userId);

        response.put("success", true);
        response.put("orders", orders);
        return ResponseEntity.ok(response);
    }

    // =============================================
    // ORDER PLACE KARO - CUSTOMER ONLY (Login zaruri)
    // URL: POST /api/order/add
    // Body: { "userId": 5, "productId": 2, "offerId": 10 }
    // =============================================
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        int userId = (int) request.get("userId");
        int productId = (int) request.get("productId");
        int offerId = (int) request.get("offerId");

        // Sab dhundho
        Optional<User> optUser = userRepository.findById(userId);
        Optional<Product> optProduct = productRepository.findById(productId);
        Optional<ProductOffer> optOffer = offerRepository.findById(offerId);

        if (optUser.isEmpty() || optProduct.isEmpty() || optOffer.isEmpty()) {
            response.put("success", false);
            response.put("message", "User, Product, ya Offer nahi mila");
            return ResponseEntity.badRequest().body(response);
        }

        Product product = optProduct.get();

        // Check karo - product still available hai?
        if (!product.getStatus().equals("Available")) {
            response.put("success", false);
            response.put("message", "Yeh product already sold ho chuka hai");
            return ResponseEntity.badRequest().body(response);
        }

        // Order banao
        Orders order = new Orders();
        order.setUser(optUser.get());
        order.setProduct(product);
        order.setOffer(optOffer.get());
        order.setStatus("Placed");
        order.setOrderDate(LocalDate.now().toString());

        orderRepository.save(order);

        // Product ko "Sold" mark karo
        product.setStatus("Sold");
        productRepository.save(product);

        // Winning offer ko "Won" mark karo
        ProductOffer winOffer = optOffer.get();
        winOffer.setStatus("Won");
        offerRepository.save(winOffer);

        response.put("success", true);
        response.put("message", "Order place ho gaya! 🎉");
        return ResponseEntity.ok(response);
    }
}
