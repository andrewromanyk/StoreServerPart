package ua.edu.ukma;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Processor {

    private static DBHandler db = new DBHandler();

    public Processor() {
        db.init();
    }

    public static void main(String[] args) {
        byte[] json = "{\"hello\":\"world\"}".getBytes();
        JSONObject jsonObject = new JSONObject(new String(json));
        System.out.println(jsonObject.toString());
        System.out.println(jsonObject.get("hello"));
        System.out.println(jsonObject.toString());
    }

    private Message result  = new Message(1, 1, new byte[]{1});

    public byte[] process(Message message) throws InterruptedException, UnknownHostException {
        Thread headerThread = new Thread(() -> messageHeader(message));
        Thread answerThread = new Thread(() -> messageBody(message));
        headerThread.start();
        answerThread.start();

        InetAddress filler = InetAddress.getLocalHost();

        Encriptor encriptor = new Encriptor();

        headerThread.join();
        answerThread.join();

        return encriptor.encript(result);
    }

    private void messageHeader(Message message){
        result.settype(message.type());
        result.setuserId(message.userId());
    }

    private void messageBody(Message message){
        result.setbyteMessage(Encryption.encrypt("Ok".getBytes(), KEY));
    }

    // 0 - create
    // 1 - delete
    // 2 - update
    // 3+ - lists
    //Json:
    //table - table to modify
    //info - inf to use
    private ResultSet handleMessage(Message message) throws SQLException {
        int type = message.type();
        byte[] info = message.getByteMessage();
        JSONObject json = new JSONObject(new String(info));
        String table = json.getString("table");
        if (type == 0){
            JSONObject infoJson = json.getJSONObject("info");
            String name = infoJson.getString("name");
            String description = infoJson.getString("description");
            String manufacturer = infoJson.getString("manufacturer");
            int amount = infoJson.getInt("amount");
            double price = infoJson.getDouble("price");
            int id_group = infoJson.getInt("id_group");
            db.createProduct(name, description, manufacturer, amount, price, id_group);
        }
        else if(type == 1){
            JSONObject infoJson = json.getJSONObject("info");
            int id = infoJson.getInt("id");
            db.deleteById(id);
        }
        else if(type == 2){
            JSONObject infoJson = json.getJSONObject("info");
            int id = infoJson.getInt("id");
            String name = infoJson.getString("name").equals("") ? null : infoJson.getString("name");
            String description = infoJson.getString("description").equals("") ? null : infoJson.getString("description");
            String manufacturer = infoJson.getString("manufacturer").equals("") ? null : infoJson.getString("manufacturer");
            String amount = infoJson.getInt("amount") == -1 ? null : String.valueOf(infoJson.getInt("amount"));
            String price = infoJson.getDouble("price") == -1 ? null : String.valueOf(infoJson.getDouble("price"));
            String id_group = infoJson.getInt("id_group") == -1 ? null : String.valueOf(infoJson.getDouble("price"));
            db.updateById(id, new String[]{name, description, manufacturer, amount, price, id_group});
        }
        else if(type == 3){
            JSONObject infoJson = json.getJSONObject("info");
            String name = infoJson.getString("name");
            ResultSet lst = db.getProduct(name);
            return lst;
        }
        ResultSet set = null;
        return set;
    }

}
