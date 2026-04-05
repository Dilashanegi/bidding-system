package com.bidding.controller;

import com.bidding.entity.User;
import com.bidding.repository.*;
import com.bidding.utility.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// NOTE: @RequestMapping nahi lagaya - URLs neeche directly likhe hain
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductOfferRepository offerRepository;
    @Autowired private OrderRepository orderRepository;

    // ============================================
    // POST /api/user/register  - Customer register
    // ============================================
    @PostMapping("/api/user/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> req) {
        Map<String, Object> res = new HashMap<>();

        if (userRepository.findByEmailId(req.get("emailId")).isPresent()) {
            res.put("success", false);
            res.put("message", "Yeh email pehle se registered hai!");
            return ResponseEntity.badRequest().body(res);
        }

        User user = new User();
        user.setFirstName(req.get("firstName"));
        user.setLastName(req.get("lastName"));
        user.setEmailId(req.get("emailId"));
        user.setPassword(passwordEncoder.encode(req.get("password")));
        user.setPhoneNo(req.get("phoneNo"));
        user.setRole("Customer");
        user.setStatus("Active");
        user.setWalletAmount(0.0);
        userRepository.save(user);

        res.put("success", true);
        res.put("message", "Registration successful!");
        return ResponseEntity.ok(res);
    }

    // ============================================
    // POST /api/user/login  - Login (Customer + Admin)
    // ============================================
    @PostMapping("/api/user/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> req) {
        Map<String, Object> res = new HashMap<>();

        Optional<User> optUser = userRepository.findByEmailIdAndStatus(req.get("emailId"), "Active");
        if (optUser.isEmpty()) {
            res.put("success", false);
            res.put("message", "User nahi mila ya account inactive hai");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        User user = optUser.get();
        if (!passwordEncoder.matches(req.get("password"), user.getPassword())) {
            res.put("success", false);
            res.put("message", "Password galat hai");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        String token = jwtUtils.generateToken(user.getEmailId(), user.getRole());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("firstName", user.getFirstName());
        userInfo.put("lastName", user.getLastName());
        userInfo.put("emailId", user.getEmailId());
        userInfo.put("role", user.getRole());
        userInfo.put("walletAmount", user.getWalletAmount());

        res.put("success", true);
        res.put("message", "Login successful!");
        res.put("jwtToken", token);
        res.put("user", userInfo);
        return ResponseEntity.ok(res);
    }

    // ============================================
    // GET /api/user/{id}  - User info fetch
    // ============================================
    @GetMapping("/api/user/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable int id) {
        Map<String, Object> res = new HashMap<>();
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isEmpty()) return ResponseEntity.notFound().build();

        User user = optUser.get();
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("firstName", user.getFirstName());
        info.put("emailId", user.getEmailId());
        info.put("walletAmount", user.getWalletAmount());

        res.put("success", true);
        res.put("user", info);
        return ResponseEntity.ok(res);
    }

    // ============================================
    // PUT /api/user/wallet/add  - Wallet mein paise add karo
    // ============================================
    @PutMapping("/api/user/wallet/add")
    public ResponseEntity<Map<String, Object>> addToWallet(@RequestBody Map<String, Object> req) {
        Map<String, Object> res = new HashMap<>();

        int userId = (int) req.get("userId");
        double amount = Double.parseDouble(req.get("amount").toString());

        if (amount <= 0) {
            res.put("success", false);
            res.put("message", "Amount 0 se zyada hona chahiye");
            return ResponseEntity.badRequest().body(res);
        }

        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            res.put("success", false);
            res.put("message", "User nahi mila");
            return ResponseEntity.badRequest().body(res);
        }

        User user = optUser.get();
        double newBalance = user.getWalletAmount() + amount;
        user.setWalletAmount(newBalance);
        userRepository.save(user);

        res.put("success", true);
        res.put("message", "Wallet update ho gaya!");
        res.put("newBalance", newBalance);
        return ResponseEntity.ok(res);
    }

    // ============================================
    // GET /api/admin/users  - Admin: saare customers
    // ============================================
    @GetMapping("/api/admin/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findByRole("Customer");

        List<Map<String, Object>> safeList = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", u.getId());
            info.put("firstName", u.getFirstName());
            info.put("lastName", u.getLastName());
            info.put("emailId", u.getEmailId());
            info.put("phoneNo", u.getPhoneNo());
            info.put("walletAmount", u.getWalletAmount());
            info.put("status", u.getStatus());
            safeList.add(info);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("users", safeList);
        return ResponseEntity.ok(res);
    }

    // ============================================
    // PUT /api/admin/user/status  - User activate/deactivate
    // ============================================
    @PutMapping("/api/admin/user/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(@RequestBody Map<String, Object> req) {
        Map<String, Object> res = new HashMap<>();

        int userId = (int) req.get("userId");
        String newStatus = req.get("status").toString();

        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            res.put("success", false);
            res.put("message", "User nahi mila");
            return ResponseEntity.badRequest().body(res);
        }

        optUser.get().setStatus(newStatus);
        userRepository.save(optUser.get());

        res.put("success", true);
        res.put("message", "User " + newStatus + " kar diya!");
        return ResponseEntity.ok(res);
    }

    // ============================================
    // GET /api/admin/stats  - Dashboard stats
    // ============================================
    @GetMapping("/api/admin/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("products", productRepository.count());
        stats.put("users", userRepository.findByRole("Customer").size());
        stats.put("activeBids", offerRepository.count());
        stats.put("orders", orderRepository.count());

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("stats", stats);
        return ResponseEntity.ok(res);
    }
}
