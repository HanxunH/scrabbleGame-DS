package com.txg.scrabble.model;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageController extends Thread {

	public static MessageController controller=null;
	Socket socket = null;
	DataInputStream input = null;
	DataOutputStream output = null;

	public MessageController(Socket socket) {
		// TODO Auto-generated constructor stub
		controller=this;
		this.socket = socket;
		try {
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendMessage(JSONObject object) {
		try {
			System.out.println("发送:  "+object.toString());
			output.write(object.toString().getBytes());
			output.flush();
			output.write("</STOP>".getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void receiveMessage() {
		String message = null;
		try {
			while ((message = input.readUTF()) != null) {
				try {
					JSONObject object=new JSONObject(message);
					HandleMessage hm=new HandleMessage(object);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		receiveMessage();
	}

}
