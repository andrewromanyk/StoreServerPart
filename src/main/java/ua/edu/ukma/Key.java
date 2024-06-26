package ua.edu.ukma;

import javax.crypto.spec.SecretKeySpec;

public class Key {
    private static final byte[] byte_key =
            {23, -13, 126, 12,
                    -1, 10, 1, -122,
                    -100, 10, 32, 1,
                    44, 123, -123, 0};
    public static final java.security.Key KEY = new SecretKeySpec(byte_key, "AES");
}
