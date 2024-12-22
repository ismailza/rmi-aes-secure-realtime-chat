package ma.fstm.ilisi.realtimechat.common.aes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class AESCoreTest {
    private AESCore aesCore;
    private byte[] testKey;

    @BeforeEach
    void setUp() {
        aesCore = new AESCore();
        // Initialize a test key (256-bit)
        testKey = new byte[32];
        for (int i = 0; i < 32; i++) {
            testKey[i] = (byte)i;
        }
        aesCore.keyExpansion(testKey);
    }

    @Test
    @DisplayName("Test single block encryption and decryption")
    void testBlockEncryptionDecryption() {
        byte[] plaintext = new byte[16];
        for (int i = 0; i < 16; i++) {
            plaintext[i] = (byte)i;
        }

        byte[] encrypted = aesCore.encryptBlock(plaintext);
        byte[] decrypted = aesCore.decryptBlock(encrypted);

        assertArrayEquals(plaintext, decrypted, "Decrypted block should match original plaintext");
        assertNotEquals(plaintext, encrypted, "Encrypted block should be different from plaintext");
    }

    @Test
    @DisplayName("Test key expansion")
    void testKeyExpansion() {
        // Create two AESCore instances with the same key
        AESCore aesCore1 = new AESCore();
        AESCore aesCore2 = new AESCore();

        aesCore1.keyExpansion(testKey);
        aesCore2.keyExpansion(testKey);

        // Test encryption with both instances
        byte[] plaintext = new byte[16];
        for (int i = 0; i < 16; i++) {
            plaintext[i] = (byte)i;
        }

        byte[] encrypted1 = aesCore1.encryptBlock(plaintext);
        byte[] encrypted2 = aesCore2.encryptBlock(plaintext);

        assertArrayEquals(encrypted1, encrypted2,
                "Same key should produce identical encryption results");
    }

    @Test
    @DisplayName("Test different plaintexts produce different ciphertexts")
    void testDifferentPlaintexts() {
        byte[] plaintext1 = new byte[16];
        byte[] plaintext2 = new byte[16];

        // Initialize with different values
        for (int i = 0; i < 16; i++) {
            plaintext1[i] = (byte)i;
            plaintext2[i] = (byte)(i + 1);
        }

        byte[] encrypted1 = aesCore.encryptBlock(plaintext1);
        byte[] encrypted2 = aesCore.encryptBlock(plaintext2);

        assertFalse(java.util.Arrays.equals(encrypted1, encrypted2),
                "Different plaintexts should produce different ciphertexts");
    }

    @Test
    @DisplayName("Test different keys produce different ciphertexts")
    void testDifferentKeys() {
        byte[] plaintext = new byte[16];
        for (int i = 0; i < 16; i++) {
            plaintext[i] = (byte)i;
        }

        // First encryption with an original key
        byte[] encrypted1 = aesCore.encryptBlock(plaintext);

        // Create new key and encrypt the same plaintext
        byte[] differentKey = new byte[32];
        for (int i = 0; i < 32; i++) {
            differentKey[i] = (byte)(i + 1);
        }

        AESCore differentAesCore = new AESCore();
        differentAesCore.keyExpansion(differentKey);
        byte[] encrypted2 = differentAesCore.encryptBlock(plaintext);

        assertFalse(java.util.Arrays.equals(encrypted1, encrypted2),
                "Different keys should produce different ciphertexts");
    }

    @Test
    @DisplayName("Test encryption of all-zero block")
    void testZeroBlock() {
        byte[] zeroBlock = new byte[16];
        byte[] encrypted = aesCore.encryptBlock(zeroBlock);
        byte[] decrypted = aesCore.decryptBlock(encrypted);

        assertNotEquals(0, encrypted[0], "Encrypted zero block should not be all zeros");
        assertArrayEquals(zeroBlock, decrypted,
                "Decrypted zero block should match original");
    }

    @Test
    @DisplayName("Test encryption of all-ones block")
    void testOnesBlock() {
        byte[] onesBlock = new byte[16];
        for (int i = 0; i < 16; i++) {
            onesBlock[i] = (byte)0xFF;
        }

        byte[] encrypted = aesCore.encryptBlock(onesBlock);
        byte[] decrypted = aesCore.decryptBlock(encrypted);

        assertArrayEquals(onesBlock, decrypted,
                "Decrypted ones block should match original");
    }

    @Test
    @DisplayName("Test block size validation")
    void testBlockSizeValidation() {
        // Test with invalid block sizes
        byte[] tooSmall = new byte[15];
        byte[] tooLarge = new byte[17];

        assertThrows(IllegalArgumentException.class,
                () -> aesCore.encryptBlock(tooSmall),
                "Block size smaller than 16 bytes should throw exception");

        assertThrows(IllegalArgumentException.class,
                () -> aesCore.encryptBlock(tooLarge),
                "Block size larger than 16 bytes should throw exception");
    }

    @Test
    @DisplayName("Test key size validation")
    void testKeySizeValidation() {
        byte[] invalidKey = new byte[24];
        AESCore newCore = new AESCore();

        assertThrows(IllegalArgumentException.class,
                () -> newCore.keyExpansion(invalidKey),
                "Invalid key size should throw exception");
    }
}