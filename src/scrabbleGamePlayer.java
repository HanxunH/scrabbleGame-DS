/***
 * @project project2
 * @author HanxunHuang ON 9/19/18
 ***/
public class scrabbleGamePlayer {
    private int userID;
    private int score;
    private String username;
    public scrabbleGame.gameAction lastAction;

    public scrabbleGamePlayer(String username, int userID){
        this.username = username;
        this.userID = userID;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }
}
