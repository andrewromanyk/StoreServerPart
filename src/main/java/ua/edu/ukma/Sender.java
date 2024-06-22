package ua.edu.ukma;

import java.net.InetAddress;
import java.util.Arrays;

public class Sender {
    public static void sendMessage(byte[] mess, InetAddress target){
        //fake function
        System.out.println("Sent message: " + Arrays.toString(mess));
    }
}
