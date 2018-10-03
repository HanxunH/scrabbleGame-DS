/***
 * @project project2
 * @author HanxunHuang ON 9/19/18
 ***/
import java.util.*;
import java.util.logging.Logger;

public class scrabbleGame {

    enum gameAction{
        ADD,PASS
    }
    /* Game Related var */
    public char[][] gameState;
    public int[][] gameStatePlayerIDs;
    public int gameStateColumn = 20;
    public int gameStateRow = 20;

    ArrayList<scrabbleGamePlayer> playerList = new ArrayList<>();
    public final int minNumberOfPlayer = 2;
    public boolean isStarted = false;
    public int nextActionUserID = 0;
    public boolean isFinished = false;
    /* Helper var */
    public orcaLogerHelper loggerHandler = new orcaLogerHelper();
    public Logger logger = loggerHandler.getLogger();


    /* Helper Function */
    public void printGameState(){
        for(int i=0; i<gameStateColumn; i++){
            for(int j=0; j<gameStateRow; j++){
                if(gameState[i][j]==' '){
                    System.out.print("* ");
                }else{
                    System.out.print(gameState[i][j] + " ");
                }
            }
            System.out.println(" ");
        }
    }

    /* Setter & Getter */
    public boolean isStarted() {
        return isStarted;
    }
    public char[][] getGameState() {
        return gameState;
    }
    public int getNextActionUserID() {
        return nextActionUserID;
    }

    /* Game Constructor */
    public scrabbleGame(){
        /* Initilazing the game */
        this.gameState = new char[gameStateColumn][gameStateRow];
        this.gameStatePlayerIDs = new int[gameStateColumn][gameStateRow];
        for(int i=0; i<gameStateColumn; i++){
            for(int j=0; j<gameStateRow; j++){
                gameState[i][j] = ' ';
                gameStatePlayerIDs[i][j] = -1;
            }
        }
    }

    /* The Game */
    public scrabbleGamePlayer getPlayeObject(int playerID){
        for(int i=0; i<playerList.size(); i++){
            if(playerID == playerList.get(i).getUserID()){
                return playerList.get(i);
            }
        }
        return null;
    }

    public int getPlayerScore(int playerID) throws scrabbleGameException{
        scrabbleGamePlayer player = this.getPlayeObject(playerID);
        if(player == null){
            throw new scrabbleGameException("No such User! userID: " + String.valueOf(this.nextActionUserID));
        }
        return player.getScore();
    }

    private boolean validateChar(char c){
        int asciiValue = (int)c;
        if((asciiValue>=65 && asciiValue<=90)||(asciiValue>=97&&asciiValue<=122)){
            return true;
        }
        return false;
    }

    private boolean checkIfUserInPlayerList(int playerID){
        for(int i=0; i<playerList.size(); i++){
            if(playerID == playerList.get(i).getUserID()){
                return true;
            }
        }
        return false;
    }

    private ArrayList<String> getListOfWordsFromGameState(int column, int row){
        ArrayList<String> words = new ArrayList<String>();
        int rowLowerBound = row;
        int rowHigherBound = row;
        int columnLowerBound = column;
        int columnHigherLowerBound = column;

        for(int i=row; i<gameStateRow;i++){
            rowHigherBound = i;
            if(gameState[column][i] == ' '){
                break;
            }
        }
        for(int i=row; i>=0;i--){
            rowLowerBound = i;
            if(gameState[column][i] == ' '){
                break;
            }
        }

        for(int i=column; i<gameStateColumn;i++){
            columnHigherLowerBound = i;
            if(gameState[i][row] == ' '){
                break;
            }
        }
        for(int i=column; i>=0;i--){
            columnLowerBound = i;
            if(gameState[i][row] == ' '){
                break;
            }
        }
        String rowWord = "";
        String columnWord = "";
        for(int i=rowLowerBound;i<rowHigherBound;i++){
            rowWord = rowWord + gameState[column][i];
        }
        for(int i=columnLowerBound;i<columnHigherLowerBound;i++){
            columnWord = columnWord + gameState[i][row];
        }
        if(rowWord.length()>1){
            words.add(rowWord);
        }
        if(columnWord.length()>1){
            words.add(columnWord);
        }

        return words;
    }

    private void calculateAndSetScore(String word, scrabbleGamePlayer player){
        /* Caution: No validation */
        int score = player.getScore();
        score = score + word.length();
        player.setScore(score);
    }

    private void incrementTheTurn(){
        if(this.nextActionUserID + 1 < this.playerList.size()){
            this.nextActionUserID = this.nextActionUserID + 1;
        }else{
            this.nextActionUserID = 0;
        }
    }

    /* TODO: Server use following methods */
    public void addPlayer(String username, int userID) throws scrabbleGameException{
        if(this.isStarted){
            throw new scrabbleGameException("Game Already Started");
        }
        if(this.checkIfUserInPlayerList(userID)){
            throw new scrabbleGameException("UserID Already Exist: " + String.valueOf(userID));
        }
        scrabbleGamePlayer newPlayer = new scrabbleGamePlayer(username, userID);
        this.playerList.add(newPlayer);
    }

    public void startGame() throws scrabbleGameException{
        if(playerList.size() < minNumberOfPlayer){
            throw new scrabbleGameException("Not Enough Players");
        }
        this.isStarted = true;
        this.nextActionUserID = this.playerList.get(0).getUserID();
    }

    public ArrayList<String> playerAddCharacter(int column, int row, char c, int playerID) throws scrabbleGameException{
        /* Validation */
        if(!this.isStarted){
            throw new scrabbleGameException("Game did not start yet...");
        }
        if(playerID != this.nextActionUserID){
            throw new scrabbleGameException("Not your turn! Next turn should be userID: " + String.valueOf(this.nextActionUserID));
        }
        if(column > gameStateColumn || column < 0){
            throw new scrabbleGameException("Invalid Column index");
        }
        if(row > gameStateRow || row < 0){
            throw new scrabbleGameException("Invalid Row index");
        }
        if(!this.validateChar(c)){
            throw new scrabbleGameException("Invalid Character");
        }
        scrabbleGamePlayer player = getPlayeObject(playerID);
        if( player == null ){
            throw new scrabbleGameException("No such User! userID: " + String.valueOf(this.nextActionUserID));
        }

        /* Update the game State */
        if(this.gameState[column][row] == ' '){
            this.gameState[column][row] = c;
            this.gameStatePlayerIDs[column][row] = playerID;
        }
        player.lastAction = gameAction.ADD;
        /* Update the turn */
        this.incrementTheTurn();
        return this.getListOfWordsFromGameState(column,row);
    }

    public void approveWord(String word, int playerID) throws scrabbleGameException{
        /* Validation */
        if(!this.isStarted){
            throw new scrabbleGameException("Game did not start yet...");
        }
        scrabbleGamePlayer player = getPlayeObject(playerID);
        if( player == null ){
            throw new scrabbleGameException("No such User! userID: " + String.valueOf(this.nextActionUserID));
        }
        /* Update the word */
        player.setScore(player.getScore() + word.length());
    }


    public void playerPassThisTurn(int playerID) throws scrabbleGameException{
        if(!this.isStarted){
            throw new scrabbleGameException("Game did not start yet...");
        }
        if(playerID != this.nextActionUserID){
            throw new scrabbleGameException("Not your turn! Next turn should be userID: " + String.valueOf(this.nextActionUserID));
        }
        scrabbleGamePlayer player = getPlayeObject(playerID);
        if( player == null ){
            throw new scrabbleGameException("No such User! userID: " + String.valueOf(this.nextActionUserID));
        }
        player.lastAction = gameAction.PASS;
        int count = 0;
        for(int i = 0; i < playerList.size();i++){
            if(playerList.get(i).lastAction == gameAction.PASS){
                count = count + 1;
            }else{
                break;
            }
        }
        if(count == playerList.size()){
            isFinished = true;
            isStarted = false;
        }
        this.incrementTheTurn();
    }

    public void stopTheGame(){
        this.isStarted = false;
    }

    public static void main(String args[]){
        /* Testing the game */
        scrabbleGame game = new scrabbleGame();
        try {
            game.addPlayer("test",0);
            game.addPlayer("test",1);
            game.startGame();
            game.playerAddCharacter(0,0,'a',0);
            System.out.println(game.getPlayerScore(0));
            game.playerPassThisTurn(1);
            game.playerAddCharacter(0,1,'s',0);
            game.playerAddCharacter(1,1,'s',1);
            game.playerAddCharacter(1,2,'d',0);
            System.out.println(game.getPlayerScore(0));
            ArrayList<String> test = game.playerAddCharacter(0,2,'d',1);
            System.out.println(test);
            game.printGameState();
        }catch (Exception e){
            game.logger.severe(e.getMessage());
        }
    }
}

