/***
 * @project project2
 * @author HanxunHuang ON 9/24/18
 ***/
import java.util.*;
import java.net.Socket;
import org.json.*;
import java.io.*;

public class serverTesting {
    public static void main(String[] args) {
        String address = "localhost";
        int port = 6666;
        Map<String, String> map = new HashMap<String, String>();
        map.put("operation","ADDPLAYER");
        map.put("player_username","yeezy");
        try{
            Socket socket = new Socket(address, port);
            JSONObject json = new JSONObject(map);
            String jsonString = json.toString();
            /* Send To Server*/
            byte[] jsonByte = jsonString.getBytes();
            DataOutputStream outputStream = null;
            outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.write(jsonByte);
            outputStream.flush();
            socket.shutdownOutput();

            /* Receive From Server */
            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            String strInputstream = inputStream.readUTF();
            JSONObject js = new JSONObject(strInputstream);
            jsonString = js.toString();
            System.out.print(jsonString);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}