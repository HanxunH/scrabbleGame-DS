package com.txg.scrabble.view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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

public class NewPlayerRoomFrame extends JFrame implements WindowListener{

	public ArrayList<User> list=new ArrayList<User>();
	public JLabel[] labels= new JLabel[3];
	public JLabel master=null;
	public JButton button=null;
	public JLabel[] states=new JLabel[3];
	public JPanel flowLayout=null;
	public static Room room=new Room();
	
	public NewPlayerRoomFrame() {
		// TODO Auto-generated constructor stub
		master=new JLabel("",JLabel.CENTER);
		master.setBorder(new LineBorder(Color.BLACK, 1));
		button=new JButton("Start");
		flowLayout=new JPanel(new FlowLayout());
		flowLayout.setBorder(new LineBorder(Color.BLACK, 1));
		flowLayout.add(button);
		this.setLayout(new GridLayout(4, 2));
		this.add(master);
		this.add(flowLayout);
		for(int i=0;i<labels.length;i++){
			labels[i]=new JLabel("",JLabel.CENTER);
			labels[i].setBorder(new LineBorder(Color.BLACK, 1));
			states[i]=new JLabel("",JLabel.CENTER);
			states[i].setBorder(new LineBorder(Color.BLACK, 1));
			this.add(labels[i]);
			this.add(states[i]);
		}
		updateStatus(list);
		this.setSize(400,200);
		this.setVisible(true);
		this.addWindowListener(this);
	}
	public void updateStatus(ArrayList<User> list){
		//this.list=list;
		User user1=new User();
		user1.setUserName("DENG");
		user1.setState(true);
		User user2=new User();
		user2.setUserName("LI");
		user2.setState(true);
		User user3=new User();
		user3.setUserName("YANG");
		user3.setState(true);
		list.add(user1);
		list.add(user2);
		list.add(user3);
		master.setText(list.get(0).getUserName());
		for(int i=1;i<list.size();i++){
			labels[i-1].setText(list.get(i).getUserName());
			if (list.get(i).isState()==false) {
				states[i-1].setText("Unready");				
			}else{
				states[i-1].setText("Ready");
			}
		}
	}
	public static void main(String[] args) {
		NewPlayerRoomFrame newPlayerRoomFrame=new NewPlayerRoomFrame();
	}
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		JSONObject object=new JSONObject();
		try {
			object.put("operation", "LEAVEROOM");
			object.put("player_id", Config.user.getId());
			object.put("player_room_id", this.room.getRoomId());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MessageController.controller.sendMessage(object);
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
}
