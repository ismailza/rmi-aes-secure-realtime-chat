package ma.fstm.ilisi.realtimechat.common.aes;

/**
 * AESCore class implements the core functionalities of the AES encryption algorithm.
 */
public class AESCore {
    /**
     * Number of columns (32-bit words) comprising the State. For AES, NB is always 4.
     */
    private static final int NB = 4;

    /**
     * Number of 32-bit words comprising the Cipher Key. For AES-256, NK is 8.
     */
    private static final int NK = 8;

    /**
     * Number of rounds, which is a function of NK and NB (which is fixed). For AES-256, NR is 14.
     */
    private static final int NR = 14;

    /**
     * State matrix representing the data to be encrypted/decrypted.
     */
    private int[][] state;

    /**
     * Expanded key matrix generated from the initial key and used in each round of the AES algorithm.
     */
    private int[][] expandedKey;

    /**
     * Constructor initializing the state and expandedKey matrices.
     */
    public AESCore() {
        state = new int[4][NB];
        expandedKey = new int[4][NB * (NR + 1)];
    }

    /**
     * SubBytes transformation in AES, which substitutes each byte in the state with its corresponding byte in the S-Box.
     */
    private void subBytes() {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < NB; col++) {
                state[row][col] = AESConstants.SBOX[state[row][col] & 0xFF];
            }
        }
    }

    /**
     * InvSubBytes transformation in AES, which substitutes each byte in the state with its corresponding byte in the inverse S-Box.
     */
    private void invSubBytes() {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < NB; col++) {
                state[row][col] = AESConstants.INV_SBOX[state[row][col] & 0xFF];
            }
        }
    }

    /**
     * ShiftRows transformation in AES, which shifts the rows of the state to the left.
     */
    private void shiftRows() {
        int[] temp = new int[4];
        for (int row = 1; row < 4; row++) {
            // Copy the row
            System.arraycopy(state[row], 0, temp, 0, 4);
            // Perform circular shift
            for (int col = 0; col < 4; col++) {
                state[row][col] = temp[(col + row) % 4];
            }
        }
    }

    /**
     * InvShiftRows transformation in AES, which shifts the rows of the state to the right.
     */
    private void invShiftRows() {
        int[] temp = new int[4];
        for (int row = 1; row < 4; row++) {
            // Copy the row
            System.arraycopy(state[row], 0, temp, 0, 4);
            // Perform inverse circular shift
            for (int col = 0; col < 4; col++) {
                state[row][col] = temp[(col + 4 - row) % 4];
            }
        }
    }

    /**
     * Multiplies two numbers in the Galois Field (2^8).
     *
     * @param a the first number
     * @param b the second number
     * @return the product of the multiplication
     */
    private int gmul(int a, int b) {
        int p = 0;
        for (int counter = 0; counter < 8; counter++) {
            if ((b & 1) != 0) {
                p ^= a;
            }
            boolean hi_bit_set = (a & 0x80) != 0;
            a <<= 1;
            if (hi_bit_set) {
                a ^= 0x1B; // x^8 + x^4 + x^3 + x + 1
            }
            b >>= 1;
        }
        return p & 0xFF;
    }

    /**
     * MixColumns transformation in AES, which mixes the columns of the state.
     */
    private void mixColumns() {
        int[] temp = new int[4];
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                temp[row] = state[row][col];
            }
            state[0][col] = gmul(temp[0], 2) ^ gmul(temp[1], 3) ^ temp[2] ^ temp[3];
            state[1][col] = temp[0] ^ gmul(temp[1], 2) ^ gmul(temp[2], 3) ^ temp[3];
            state[2][col] = temp[0] ^ temp[1] ^ gmul(temp[2], 2) ^ gmul(temp[3], 3);
            state[3][col] = gmul(temp[0], 3) ^ temp[1] ^ temp[2] ^ gmul(temp[3], 2);
        }
    }

    /**
     * InvMixColumns transformation in AES, which mixes the columns of the state using the inverse mix columns matrix.
     */
    private void invMixColumns() {
        int[] temp = new int[4];
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                temp[row] = state[row][col];
            }
            state[0][col] = gmul(temp[0], 0x0E) ^ gmul(temp[1], 0x0B) ^ gmul(temp[2], 0x0D) ^ gmul(temp[3], 0x09);
            state[1][col] = gmul(temp[0], 0x09) ^ gmul(temp[1], 0x0E) ^ gmul(temp[2], 0x0B) ^ gmul(temp[3], 0x0D);
            state[2][col] = gmul(temp[0], 0x0D) ^ gmul(temp[1], 0x09) ^ gmul(temp[2], 0x0E) ^ gmul(temp[3], 0x0B);
            state[3][col] = gmul(temp[0], 0x0B) ^ gmul(temp[1], 0x0D) ^ gmul(temp[2], 0x09) ^ gmul(temp[3], 0x0E);
        }
    }

    /**
     * AddRoundKey transformation in AES, which adds (XORs) the round key to the state.
     *
     * @param round the current round number
     */
    private void addRoundKey(int round) {
        for (int col = 0; col < NB; col++) {
            for (int row = 0; row < 4; row++) {
                state[row][col] ^= expandedKey[row][round * NB + col];
            }
        }
    }

    /**
     * Expands the initial key into the round keys for the AES algorithm.
     *
     * @param key the initial key used for the AES encryption/decryption
     */
    public void keyExpansion(byte[] key) {
        // Check if the key is valid
        if (key == null || key.length != 32) {
            throw new IllegalArgumentException("Key must be exactly 32 bytes for AES-256");
        }

        int[] w = new int[NB * (NR + 1) * 4];
        int k = NK * 4;

        // Copy the initial key
        for (int i = 0; i < k; i++) {
            w[i] = key[i] & 0xFF;
        }

        while (k < NB * (NR + 1) * 4) {
            int[] temp = new int[4];
            System.arraycopy(w, k - 4, temp, 0, 4);

            if (k % (NK * 4) == 0) {
                // RotWord
                int tempByte = temp[0];
                temp[0] = temp[1];
                temp[1] = temp[2];
                temp[2] = temp[3];
                temp[3] = tempByte;

                // SubWord
                for (int i = 0; i < 4; i++) {
                    temp[i] = AESConstants.SBOX[temp[i] & 0xFF];
                }

                // XOR with Rcon
                temp[0] ^= AESConstants.RCON[k / (NK * 4) - 1];
            } else if (NK > 6 && k % (NK * 4) == 16) {
                // Additional SubWord for AES-256
                for (int i = 0; i < 4; i++) {
                    temp[i] = AESConstants.SBOX[temp[i] & 0xFF];
                }
            }

            for (int i = 0; i < 4; i++) {
                w[k] = w[k - NK * 4] ^ temp[i];
                k++;
            }
        }

        // Convert to matrix format for expandedKey
        for (int i = 0; i < NB * (NR + 1); i++) {
            for (int j = 0; j < 4; j++) {
                expandedKey[j][i] = w[i * 4 + j];
            }
        }
    }

    /**
     * Encrypts a single block of plaintext using the AES algorithm.
     *
     * @param input the plaintext block to be encrypted
     * @return the encrypted block (ciphertext)
     */
    public byte[] encryptBlock(byte[] input) {
        // Check if the input block is valid
        if (input == null || input.length != 16) {
            throw new IllegalArgumentException("Input block must be exactly 16 bytes");
        }
        // Initialize the state with the input block
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[j][i] = input[i * 4 + j] & 0xFF;
            }
        }

        // Initial round
        addRoundKey(0);

        // Main rounds
        for (int round = 1; round < NR; round++) {
            subBytes();
            shiftRows();
            mixColumns();
            addRoundKey(round);
        }

        // Final round
        subBytes();
        shiftRows();
        addRoundKey(NR);

        // Convert the state to output
        byte[] output = new byte[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                output[i * 4 + j] = (byte) (state[j][i] & 0xFF);
            }
        }

        return output;
    }

    /**
     * Decrypts a single block of ciphertext using the AES algorithm.
     *
     * @param input the ciphertext block to be decrypted
     * @return the decrypted block (plaintext)
     */
    public byte[] decryptBlock(byte[] input) {
        // Check if the input block is valid
        if (input == null || input.length != 16) {
            throw new IllegalArgumentException("Input block must be exactly 16 bytes");
        }
        // Initialize the state with the input block
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[j][i] = input[i * 4 + j] & 0xFF;
            }
        }

        // Initial round (inverse)
        addRoundKey(NR);

        for (int round = NR - 1; round > 0; round--) {
            invShiftRows();
            invSubBytes();
            addRoundKey(round);
            invMixColumns();
        }

        // Final round (inverse)
        invShiftRows();
        invSubBytes();
        addRoundKey(0);

        // Convert the state to output
        byte[] output = new byte[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                output[i * 4 + j] = (byte) (state[j][i] & 0xFF);
            }
        }

        return output;
    }
}