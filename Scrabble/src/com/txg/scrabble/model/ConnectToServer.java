package com.txg.scrabble.model;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

public class ConnectToServer extends Thread{

	public static Socket socket=null;
	private String ipAddress=null;
	private int port=0;
	
	public ConnectToServer(String ipAddress,int port) {
		// TODO Auto-generated constructor stub
		this.ipAddress=ipAddress;
		this.port=port;
	}
	
	public Socket connect(){
		try {
			this.socket=new Socket(ipAddress,port);
			JOptionPane.showMessageDialog(null, "Connection Success");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Connection Fail");
			e.printStackTrace();
		}
		return socket;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		connect();
	}
}
