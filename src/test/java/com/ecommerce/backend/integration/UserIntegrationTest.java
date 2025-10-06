package com.ecommerce.backend.integration;

import com.ecommerce.backend.dto.request.UserUpdateRequest;
import com.ecommerce.backend.dto.response.UserResponse;
import com.ecommerce.backend.entity.Customer;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserService with H2 database.
 * Tests user management operations with real database interactions.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("User Integration Tests with H2")
class UserIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should retrieve all users with pagination from H2 database")
    void testGetAllUsers() {
        // Arrange - Create 3 test users
        for (int i = 1; i <= 3; i++) {
            Customer user = new Customer();
            user.setEmail("user" + i + "@test.com");
            user.setPassword("password" + i);
            user.setFirstName("User" + i);
            user.setLastName("Test");
            user.setPhone("+123456789" + i);
            user.setAddress("Address " + i);
            user.setActive(true);
            userRepository.save(user);
        }

        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponse> users = userService.getAllUsers(pageable);

        // Assert
        assertNotNull(users);
        assertEquals(3, users.getTotalElements());
        assertEquals(3, users.getContent().size());
    }

    @Test
    @DisplayName("Should retrieve user by ID from H2 database")
    void testGetUserById() {
        // Arrange - Create a user
        Customer user = new Customer();
        user.setEmail("getbyid@test.com");
        user.setPassword("password");
        user.setFirstName("GetById");
        user.setLastName("Test");
        user.setPhone("+1234567890");
        user.setAddress("Test Address");
        user.setActive(true);
        User saved = userRepository.save(user);

        // Act
        UserResponse result = userService.getUserById(saved.getId());

        // Assert
        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals("getbyid@test.com", result.getEmail());
        assertEquals("GetById", result.getFirstName());
        assertEquals("Test", result.getLastName());
    }

    @Test
    @DisplayName("Should retrieve user profile by email from H2 database")
    void testGetUserProfile() {
        // Arrange - Create a user
        Customer user = new Customer();
        user.setEmail("profile@test.com");
        user.setPassword("password");
        user.setFirstName("Profile");
        user.setLastName("User");
        user.setPhone("+9876543210");
        user.setAddress("Profile Address");
        user.setActive(true);
        userRepository.save(user);

        // Act
        UserResponse result = userService.getUserProfile("profile@test.com");

        // Assert
        assertNotNull(result);
        assertEquals("profile@test.com", result.getEmail());
        assertEquals("Profile", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("+9876543210", result.getPhone());
    }

    @Test
    @DisplayName("Should update user profile in H2 database")
    void testUpdateUserProfile() {
        // Arrange - Create a user
        Customer user = new Customer();
        user.setEmail("update@test.com");
        user.setPassword("password");
        user.setFirstName("Original");
        user.setLastName("Name");
        user.setPhone("+1111111111");
        user.setAddress("Original Address");
        user.setActive(true);
        userRepository.save(user);

        // Act - Update the user
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("User");
        updateRequest.setPhone("+2222222222");
        updateRequest.setAddress("Updated Address");

        UserResponse result = userService.updateUserProfile("update@test.com", updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("+2222222222", result.getPhone());
        assertEquals("Updated Address", result.getAddress());

        // Verify in database
        User updated = userRepository.findByEmail("update@test.com").orElseThrow();
        assertEquals("Updated", updated.getFirstName());
        assertEquals("User", updated.getLastName());
    }

    @Test
    @DisplayName("Should deactivate user in H2 database")
    void testDeleteUser() {
        // Arrange - Create an active user
        Customer user = new Customer();
        user.setEmail("delete@test.com");
        user.setPassword("password");
        user.setFirstName("ToDelete");
        user.setLastName("User");
        user.setPhone("+3333333333");
        user.setAddress("Delete Address");
        user.setActive(true);
        User saved = userRepository.save(user);

        // Verify user is active
        assertTrue(saved.getActive(), "User should be active initially");

        // Act - Deactivate the user
        userService.deleteUser(saved.getId());

        // Assert - Verify user is deactivated
        // Clear the persistence context to force a fresh query from database
        userRepository.flush();
        User deactivated = userRepository.findById(saved.getId()).orElse(null);
        assertNotNull(deactivated, "User should still exist in database");
        assertNotNull(deactivated.getActive(), "Active flag should not be null");
        assertEquals(Boolean.FALSE, deactivated.getActive(), "User should be deactivated");
    }

    @Test
    @DisplayName("Should handle multiple user updates correctly")
    void testMultipleUpdates() {
        // Arrange - Create a user
        Customer user = new Customer();
        user.setEmail("multiupdate@test.com");
        user.setPassword("password");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setPhone("+4444444444");
        user.setAddress("Address 1");
        user.setActive(true);
        userRepository.save(user);

        // Act - Update first time
        UserUpdateRequest update1 = new UserUpdateRequest();
        update1.setFirstName("Second");
        update1.setLastName("Last");
        update1.setPhone("+4444444444");
        update1.setAddress("Address 2");
        userService.updateUserProfile("multiupdate@test.com", update1);

        // Update second time
        UserUpdateRequest update2 = new UserUpdateRequest();
        update2.setFirstName("Third");
        update2.setLastName("Final");
        update2.setPhone("+5555555555");
        update2.setAddress("Address 3");
        UserResponse result = userService.updateUserProfile("multiupdate@test.com", update2);

        // Assert
        assertEquals("Third", result.getFirstName());
        assertEquals("Final", result.getLastName());
        assertEquals("+5555555555", result.getPhone());
        assertEquals("Address 3", result.getAddress());

        // Verify in database
        User finalUser = userRepository.findByEmail("multiupdate@test.com").orElseThrow();
        assertEquals("Third", finalUser.getFirstName());
        assertEquals("Final", finalUser.getLastName());
    }

    @Test
    @DisplayName("Should test user pagination with multiple pages")
    void testUserPagination() {
        // Arrange - Create 12 users
        for (int i = 1; i <= 12; i++) {
            Customer user = new Customer();
            user.setEmail("page" + i + "@test.com");
            user.setPassword("password");
            user.setFirstName("Page");
            user.setLastName("User" + i);
            user.setPhone("+123456" + String.format("%04d", i));
            user.setAddress("Address " + i);
            user.setActive(true);
            userRepository.save(user);
        }

        // Act - Get first page (5 users)
        Pageable page1 = PageRequest.of(0, 5);
        Page<UserResponse> firstPage = userService.getAllUsers(page1);

        // Assert
        assertNotNull(firstPage);
        assertEquals(12, firstPage.getTotalElements());
        assertEquals(3, firstPage.getTotalPages());
        assertEquals(5, firstPage.getContent().size());
        assertTrue(firstPage.hasNext());

        // Act - Get second page
        Pageable page2 = PageRequest.of(1, 5);
        Page<UserResponse> secondPage = userService.getAllUsers(page2);

        // Assert
        assertEquals(5, secondPage.getContent().size());
        assertTrue(secondPage.hasPrevious());
        assertTrue(secondPage.hasNext());
    }

    @Test
    @DisplayName("Should verify user data persistence in H2")
    void testUserDataPersistence() {
        // Arrange - Create a user with specific data
        Customer user = new Customer();
        user.setEmail("persist@test.com");
        user.setPassword("securePassword123");
        user.setFirstName("Persistence");
        user.setLastName("Test");
        user.setPhone("+9999999999");
        user.setAddress("123 Persistence Lane");
        user.setActive(true);
        User saved = userRepository.save(user);

        // Act - Clear the persistence context and retrieve again
        userRepository.flush();
        User retrieved = userRepository.findById(saved.getId()).orElseThrow();

        // Assert - Verify all data persisted correctly
        assertEquals(saved.getId(), retrieved.getId());
        assertEquals("persist@test.com", retrieved.getEmail());
        assertEquals("securePassword123", retrieved.getPassword());
        assertEquals("Persistence", retrieved.getFirstName());
        assertEquals("Test", retrieved.getLastName());
        assertEquals("+9999999999", retrieved.getPhone());
        assertEquals("123 Persistence Lane", retrieved.getAddress());
        assertTrue(retrieved.getActive());
    }
}
