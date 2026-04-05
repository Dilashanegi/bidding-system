package com.bidding.repository;

import com.bidding.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmailId(String emailId);
    Optional<User> findByEmailIdAndStatus(String emailId, String status);
    // NEW: Role se saare users dhundho - admin ke liye
    List<User> findByRole(String role);
}
