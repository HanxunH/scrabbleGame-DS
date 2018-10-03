package com.txg.scrabble.model;

import java.util.ArrayList;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txg.scrabble.config.Config;
import com.txg.scrabble.domain.Room;
import com.txg.scrabble.domain.User;
import com.txg.scrabble.view.GameView;
import com.txg.scrabble.view.PlayerRoomFrame;
import com.txg.scrabble.view.PlayersList;

public class DataOperations {

	public void RAddPlayer(JSONObject object) {
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
	}

	public void RListRoom(JSONObject object) {
		try {
			Vector<Integer> roomList = new Vector<Integer>();
			Config.roomList.removeAllElements();
			JSONArray array = object.getJSONArray("room_list");
			for (int i = 0; i < array.length(); i++) {
				JSONObject temp = array.getJSONObject(i);
				Room room = new Room();
				room.setRoomId(temp.getInt("room_id"));
				room.setRoomAvaliableSpot(temp.getInt("room_avaliable_spot"));
				room.setRoomIsGameStarted(temp.getBoolean("room_is_game_started"));
				Config.roomList.add(room);
				roomList.add(room.getRoomId());
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
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void RUpdatePlayerInRoom(JSONObject object) {
		// TODO Auto-generated method stub
		JSONArray array;
		PlayerRoomFrame.playerRoomFrame.list.clear();
		try {
			array = object.getJSONArray("player_list");
			for(int i=0;i<array.length();i++){
				JSONObject temp=array.getJSONObject(i);
				User user=new User();
				user.setId(temp.getInt("player_id"));
				user.setScore(temp.getInt("score"));
				user.setUserName(temp.getString("player_username"));
				user.setState(temp.getBoolean("is_ready"));
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
		try {
			JSONArray playerList=object.getJSONArray("plauer_list");
			for (int j = 0; j < playerList.length(); j++) {
				JSONObject temp=playerList.getJSONObject(j);
				User user=new User();
				user.setId(temp.getInt("player_id"));
				user.setScore(temp.getInt("player_score"));
				user.setUserName(temp.getString("player_username"));
				list.add(user);
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
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
