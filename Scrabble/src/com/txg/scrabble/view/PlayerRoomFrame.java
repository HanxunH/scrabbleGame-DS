package com.txg.scrabble.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.json.JSONException;
import org.json.JSONObject;

import com.txg.scrabble.config.Config;
import com.txg.scrabble.domain.Room;
import com.txg.scrabble.domain.User;
import com.txg.scrabble.model.MessageController;
// This is a room class, which will show the player and whether a player is ready now
public class PlayerRoomFrame extends JFrame implements WindowListener,ActionListener {

	public static PlayerRoomFrame playerRoomFrame = null;
	public ArrayList<User> list = new ArrayList<User>();
	public JLabel[] labels = new JLabel[4];
	public JButton button = null;
	public JLabel[] states = new JLabel[4];
	public JPanel flowLayout = null;
	public static Room room = new Room();
	public JPanel grid=null;
	public PlayerRoomFrame() {
		// TODO Auto-generated constructor stub
		playerRoomFrame = this;
		
		User initUser=new User();
		initUser.setId(Config.user.getId());
		initUser.setState(false);
		initUser.setUserName(Config.user.getUserName());
		list.add(initUser);
		
		button = new JButton("READY");
		button.addActionListener(this);
		flowLayout = new JPanel(new FlowLayout());
		flowLayout.setBorder(new LineBorder(Color.BLACK, 1));
		flowLayout.add(button);
		grid=new JPanel(new GridLayout(4, 2));
		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel("", JLabel.CENTER);
			labels[i].setBorder(new LineBorder(Color.BLACK, 1));
			states[i] = new JLabel("", JLabel.CENTER);
			states[i].setBorder(new LineBorder(Color.BLACK, 1));
			grid.add(labels[i]);
			grid.add(states[i]);
		}
		updateStatus();
		this.setLayout(new BorderLayout());
		this.add(grid,BorderLayout.CENTER);
		this.add(flowLayout,BorderLayout.SOUTH);
		//updateStatus();
		this.setSize(400, 200);
		this.setVisible(true);
		this.addWindowListener(this);
	}
	// Clear the list
	public void clearStatus(){
		for(int i=0;i<labels.length;i++){
			labels[i].setText("");
			states[i].setText("");
		}
	}
	// Then write the new data	
	public void updateStatus() {
		clearStatus();
		for (int i = 0; i < list.size(); i++) {
			labels[i].setText(list.get(i).getUserName());
			if (list.get(i).isState()==false) {
				states[i].setText("UNREADY");				
			}else{
				states[i].setText("READY");
			}

		}
	}

	public static void main(String[] args) {
		PlayerRoomFrame newPlayerRoomFrame = new PlayerRoomFrame();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}
	// If the user close the room frame, it will send LEAVEROOM request to the server
	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		JSONObject object = new JSONObject();
		try {
			object.put("operation", "LEAVEROOM");
			object.put("player_id", Config.user.getId());
			object.put("player_room_id", this.room.getRoomId());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MessageController.controller.sendMessage(object);
		Config.inRoom=false;
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}
	// Change the user status.
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource()==button) {
			JSONObject object=new JSONObject();
			try {
				object.put("player_id", Config.user.getId());
				if (button.getText().equals("READY")) {
					object.put("operation", "READY");
				}else if(button.getText().equals("UNREADY")){
					object.put("operation", "UNREADY");
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			MessageController.controller.sendMessage(object);
		}
	}
}
