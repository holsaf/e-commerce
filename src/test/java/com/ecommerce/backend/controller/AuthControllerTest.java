package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.request.AuthRequest;
import com.ecommerce.backend.dto.request.UserRequest;
import com.ecommerce.backend.dto.response.AuthResponse;
import com.ecommerce.backend.dto.response.UserResponse;
import com.ecommerce.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController MockMvc Tests")
class AuthControllerTest {



    @Mock
    private AuthService authService;
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("POST /api/auth/register - Should register new user successfully")
    void testRegisterUser() throws Exception {
        // Arrange
        UserRequest request = new UserRequest();
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("+1234567890");
        request.setAddress("123 Test St");

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail(request.getEmail());
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());

        when(authService.register(any(UserRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for invalid request")
    void testRegisterUserWithInvalidData() throws Exception {
        // Arrange
        UserRequest request = new UserRequest();
        request.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should login successfully and return JWT token")
    void testLoginUser() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail("user@test.com");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse();
        response.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        response.setEmail("user@test.com");

        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 400 for missing credentials")
    void testLoginWithMissingCredentials() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/admin/register - Should register admin user successfully")
    void testRegisterAdmin() throws Exception {
        // Arrange
        UserRequest request = new UserRequest();
        request.setEmail("admin@test.com");
        request.setPassword("adminpass123");
        request.setFirstName("Admin");
        request.setLastName("User");
        request.setPhone("+9876543210");
        request.setAddress("456 Admin Ave");

        UserResponse response = new UserResponse();
        response.setId(2L);
        response.setEmail(request.getEmail());
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());

        when(authService.registerAdmin(any(UserRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/admin/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.firstName").value("Admin"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for malformed JSON")
    void testRegisterWithMalformedJson() throws Exception {
        // Arrange
        String malformedJson = "{email: 'test@test.com', invalid}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
}
