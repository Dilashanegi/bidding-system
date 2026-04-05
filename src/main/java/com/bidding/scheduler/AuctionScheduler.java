package com.bidding.scheduler;

import com.bidding.entity.Orders;
import com.bidding.entity.Product;
import com.bidding.entity.ProductOffer;
import com.bidding.repository.OrderRepository;
import com.bidding.repository.ProductOfferRepository;
import com.bidding.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

// Yeh class automatically chalta hai - koi manually nahi chalata
// Raat 12 baje check karta hai ki kisi auction ki end date aa gayi kya
@Component
public class AuctionScheduler {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductOfferRepository offerRepository;
    @Autowired private OrderRepository orderRepository;

    // cron = "0 0 0 * * *" = har din raat 12:00:00 baje chalega
    // Test ke liye "0 */1 * * * *" use karo = har 1 minute mein chalega
    @Scheduled(cron = "0 0 0 * * *")
    public void closeExpiredAuctions() {
        System.out.println("=== Auction Scheduler Chal Raha Hai: " + LocalDate.now() + " ===");

        String today = LocalDate.now().toString();

        // Saare "Available" products lo
        List<Product> products = productRepository.findByStatus("Available");

        for (Product product : products) {
            // Agar endDate nahi hai ya future mein hai - skip karo
            if (product.getEndDate() == null) continue;
            if (product.getEndDate().compareTo(today) > 0) continue;

            System.out.println("Closing auction: " + product.getName());

            // Is product ke saare bids lo (highest pehle)
            List<ProductOffer> bids = offerRepository.findByProductIdOrderByAmountDesc(product.getId());

            if (bids.isEmpty()) {
                // Koi bid nahi - Unsold
                product.setStatus("Unsold");
                productRepository.save(product);
                System.out.println("  Koi bid nahi - Unsold");
            } else {
                // Pehli bid = highest = WINNER
                ProductOffer winner = bids.get(0);
                winner.setStatus("Won");
                offerRepository.save(winner);

                // Baaki sab = Lost
                for (int i = 1; i < bids.size(); i++) {
                    bids.get(i).setStatus("Lost");
                    offerRepository.save(bids.get(i));
                }

                // Winner ka order automatically banao
                Orders order = new Orders();
                order.setUser(winner.getUser());
                order.setProduct(product);
                order.setOffer(winner);
                order.setStatus("Placed");
                order.setOrderDate(today);
                orderRepository.save(order);

                // Product = Sold
                product.setStatus("Sold");
                productRepository.save(product);

                System.out.println("  Winner: " + winner.getUser().getFirstName() + " - Rs." + winner.getAmount());
            }
        }
        System.out.println("=== Scheduler Complete ===");
    }
}
