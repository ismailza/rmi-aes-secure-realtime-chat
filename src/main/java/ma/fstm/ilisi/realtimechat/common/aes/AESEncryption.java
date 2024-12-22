package ma.fstm.ilisi.realtimechat.common.aes;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AESEncryption class provides methods for AES-256 encryption and decryption.
 */
public class AESEncryption {
    /**
     * The AESCore instance used for encryption operations.
     */
    private final AESCore aesCore;

    /**
     * The AES encryption key.
     */
    private byte[] key;

    /**
     * Constructor that initializes the AESCore instance and generates the encryption key.
     */
    public AESEncryption() {
        this.aesCore = new AESCore();
        generateKey();
    }

    /**
     * Generates a random AES-256 key and expands it for use in encryption.
     */
    private void generateKey() {
        key = new byte[32]; // AES-256
        new SecureRandom().nextBytes(key);
        aesCore.keyExpansion(key);
    }

    /**
     * Encrypts a message using AES-256 encryption with PKCS7 padding and CBC mode.
     *
     * @param message the plaintext message to be encrypted
     * @return the Base64-encoded ciphertext
     */
    public String encrypt(String message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        try {
            // Convert the message to bytes
            byte[] data = message.getBytes(StandardCharsets.UTF_8);

            // Add PKCS7 padding
            int blockSize = 16;
            int padding = blockSize - (data.length % blockSize);
            byte[] paddedData = new byte[data.length + padding];
            System.arraycopy(data, 0, paddedData, 0, data.length);
            Arrays.fill(paddedData, data.length, paddedData.length, (byte) padding);

            // Generate the IV
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);

            // Encrypt in CBC mode
            byte[] encrypted = new byte[paddedData.length];
            byte[] previousBlock = iv;

            for (int i = 0; i < paddedData.length; i += 16) {
                // XOR with the previous block
                byte[] block = new byte[16];
                System.arraycopy(paddedData, i, block, 0, 16);
                for (int j = 0; j < 16; j++) {
                    block[j] ^= previousBlock[j];
                }

                // Encrypt the block
                byte[] encryptedBlock = aesCore.encryptBlock(block);
                System.arraycopy(encryptedBlock, 0, encrypted, i, 16);
                previousBlock = encryptedBlock;
            }

            // Concatenate the IV and the encrypted data
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a Base64-encoded ciphertext using AES-256 decryption with PKCS7 padding and CBC mode.
     *
     * @param encryptedMessage the Base64-encoded ciphertext to be decrypted
     * @return the decrypted plaintext message
     */
    public String decrypt(String encryptedMessage) {
        if (encryptedMessage == null) {
            throw new IllegalArgumentException("Encrypted message cannot be null");
        }
        try {
            // Decode Base64
            byte[] encryptedData = Base64.getDecoder().decode(encryptedMessage);

            // Validate minimum length (IV + at least one block)
            if (encryptedData.length < 32) { // 16 bytes IV + 16 bytes minimum data
                throw new IllegalArgumentException("Invalid encrypted data length");
            }

            // Extract the IV
            byte[] iv = new byte[16];
            System.arraycopy(encryptedData, 0, iv, 0, 16);

            // Extract the encrypted data
            byte[] encrypted = new byte[encryptedData.length - 16];
            System.arraycopy(encryptedData, 16, encrypted, 0, encrypted.length);

            // Validate block size
            if (encrypted.length % 16 != 0) {
                throw new IllegalArgumentException("Invalid encrypted data length");
            }

            // Decrypt in CBC mode
            byte[] decrypted = new byte[encrypted.length];
            byte[] previousBlock = iv;

            for (int i = 0; i < encrypted.length; i += 16) {
                byte[] currentBlock = new byte[16];
                System.arraycopy(encrypted, i, currentBlock, 0, 16);

                byte[] decryptedBlock = aesCore.decryptBlock(currentBlock);

                // XOR with the previous block
                for (int j = 0; j < 16; j++) {
                    decryptedBlock[j] ^= previousBlock[j];
                }

                System.arraycopy(decryptedBlock, 0, decrypted, i, 16);
                previousBlock = currentBlock;
            }

            // Remove PKCS7 padding
            int paddingLength = decrypted[decrypted.length - 1] & 0xFF;
            byte[] unpaddedData = new byte[decrypted.length - paddingLength];
            System.arraycopy(decrypted, 0, unpaddedData, 0, unpaddedData.length);

            return new String(unpaddedData, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 encoding", e);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}