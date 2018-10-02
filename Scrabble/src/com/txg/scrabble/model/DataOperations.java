package com.txg.scrabble.model;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txg.scrabble.config.Config;
import com.txg.scrabble.domain.Room;
import com.txg.scrabble.view.NewPlayerRoomFrame;
import com.txg.scrabble.view.PlayersList;


public class DataOperations {

	public void RAddPlayer(JSONObject object){
		JSONObject listRoom=new JSONObject();
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
	public void RListRoom(JSONObject object){
		try {
			Vector<Integer> roomList=new Vector<Integer>();
			Config.roomList.removeAllElements();
			JSONArray array=object.getJSONArray("room_list");
			for(int i=0;i<array.length();i++){
				JSONObject temp=array.getJSONObject(i);
				Room room=new Room();
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
	public void RCreateRoom(JSONObject object){
		NewPlayerRoomFrame frame=new NewPlayerRoomFrame();
		try {
			frame.room.setRoomId(object.getInt("player_room_id"));
			frame.room.players.add(Config.user);
			JSONObject listRoom=new JSONObject();
			listRoom.put("operation", "LISTROOM");
			listRoom.put("player_id", Config.user.getId());
			MessageController.controller.sendMessage(listRoom);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
