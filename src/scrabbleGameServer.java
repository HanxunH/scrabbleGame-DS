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
        public boolean isAgree = false;
        public int score = 0;
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
        public int firstWordCount;
        public int secondWordCount;
        public int totalVoteCount = 0;
        public gameRoom() {
            this.game = new scrabbleGame();
        }

        public int availableSpot(){
            synchronized (this){
                return maximumPlayersPerGame - connectedPlayers.size();
            }
        }

        public void unreadyAllPlayer(){
            for(int i=0; i<connectedPlayers.size(); i++){
                connectedPlayers.get(i).isPlayerReady = false;
            }
        }

        public void connectPlayer(connectedPlayerClient player){
            synchronized (this){
                if(connectedPlayers.size() < maximumPlayersPerGame){
                    connectedPlayers.add(player);
                    player.gameRoomObject = this;
                    player.isInRoom = true;
                    player.roomID = this.id;
                }else{
                    logger.severe("GameRoom connectPlayer: Room Full");
                }
            }
        }

        public void disconnectPlayer(connectedPlayerClient player){
            synchronized (this){
                connectedPlayers.remove(player);
//                if(connectedPlayers.size() == 0){
//                    synchronized(gameRoomList){
//                        gameRoomList.remove(this);
//                        updateRoomListToAllPlayers();
//                    }
//                }
                player.isInRoom = false;
                player.roomID = 0;
            }
        }

        
        public void updateGameRoomInfoToPlayers(){
            /* Send Game Room Update to All Connected Players */
            JSONObject json = new JSONObject();
            System.out.println("updateGameRoomInfoToPlayers");
            try {
                json.put("update", true);
                json.put("update_type", "game_room");
                json.put("game_room_id", this.id);
                json.put("response_code", 230);
                json.put("is_game_started", this.game.isStarted());
                json.put("minimal_player_per_game", minimalPlayersPerGame);
                json.put("maximum_player_per_game", maximumPlayersPerGame);
                json.put("current_players_count", connectedPlayers.size());
                json.put("available_spot", this.availableSpot());
                JSONArray playerInThisGameRoomList=new JSONArray();
                for(int i=0; i < connectedPlayers.size(); i++){
                    JSONObject playerJsonObject = new JSONObject();
                    // Update the score
                    if(this.game.isStarted()){
                        connectedPlayers.get(i).score = this.game.getPlayerScore(connectedPlayers.get(i).userID);
                    }
                    playerJsonObject.put("player_id", connectedPlayers.get(i).userID);
                    playerJsonObject.put("player_username", connectedPlayers.get(i).username);
                    playerJsonObject.put("is_ready", connectedPlayers.get(i).isPlayerReady);
                    playerJsonObject.put("score",connectedPlayers.get(i).score);
                    playerInThisGameRoomList.put(playerJsonObject);
                }
                json.put("player_list", playerInThisGameRoomList);
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

        public void askPlayersToVote(ArrayList<String> words, int wordOwnerPlayerID){
            /* Ask Every Player in the room to vote on a word */
            if(game.isStarted == false) {
        		logger.severe("Game did not started, cannot ask players to vote");
                return;
        	}
            JSONObject json = new JSONObject();
        	 try {
        		 json.put("vote", true);
                 json.put("words", words);
                 json.put("word_owner_id", wordOwnerPlayerID);
                 json.put("response_code", 240);
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

        public void updateGameStateToPlayers(){
            if(game.isStarted() == false && game.isFinished == false){
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
                json.put("response_code", 225);
                // TODO: UPDATE Game State to Client
                /*
                add Game State[[]]
                add Player list and score
                [{player_id : 0, player_username: "test", player_score : game.getPlayeObject(0).getScore()},
                 {player_id : 1,player_username: "test1", player_score : game.getPlayeObject(1).getScore()}]
                */
                json.put("game_state", gameState);
                JSONArray playerList = new JSONArray();
    			for(int i=0;i<this.connectedPlayers.size();i++) {
                    JSONObject player=new JSONObject();
                    if(this.game.isStarted()){
                        connectedPlayers.get(i).score = this.game.getPlayerScore(connectedPlayers.get(i).userID);
                    }
                    int id = connectedPlayers.get(i).userID;
    				String username = connectedPlayers.get(i).username;
    				int score = connectedPlayers.get(i).score;
                    player.put("player_id",id);
    				player.put("player_username",username);
    				player.put("player_score",score);
    				playerList.put(player);
    			}    
                json.put("player_list", playerList);
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

    private static connectedPlayerClient getConnectedPlayerClientObject(int id){
        for(int i = 0; i < clientList.size(); i++){
            if(clientList.get(i).userID == id){
                return clientList.get(i);
            }
        }
        return null;
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

    public static void invitePlayerToRoom(int inviteID, int ownerID, int roomID){
        /* Send a invite Request to player base on id */
        JSONObject json = new JSONObject();
        try {
            connectedPlayerClient invitePlayerObject = getConnectedPlayerClientObject(inviteID);
            if(invitePlayerObject == null){
                logger.severe("invitePlayerToRoom: No such user! ID: " + String.valueOf(inviteID));
                return;
            }
            json.put("response_code", 234);
            json.put("update", true);
            json.put("update_type", "invite");
            json.put("inviter_id", ownerID);
            json.put("room_id", roomID);
            String responseJSONString = json.toString();
            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream( invitePlayerObject.socket.getOutputStream()));
            outputStream.writeUTF(responseJSONString);
            outputStream.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void updatePlayerListToPlayers() {
        /* Called when new player joined room or leave room */
        JSONObject json = new JSONObject();
        System.out.println("updatePlayerListToPlayers");
        try {
            json.put("response_code", 220);
            JSONArray playerList=new JSONArray();
            for(int i=0; i < clientList.size(); i++){
                if(clientList.get(i).isLogin == false){
                    continue;
                }
                JSONObject playerJsonObject = new JSONObject();
                playerJsonObject.put("player_id", clientList.get(i).userID);
                playerJsonObject.put("player_is_in_room", clientList.get(i).isInRoom);
                playerJsonObject.put("player_username", clientList.get(i).username);
                playerList.put(playerJsonObject);
            }
            json.put("connected_client_list", playerList);
            String responseJSONString = json.toString();
            for(int i=0; i < clientList.size(); i++){
                DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(clientList.get(i).socket.getOutputStream()));
                outputStream.writeUTF(responseJSONString);
                outputStream.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void updateRoomListToAllPlayers(){
        /* Update All current room to All connected player */
        JSONObject json = new JSONObject();
        try {
            if(gameRoomList != null) {
                JSONArray roomList=new JSONArray();
                for(int i=0;i<gameRoomList.size();i++) {
                    JSONObject roomInformation=new JSONObject();
                    int id = gameRoomList.get(i).id;
                    int availableSpot = gameRoomList.get(i).availableSpot();
                    boolean IsGameStarted = gameRoomList.get(i).game.isStarted();
                    roomInformation.put("room_id",id);
                    roomInformation.put("room_avaliable_spot",availableSpot);
                    roomInformation.put("room_is_game_started",IsGameStarted);
                    roomList.put(roomInformation);
                }
                json.put("room_list", roomList);
            }else {
                try{
                    json.put("response", false);
                    json.put("response_code", 406);
                    json.put("error_message", "There is no rooms!");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            json.put("response_code", 204);
            json.put("update", true);
            json.put("update_type", "game_room");
            String responseJSONString = json.toString();
            for(int i=0; i < clientList.size(); i++){
                DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(clientList.get(i).socket.getOutputStream()));
                outputStream.writeUTF(responseJSONString);
                outputStream.flush();
            }
        }catch (Exception e){
            e.printStackTrace();}
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
    private static Object clientCounterLock = new Object();
    private static int getClientCounter() {
        synchronized (clientCounterLock){
            return clientCounter;
        }
    }

    private static void incrementClientCounter(){
        synchronized (clientCounterLock){
            clientCounter = clientCounter + 1;
        }
    }

    private static void decrementClientCounter(){
        synchronized (clientCounterLock){
            clientCounter = clientCounter - 1;
        }
    }

    private static void resetClientCounter(int n){
        synchronized (clientCounterLock){
            clientCounter = n;
        }
    }

    private static Object gameRoomCounterLock = new Object();
    private static int getRoomCounter() {
        synchronized (gameRoomList){
            return gameRoomList.size();
        }
    }

    private static gameRoom getGameRoomObject(int roomID){
        synchronized (gameRoomList){
            for(int i=0;i<gameRoomList.size();i++){
                if(gameRoomList.get(i).id == roomID){
                    return gameRoomList.get(i);
                }
            }
        }
        return null;
    }

    public static class clientHandlerThread extends Thread {
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
                json.put("error_code", errorCode);
                json.put("error_message", errorMessage);
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        public JSONObject clientRequestHandler(JSONObject json){
            /* Handle Client Request Json */
            JSONObject responseJson = new JSONObject();
            if(json.has("operation")){
                try{
                    responseJson.put("response", true);
                    String operationRequestString = json.getString("operation");
                    if(operationRequestString.equals("ADDPLAYER")){
                        /*
                        Add a player
                        */
                        if(json.has("player_username")){
                            boolean check = true;
                            for(int i=0;i<clientList.size();i++){
                                if(clientList.get(i).isLogin && clientList.get(i).username.equals(json.getString("player_username"))){
                                    jsonErrorHandler("Username Already Exist!", 404, responseJson);
                                    check = false;
                                }
                            }
                            if(json.getString("player_username").equals("")){
                                jsonErrorHandler("Username cannot be Empty!", 404, responseJson);
                                check = false;
                            }
                            if(check){
                                clientObject.username = json.getString("player_username");
                                clientObject.isLogin = true;
                                responseJson.put("player_id",clientObject.userID);
                                responseJson.put("response_code", 200);
                                updatePlayerListToPlayers();
                            }
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
                                responseJson.put("response_code", 201);
                                updateRoomListToAllPlayers();
                                updatePlayerListToPlayers();
                            }
                        }else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("LEAVEROOM")){
                        /* Player leave room */
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            if(json.has("player_room_id") && json.getInt("player_room_id") == clientObject.roomID){
                                gameRoom tempGameRoomObject = clientObject.gameRoomObject;
                                clientObject.gameRoomObject.disconnectPlayer(clientObject);
                                clientObject.gameRoomObject = null;
                                clientObject.isInRoom = false;
                                clientObject.isPlayerReady = false;
                                responseJson.put("player_id", clientObject.userID);
                                responseJson.put("player_is_in_game_room",clientObject.isInRoom);
                                responseJson.put("response_code", 202);
                                tempGameRoomObject.updateGameRoomInfoToPlayers();
                                updateRoomListToAllPlayers();
                                updatePlayerListToPlayers();
                            }else{
                                jsonErrorHandler("No such game room!", 404, responseJson);
                            }
                        }else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("JOINROOM")){
                        /* Player join room */
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            if(json.has("player_room_id")){
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
                                    responseJson.put("response_code", 203);
                                    gameRoom.updateGameRoomInfoToPlayers();
                                    updateRoomListToAllPlayers();
                                    updatePlayerListToPlayers();
                                }
                            }else{
                                jsonErrorHandler("Please provide a game room id!", 404, responseJson);
                            }
                        }else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("LISTROOM")){
                        /* List All avaliable room to the client */
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
                                responseJson.put("response_code", 204);
                                responseJson.put("player_id",clientObject.userID);
                                responseJson.put("room_list", roomList);
                    		}else {
                    			 jsonErrorHandler("There is no rooms!", 406, responseJson);
                    		}  		
                    	}else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("LISTCLIENTS")){
                        /*
                        show all connected clients
                        */
                    	 if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                     		if(clientList.size() != 0) {
                     			JSONArray connectedCilentList=new JSONArray(); 
                     			JSONObject connectedClientStatus=new JSONObject();
                     			for(int i = 0; i < clientList.size(); i++) {
                            		if(clientList.get(i).isLogin) {
                            			connectedClientStatus.put("player_id", clientList.get(i).userID);
                                        connectedClientStatus.put("player_name", clientList.get(i).username);
                            			connectedClientStatus.put("player_is_in_room", clientList.get(i).isInRoom);
                            			connectedCilentList.put(connectedClientStatus);
                            		}                            
                                 responseJson.put("connected_client_list", connectedCilentList);
                                 responseJson.put("response_code", 220);
                     			}
                     		}else {
                     			 jsonErrorHandler("There is no other users!", 420, responseJson);
                     		}  		
                     	}else{
                             jsonErrorHandler("Unauthorised", 403, responseJson);
                         }                	                   
                    }else if(operationRequestString.equals("INVITE")){
                    	 //TODO: invite users to join the room
                    	 if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                     		if(this.clientObject.isInRoom) {
                     			//int roomId = this.clientObject.gameRoomObject.id;	
                     			if(this.clientObject.gameRoomObject.availableSpot() <= 0){
                                    jsonErrorHandler("Room is full!", 405, responseJson);
                     			}else{
                     			    if(json.has("invitor_id") && getConnectedPlayerClientObject(json.getInt("invitor_id"))!= null ){
                                        connectedPlayerClient invite_target_player = getConnectedPlayerClientObject(json.getInt("invitor_id"));
                                        responseJson.put("response_code", 250);
                                        invitePlayerToRoom(invite_target_player.userID, this.clientObject.userID, this.clientObject.roomID);
                                    }else{
                                        jsonErrorHandler("No Such User!", 404, responseJson);
                                    }
                     				//int invitorID = json.getInt("player_id");
                     				//this.clientObject.gameRoomObject.connectedPlayers.add(invitor);
                     				//int roomID = this.clientObject.gameRoomObject.id;                                 
                                    //responseJson.put("invitor_id",this.clientObject.userID);
                                    //responseJson.put("invitor_name",this.clientObject.username);
                                    //responseJson.put("player_room_id",invitor.roomID);
                                    //responseJson.put("player_is_in_game_room",invitor.isInRoom);
                                    //responseJson.put("response_code", 234);
                                    //this.clientObject.gameRoomObject.updateGameRoomInfoToPlayers();
                                    //updateRoomListToAllPlayers();
                     			}
                     		}else {
                     			jsonErrorHandler("Player is not in room", 444, responseJson);
                     		}
                     	 }else{
                             jsonErrorHandler("Unauthorised", 403, responseJson);
                         }                	                   
                    }else if(operationRequestString.equals("INVITEJOIN")){
                   	 //TODO: invite users to join the room
                   	 	if(json.has("player_id_b") && json.getInt("player_id_b") == clientObject.userID){
                   	 	    if(json.has("accept") && json.getBoolean("accept")) {
                                //int roomId = this.clientObject.gameRoomObject.id;
                                connectedPlayerClient playerIDA = getConnectedPlayerClientObject(json.getInt("player_id_a"));
                                connectedPlayerClient playerIDB = getConnectedPlayerClientObject(json.getInt("player_id_b"));
                                if(playerIDA == null || playerIDB == null){
                                    jsonErrorHandler("No Such user!", 404, responseJson);
                                }else{
                                    System.out.println(playerIDA.username + playerIDB.username);
                                    playerIDA.gameRoomObject.connectPlayer(playerIDB);
                                    playerIDA.gameRoomObject.updateGameRoomInfoToPlayers();
                                    updateRoomListToAllPlayers();
                                    updatePlayerListToPlayers();
                                    responseJson.put("response_code", 235);
                                }
                            }else{
                   	 	        /* User Decline */
                   	 	        System.out.print(json.getBoolean("accept"));
                            }
                    	}else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("READY")){
                        /* Player Ready To PLAY */
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            this.clientObject.isPlayerReady = true;
                            responseJson.put("player_id",clientObject.userID);
                            responseJson.put("is_player_ready",clientObject.isPlayerReady);
                            responseJson.put("response_code", 205);
                            int readyClientCount = 0;
                            for(int i=0;i<this.clientObject.gameRoomObject.connectedPlayers.size();i++) {
                                if(this.clientObject.gameRoomObject.connectedPlayers.get(i).isPlayerReady)
                                    readyClientCount += 1;
                                else {
                                    break;
                                }
                            }
                            if(readyClientCount == this.clientObject.gameRoomObject.connectedPlayers.size() && readyClientCount >= minimalPlayersPerGame){
                                for(int i = 0; i < this.clientObject.gameRoomObject.connectedPlayers.size(); i++){
                                    this.clientObject.gameRoomObject.game.addPlayer(this.clientObject.gameRoomObject.connectedPlayers.get(i).username, this.clientObject.gameRoomObject.connectedPlayers.get(i).userID);
                                }
                                this.clientObject.gameRoomObject.game.startGame();
                            }
                            this.clientObject.gameRoomObject.updateGameRoomInfoToPlayers();
                            if(this.clientObject.gameRoomObject.game.isStarted()){
                                this.clientObject.gameRoomObject.updateGameStateToPlayers();
                            }
                    	}else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }                                   	
                    }else if(operationRequestString.equals("UNREADY")){
                        /* Player Unready to Play */
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            this.clientObject.isPlayerReady = false;
                            responseJson.put("player_id",clientObject.userID);
                            responseJson.put("is_player_ready",clientObject.isPlayerReady);
                            responseJson.put("response_code", 206);
                            this.clientObject.gameRoomObject.updateGameRoomInfoToPlayers();
                        }else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("ADDCHAR")){
                        /* Player ADD Char*/
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                    		gameRoom gameRoom = this.clientObject.gameRoomObject;
                    		if(gameRoom.game.isStarted()) {
                    			int column = json.getInt("colum");
                    			int row = json.getInt("row");
                    			String c = json.getString("character");
                    			char character = c.charAt(0);
                    			ArrayList<String> list = gameRoom.game.playerAddCharacter(column, row, character, this.clientObject.userID);
                                synchronized (gameRoom) {
                                    gameRoom.updateGameStateToPlayers();
                                    gameRoom.firstWordCount = 0;
                                    gameRoom.secondWordCount = 0;
                                    gameRoom.totalVoteCount = 0;
                                    gameRoom.askPlayersToVote(list, this.clientObject.userID);
                                }
                                responseJson.put("response_code", 207);
                            }else {
                    			jsonErrorHandler("game is not started", 407, responseJson);
                    		} 
                    	}else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }     
                    }else if(operationRequestString.equals("PASS")){
                        /* Player Pass this turn */
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                            gameRoom gameRoom = this.clientObject.gameRoomObject;
                            if(gameRoom.game.isStarted()) {
                                gameRoom.game.playerPassThisTurn(this.clientObject.userID);
                                gameRoom.updateGameStateToPlayers();
                                responseJson.put("response_code", 208);
                                if(gameRoom.game.isFinished){
                                    System.out.println(gameRoom.game.isFinished);
                                    gameRoom.unreadyAllPlayer();
                                    gameRoom.updateGameRoomInfoToPlayers();
                                    gameRoom.game = new scrabbleGame();
                                }else{
                                    System.out.println(gameRoom.game.isFinished);
                                }
                            }else {
                                jsonErrorHandler("game is not started", 407, responseJson);
                            }
                        }else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else if(operationRequestString.equals("VOTE")){
                        //TODO: If All players agree, game.approveWord(word, wordOwnerPlayerID) to update score
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID){
                    		gameRoom gameRoom = this.clientObject.gameRoomObject;
                    		 if(gameRoom.game.isStarted()) {
                    			 String word1 = json.getString("word1");
                                 String word2 = json.getString("word2");
                                 int wordOwnerPlayerID = json.getInt("vote_owner_id");
                                 Boolean firstWord = json.getBoolean("first_word");
                                 Boolean secondWord = json.getBoolean("second_word");
                                 synchronized (gameRoom) {
                                     gameRoom.totalVoteCount++;
                                     if (gameRoom.totalVoteCount >= gameRoom.connectedPlayers.size()) {
                                         gameRoom.game.incrementTheTurn();
                                     }
                                     if (firstWord)
                                         gameRoom.firstWordCount++;
                                     if (secondWord)
                                         gameRoom.secondWordCount++;
                                     if (gameRoom.firstWordCount == this.clientObject.gameRoomObject.connectedPlayers.size() && gameRoom.secondWordCount == this.clientObject.gameRoomObject.connectedPlayers.size()) {
                                         if (word1.length() >= word2.length()) {
                                             this.clientObject.gameRoomObject.game.approveWord(word1, wordOwnerPlayerID);
                                         } else {
                                             this.clientObject.gameRoomObject.game.approveWord(word2, wordOwnerPlayerID);
                                         }
                                     } else if (gameRoom.firstWordCount == this.clientObject.gameRoomObject.connectedPlayers.size() && gameRoom.secondWordCount != this.clientObject.gameRoomObject.connectedPlayers.size()) {
                                         this.clientObject.gameRoomObject.game.approveWord(word1, wordOwnerPlayerID);
                                     } else if (gameRoom.firstWordCount != this.clientObject.gameRoomObject.connectedPlayers.size() && gameRoom.secondWordCount == this.clientObject.gameRoomObject.connectedPlayers.size()) {
                                         this.clientObject.gameRoomObject.game.approveWord(word2, wordOwnerPlayerID);
                                     }
                                     gameRoom.updateGameStateToPlayers();
                                 }
                                 responseJson.put("response_code", 299);
                             }else {
                                 jsonErrorHandler("game is not started", 407, responseJson);
                             }	 
                    	}else{
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        } 
                    }else if(operationRequestString.equals("QUITGAME")){
                        /* Player Quit the game */
                        if(json.has("player_id") && json.getInt("player_id") == clientObject.userID) {
                            if(clientObject.gameRoomObject.game.isStarted){
                                clientObject.gameRoomObject.game.stopTheGame();
                                clientObject.gameRoomObject.disconnectPlayer(clientObject);
                                clientObject.gameRoomObject.updateGameStateToPlayers();
                                gameRoom tempGameRoomObject = clientObject.gameRoomObject;
                                clientObject.gameRoomObject = null;
                                clientObject.isInRoom = false;
                                clientObject.isPlayerReady = false;
                                tempGameRoomObject.unreadyAllPlayer();
                                tempGameRoomObject.game = new scrabbleGame();

                                tempGameRoomObject.updateGameRoomInfoToPlayers();
                                updatePlayerListToPlayers();
                                updateRoomListToAllPlayers();

                                responseJson.put("response_code", 330);
                            }else{
                                jsonErrorHandler("game is not started", 407, responseJson);
                            }
                        }else {
                            jsonErrorHandler("Unauthorised", 403, responseJson);
                        }
                    }else{
                        jsonErrorHandler("Operation Not Implemented", 501, responseJson);
                    }
                }catch (Exception e){
                    e.printStackTrace();
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
                    byte[] by = new byte[4096];
                    int n;
                    while ((n = inputStream.read(by)) != -1) {
                        data.write(by, 0, n);
                        if(data.toString().contains("</STOP>")){
                            break;
                        }
                    }
                    System.out.println(data);
                    String strInputstream = new String(data.toByteArray());
                    data.close();
                    /*
                        JSON Parsing
                    */

                    JSONObject json = new JSONObject(strInputstream);
                    /*
                    Handle client Request
                    */
                    JSONObject responseJson = clientRequestHandler(json);
                    System.out.println(responseJson);
                    String responseJSONString = responseJson.toString();

                    /*
                    Reply Client
                    */
                    DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(this.clientObject.socket.getOutputStream()));
                    outputStream.writeUTF(responseJSONString);outputStream.flush();logger.info("Replay for client ID: " + String.valueOf(this.clientObject.userID));

                }

            }catch (Exception e){
                logger.severe(e.getMessage());

                /* Disconnected Socket */
                if(clientObject.isInRoom || clientObject.gameRoomObject != null){
                    clientObject.gameRoomObject.disconnectPlayer(this.clientObject);
                    if(this.clientObject.gameRoomObject.game.isStarted){
                        clientObject.gameRoomObject.game.stopTheGame();
                        clientObject.gameRoomObject.updateGameStateToPlayers();
                    }
                    gameRoom tempGameRoomObject = clientObject.gameRoomObject;
                    clientObject.gameRoomObject = null;
                    clientObject.isInRoom = false;
                    clientObject.isPlayerReady = false;
                    tempGameRoomObject.unreadyAllPlayer();
                    tempGameRoomObject.game = new scrabbleGame();
                    tempGameRoomObject.updateGameRoomInfoToPlayers();
                    updateRoomListToAllPlayers();
                }
                synchronized (clientList){
                    clientList.remove(this.clientObject);
                }
                decrementClientCounter();
                updatePlayerListToPlayers();
                logger.info("Remove player ID: " + String.valueOf(this.clientObject.userID));
            }

        }
    }

    private static void gameServer(int port){
        /* Start the Server */
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Server started on port: " + String.valueOf(port));
            while (true) {
                logger.info("Waiting for Client ...");
                Socket socket = serverSocket.accept();
                logger.info("Client ID: " + String.valueOf(getClientCounter()) + " accepted");
                connectedPlayerClient client = new connectedPlayerClient();
                client.socket = socket;
                client.userID = getClientCounter();
                client.userIP = socket.getInetAddress();
                client.userIPString = client.userIP.getHostAddress();
                clientList.add(client);
                clientHandlerThread clientThread = new clientHandlerThread(client);
                clientThread.start();
                incrementClientCounter();
            }
        } catch (Exception e) {
            logger.severe(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
