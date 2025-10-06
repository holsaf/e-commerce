package com.ecommerce.backend.service;

import com.ecommerce.backend.data.TestData;
import com.ecommerce.backend.dto.request.AuthRequest;
import com.ecommerce.backend.dto.request.UserRequest;
import com.ecommerce.backend.dto.response.AuthResponse;
import com.ecommerce.backend.dto.response.UserResponse;
import com.ecommerce.backend.entity.Admin;
import com.ecommerce.backend.entity.Customer;
import com.ecommerce.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should register customer successfully")
    void testRegister_Success() {
        // Arrange
        UserRequest request = TestData.createTestUserRequest();
        Customer customer = TestData.createTestCustomer();
        
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        UserResponse result = authService.register(request);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegister_EmailExists() {
        // Arrange
        UserRequest request = TestData.createTestUserRequest();
        
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should register admin successfully")
    void testRegisterAdmin_Success() {
        // Arrange
        UserRequest request = TestData.createTestUserRequest();
        Admin admin = TestData.createTestAdmin();
        
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(Admin.class))).thenReturn(admin);

        // Act
        UserResponse result = authService.registerAdmin(request);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userRepository, times(1)).save(any(Admin.class));
    }

    @Test
    @DisplayName("Should login successfully and return JWT token")
    void testLogin_Success() {
        // Arrange
        AuthRequest authRequest = TestData.createTestAuthRequest();
        Customer customer = TestData.createTestCustomer();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(customer);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("jwt-token");

        // Act
        AuthResponse result = authService.login(authRequest);

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when user not found during login")
    void testLogin_UserNotFound() {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("nonexistent@example.com");
        authRequest.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.login(authRequest);
        });
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }
}
