package ma.fstm.ilisi.realtimechat.common.aes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class AESEncryptionTest {
    private AESEncryption aes;

    @BeforeEach
    void setUp() {
        aes = new AESEncryption();
    }

    @Test
    @DisplayName("Test basic encryption and decryption")
    void testBasicEncryptionDecryption() {
        String message = "Hello, World!";
        String encrypted = aes.encrypt(message);
        String decrypted = aes.decrypt(encrypted);
        assertEquals(message, decrypted, "Decrypted message should match original");
    }

    @Test
    @DisplayName("Test empty string encryption")
    void testEmptyString() {
        String message = "";
        String encrypted = aes.encrypt(message);
        String decrypted = aes.decrypt(encrypted);
        assertEquals(message, decrypted, "Empty string should be handled correctly");
    }

    @Test
    @DisplayName("Test long message encryption")
    void testLongMessage() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("Long message test. ");
        }
        String message = sb.toString();
        String encrypted = aes.encrypt(message);
        String decrypted = aes.decrypt(encrypted);
        assertEquals(message, decrypted, "Long message should be handled correctly");
    }

    @Test
    @DisplayName("Test special characters")
    void testSpecialCharacters() {
        String message = "!@#$%^&*()_+-=[]{}|;:'\",.<>?/~`€₹¥";
        String encrypted = aes.encrypt(message);
        String decrypted = aes.decrypt(encrypted);
        assertEquals(message, decrypted, "Special characters should be handled correctly");
    }

    @Test
    @DisplayName("Test Unicode characters")
    void testUnicodeCharacters() {
        String message = "Hello, 世界! Γειά σου Κόσμε! привет мир! مرحبا بالعالم!";
        String encrypted = aes.encrypt(message);
        String decrypted = aes.decrypt(encrypted);
        assertEquals(message, decrypted, "Unicode characters should be handled correctly");
    }

    @Test
    @DisplayName("Test null input")
    void testNullInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            aes.encrypt(null);
        }, "Null input should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Test invalid encrypted message")
    void testInvalidEncryptedMessage() {
        assertThrows(RuntimeException.class, () -> {
            aes.decrypt("InvalidBase64String");
        }, "Invalid encrypted message should throw RuntimeException");
    }

    @Test
    @DisplayName("Test different message lengths")
    void testDifferentMessageLengths() {
        // Test messages of different lengths around block size (16 bytes)
        String[] messages = {
                "15bytes_message!",      // 15 bytes
                "16bytes_messages",      // 16 bytes
                "17bytes_messages!",     // 17 bytes
                "32bytes_messages_exactly_testing!"  // 32 bytes
        };

        for (String message : messages) {
            String encrypted = aes.encrypt(message);
            String decrypted = aes.decrypt(encrypted);
            assertEquals(message, decrypted,
                    "Message of length " + message.length() + " should be handled correctly");
        }
    }

    @Test
    @DisplayName("Test consecutive encryptions")
    void testConsecutiveEncryptions() {
        String message = "Test message";
        String firstEncryption = aes.encrypt(message);
        String secondEncryption = aes.encrypt(message);

        assertNotEquals(firstEncryption, secondEncryption,
                "Consecutive encryptions should produce different results due to random IV");

        assertEquals(message, aes.decrypt(firstEncryption),
                "First encryption should decrypt correctly");
        assertEquals(message, aes.decrypt(secondEncryption),
                "Second encryption should decrypt correctly");
    }

    @Test
    @DisplayName("Test padding handling")
    void testPadding() {
        // Test different message lengths to verify padding
        for (int length = 0; length < 40; length++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append('A');
            }
            String message = sb.toString();
            String encrypted = aes.encrypt(message);
            String decrypted = aes.decrypt(encrypted);
            assertEquals(message, decrypted,
                    "Message of length " + length + " should be padded and unpadded correctly");
        }
    }

    @Test
    @DisplayName("Test large message handling")
    void testLargeMessage() {
        // Create a message larger than 1MB
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1024 * 1024; i++) {
            sb.append('X');
        }
        String message = sb.toString();

        String encrypted = aes.encrypt(message);
        String decrypted = aes.decrypt(encrypted);

        assertEquals(message, decrypted,
                "Large messages should be handled correctly");
    }

    @Test
    @DisplayName("Test IV uniqueness")
    void testIVUniqueness() {
        String message = "Test message";
        // Collect multiple encrypted versions
        int numberOfTests = 1000;
        String[] encryptedMessages = new String[numberOfTests];

        for (int i = 0; i < numberOfTests; i++) {
            encryptedMessages[i] = aes.encrypt(message);
        }

        // Check that all encrypted messages are different
        for (int i = 0; i < numberOfTests - 1; i++) {
            for (int j = i + 1; j < numberOfTests; j++) {
                assertNotEquals(encryptedMessages[i], encryptedMessages[j],
                        "Each encryption should produce unique ciphertext");
            }
        }
    }

    @Test
    @DisplayName("Test tampered ciphertext")
    void testTamperedCiphertext() {
        String message = "Original message";
        String encrypted = aes.encrypt(message);

        // Tamper with the encrypted message
        String tamperedEncrypted = encrypted.substring(0, encrypted.length() - 1) + "X";

        assertThrows(RuntimeException.class, () -> {
            aes.decrypt(tamperedEncrypted);
        }, "Tampered ciphertext should throw exception");
    }
}