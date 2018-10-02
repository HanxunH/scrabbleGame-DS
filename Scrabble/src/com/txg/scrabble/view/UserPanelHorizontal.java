package com.txg.scrabble.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JPanel;

import com.txg.scrabble.config.Config;

public class UserPanelHorizontal extends JPanel{
	
	private int width=0;
	private int height=0;
	private String username;
	private int score;
	private int level;
	
	private int offsetBlock;
	
	public UserPanelHorizontal(int width,int height,String username,int score, int level) {
		// TODO Auto-generated constructor stub
		this.width=width;
		this.height=height;
		this.username=username;
		this.score=score;
		this.level=level;
		offsetBlock=height/15;
	}
	
	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		g.setColor(Config.bgColor);
		g.fillRect(0, 0, width, height);
		Image image=Toolkit.getDefaultToolkit().getImage("images/level_"+level+".png");
		g.drawImage(image, offsetBlock*15, (int)(offsetBlock*2.5), offsetBlock*10, offsetBlock*10, this);
		g.setColor(Color.black);
		g.setFont(new Font("Arial", Font.BOLD, 22));
		g.drawString(username, offsetBlock*30, offsetBlock*5);
		g.drawString("Score: "+score, offsetBlock*30, offsetBlock*10);
	}
}
