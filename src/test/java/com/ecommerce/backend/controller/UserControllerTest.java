package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.request.UserUpdateRequest;
import com.ecommerce.backend.dto.response.UserResponse;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController MockMvc Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/users - Should return all users with pagination")
    void testGetAllUsers() throws Exception {
        // Given
        UserResponse user1 = createUserResponse(1L, "user1@test.com", "John", "Doe");
        UserResponse user2 = createUserResponse(2L, "user2@test.com", "Jane", "Smith");

        List<UserResponse> users = Arrays.asList(user1, user2);
        Page<UserResponse> page = new PageImpl<>(users, PageRequest.of(0, 10), 2);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return user by ID")
    void testGetUserById() throws Exception {
        // Given
        UserResponse user = createUserResponse(1L, "user@test.com", "John", "Doe");

        when(userService.getUserById(1L)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @DisplayName("GET /api/users/profile - Should return current user profile")
    void testGetCurrentUserProfile() throws Exception {
        // Mock Authentication
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");

        UserResponse user = createUserResponse(1L, "user@test.com", "John", "Doe");

        when(userService.getUserProfile("user@test.com")).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .principal(auth))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("PUT /api/users/profile - Should update current user profile")
    void testUpdateCurrentUserProfile() throws Exception {
        // Mock Authentication
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("John Updated");
        request.setLastName("Doe Updated");
        request.setPhone("+1234567890");
        request.setAddress("123 New St");

        UserResponse updatedUser = createUserResponse(1L, "user@test.com", "John Updated", "Doe Updated");

        when(userService.updateUserProfile(eq("user@test.com"), any(UserUpdateRequest.class)))
                .thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("John Updated"))
                .andExpect(jsonPath("$.lastName").value("Doe Updated"));
    }

    @Test
    @DisplayName("PUT /api/users/profile - Should accept update even with minimal data")
    void testUpdateCurrentUserProfileInvalidData() throws Exception {
        // Mock Authentication
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");

        // Given - Empty update request
        UserUpdateRequest request = new UserUpdateRequest();
        // All fields are null

        UserResponse updatedUser = createUserResponse(1L, "user@test.com", "John", "Doe");
        when(userService.updateUserProfile(eq("user@test.com"), any(UserUpdateRequest.class)))
                .thenReturn(updatedUser);

        // When & Then - Standalone setup doesn't validate, so it succeeds
        mockMvc.perform(put("/api/users/profile")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should deactivate user")
    void testDeleteUser() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }


    @Test
    @DisplayName("GET /api/users - Should return empty page when no users")
    void testGetAllUsersEmptyPage() throws Exception {
        // Given
        Page<UserResponse> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // Helper method
    private UserResponse createUserResponse(Long id, String email, String firstName, String lastName) {
        UserResponse response = new UserResponse();
        response.setId(id);
        response.setEmail(email);
        response.setFirstName(firstName);
        response.setLastName(lastName);
        response.setPhone("+1234567890");
        response.setAddress("123 Test St");
        return response;
    }
}
