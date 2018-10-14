package com.txg.scrabble.model;

import java.net.Socket;

import javax.swing.JOptionPane;

public class ConnectToServer extends Thread{

	public static Socket socket=null;
	private String ipAddress=null;
	private int port=0;
	//The constructor to initialize the ip address and the port which come from the login frame.
	public ConnectToServer(String ipAddress,int port) {
		// TODO Auto-generated constructor stub
		this.ipAddress=ipAddress;
		this.port=port;
	}
	
	public Socket connect(){
		try {
			
			//Connect to the server
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
