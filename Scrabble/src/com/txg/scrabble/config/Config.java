package com.txg.scrabble.config;

import java.awt.Color;
import java.util.Vector;

import com.txg.scrabble.domain.Room;
import com.txg.scrabble.domain.User;

public class Config {
	//Define some constants here.
	public static final int MATRIXNUM=20;
	public static final Color bgColor=new Color(216, 200, 148);
	public static User user=new User();
	public static Vector<Room> roomList=new Vector<Room>();
	//Whether the game has already started
	public static boolean gameStartClick=true;
	//Whether the user is in a room
	public static boolean inRoom=false;
}
