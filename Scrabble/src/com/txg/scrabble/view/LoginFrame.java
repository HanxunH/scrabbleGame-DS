package com.txg.scrabble.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.json.JSONException;
import org.json.JSONObject;

import com.txg.scrabble.config.Config;
import com.txg.scrabble.model.ConnectToServer;
import com.txg.scrabble.model.MessageController;

public class LoginFrame extends JFrame implements ActionListener{
	
	JLabel logo = null;
	JLabel usernameLabel = null;
	JLabel ipAddress = null;
	JLabel port = null;
	JTextField usernameInput = null;
	JPanel centerContent = null;
	JTextField ipAddressInput = null;
	JTextField portInput = null;

	JButton login = null;
	JButton test=null;
	JPanel southContent = null;

	public LoginFrame() {
		// TODO Auto-generated constructor stub
		logo = new JLabel(new ImageIcon(this.getClass().getResource("/images/logo.png")));

		usernameLabel = new JLabel("username :", JLabel.CENTER);
		usernameLabel.setBounds(10, 15, 100, 35);
		usernameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
		usernameInput = new JTextField();
		usernameInput.setFont(new Font("Arial", Font.PLAIN, 16));
		usernameInput.setBounds(150, 15, 300, 35);
		ipAddress = new JLabel("ipAddress", JLabel.CENTER);
		ipAddress.setBounds(10, 75, 85, 35);
		ipAddress.setFont(new Font("Arial", Font.PLAIN, 18));
		ipAddressInput = new JTextField();
		ipAddressInput.setBounds(105, 75, 185, 35);
		ipAddressInput.setFont(new Font("Arial", Font.PLAIN, 16));
		port = new JLabel("port :", JLabel.CENTER);
		port.setBounds(295, 75, 75, 35);
		port.setFont(new Font("Arial", Font.PLAIN, 18));
		portInput = new JTextField();
		portInput.setBounds(375, 75, 75, 35);
		portInput.setFont(new Font("Arial", Font.PLAIN, 16));
		
		centerContent = new JPanel(null);
		centerContent.add(usernameLabel);
		centerContent.add(usernameInput);
		centerContent.add(ipAddress);
		centerContent.add(ipAddressInput);
		centerContent.add(port);
		centerContent.add(portInput);
		centerContent.setBorder(new LineBorder(Color.GRAY, 1));

		login = new JButton("login");
		test=new JButton("test Connection");
		
		login.addActionListener(this);
		test.addActionListener(this);
		
		southContent = new JPanel(new FlowLayout(FlowLayout.CENTER,100,10));
		southContent.add(test);
		southContent.add(login);

		this.setLayout(new BorderLayout());
		this.add(logo, BorderLayout.NORTH);
		this.add(centerContent, BorderLayout.CENTER);
		this.add(southContent, BorderLayout.SOUTH);
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(size.width / 2 - 250, size.height / 2 - 200);
		this.setTitle("TXG_Scrabble");
		this.setSize(500, 400);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String[] args) {
		LoginFrame loginFrame = new LoginFrame();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource()==test) {
			if (ConnectToServer.socket==null||!ConnectToServer.socket.isConnected()) {
				ConnectToServer cts=new ConnectToServer(ipAddressInput.getText(),Integer.parseInt(portInput.getText()));
				cts.start();				
			}else{
				JOptionPane.showMessageDialog(null, "Connection has already established");
			}
		}else if(e.getSource()==login){
			if(ConnectToServer.socket!=null && ConnectToServer.socket.isConnected()){
				MessageController controller=new MessageController(ConnectToServer.socket);
				PlayersList playersList=new PlayersList();
				controller.start();
				JSONObject object=new JSONObject();
				try {
					object.put("operation", "ADDPLAYER");
					object.put("player_username", usernameInput.getText());
					Config.user.setUserName(usernameInput.getText());
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				controller.sendMessage(object);
				this.dispose();
			}
		}
	}
}
