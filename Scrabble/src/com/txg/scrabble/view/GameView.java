package com.txg.scrabble.view;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.json.JSONException;
import org.json.JSONObject;

import com.txg.scrabble.config.Config;
import com.txg.scrabble.domain.User;
import com.txg.scrabble.model.MessageController;

public class GameView extends JFrame implements KeyListener,ActionListener{

	public static GameView gameView=null;
	private int screenSize;
	public GamePanel gamePanel;

	private UserPanelHorizontal userPanelHorizontal1;
	private UserPanelHorizontal userPanelHorizontal2;
	private UserPanelVertical userPanelVertical1;
	private UserPanelVertical userPanelVertical2;
	private ArrayList<User> userList;
	
	private JButton agree=null;
	private JButton deny=null;
	private JButton pass=null;
	public JLabel character=null;

	public GameView(ArrayList<User> userList) {
		// TODO Auto-generated constructor stub
		this.userList = userList;
		gameView=this;
		screenSize = getGameViewSize();
		
		agree=new JButton("AGREE");
		agree.setBounds(screenSize/40, screenSize*35/40, screenSize/10, screenSize/30);
		agree.setBackground(Config.bgColor);
		deny=new JButton("DISAGREE");
		deny.setBounds(screenSize/40, screenSize*73/80, screenSize/10, screenSize/30);
		deny.setBackground(Config.bgColor);
		pass=new JButton("PASS");
		pass.setBounds(screenSize/40, screenSize*76/80, screenSize/10, screenSize/30);
		pass.setBackground(Config.bgColor);
		
		agree.addActionListener(this);
		deny.addActionListener(this);
		pass.addActionListener(this);
		agree.setFocusable(false);
		deny.setFocusable(false);
		pass.setFocusable(false);
		
		character=new JLabel("A",JLabel.CENTER);
		character.setFont(new Font("Arial", Font.BOLD, 100));
		character.setBounds(screenSize*17/20, screenSize*17/20, screenSize*3/20, screenSize*3/20);
		character.setBackground(Config.bgColor);
		gamePanel = new GamePanel((int) (screenSize * 0.7));
		gamePanel.setBounds(screenSize * 3 / 20, screenSize * 3 / 20, screenSize * 7 / 10, screenSize * 7 / 10);
		this.setLayout(null);
		this.add(gamePanel);
		if (userList.size() == 2) {
			userPanelHorizontal1 = new UserPanelHorizontal(screenSize * 7 / 10, screenSize * 3 / 20, "DENG Yue", 100,
					5);
			userPanelHorizontal1.setBounds(screenSize * 3 / 20, 0, screenSize * 7 / 10, screenSize * 3 / 20);
			userPanelHorizontal2 = new UserPanelHorizontal(screenSize * 7 / 10, screenSize * 3 / 20, "GUO Yuxin", 200,
					6);
			userPanelHorizontal2.setBounds(screenSize * 3 / 20, screenSize * 17 / 20, screenSize * 7 / 10,
					screenSize * 3 / 20);
			this.add(userPanelHorizontal1);
			this.add(userPanelHorizontal2);
		} else if (userList.size() == 3) {
			userPanelHorizontal1 = new UserPanelHorizontal(screenSize * 7 / 10, screenSize * 3 / 20, "DENG Yue", 100,
					5);
			userPanelHorizontal1.setBounds(screenSize * 3 / 20, 0, screenSize * 7 / 10, screenSize * 3 / 20);
			userPanelVertical1 = new UserPanelVertical(screenSize * 3 / 20, screenSize * 7 / 10, "YANG Zihan", 300, 2);
			userPanelVertical1.setBounds(0, screenSize * 3 / 20, screenSize * 3 / 20, screenSize * 7 / 10);
			userPanelVertical2 = new UserPanelVertical(screenSize * 3 / 20, screenSize * 7 / 10, "SHI Xiao", 400, 7);
			userPanelVertical2.setBounds(screenSize * 17 / 20, screenSize * 3 / 20, screenSize * 3 / 20,
					screenSize * 7 / 10);
			this.add(userPanelHorizontal1);
			this.add(userPanelVertical1);
			this.add(userPanelVertical2);
		} else if (userList.size() == 4) {
			userPanelHorizontal1 = new UserPanelHorizontal(screenSize * 7 / 10, screenSize * 3 / 20, "DENG Yue", 100,
					5);
			userPanelHorizontal1.setBounds(screenSize * 3 / 20, 0, screenSize * 7 / 10, screenSize * 3 / 20);
			userPanelHorizontal2 = new UserPanelHorizontal(screenSize * 7 / 10, screenSize * 3 / 20, "GUO Yuxin", 200,
					6);
			userPanelHorizontal2.setBounds(screenSize * 3 / 20, screenSize * 17 / 20, screenSize * 7 / 10,
					screenSize * 3 / 20);
			userPanelVertical1 = new UserPanelVertical(screenSize * 3 / 20, screenSize * 7 / 10, "YANG Zihan", 300, 2);
			userPanelVertical1.setBounds(0, screenSize * 3 / 20, screenSize * 3 / 20, screenSize * 7 / 10);
			userPanelVertical2 = new UserPanelVertical(screenSize * 3 / 20, screenSize * 7 / 10, "SHI Xiao", 400, 7);
			userPanelVertical2.setBounds(screenSize * 17 / 20, screenSize * 3 / 20, screenSize * 3 / 20,
					screenSize * 7 / 10);
			this.add(userPanelHorizontal1);
			this.add(userPanelHorizontal2);
			this.add(userPanelVertical1);
			this.add(userPanelVertical2);
		}else{
			userPanelHorizontal1 = new UserPanelHorizontal(screenSize * 7 / 10, screenSize * 3 / 20, "DENG Yue", 100,
					5);
			userPanelHorizontal1.setBounds(screenSize * 3 / 20, 0, screenSize * 7 / 10, screenSize * 3 / 20);
			this.add(userPanelHorizontal1);
		}
		this.add(character);
		this.add(agree);
		this.add(deny);
		this.add(pass);
		this.addKeyListener(this);
		this.setBackground(Config.bgColor);
		this.getContentPane().setBackground(Config.bgColor);
		this.setResizable(false);
		this.setVisible(true);
		this.setFocusable(true);
		this.setSize(screenSize, screenSize + 25);
		this.setTitle("TXG-Scrabble");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public int getGameViewSize() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = (int) screen.getWidth() * 9 / 10;
		int screenHeight = (int) screen.getHeight() * 9 / 10;
		int screenSize = (screenWidth >= screenHeight ? screenHeight : screenHeight) / 84 * 84;
		return screenSize;
	}

	public static void main(String[] args) {
		ArrayList<User> list = new ArrayList<User>();
		list.add(new User());
		list.add(new User());
		list.add(new User());
		list.add(new User());
		GameView gameView = new GameView(list);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyChar()<='z' && e.getKeyChar()>='a') {
			character.setText((char)(e.getKeyChar()+'A'-'a')+"");
		}else if(e.getKeyChar()<='Z' && e.getKeyChar()>='A'){
			character.setText(e.getKeyChar()+"");
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource()==agree) {
			System.out.println("agree");
		}else if(e.getSource()==deny){
			System.out.println("deny");
		}else if(e.getSource()==pass){
			JSONObject object=new JSONObject();
			try {
				object.put("operation", "PASS");
				object.put("player_id", Config.user.getId());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			MessageController.controller.sendMessage(object);
		}
	}
}
