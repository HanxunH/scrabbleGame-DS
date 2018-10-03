package com.txg.scrabble.model;

import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;

import com.txg.scrabble.config.Config;

public class HandleMessage {

	JSONObject object = null;

	public HandleMessage(JSONObject object) {
		// TODO Auto-generated constructor stub
		this.object = object;
		checkMessage();
	}

	public void checkMessage() {
		System.out.println(object.toString());
		DataOperations operations=new DataOperations();
		int code = 0;
		try {
			code = object.getInt("response_code");

			switch (code) {
			case 200:
				//System.out.println(object);
				operations.RAddPlayer(object);
				break;
			case 201:
				operations.RCreateRoom(object);
				break;
			case 202:
				break;
			case 203:
				break;
			case 204://
				operations.RListRoom(object);
				break;
			case 205:
				operations.RChangeButton(object);
				break;
			case 206:
				operations.RChangeButton(object);
				break;
			case 207:
				break;
			case 208:
				break;
			case 230:
				operations.RUpdatePlayerInRoom(object);
				break;
			case 225:
				operations.RStartGame(object);
				break;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
