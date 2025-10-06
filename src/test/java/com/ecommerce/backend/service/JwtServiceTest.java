package com.ecommerce.backend.service;

import io.jsonwebtoken.Claims;
import io.j    @Test
    @DisplayName("Should validate token successfully for correct user")
    void testValidateToken_Success() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
    }e    @Test
    @DisplayName("Should fail validation for different user")
    void testValidateToken_DifferentUser() {
        // Arrange
        String token = jwtService.generateToken(userDetails);
        
        List<GrantedAuthority> otherAuthorities = new ArrayList<>();
        otherAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails differentUser = new User("other@example.com", "password", otherAuthorities);

        // Act
        boolean isValid = jwtService.validateToken(token, differentUser);

        // Assert
        assertFalse(isValid);
    }rt io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;
    private String testSecret;
    private long testExpiration;

    @BeforeEach
    void setUp() {
        // Use a test secret key (must be at least 256 bits for HS256)
        testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        testExpiration = 3600000L; // 1 hour

        // Set the private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationInMs", testExpiration);

        // Create test user details
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        userDetails = new User("testuser@example.com", "password", authorities);
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerateToken() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("Should extract username from token")
    void testExtractUsername() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("testuser@example.com", username);
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void testExtractExpiration() {
        String token = jwtService.generateToken(userDetails);

        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
        long expectedExpiration = System.currentTimeMillis() + testExpiration;
        assertTrue(Math.abs(expiration.getTime() - expectedExpiration) < 10000);
    }

    @Test
    @DisplayName("Should validate token successfully for correct user")
    void testValidateToken_Success() {
        String token = jwtService.generateToken(userDetails);

        Boolean isValid = jwtService.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should fail validation for different user")
    void testValidateToken_DifferentUser() {
        String token = jwtService.generateToken(userDetails);
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails differentUser = new User("different@example.com", "password", authorities);

        Boolean isValid = jwtService.validateToken(token, differentUser);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should fail validation for expired token")
    void testValidateToken_ExpiredToken() throws InterruptedException {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtExpirationInMs", 100L);
        String expiredToken = jwtService.generateToken(userDetails);
        
        Thread.sleep(200);
        
        ReflectionTestUtils.setField(jwtService, "jwtExpirationInMs", testExpiration);

        // Act & Assert
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            jwtService.validateToken(expiredToken, userDetails);
        });
    }

    @Test
    @DisplayName("Should extract role claim from token")
    void testExtractRoleClaim() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        // Assert
        assertEquals("ROLE_USER", role);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateToken_DifferentUsers() {
        // Arrange
        List<GrantedAuthority> adminAuthorities = new ArrayList<>();
        adminAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UserDetails adminUser = new User("admin@example.com", "password", adminAuthorities);

        // Act
        String userToken = jwtService.generateToken(userDetails);
        String adminToken = jwtService.generateToken(adminUser);

        // Assert
        assertNotNull(userToken);
        assertNotNull(adminToken);
        assertNotEquals(userToken, adminToken);
        
        assertEquals("testuser@example.com", jwtService.extractUsername(userToken));
        assertEquals("admin@example.com", jwtService.extractUsername(adminToken));
        
        String userRole = jwtService.extractClaim(userToken, claims -> claims.get("role", String.class));
        String adminRole = jwtService.extractClaim(adminToken, claims -> claims.get("role", String.class));
        
        assertEquals("ROLE_USER", userRole);
        assertEquals("ROLE_ADMIN", adminRole);
    }

    @Test
    @DisplayName("Should extract issued at date from token")
    void testExtractIssuedAt() {
        // Arrange
        long beforeGeneration = System.currentTimeMillis() - 1000;
        String token = jwtService.generateToken(userDetails);
        long afterGeneration = System.currentTimeMillis() + 1000;

        // Act
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

        // Assert
        assertNotNull(issuedAt);
        assertTrue(issuedAt.getTime() >= beforeGeneration);
        assertTrue(issuedAt.getTime() <= afterGeneration);
    }

    @Test
    @DisplayName("Should extract subject from token")
    void testExtractSubject() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Assert
        assertEquals("testuser@example.com", subject);
    }

    @Test
    @DisplayName("Token should not be expired immediately after generation")
    void testTokenNotExpiredImmediately() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        Date expiration = jwtService.extractExpiration(token);
        boolean isExpired = expiration.before(new Date());

        // Assert
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Should generate token with correct algorithm signature")
    void testTokenSignature() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecret));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Assert
        assertNotNull(claims);
        assertEquals("testuser@example.com", claims.getSubject());
        assertEquals("ROLE_USER", claims.get("role"));
    }
}
