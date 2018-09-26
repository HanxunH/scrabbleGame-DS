/***
 * @project project2
 * @author HanxunHuang ON 9/19/18
 ***/

import java.util.logging.Logger;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONArray;
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
        private int minimalPlayers = 2;
        private int maximumPlayers = 4;
        public ArrayList<connectedPlayerClient> connectedPlayers = new ArrayList<>();
        public scrabbleGame game;
        public int id;
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
            JSONObject json = new JSONObject();
            try {
                json.put("update", true);
                json.put("update_type", "game_room");
                json.put("game_room_id", this.id);
                json.put("is_game_started", this.game.isStarted());
                json.put("minimal_player_per_game", minimalPlayersPerGame);
                json.put("maximum_player_per_game", maximumPlayersPerGame);
                json.put("current_players_count", connectedPlayers.size());
                json.put("available_spot", this.availableSpot());
                String responseJSONString = json.toString();
                for(int i=0; i < connectedPlayers.size(); i++){
                    DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(connectedPlayers.get(i).socket.getOutputStream()));
                    outputStream.writeUTF(responseJSONString);
                    outputStream.flush();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public synchronized void askPlayersToVote(ArrayList<String> words, int wordOwnerPlayerID){
            // TODO:
        }

        public synchronized void updateGameStateToPlayers(){
            if(game.isStarted() == false){
                logger.severe("Game did not started, cannot update to players");
                return;
            }
            char[][] gameState = game.getGameState();
            JSONObject json = new JSONObject();
            try{
                json.put("next_action_user_id",game.getNextActionUserID());
                json.put("next_action_username", game.getPlayeObject(game.getNextActionUserID()).getUsername());
                json.put("is_started", game.isStarted());
                json.put("is_finish", game.isFinished);
                // TODO: UPDATE Game State to Client
                 /*
                add Game State[[]]
                add Player list and score
                [{player_id : 0, player_username: "test", player_score : game.getPlayeObject(0).getScore()},
                 {player_id : 1,player_username: "test1", player_score : game.getPlayeObject(1).getScore()}]
                */
            }catch (Exception e){
                e.printStackTrace();
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
    private static final byte[] STOP = "</STOP>".getBytes();


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

        public void jsonErrorHandler(String errorMessage, int errorCode, JSONObject json){
            try{
                json.put("response", false);
                json.put("response_code", errorCode);
                json.put("error_message", errorMessage);
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        public JSONObject clientRequestHandler(JSONObject json){
            JSONObject responseJson = new JSONObject();
            if(json.has("operation")){
                try{
                    responseJson.put("response", true);
                    responseJson.put("response_code", 200);
                    String operationRequestString = json.getString("operation");
                    if(operationRequestString.equals("ADDPLAYER")){
                        /*
                        Add a player
                        */
                        if(json.has("player_username")){
                            clientObject.username = json.getString("player_username");
                            clientObject.isLogin = true;
                            responseJson.put("player_id",clientObject.userID);
                        }else{
                            /*
                            No player_username Key
                            */
                            jsonErrorHandler("Please provide player Username", 404, responseJson);
                        }
                    }else if(operationRequestString.equals("CREATEROOM")){
                        /*
                        Create a game room
                         */
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            if(this.clientObject.isInRoom){
                                jsonErrorHandler("Player Already in Game Room", 405, responseJson);
                            }else{
                                gameRoom gameRoom = new gameRoom();
                                gameRoom.id = getRoomCounter();
                                incrementGameRoomCounter();
                                gameRoom.minimalPlayers = maximumPlayersPerGame;
                                gameRoom.minimalPlayers = minimalPlayersPerGame;
                                gameRoom.connectedPlayers.add(clientObject);
                                this.clientObject.isInRoom = true;
                                this.clientObject.roomID = gameRoom.id;
                                this.clientObject.gameRoomObject = gameRoom;
                                synchronized (gameRoomList){
                                    gameRoomList.add(gameRoom);
                                }
                                responseJson.put("player_id",clientObject.userID);
                                responseJson.put("player_room_id",gameRoom.id);
                                responseJson.put("player_is_in_game_room",clientObject.isInRoom);
                            }
                        }else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("LEAVEROOM")){
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            if(json.has("player_room_id") && json.getInt("player_room_id") == clientObject.roomID){
                                gameRoom tempGameRoomObject = clientObject.gameRoomObject;
                                clientObject.gameRoomObject.disconnectPlayer(clientObject);
                                clientObject.gameRoomObject = null;
                                clientObject.isInRoom = false;
                                clientObject.isPlayerReady = false;
                                responseJson.put("player_id", clientObject.userID);
                                responseJson.put("player_is_in_game_room",clientObject.isInRoom);
                                tempGameRoomObject.updateGameRoomInfoToPlayers();
                            }else{
                                jsonErrorHandler("No such game room!", 404, responseJson);
                            }
                        }else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("JOINROOM")){
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            if(json.has("player_room_id") && json.getInt("player_room_id") == clientObject.roomID){
                                gameRoom gameRoom = getGameRoomObject(json.getInt("player_room_id"));
                                if(gameRoom == null){
                                    jsonErrorHandler("No such game room!", 404, responseJson);
                                }else if(gameRoom.availableSpot() <= 0){
                                    jsonErrorHandler("Room is full!", 405, responseJson);
                                }else{
                                    gameRoom.connectedPlayers.add(this.clientObject);
                                    this.clientObject.roomID = gameRoom.id;
                                    this.clientObject.gameRoomObject = gameRoom;
                                    this.clientObject.isInRoom = true;
                                    responseJson.put("player_id",clientObject.userID);
                                    responseJson.put("player_room_id",clientObject.roomID);
                                    responseJson.put("player_is_in_game_room",clientObject.isInRoom);
                                    gameRoom.updateGameRoomInfoToPlayers();
                                }
                            }else{
                                jsonErrorHandler("Please provide a game room id!", 404, responseJson);
                            }
                        }else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("LISTROOM")){
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                    		if(gameRoomList != null) {
                    			JSONArray roomList=new JSONArray(); 
                    			JSONObject roomInformation=new JSONObject();
                    			for(int i=0;i<gameRoomList.size();i++) {
                    				int id = gameRoomList.get(i).id;
                    				int availableSpot = gameRoomList.get(i).availableSpot();
                    				boolean IsGameStarted = gameRoomList.get(i).game.isStarted();
                    				roomInformation.put("room_id",id);
                    				roomInformation.put("room_avaliable_spot",availableSpot);
                    				roomInformation.put("room_is_game_started",IsGameStarted);
                    				roomList.put(roomInformation);
                    			}
                                responseJson.put("player_id",clientObject.userID);
                                responseJson.put("room_list", roomList);
                    		}else {
                    			 jsonErrorHandler("There is no rooms!", 406, responseJson);
                    		}  		
                    	}else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("READY")){
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            this.clientObject.isPlayerReady = true;
                            responseJson.put("player_id",clientObject.userID);
                            responseJson.put("is_player_ready",clientObject.isPlayerReady);
                            int readyClient = 0;
                            for(int i=0;i<this.clientObject.gameRoomObject.connectedPlayers.size();i++) {
                                if(this.clientObject.gameRoomObject.connectedPlayers.get(i).isPlayerReady)
                                    readyClient += 1;
                                else {
                                    break;
                                }
                            }
                            if(readyClient == this.clientObject.gameRoomObject.connectedPlayers.size() && readyClient >= minimalPlayersPerGame){
                                for(int i = 0; i < this.clientObject.gameRoomObject.connectedPlayers.size(); i++){
                                    this.clientObject.gameRoomObject.game.addPlayer(this.clientObject.gameRoomObject.connectedPlayers.get(i).username, this.clientObject.gameRoomObject.connectedPlayers.get(i).userID);
                                }
                                this.clientObject.gameRoomObject.game.startGame();
                            }
                            this.clientObject.gameRoomObject.updateGameRoomInfoToPlayers();
                            this.clientObject.gameRoomObject.updateGameStateToPlayers();
//                    		gameRoom gameRoom = getGameRoomObject(json.getInt("player_room_id"));
//                    		ArrayList<connectedPlayerClient> connectedPlayers = gameRoom.connectedPlayers;
//                    		if(connectedPlayers.size()>1) {
//                    			int readyClient = 0;
//                    			for(int i=0;i<connectedPlayers.size();i++) {
//                    				if(connectedPlayers.get(i).isPlayerReady)
//                    					readyClient += 1;
//                    				else {
//                    					break;
//                    				}
//                    			}
//                    			if(readyClient == connectedPlayers.size())
//                    				gameRoom.roomIsGameStarted = true; //game start
//                        		jsonMap.put("is_game_start",String.valueOf(gameRoom.roomIsGameStarted));
//                    		}
                    	}else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }                                   	
                    }else if(operationRequestString.equals("UNREADY")){
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            this.clientObject.isPlayerReady = false;
                            responseJson.put("player_id",clientObject.userID);
                            responseJson.put("is_player_ready",clientObject.isPlayerReady);
                            this.clientObject.gameRoomObject.updateGameRoomInfoToPlayers();
                        }else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("ADDCHAR")){
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                    		gameRoom gameRoom = this.clientObject.gameRoomObject;
                    		if(gameRoom.game.isStarted()) {
                    			int column = json.getInt("colum");
                    			int row = json.getInt("row");
                    			String c = json.getString("character");
                    			char character = c.charAt(0);
                    			ArrayList<String> list = gameRoom.game.playerAddCharacter(column, row, character, this.clientObject.userID);
                    		    gameRoom.askPlayersToVote(list, this.clientObject.userID);
                    			gameRoom.updateGameStateToPlayers();
                    		}else {
                    			jsonErrorHandler("game is not started", 407, responseJson);
                    		} 
                    	}else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }     
                    }else if(operationRequestString.equals("PASS")){
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            gameRoom gameRoom = this.clientObject.gameRoomObject;
                            if(gameRoom.game.isStarted()) {
                                gameRoom.game.playerPassThisTurn(this.clientObject.userID);
                                gameRoom.updateGameStateToPlayers();
                            }else {
                                jsonErrorHandler("game is not started", 407, responseJson);
                            }
                        }else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("VOTE")){
                        //TODO: If All players agree, game.approveWord(word, wordOwnerPlayerID) to update score
                    }
                    else{
                        jsonErrorHandler("Operation Not Implemented", 501, responseJson);
                    }
                }catch (Exception e){
                    jsonErrorHandler(e.getMessage(), 500, responseJson);
                }
            }else{
                // No operation Keyword
                jsonErrorHandler("No Operation Keyword found in request", 404, responseJson);
            }

            /* Respond to Client */
            return responseJson;
        }

        public void clientHandler() {
            try{
                while(this.clientObject.socket.isConnected()){
                    logger.info("Handling request for client ID: " + String.valueOf(this.clientObject.userID));
                    /* Get Data From Client */
                    DataInputStream inputStream = new DataInputStream(this.clientObject.socket.getInputStream());
                    ByteArrayOutputStream data = new ByteArrayOutputStream();
                    byte[] by = new byte[2048];
                    int n;
                    while ((n = inputStream.read(by)) != -1) {
                        data.write(by, 0, n);
                        if(data.toString().contains("</STOP>")){
                            break;
                        }
                    }
                    System.out.println(data);
                    String strInputstream = new String(data.toByteArray());
//                    this.clientObject.socket.shutdownInput();
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
                    logger.info("Replay for client ID: " + String.valueOf(this.clientObject.userID));
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
