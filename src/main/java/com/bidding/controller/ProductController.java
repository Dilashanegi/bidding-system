package com.bidding.controller;

import com.bidding.entity.*;
import com.bidding.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/product")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    @Value("${product.image.folder}")
    private String imageFolder;

    // GET /api/product/fetch/all
    @GetMapping("/fetch/all")
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "Available") String status) {
        Map<String, Object> response = new HashMap<>();
        List<Product> products;
        if (status.isEmpty()) {
            products = productRepository.findAll();
        } else {
            products = productRepository.findByStatus(status);
        }
        response.put("success", true);
        response.put("products", products);
        return ResponseEntity.ok(response);
    }

    // GET /api/product/fetch/category-wise?categoryId=1
    @GetMapping("/fetch/category-wise")
    public ResponseEntity<Map<String, Object>> getByCategory(@RequestParam int categoryId) {
        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findByCategoryIdAndStatus(categoryId, "Available");
        response.put("success", true);
        response.put("products", products);
        return ResponseEntity.ok(response);
    }

    // GET /api/product/search?productName=phone
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam String productName) {
        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndStatus(productName, "Available");
        response.put("success", true);
        response.put("products", products);
        return ResponseEntity.ok(response);
    }

    // GET /api/product/fetch?productId=1
    @GetMapping("/fetch")
    public ResponseEntity<Map<String, Object>> getProduct(@RequestParam int productId) {
        Map<String, Object> response = new HashMap<>();
        Optional<Product> optProduct = productRepository.findById(productId);
        if (optProduct.isEmpty()) {
            response.put("success", false);
            response.put("message", "Product nahi mila");
            return ResponseEntity.notFound().build();
        }
        response.put("success", true);
        response.put("product", optProduct.get());
        return ResponseEntity.ok(response);
    }

    // GET /api/product/{imageName} - image serve karo
    // NOTE: yeh /fetch/** ke baad aata hai isliye conflict nahi hoga
    @GetMapping(value = "/{imageName}", produces = "image/*")
    public ResponseEntity<byte[]> getProductImage(@PathVariable String imageName) {
        try {
            Path imagePath = Paths.get(imageFolder + imageName);
            if (!Files.exists(imagePath)) {
                return ResponseEntity.notFound().build();
            }
            byte[] imageBytes = Files.readAllBytes(imagePath);

            HttpHeaders headers = new HttpHeaders();
            String name = imageName.toLowerCase();
            if (name.endsWith(".png")) headers.setContentType(MediaType.IMAGE_PNG);
            else if (name.endsWith(".gif")) headers.setContentType(MediaType.IMAGE_GIF);
            else headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/product/add
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addProduct(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam int quantity,
            @RequestParam int categoryId,
            @RequestParam int adminId,
            @RequestParam(required = false) String endDate,
            @RequestParam MultipartFile image) {

        Map<String, Object> response = new HashMap<>();

        Optional<Category> optCat = categoryRepository.findById(categoryId);
        if (optCat.isEmpty()) {
            response.put("success", false);
            response.put("message", "Category nahi mili");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<User> optAdmin = userRepository.findById(adminId);
        if (optAdmin.isEmpty()) {
            response.put("success", false);
            response.put("message", "Admin nahi mila");
            return ResponseEntity.badRequest().body(response);
        }

        // Image save karo folder mein
        String imageName = "";
        try {
            imageName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path savePath = Paths.get(imageFolder + imageName);
            Files.createDirectories(savePath.getParent());
            Files.write(savePath, image.getBytes());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Image save nahi hui: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setImage1(imageName);
        product.setStatus("Available");
        product.setCategory(optCat.get());
        product.setAddedBy(optAdmin.get());
        if (endDate != null && !endDate.isEmpty()) {
            product.setEndDate(endDate);
        }

        productRepository.save(product);
        response.put("success", true);
        response.put("message", "Product add ho gaya!");
        return ResponseEntity.ok(response);
    }

    // DELETE /api/product/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Product> optProduct = productRepository.findById(id);
        if (optProduct.isEmpty()) {
            response.put("success", false);
            response.put("message", "Product nahi mila");
            return ResponseEntity.notFound().build();
        }
        optProduct.get().setStatus("Deleted");
        productRepository.save(optProduct.get());
        response.put("success", true);
        response.put("message", "Product delete ho gaya!");
        return ResponseEntity.ok(response);
    }
}
