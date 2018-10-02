package com.txg.scrabble.domain;

import java.util.ArrayList;

public class Room {
	public ArrayList<User> players=new ArrayList<User>();
	private int roomId;
	private int roomAvaliableSpot;
	private boolean roomIsGameStarted;
	public int getRoomId() {
		return roomId;
	}
	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}
	public int getRoomAvaliableSpot() {
		return roomAvaliableSpot;
	}
	public void setRoomAvaliableSpot(int roomAvaliableSpot) {
		this.roomAvaliableSpot = roomAvaliableSpot;
	}
	public boolean isRoomIsGameStarted() {
		return roomIsGameStarted;
	}
	public void setRoomIsGameStarted(boolean roomIsGameStarted) {
		this.roomIsGameStarted = roomIsGameStarted;
	}
	
}
