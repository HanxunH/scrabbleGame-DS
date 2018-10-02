package com.txg.scrabble.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.JSONException;
import org.json.JSONObject;

import com.txg.scrabble.config.Config;
import com.txg.scrabble.model.MessageController;

public class PlayersList extends JFrame{
	private JScrollPane listScrollPane;
	private JScrollPane list1ScrollPane;
	public static JList list = new JList();
	public static  JList list_1 = new JList();
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PlayersList window = new PlayersList();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PlayersList() {
		initialize();
		this.setVisible(true);
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		this.setBounds(100, 100, 450, 300);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 1.0 };
		gbl_panel.columnWeights = new double[] { 1.0, 0.0, 1.0 };
		JPanel panel = new JPanel(gbl_panel);
		panel.setBackground(new Color(204, 153, 51));
		this.getContentPane().add(panel, BorderLayout.CENTER);

		JLabel lblOnlinePlayers = new JLabel("Online Players");
		lblOnlinePlayers.setBounds(10, 15, 100, 35);
		GridBagConstraints gbc_lblOnlinePlayers = new GridBagConstraints();
		gbc_lblOnlinePlayers.insets = new Insets(0, 0, 5, 5);
		gbc_lblOnlinePlayers.gridx = 0;
		gbc_lblOnlinePlayers.gridy = 0;
		panel.add(lblOnlinePlayers, gbc_lblOnlinePlayers);

		JLabel lblOnlineRooms = new JLabel("Online Rooms");
		GridBagConstraints gbc_lblOnlineRooms = new GridBagConstraints();
		gbc_lblOnlineRooms.insets = new Insets(0, 0, 5, 0);
		gbc_lblOnlineRooms.gridx = 2;
		gbc_lblOnlineRooms.gridy = 0;
		panel.add(lblOnlineRooms, gbc_lblOnlineRooms);

		JButton btnInvite = new JButton("CREATE");
		GridBagConstraints gbc_btnInvite = new GridBagConstraints();
		gbc_btnInvite.insets = new Insets(0, 0, 5, 5);
		gbc_btnInvite.gridx = 0;
		gbc_btnInvite.gridy = 1;
		panel.add(btnInvite, gbc_btnInvite);
		btnInvite.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JSONObject object=new JSONObject();
				try {
					object.put("operation", "CREATEROOM");
					object.put("player_id", Config.user.getId());
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				MessageController.controller.sendMessage(object);
			}
		});

		JButton btnJoin = new JButton("JOIN");
		GridBagConstraints gbc_btnJoin = new GridBagConstraints();
		gbc_btnJoin.insets = new Insets(0, 0, 5, 0);
		gbc_btnJoin.gridx = 2;
		gbc_btnJoin.gridy = 1;
		panel.add(btnJoin, gbc_btnJoin);

		
		list.setBackground(new Color(255, 204, 102));
		GridBagConstraints gbc_list = new GridBagConstraints();
		gbc_list.insets = new Insets(0, 0, 0, 5);
		gbc_list.fill = GridBagConstraints.BOTH;
		gbc_list.gridx = 0;
		gbc_list.gridy = 2;
		panel.add(list, gbc_list);
		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				// TODO Auto-generated method stub
				if (arg0.getValueIsAdjusting()) {
					
					int[] index = list.getSelectedIndices();
					ListModel listModel = list.getModel();
					for (int i : index) {
						System.out.println("clicked: " + listModel.getElementAt(i));
					}
				}
			}

		});

		list_1.setBackground(new Color(255, 204, 102));
		GridBagConstraints gbc_list_1 = new GridBagConstraints();
		gbc_list_1.fill = GridBagConstraints.BOTH;
		gbc_list_1.gridx = 2;
		gbc_list_1.gridy = 2;
		panel.add(list_1, gbc_list_1);
		list_1.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if (e.getValueIsAdjusting()) {
					int[] index = list_1.getSelectedIndices();
					ListModel listModel = list_1.getModel();
					for (int i : index) {
						JSONObject object=new JSONObject();
						try {
							object.put("operation", "JOINROOM");
							object.put("player_id", Config.user.getId());
							object.put("room_id", listModel.getElementAt(i));
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						MessageController.controller.sendMessage(object);
					}					
				}

			}

		});

	}

}
