package com.ecommerce.backend.dto;

import com.ecommerce.backend.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String email;
    private Role role;
    private String token; // For future JWT implementation
}