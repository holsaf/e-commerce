package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email for authentication
    Optional<User> findByEmail(String email);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find active users only
    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE SIZE(u.orders) > :minOrders ORDER BY SIZE(u.orders) DESC")
    List<User> findUsersWithMostOrders(@Param("minOrders") int minOrders);
}
