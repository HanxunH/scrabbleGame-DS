package com.txg.scrabble.model;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txg.scrabble.config.Config;
import com.txg.scrabble.domain.Room;
import com.txg.scrabble.domain.User;
import com.txg.scrabble.view.GameView;
import com.txg.scrabble.view.LoginFrame;
import com.txg.scrabble.view.PlayerRoomFrame;
import com.txg.scrabble.view.PlayersList;

public class DataOperations {

	public void RAddPlayer(JSONObject object) {
		LoginFrame.loginFrame.dispose();
		PlayersList playersList=new PlayersList();
		JSONObject listRoom = new JSONObject();
		try {
			Config.user.setId(object.getInt("player_id"));
			listRoom.put("operation", "LISTROOM");
			listRoom.put("player_id", Config.user.getId());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MessageController.controller.sendMessage(listRoom);
		PlayersList.playersList.setTitle(Config.user.getUserName());
	}

	public void RListRoom(JSONObject object) {
		try {
			Vector<String> roomList = new Vector<String>();
			Config.roomList.removeAllElements();
			JSONArray array = object.getJSONArray("room_list");
			for (int i = 0; i < array.length(); i++) {
				JSONObject temp = array.getJSONObject(i);
				Room room = new Room();
				room.setRoomId(temp.getInt("room_id"));
				room.setRoomAvaliableSpot(temp.getInt("room_avaliable_spot"));
				room.setRoomIsGameStarted(temp.getBoolean("room_is_game_started"));
				Config.roomList.add(room);
				roomList.add(room.getRoomId()+"     "+room.getRoomAvaliableSpot()+"'s left     "+room.isRoomIsGameStarted());
			}
			PlayersList.list_1.setListData(roomList);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void RCreateRoom(JSONObject object) {
		PlayerRoomFrame frame = new PlayerRoomFrame();

		try {
			frame.room.setRoomId(object.getInt("player_room_id"));
			frame.room.players.add(Config.user);
			Config.inRoom=true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void RGetInvitation(JSONObject object){
		JSONObject reply=new JSONObject();
		// A invites B
		try {
			int invitorId=object.getInt("inviter_id");
			int result=JOptionPane.showConfirmDialog(null, invitorId+" invites you to game !", "Invitation",JOptionPane.YES_NO_OPTION);
			reply.put("operation", "INVITEJOIN");
			reply.put("player_id_b", Config.user.getId());
			reply.put("player_id_a", invitorId);
			if (result==0) {
				//YES
				reply.put("accept", true);
				PlayerRoomFrame frame = new PlayerRoomFrame();
			}else{
				//NO
				reply.put("accept", false);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageController.controller.sendMessage(reply);
	}

	public void RUpdatePlayerInRoom(JSONObject object) {
		// TODO Auto-generated method stub
		JSONArray array;
		PlayerRoomFrame.playerRoomFrame.list.removeAll(PlayerRoomFrame.playerRoomFrame.list);
		try {
			array = object.getJSONArray("player_list");
			for(int i=0;i<array.length();i++){
				JSONObject temp=array.getJSONObject(i);
				User user=new User();
				user.setId(temp.getInt("player_id"));
				user.setScore(temp.getInt("score"));
				user.setUserName(temp.getString("player_username"));
				user.setState(temp.getBoolean("is_ready"));
				if (user.getId()==Config.user.getId()) {
					if(temp.getBoolean("is_ready")){
						PlayerRoomFrame.playerRoomFrame.button.setText("UNREADY");
					}else{
						PlayerRoomFrame.playerRoomFrame.button.setText("READY");
					}					
				}
				PlayerRoomFrame.playerRoomFrame.list.add(user);
			}
			PlayerRoomFrame.playerRoomFrame.updateStatus();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void RChangeButton(JSONObject object) {
		// TODO Auto-generated method stub
		try {
			if (object.getBoolean("is_player_ready")==true) {
				PlayerRoomFrame.playerRoomFrame.button.setText("UNREADY");
			}else{
				PlayerRoomFrame.playerRoomFrame.button.setText("READY");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void RStartGame(JSONObject object) {
		// TODO Auto-generated method stub
		ArrayList<User> list=new ArrayList<User>();
		char[][] characters=new char[20][20];
		int maxScore=0;
		boolean flag=false;
		String winner="";
		try {
			JSONArray playerList=object.getJSONArray("player_list");
			for (int j = 0; j < playerList.length(); j++) {
				JSONObject temp=playerList.getJSONObject(j);
				User user=new User();
				user.setId(temp.getInt("player_id"));
				user.setScore(temp.getInt("player_score"));
				user.setUserName(temp.getString("player_username"));
				list.add(user);
				if (temp.getInt("player_score")>=maxScore) {
					maxScore=temp.getInt("player_score");
					winner=temp.getString("player_username");
				}
				if (temp.getInt("player_id")==Config.user.getId()) {
					flag=true;
				}
			}
			if (flag==true) {
				if (object.getBoolean("is_started")==false) {
					Config.gameStartClick=true;
					JOptionPane.showMessageDialog(null, "Game Over, the winner is "+winner);
					GameView.gameView.dispose();
					return;
				}				
			}
			if (Config.gameStartClick==true) {
				GameView gameView=new GameView(list);	
				Config.gameStartClick=false;
			}
			JSONArray array = object.getJSONArray("game_state");
			for(int i=0;i<array.length();i++){
				JSONArray chars=array.getJSONArray(i);
				for (int j = 0; j < chars.length(); j++) {
					characters[i][j]= chars.getString(j).charAt(0);
				}
				GameView.gameView.gamePanel.setCharacters(characters);
				GameView.gameView.gamePanel.repaint();
			}
			int nextId=object.getInt("next_action_user_id");
			GameView.gameView.updateInfomation(list,nextId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void RVote(JSONObject object){
		try {
			JSONArray array= object.getJSONArray("words");
			int ownerid=object.getInt("word_owner_id");
			String word1=array.getString(0);
			String word2=array.getString(1);
			GameView.gameView.showVoteDialog(GameView.gameView, GameView.gameView, word1, word2, ownerid);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void RPlayerList(JSONObject object){
		try {
			Vector<String> playerList = new Vector<String>();
			Config.roomList.removeAllElements();
			JSONArray array = object.getJSONArray("connected_client_list");
			for (int i = 0; i < array.length(); i++) {
				JSONObject temp = array.getJSONObject(i);
				int id=temp.getInt("player_id");
				boolean inRoom=temp.getBoolean("player_is_in_room");
				String username=temp.getString("player_username");
				if (inRoom==true) {
					playerList.add(id+"     "+username+"     UNAVAILABLE");
				}else{
					playerList.add(id+"     "+username+"       AVAILABLE");					
				}
			}
			PlayersList.list.setListData(playerList);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
