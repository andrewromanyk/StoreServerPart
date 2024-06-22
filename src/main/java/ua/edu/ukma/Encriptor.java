package ua.edu.ukma;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;

public class Encriptor {
    private static final byte[] byte_key =
            {23, -13, 126, 12,
                    -1, 10, 1, -122,
                    -100, 10, 32, 1,
                    44, 123, -123, 0};
    private static final Key KEY = new SecretKeySpec(byte_key, "AES");

    private int ENCRYPTED_SIZE;
    private ByteBuffer buffer;

    private void setBufferSize(Message message){
        ENCRYPTED_SIZE = (int) Math.ceil((double) message.getByteMessage().length /16) * 16;
    };

    public byte[] encript(Message message){
        setBufferSize(message);
        buffer = ByteBuffer.allocate(ENCRYPTED_SIZE+9);
        new Thread(() -> messageHeader(message)).start();
        new Thread(() -> {
            try {
                messageBody(message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        return buffer.array();
    }

    private void messageHeader(Message message){
        synchronized (buffer) {
            buffer.putInt(message.type());
            buffer.putInt(message.userId());
        }
    }

    private void messageBody(Message message) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] array = Encryption.encrypt(message.getByteMessage(), KEY);
        buffer.put(array);
    }
}
