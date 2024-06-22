package ua.edu.ukma;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Arrays;

public class Message {
    private int cType;
    private int bUserId;
    private byte[] message;

    private byte[] byte_key =
            {0, -15, -9, 127,
                    65, -77, 1, 123,
                    -36, 12, -32, 1,
                    44, -15, 15, 99};
    private Key KEY = new SecretKeySpec(byte_key, "AES");

    public Message(int type, int id, byte[] mess){
        cType = type;
        bUserId = id;
        message = mess;
    }

    public int type() {return cType;}
    public int userId() {return bUserId;}
    public byte[] getByteMessage() {return message;}

    public byte[] toByteArray() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] encrypted = Encryption.encrypt(message, KEY);
        ByteBuffer buffer = ByteBuffer.allocate(8 + encrypted.length);
        buffer.putInt(cType);
        buffer.putInt(bUserId);
        buffer.put(encrypted);
        return buffer.array();
    }

    public void settype(int type) { cType = type;}
    public void setuserId(int id) { bUserId = id;}
    public void setbyteMessage(byte[] mess) { message = Arrays.copyOf(mess, message.length);}

/*    public byte[] toByteArray() throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] encryptedMessage = encrypt(message, KEY);
        int messageLength = encryptedMessage.length;

        byte[] bytearray = new byte[8 + messageLength];

        System.arraycopy(PacketCreator.toBigEndian(cType, 4), 0, bytearray, 0, 4);
        System.arraycopy(PacketCreator.toBigEndian(bUserId, 4), 0, bytearray, 4, 4);
        System.arraycopy(encryptedMessage, 0, bytearray, 8, messageLength);
        return bytearray;
    }*/
}
