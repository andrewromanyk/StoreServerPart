package ua.edu.ukma;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Arrays;
import static ua.edu.ukma.Key.KEY;

public class Reciever {

    public static byte[] recievePacket(byte[] message) throws Exception {
        Packet packet =  new Packet(message, KEY);

        Decriptor decriptor = new Decriptor(packet.message().toByteArray());
        //System.out.println("Server started doing job!");
        byte[] result = decriptor.decrypt();

        packet.message().setbyteMessage(Encryption.decrypt(Arrays.copyOfRange(result, 8, result.length), KEY));
        Packet resPkt = new Packet(packet.src(), packet.message().type(), Encryption.decrypt(Arrays.copyOfRange(result, 8, result.length), KEY), packet.message().userId());
        return resPkt.toByteArray();
    }
}
