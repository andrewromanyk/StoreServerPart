package ua.edu.ukma;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.sql.SQLException;
import java.util.Arrays;

import static ua.edu.ukma.Key.KEY;

public class Decriptor {

    private Message message;
    byte[] mess;

    public Decriptor(byte[] message) {
        mess = message;
    }

    public byte[] decrypt() throws InterruptedException, UnknownHostException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, SQLException {
        message = new Message(0, 0, new byte[1]);
        //System.out.println("Server started decrypting!");
        Thread headerThread = new Thread(() -> messageHeader(Arrays.copyOfRange(mess, 0, 8)));
        Thread messageThread = new Thread(() -> {
            try {
                messageBody(Arrays.copyOfRange(mess, 8, mess.length));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        headerThread.start();
        messageThread.start();
        messageThread.join();
        //System.out.println("Server stopped decrypting!");
        //System.out.println("Recived message: " + Arrays.toString(message.getByteMessage()));
        Processor processor = new Processor();
        return processor.process(message);
    }

    private void messageHeader(byte[] mess){
        synchronized (message) {
            ByteBuffer buffer = ByteBuffer.wrap(mess);
            message.settype(buffer.getInt());
            message.setuserId(buffer.getInt());
        }
    }

    private void messageBody(byte[] mess) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        message.setbyteMessage(Encryption.decrypt(mess, KEY));
    }
}
