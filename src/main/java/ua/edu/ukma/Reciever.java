package ua.edu.ukma;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Arrays;

public class Reciever {
    private static final byte[] byte_key =
            {23, -13, 126, 12,
                    -1, 10, 1, -122,
                    -100, 10, 32, 1,
                    44, 123, -123, 0};
    private static final Key KEY = new SecretKeySpec(byte_key, "AES");

    public static byte[] recievePacket(byte[] message) throws Exception {
        Packet packet =  new Packet(message, KEY);

        Decriptor decriptor = new Decriptor(packet.message().toByteArray());
        byte[] result = decriptor.decrypt();

        packet.message().setbyteMessage(Arrays.copyOfRange(result, 8, result.length));
        packet.setwLen(result.length-8);
        return packet.toByteArray();
    }
}
