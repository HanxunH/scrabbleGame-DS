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
     * Operation Action
     */
    enum operationAction{
        ADDPLAYER, CREATEROOM, LISTROOM, JOINROOM, READY, ADDCHAR;
    }


    /*
     * Connected Player Client Class
     */
    public static class connectedPlayerClient{
        public int userID;
        public String username;
        public InetAddress userIP;
        public String userIPString;
        public Socket socket;
        public boolean isLogin = false;
        public boolean isInRoom = false;
        public int roomID = 0;
        public gameRoom gameRoomObject;
        public boolean isPlayerReady = false;
    }

    /*
     * Game Room Class
     */
    public static class gameRoom{
        private int minimalPlayersPerGame = 2;
        private int maximumPlayersPerGame = 4;
        public ArrayList<connectedPlayerClient> connectedPlayers = new ArrayList<>();
        public scrabbleGame game;
        public int id;
        public boolean roomIsGameStarted = false;
        public gameRoom() {
            this.game = new scrabbleGame();
        }

        public int availableSpot(){
            return maximumPlayersPerGame - connectedPlayers.size();
        }

        public synchronized void connectPlayer(){

        }

        public synchronized void disconnectPlayer(connectedPlayerClient player){
            connectedPlayers.remove(player);
            if(connectedPlayers.size() == 0){
                gameRoomList.remove(this);
            }
            player.isInRoom = false;
            player.roomID = 0;
        }

        public synchronized void updateGameRoomInfoToPlayers(){
            Map<String, String> jsonMap = new HashMap<String, String>();
            jsonMap.put("update", "true");
            jsonMap.put("update_type", "game_room");
            jsonMap.put("game_room_id", String.valueOf(this.id));
            jsonMap.put("is_game_started", String.valueOf(this.game.isStarted()));
            jsonMap.put("minimal_player_per_game", String.valueOf(minimalPlayersPerGame));
            jsonMap.put("maximum_player_per_game", String.valueOf(maximumPlayersPerGame));
            jsonMap.put("current_players_count", String.valueOf(connectedPlayers.size()));
            jsonMap.put("available_spot", String.valueOf(this.availableSpot()));

            String responseJSONString = jsonMap.toString();
            for(int i=0; i < connectedPlayers.size(); i++){
                try{
                    DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(connectedPlayers.get(i).socket.getOutputStream()));
                    outputStream.writeUTF(responseJSONString);
                    outputStream.flush();
                    outputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


    /*
    * Initializing the Scrabble Game Server
    */
    private static int defaultPort = 6666;
    private static int clientCounter = 0;
    private static int gameRoomCounter = 0;
    private static int minimalPlayersPerGame = 2;
    private static int maximumPlayersPerGame = 4;
    private static orcaLogerHelper loggerHandler = new orcaLogerHelper();
    private static Logger logger = loggerHandler.getLogger();
    private static ArrayList<connectedPlayerClient> clientList = new ArrayList<>();
    private static ArrayList<gameRoom> gameRoomList = new ArrayList<>();
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

    private static synchronized int getRoomCounter() {
        return gameRoomCounter;
    }

    private static synchronized void incrementGameRoomCounter(){
        gameRoomCounter = gameRoomCounter + 1;
    }

    private static synchronized void decrementGameRoomCounter(){
        gameRoomCounter = gameRoomCounter - 1;
    }

    private static synchronized void resetGameRoomCounter(int n){
        gameRoomCounter = n;
    }

    private static synchronized gameRoom getGameRoomObject(int roomID){
        synchronized (gameRoomList){
            for(int i=0;i<gameRoomList.size();i++){
                if(gameRoomList.get(i).id == roomID){
                    return gameRoomList.get(i);
                }
            }
        }
        return null;
    }

    public static class clientHandlerThread implements Runnable {
        connectedPlayerClient clientObject;

        public clientHandlerThread(connectedPlayerClient clientObject) {
            this.clientObject = clientObject;
        }

        public void run() {
            clientHandler();
        }

        public void jsonErrorHandler(String errorMessage, int errorCode, Map<String, String> jsonMap){
            jsonMap.put("response_code", String.valueOf(errorCode));
            jsonMap.put("error_message", errorMessage);
        }


        public JSONObject clientRequestHandler(JSONObject json){
            Map<String, String> jsonMap = new HashMap<String, String>();
            jsonMap.put("response", "true");
            jsonMap.put("response_code", "200");
            if(json.has("operation")){
                try{
                    String operationRequestString = json.getString("operation");
                    if(operationRequestString.equals("ADDPLAYER")){
                        /*
                        Add a player
                        */
                        if(json.has("player_username")){
                            clientObject.username = json.getString("player_username");
                            clientObject.isLogin = true;
                            jsonMap.put("player_id",String.valueOf(clientObject.userID));
                        }else{
                            /*
                            No player_username Key
                            */
                            jsonErrorHandler("Please provide player Username", 404, jsonMap);
                        }
                    }else if(operationRequestString.equals("CREATEROOM")){
                        /*
                        Create a game room
                         */
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            if(this.clientObject.isInRoom){
                                jsonErrorHandler("Player Already in Game Room", 405, jsonMap);
                            }else{
                                gameRoom gameRoom = new gameRoom();
                                gameRoom.id = getRoomCounter();
                                incrementGameRoomCounter();
                                gameRoom.maximumPlayersPerGame = maximumPlayersPerGame;
                                gameRoom.minimalPlayersPerGame = minimalPlayersPerGame;
                                gameRoom.connectedPlayers.add(clientObject);
                                this.clientObject.isInRoom = true;
                                this.clientObject.roomID = gameRoom.id;
                                this.clientObject.gameRoomObject = gameRoom;
                                synchronized (gameRoomList){
                                    gameRoomList.add(gameRoom);
                                }
                                jsonMap.put("player_id",String.valueOf(clientObject.userID));
                                jsonMap.put("player_room_id",String.valueOf(gameRoom.id));
                                jsonMap.put("player_is_in_game_room",String.valueOf(clientObject.isInRoom));
                            }
                        }else{
                            jsonErrorHandler("Unauthorised", 403, jsonMap);
                        }
                    }else if(operationRequestString.equals("LEAVEROOM")){
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            if(json.has("player_room_id") && json.getInt("player_room_id") == clientObject.roomID){
                                gameRoom tempGameRoomObject = clientObject.gameRoomObject;
                                clientObject.gameRoomObject.disconnectPlayer(clientObject);
                                clientObject.gameRoomObject = null;
                                clientObject.isInRoom = false;
                                clientObject.isPlayerReady = false;
                                jsonMap.put("player_id",String.valueOf(clientObject.userID));
                                jsonMap.put("player_is_in_game_room",String.valueOf(clientObject.isInRoom));
                                tempGameRoomObject.updateGameRoomInfoToPlayers();
                            }else{
                                jsonErrorHandler("No such game room!", 404, jsonMap);
                            }
                        }else{
                            jsonErrorHandler("Unauthorised", 403, jsonMap);
                        }
                    }else if(operationRequestString.equals("JOINROOM")){
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            if(json.has("player_room_id") && json.getInt("player_room_id") == clientObject.roomID){
                                gameRoom gameRoom = getGameRoomObject(json.getInt("player_room_id"));
                                if(gameRoom == null){
                                    jsonErrorHandler("No such game room!", 404, jsonMap);
                                }else if(gameRoom.availableSpot() <= 0){
                                    jsonErrorHandler("Room is full!", 405, jsonMap);
                                }else{
                                    gameRoom.connectedPlayers.add(this.clientObject);
                                    this.clientObject.roomID = gameRoom.id;
                                    this.clientObject.gameRoomObject = gameRoom;
                                    this.clientObject.isInRoom = true;
                                    jsonMap.put("player_id",String.valueOf(clientObject.userID));
                                    jsonMap.put("player_room_id",String.valueOf(clientObject.roomID));
                                    jsonMap.put("player_is_in_game_room",String.valueOf(clientObject.isInRoom));
                                    gameRoom.updateGameRoomInfoToPlayers();
                                }
                            }else{
                                jsonErrorHandler("Please provide a game room id!", 404, jsonMap);
                            }
                        }else{
                            jsonErrorHandler("Unauthorised", 403, jsonMap);
                        }
                    }else if(operationRequestString.equals("LISTROOM")){
                        // TODO: List all room, check template
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                    		if(gameRoomList != null) {
                    			JSONArray roomList=new JSONArray(); 
                    			JSONObject roomInformation=new JSONObject();
                    			for(int i=0;i<gameRoomList.size();i++) {
                    				int id = gameRoomList.get(i).id;
                    				int availableSpot = gameRoomList.get(i).availableSpot();
                    				boolean IsGameStarted = gameRoomList.get(i).roomIsGameStarted;                				
                    				roomInformation.put("room_id",id);
                    				roomInformation.put("room_avaliable_spot",availableSpot);
                    				roomInformation.put("room_is_game_started",IsGameStarted);
                    				roomList.put(roomInformation);
                    			}
                    			jsonMap.put("player_id",String.valueOf(clientObject.userID));
                    			jsonMap.put("room_list", roomList.toString());
                    		}else {
                    			 jsonErrorHandler("There is no rooms!", 406, jsonMap);
                    		}  		
                    	}else{
                            jsonErrorHandler("Unauthorised", 403, jsonMap);
                        }
                    }else if(operationRequestString.equals("READY")){
                        // TODO: Set player to Ready, check if in the room, start the game if all player are ready
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                    		if(this.clientObject.isPlayerReady)
                    			this.clientObject.isPlayerReady = false;
                    		else
                    			this.clientObject.isPlayerReady = true;
                    		jsonMap.put("player_id",String.valueOf(clientObject.userID));
                    		jsonMap.put("is_player_ready",String.valueOf(clientObject.isPlayerReady));
                    		gameRoom gameRoom = getGameRoomObject(json.getInt("player_room_id"));                   		
                    		ArrayList<connectedPlayerClient> connectedPlayers = gameRoom.connectedPlayers;
                    		if(connectedPlayers.size()>1) {
                    			int readyClient = 0;
                    			for(int i=0;i<connectedPlayers.size();i++) {
                    				if(connectedPlayers.get(i).isPlayerReady)
                    					readyClient += 1;
                    				else {
                    					break;
                    				}
                    			}
                    			if(readyClient == connectedPlayers.size()) 
                    				gameRoom.roomIsGameStarted = true; //game start  
                        		jsonMap.put("is_game_start",String.valueOf(gameRoom.roomIsGameStarted));		
                    		} 		
                    	}else{
                            jsonErrorHandler("Unauthorised", 403, jsonMap);
                        }                                   	
                    }else if(operationRequestString.equals("ADDCHAR")){
                        // TODO: ADD Char
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                    		gameRoom gameRoom = getGameRoomObject(json.getInt("player_room_id"));
                    		if(gameRoom.roomIsGameStarted) {
                    			int column = json.getInt("colum");
                    			int row = json.getInt("row");
                    			String c = json.getString("character");
                    			char character = c.charAt(0);
                    			gameRoom.game.playerAddCharacter(column, row, character, this.clientObject.userID);		
                    		}else {
                    			jsonErrorHandler("game is not started", 407, jsonMap);
                    		} 
                    	}else{
                            jsonErrorHandler("Unauthorised", 403, jsonMap);
                        }     
                    }else{
                        jsonErrorHandler("Operation Not Implemented", 501, jsonMap);
                    }
                }catch (Exception e){
                    jsonErrorHandler(e.getMessage(), 500, jsonMap);
                }
            }else{
                // No operation Keyword
                jsonErrorHandler("No Operation Keyword found in request", 404, jsonMap);
            }

            /* Respond to Client */
            JSONObject responseJSON = new JSONObject(jsonMap);
            return responseJSON;
        }

        public void clientHandler() {
            try{
                while(this.clientObject.socket.isConnected()){
                    /* Get Data From Client */
                    DataInputStream inputStream = new DataInputStream(this.clientObject.socket.getInputStream());
                    ByteArrayOutputStream data = new ByteArrayOutputStream();
                    byte[] by = new byte[2048];
                    int n;
                    while ((n = inputStream.read(by)) != -1) {
                        data.write(by, 0, n);
                    }
                    String strInputstream = new String(data.toByteArray());
                    this.clientObject.socket.shutdownInput();
                    data.close();

                    /*
                        JSON Parsing
                    */
                    JSONObject json = new JSONObject(strInputstream);

                    /*
                        Handle client Request
                     */
                    JSONObject responseJson = clientRequestHandler(json);
                    String responseJSONString = responseJson.toString();

                    /*
                        Reply Client
                     */
                    DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(this.clientObject.socket.getOutputStream()));
                    outputStream.writeUTF(responseJSONString);
                    outputStream.flush();
                    outputStream.close();
                }

            }catch (Exception e){
                logger.severe(e.getMessage());

                /* Disconnect */
                synchronized (clientList){
                    clientList.remove(this.clientObject);
                }
                decrementClientCounter();
                if(clientObject.isInRoom || clientObject.gameRoomObject != null){
                    clientObject.gameRoomObject.disconnectPlayer(this.clientObject);
                }
                logger.info("Remove player ID: " + String.valueOf(this.clientObject.userID));
            }

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
                synchronized (clientList){
                    clientList.add(client);
                }
                clientHandlerThread clientThread = new clientHandlerThread(client);
                clientThread.run();
                incrementClientCounter();
            }
        } catch (Exception e) {
            logger.severe(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
