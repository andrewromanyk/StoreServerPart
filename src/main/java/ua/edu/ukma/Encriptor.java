package ua.edu.ukma;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import static ua.edu.ukma.Key.KEY;

public class Encriptor {

    private int ENCRYPTED_SIZE;
    private ByteBuffer buffer;

    private void setBufferSize(Message message){
        ENCRYPTED_SIZE = (int) Math.ceil((double) message.getByteMessage().length /16) * 16;
    };

    public byte[] encript(Message message) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
//        setBufferSize(message);
//        buffer = ByteBuffer.allocate(ENCRYPTED_SIZE+8);
//        new Thread(() -> messageHeader(message)).start();
//        new Thread(() -> {
//            try {
//                messageBody(message);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }).start();

        return message.toByteArray();
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
