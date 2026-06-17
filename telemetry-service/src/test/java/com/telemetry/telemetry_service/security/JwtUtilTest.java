package com.telemetry.telemetry_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        String token = jwtUtil.generateToken("testuser");
        assertNotNull(token);
        
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void testValidateToken_Valid() {
        String token = jwtUtil.generateToken("testuser");
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void testValidateToken_Invalid() {
        assertFalse(jwtUtil.isTokenValid("invalid.token.here"));
    }
}
