/***
 * @project project2
 * @author HanxunHuang ON 9/24/18
 ***/
import java.util.*;
import java.net.Socket;
import org.json.*;
import java.io.*;

public class serverTesting {
    public static String address = "localhost";
    public static int port = 6666;
    public static Socket socket;
    private static final byte[] STOP = "</STOP>".getBytes();

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<String, String>();
        try{
            socket = new Socket(address, port);
        }catch (Exception e){
            e.printStackTrace();
        }
        map.put("operation","ADDPLAYER");
        map.put("player_username","yeezy");
        sendRequest(map);
        map.put("operation","CREATEROOM");
        map.put("player_id","0");
        sendRequest(map);
        map.put("operation","LISTROOM");
        map.put("player_id","0");
        sendRequest(map);
        map.put("operation","READY");
        map.put("player_id","0");
        sendRequest(map);
        try{
            Thread.sleep(5000);
        }catch (Exception e){

        }
    }

    public static void sendRequest(Map<String, String> map){
        try{
            JSONObject json = new JSONObject(map);
            String jsonString = json.toString();
            /* Send To Server*/
            byte[] jsonByte = jsonString.getBytes();
            DataOutputStream outputStream = null;
            outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.write(jsonByte);
            outputStream.flush();
            outputStream.write(STOP);
            outputStream.flush();
            /* Receive From Server */
            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            String strInputstream = inputStream.readUTF();
            System.out.println(strInputstream);
            JSONObject js = new JSONObject(strInputstream);
            jsonString = js.toString();
            System.out.println(jsonString);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}