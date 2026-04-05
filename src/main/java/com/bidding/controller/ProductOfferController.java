package com.bidding.controller;

import com.bidding.entity.*;
import com.bidding.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/product/offer")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductOfferController {

    @Autowired
    private ProductOfferRepository offerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // =============================================
    // EK PRODUCT KE SAARE BIDS DEKHO (Public)
    // URL: GET /api/product/offer/fetch/product?productId=1
    // =============================================
    @GetMapping("/fetch/product")
    public ResponseEntity<Map<String, Object>> getOffersByProduct(@RequestParam int productId) {
        Map<String, Object> response = new HashMap<>();

        // Highest bid pehle aaye - isliye desc order
        List<ProductOffer> offers = offerRepository.findByProductIdOrderByAmountDesc(productId);

        response.put("success", true);
        response.put("offers", offers);
        return ResponseEntity.ok(response);
    }

    // =============================================
    // CUSTOMER KE SAARE BIDS DEKHO
    // URL: GET /api/product/offer/fetch/user?userId=5
    // =============================================
    @GetMapping("/fetch/user")
    public ResponseEntity<Map<String, Object>> getOffersByUser(@RequestParam int userId) {
        Map<String, Object> response = new HashMap<>();

        List<ProductOffer> offers = offerRepository.findByUserId(userId);

        response.put("success", true);
        response.put("offers", offers);
        return ResponseEntity.ok(response);
    }

    // =============================================
    // BID LAGAO - CUSTOMER ONLY (Login zaruri)
    // URL: POST /api/product/offer/add
    // Body: { "userId": 5, "productId": 2, "amount": 500.0 }
    // =============================================
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> placeBid(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        int userId = (int) request.get("userId");
        int productId = (int) request.get("productId");
        double amount = Double.parseDouble(request.get("amount").toString());

        // Product dhundho
        Optional<Product> optProduct = productRepository.findById(productId);
        if (optProduct.isEmpty() || !optProduct.get().getStatus().equals("Available")) {
            response.put("success", false);
            response.put("message", "Product available nahi hai");
            return ResponseEntity.badRequest().body(response);
        }

        // Customer dhundho
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            response.put("success", false);
            response.put("message", "User nahi mila");
            return ResponseEntity.badRequest().body(response);
        }

        Product product = optProduct.get();

        // Check karo - bid starting price se zyada honi chahiye
        if (amount <= product.getPrice()) {
            response.put("success", false);
            response.put("message", "Bid amount starting price se zyada honi chahiye: ₹" + product.getPrice());
            return ResponseEntity.badRequest().body(response);
        }

        // Check karo - existing bids se zyada honi chahiye
        List<ProductOffer> existingOffers = offerRepository.findByProductIdOrderByAmountDesc(productId);
        if (!existingOffers.isEmpty() && amount <= existingOffers.get(0).getAmount()) {
            response.put("success", false);
            response.put("message", "Bid highest existing bid se zyada honi chahiye: ₹" + existingOffers.get(0).getAmount());
            return ResponseEntity.badRequest().body(response);
        }

        // Naya bid banao
        ProductOffer offer = new ProductOffer();
        offer.setAmount(amount);
        offer.setStatus("Pending");
        offer.setUser(optUser.get());
        offer.setProduct(product);

        offerRepository.save(offer);

        response.put("success", true);
        response.put("message", "Bid place ho gayi! ₹" + amount);
        return ResponseEntity.ok(response);
    }

    // =============================================
    // BID DELETE KARO - CUSTOMER ONLY
    // URL: DELETE /api/product/offer/{id}
    // =============================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBid(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();

        Optional<ProductOffer> optOffer = offerRepository.findById(id);
        if (optOffer.isEmpty()) {
            response.put("success", false);
            response.put("message", "Bid nahi mili");
            return ResponseEntity.notFound().build();
        }

        offerRepository.deleteById(id);

        response.put("success", true);
        response.put("message", "Bid delete ho gayi!");
        return ResponseEntity.ok(response);
    }
}
