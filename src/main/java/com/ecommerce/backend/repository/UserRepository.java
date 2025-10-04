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

    //save a user
    User save(User user);

    //find a user by id
    Optional<User> findById(Long id);

    // finds all users
    List<User> findAll();

    //delete a user by id
    void deleteById(Long id);

    //update a user
    User saveAndFlush(User user);


    @Query("SELECT u FROM User u WHERE SIZE(u.orders) > :minOrders ORDER BY SIZE(u.orders) DESC")
    List<User> findUsersWithMostOrders(@Param("minOrders") int minOrders);
}
