package com.txg.scrabble.config;

import java.awt.Color;
import java.util.Vector;

import com.txg.scrabble.domain.Room;
import com.txg.scrabble.domain.User;

public class Config {

	public static final int MATRIXNUM=20;
	public static final Color bgColor=new Color(216, 200, 148);
	public static User user=new User();
	public static Vector<Room> roomList=new Vector<Room>();
}
