package ua.edu.ukma;

import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionTest {

    @Test
    void dataConsistency() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        assertArrayEquals(bytes, Encryption.decrypt(Encryption.encrypt(bytes, Key.KEY), Key.KEY));
        bytes = new byte[]{1, -1, 15, 100};
        assertArrayEquals(bytes, Encryption.decrypt(Encryption.encrypt(bytes, Key.KEY), Key.KEY));
        bytes = new byte[]{1};
        assertArrayEquals(bytes, Encryption.decrypt(Encryption.encrypt(bytes, Key.KEY), Key.KEY));
        bytes = new byte[]{10, 11, -123, 15, -10};
        assertArrayEquals(bytes, Encryption.decrypt(Encryption.encrypt(bytes, Key.KEY), Key.KEY));
    }
}