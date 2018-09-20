/***
 * @project project2
 * @author HanxunHuang ON 9/19/18
 ***/

import java.util.logging.Logger;
import java.util.*;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.*;

public class scrabbleGameServer {
    /*
     * Connected Player Client Class
     */
    public static class connectedPlayerClient{
        public int userID;
        public String username;
        public InetAddress userIP;
        public String userIPString;
        public Socket socket;

    }

    /*
     * Game Room Class
     */
    public static class gameRoom{
        private int minimalPlayersPerGame = 2;
        private int maximumPlayersPerGame = 4;
        public ArrayList<connectedPlayerClient> connectedPlayers = new ArrayList<>();
        public scrabbleGame game;
        public gameRoom() {
            this.game = new scrabbleGame();
        }
    }


    /*
    * Initializing the Scrabble Game Server
    */
    private static int defaultPort = 6666;
    private static int clientCounter = 0;
    private static int minimalPlayersPerGame = 2;
    private static int maximumPlayersPerGame = 4;
    private static orcaLogerHelper loggerHandler = new orcaLogerHelper();
    private static Logger logger = loggerHandler.getLogger();
    private static ArrayList<connectedPlayerClient> clientList = new ArrayList<>();
    private static ServerSocket serverSocket = null;


    /*
     * Helper function
     */
    private static boolean checkNextArgumentStatus(String[] args, int i) {
        /* Check if command line args at i have next argument */
        if (i + 1 >= args.length || args[i + 1] == null) {
            logger.severe("Bad argument, Check Manual!");
            return false;
        }
        return true;
    }

    private static void setServerBaseOnConfig(String filePath) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;
            if (jsonObject.has("server_port")) {
                defaultPort = jsonObject.getInt("server_port");
            }
            if (jsonObject.has("minimal_players_per_game")) {
                minimalPlayersPerGame = jsonObject.getInt("minimal_players_per_game");
            }
            if (jsonObject.has("maximum_players_per_game")) {
                maximumPlayersPerGame = jsonObject.getInt("maximum_players_per_game");
            }
        } catch (Exception e) {
            logger.severe(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }


    /*
     * Main
     * Handle the arguments and server configuration
     */
    public static void main(String args[]) {
        // Handle the command args
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-config")) {
                if (checkNextArgumentStatus(args, i)) {
                    setServerBaseOnConfig(args[i + 1]);
                }
            }
            if (args[i].equals("-p")) {
                if (checkNextArgumentStatus(args, i)) {
                    try {
                        defaultPort = Integer.parseInt(args[i + 1]);
                    } catch (Exception e) {
                        logger.severe(e.getMessage());
                    }
                }
            }
        }

        // Start The Server
        gameServer(defaultPort);

    }



    /*
     * Socket Server
     */

    private static synchronized int getClientCounter() {
        return clientCounter;
    }

    private static synchronized void incrementClientCounter(){
        clientCounter = clientCounter + 1;
    }

    private static synchronized void decrementClientCounter(){
        clientCounter = clientCounter - 1;
    }

    private static synchronized void resetClientCounter(int n){
        clientCounter = n;
    }


    public static class clientHandlerThread implements Runnable {
        connectedPlayerClient clientObject;

        public clientHandlerThread(connectedPlayerClient clientObject) {
            this.clientObject = clientObject;
        }

        public void run() {
            clientHandler();
        }

        public void clientHandler() {
            
        }
    }

    private static void gameServer(int port){
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Server started on port: " + String.valueOf(port));
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("Client ID: " + String.valueOf(getClientCounter()) + " accepted");
                connectedPlayerClient client = new connectedPlayerClient();
                client.socket = socket;
                client.userID = getClientCounter();
                client.userIP = socket.getInetAddress();
                client.userIPString = client.userIP.getHostAddress();
                clientList.add(client);
                clientHandlerThread clientThread = new clientHandlerThread(client);
                clientThread.run();
                incrementClientCounter();
            }
        } catch (Exception e) {
            logger.severe(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
