package ua.edu.ukma;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Processor {

    private static DBHandler db = new DBHandler();

    public Processor() throws SQLException {
        db.init();
    }

    public static void main(String[] args) {
        byte[] json = "{\"hello\":\"world\"}".getBytes();
        JSONObject jsonObject = new JSONObject(new String(json));
        System.out.println(jsonObject.toString());
        System.out.println(jsonObject.get("hello"));
        System.out.println(jsonObject.toString());
    }

    private Message result  = new Message(1, 1, new byte[0]);

    public byte[] process(Message message) throws InterruptedException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, SQLException {
        //System.out.println("Server started processing!");
        Thread headerThread = new Thread(() -> messageHeader(message));
        Thread answerThread = new Thread(() -> {
            try {
                messageBody(message);
            } catch (SQLException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        headerThread.start();
        answerThread.start();
        headerThread.join();
        answerThread.join();

        Encriptor encriptor = new Encriptor();

        //System.out.println("Server stopped processing!");
        return encriptor.encript(result);
    }

    private void messageHeader(Message message){
        result.settype(message.type());
        result.setuserId(message.userId());
    }

    private void messageBody(Message message) throws SQLException, JsonProcessingException {
        String res = null;
        try {
            res = handleMessage(message);
        }
        catch (Exception e) {
            res = "Bad";
        }
        finally {
            if (res == null) result.setbyteMessage("Ok".getBytes());
            else {
                //System.out.println(Arrays.toString(res.getBytes()));
                result.setbyteMessage(res.getBytes());
            }
        }
    }

    // 0 - create
    // 1 - delete
    // 2 - update
    // 3+ - lists
    //Json:
    //table - table to modify
    //info - inf to use
    private String handleMessage(Message message) throws SQLException, JsonProcessingException {
        //System.out.println("Server started handling!");
        int type = message.type();
        byte[] info = message.getByteMessage();
        //System.out.println("string info: " + new String(info));
        JSONObject json = new JSONObject(new String(info));
        String table = json.getString("table");
        if (table.equals("goods")) {
            if (type == 0) {
                JSONObject infoJson = json.getJSONObject("info");
                String name = infoJson.getString("name");
                String description = infoJson.getString("description");
                String manufacturer = infoJson.getString("manufacturer");
                int amount = infoJson.getInt("amount");
                double price = infoJson.getDouble("price");
                int id_group = infoJson.getInt("id_group");
                db.createProduct(name, description, manufacturer, amount, price, id_group);
            } else if (type == 1) {JSONObject infoJson = json.getJSONObject("info");
                int id = infoJson.getInt("id");
                db.deleteByIdGood(id);
            } else if (type == 2) {
                JSONObject infoJson = json.getJSONObject("info");
                int id = infoJson.getInt("id");
                String name = infoJson.getString("name").isEmpty() ? null : infoJson.getString("name");
                String description = infoJson.getString("description").isEmpty() ? null : infoJson.getString("description");
                String manufacturer = infoJson.getString("manufacturer").isEmpty() ? null : infoJson.getString("manufacturer");
                String amount = infoJson.getInt("amount") == -1 ? null : String.valueOf(infoJson.getInt("amount"));
                String price = infoJson.getDouble("price") == -1 ? null : String.valueOf(infoJson.getDouble("price"));
                String id_group = infoJson.getInt("id_group") == -1 ? null : String.valueOf(infoJson.getInt("id_group"));
                db.updateByIdGood(id, new String[]{name, description, manufacturer, amount, price, id_group});
            } else if (type == 3) {
                List<goods> lst = db.getAllProductsList();
                ObjectMapper mapper = new ObjectMapper();
                String tosend = mapper.writeValueAsString(lst);
                return tosend;
            }
        }
        else if (table.equals("group")){
            if (type == 0) {
                JSONObject infoJson = json.getJSONObject("info");
                String name = infoJson.getString("name");
                String description = infoJson.getString("description");
                db.createGroup(name, description);
            } else if (type == 1) {
                JSONObject infoJson = json.getJSONObject("info");
                int id = infoJson.getInt("id");
                db.deleteByGroupGood(id);
                db.deleteByIdGroup(id);
            } else if (type == 2) {
                JSONObject infoJson = json.getJSONObject("info");
                int id = infoJson.getInt("id");
                String name = infoJson.getString("name").isEmpty() ? null : infoJson.getString("name");
                String description = infoJson.getString("description").isEmpty() ? null : infoJson.getString("description");
                System.out.println("Description");
                db.updateByIdGroup(id, new String[]{name, description});
            } else if (type == 3) {
                List<groups> lst = db.getAllGroups();
                ObjectMapper mapper = new ObjectMapper();
                String tosend = mapper.writeValueAsString(lst);
                //System.out.println(tosend);
                return tosend;
            }
        }
        else if (table.equals("hash")){
            JSONObject infoJson = json.getJSONObject("info");
            String name = infoJson.getString("name");

            List<String> lst = db.getCreds(name);
            if (lst == null) return null;

            ObjectMapper mapper = new ObjectMapper();

            return mapper.writeValueAsString(lst);
        }

        if (type == 2) {
            db.stopConnection();
            db.init();
        }
        //System.out.println("Server stopped handling!");
        return null;
    }

}
