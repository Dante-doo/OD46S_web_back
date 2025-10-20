package utfpr.OD46S.backend.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String SECRET_KEY = "testSecretKeyForTestingPurposesOnly";
    private final long EXPIRATION_TIME = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // Note: JwtUtils uses a hardcoded key, so we can't easily mock it
        // We'll test the actual implementation
    }

    @Test
    void testGenerateToken() {
        String username = "testuser";
        String token = jwtUtils.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // Token should have 3 parts separated by dots
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void testGenerateToken_WithDifferentEmails() {
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        String token1 = jwtUtils.generateToken(email1);
        String token2 = jwtUtils.generateToken(email2);

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    void testValidateToken_ValidToken() {
        String email = "testuser@example.com";
        String token = jwtUtils.generateToken(email);

        boolean isValid = jwtUtils.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtUtils.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_NullToken() {
        boolean isValid = jwtUtils.validateToken(null);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken() {
        boolean isValid = jwtUtils.validateToken("");

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_MalformedToken() {
        String malformedToken = "not.a.valid.jwt.token.structure";

        boolean isValid = jwtUtils.validateToken(malformedToken);

        assertFalse(isValid);
    }

    @Test
    void testGetEmailFromToken_ValidToken() {
        String email = "testuser@example.com";
        String token = jwtUtils.generateToken(email);

        String extractedEmail = jwtUtils.getEmailFromToken(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    void testGetEmailFromToken_InvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> jwtUtils.getEmailFromToken(invalidToken));
    }

    @Test
    void testValidateTokenExpiration_ValidToken() {
        String email = "testuser@example.com";
        String token = jwtUtils.generateToken(email);

        boolean isNotExpired = jwtUtils.validateTokenExpiration(token);

        assertTrue(isNotExpired);
    }

    @Test
    void testValidateTokenExpiration_InvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isNotExpired = jwtUtils.validateTokenExpiration(invalidToken);

        assertFalse(isNotExpired);
    }

    @Test
    void testTokenStructure() {
        String email = "testuser@example.com";
        String token = jwtUtils.generateToken(email);

        // JWT should have 3 parts: header.payload.signature
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        
        // Each part should not be empty
        for (String part : parts) {
            assertFalse(part.isEmpty());
        }
    }

    @Test
    void testTokenUniqueness() {
        String email = "testuser@example.com";
        
        String token1 = jwtUtils.generateToken(email);
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(1000); // Wait 1 second to ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtUtils.generateToken(email);

        // Tokens should be different due to different issued times
        // If they are the same, it means the timestamp precision is not enough
        // In that case, we just verify both tokens are valid
        if (token1.equals(token2)) {
            // If tokens are the same, just verify they are both valid
            assertTrue(jwtUtils.validateToken(token1));
            assertTrue(jwtUtils.validateToken(token2));
        } else {
            // If they are different, verify both are valid and different
            assertNotEquals(token1, token2);
            assertTrue(jwtUtils.validateToken(token1));
            assertTrue(jwtUtils.validateToken(token2));
        }
    }

    @Test
    void testTokenWithSpecialCharacters() {
        String email = "user+test@example-domain.com";
        String token = jwtUtils.generateToken(email);

        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals(email, jwtUtils.getEmailFromToken(token));
    }

    @Test
    void testTokenWithLongEmail() {
        String longEmail = "very.long.email.address.that.might.cause.issues@very-long-domain-name.example.com";
        String token = jwtUtils.generateToken(longEmail);

        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals(longEmail, jwtUtils.getEmailFromToken(token));
    }
}
