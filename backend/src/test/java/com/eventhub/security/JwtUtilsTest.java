package com.eventhub.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eventhub.entity.UserRole;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(secret, expirationMs);
    }

    @Test
    void generateTokenAndExtractClaims() {
        String token = jwtUtils.generateToken(101L, "student@eventhub.com", UserRole.STUDENT);

        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals("student@eventhub.com", jwtUtils.getEmailFromToken(token));
        assertEquals(101L, jwtUtils.getUserIdFromToken(token));
        assertEquals("STUDENT", jwtUtils.getRoleFromToken(token));
    }

    @Test
    void validateTokenReturnsFalseForInvalidToken() {
        assertFalse(jwtUtils.validateToken("invalid.jwt.token"));
        assertFalse(jwtUtils.validateToken(""));
        assertFalse(jwtUtils.validateToken(null));
    }

    @Test
    void validateTokenReturnsFalseForExpiredToken() throws InterruptedException {
        // JwtUtils with 1ms expiration
        JwtUtils shortLivedJwtUtils = new JwtUtils(secret, 1);
        String token = shortLivedJwtUtils.generateToken(202L, "expired@eventhub.com", UserRole.STUDENT);

        Thread.sleep(50); // wait for expiry

        assertFalse(shortLivedJwtUtils.validateToken(token));
    }
}
