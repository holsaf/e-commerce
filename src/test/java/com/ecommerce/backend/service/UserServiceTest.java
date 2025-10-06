package com.ecommerce.backend.service;

import com.ecommerce.backend.data.TestData;
import com.ecommerce.backend.dto.request.UserUpdateRequest;
import com.ecommerce.backend.dto.response.UserResponse;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.model.mapper.UserMapper;
import com.ecommerce.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should get all users with pagination")
    void testGetAllUsers_Success() {
        // Arrange
        User user = TestData.createTestCustomer();
        UserResponse response = TestData.createTestUserResponse();
        List<User> users = Arrays.asList(user, user);
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.userToUserResponse(any(User.class))).thenReturn(response);

        // Act
        Page<UserResponse> result = userService.getAllUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(userRepository, times(1)).findAll(pageable);
        verify(userMapper, times(2)).userToUserResponse(any(User.class));
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserById_Success() {
        // Arrange
        User user = TestData.createTestCustomer();
        UserResponse response = TestData.createTestUserResponse();
        
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(userMapper.userToUserResponse(user)).thenReturn(response);

        // Act
        UserResponse result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userMapper, times(1)).userToUserResponse(user);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999L);
        });
        verify(userMapper, never()).userToUserResponse(any());
    }

    @Test
    @DisplayName("Should get user profile by email")
    void testGetUserProfile_Success() {
        // Arrange
        User user = TestData.createTestCustomer();
        UserResponse response = TestData.createTestUserResponse();
        String email = "test@example.com";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.userToUserResponse(user)).thenReturn(response);

        // Act
        UserResponse result = userService.getUserProfile(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository, times(1)).findByEmail(email);
        verify(userMapper, times(1)).userToUserResponse(user);
    }

    @Test
    @DisplayName("Should throw exception when user profile not found")
    void testGetUserProfile_NotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserProfile(email);
        });
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateUserProfile_Success() {
        // Arrange
        User user = TestData.createTestCustomer();
        UserResponse response = TestData.createTestUserResponse();
        String email = "test@example.com";
        UserUpdateRequest updateRequest = TestData.createTestUserUpdateRequest();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.userToUserResponse(any(User.class))).thenReturn(response);

        // Act
        UserResponse result = userService.updateUserProfile(email, updateRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void testDeleteUser_Success() {
        // Arrange
        User user = TestData.createTestCustomer();
        Long userId = 1L;
        
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        userService.deleteUser(userId);

        // Assert
        assertEquals(false, user.getActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void testDeleteUser_NotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });
        verify(userRepository, never()).save(any());
    }
}
