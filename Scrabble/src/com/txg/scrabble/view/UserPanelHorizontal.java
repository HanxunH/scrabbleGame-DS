package com.txg.scrabble.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JPanel;

import com.txg.scrabble.config.Config;
import com.txg.scrabble.model.LevelCalculation;
// One of a sub-frame of the game frame
public class UserPanelHorizontal extends JPanelWrapper{
	
	private int width=0;
	private int height=0;
	private String username;
	//private int score;
	private int level;
	//public int id;
	private boolean thisTurn=false;
	private int offsetBlock;
	
	public UserPanelHorizontal(int type,int width,int height,String username,int score,int id) {
		// TODO Auto-generated constructor stub
		this.width=width;
		this.height=height;
		this.username=username;
		this.score=score;
		this.level=1;
		offsetBlock=height/15;
		this.id=id;
		this.type=type;
	}
	
	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		g.setColor(Config.bgColor);
		g.fillRect(0, 0, width, height);
		Image image=Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/images/level_"+level+".png"));
		g.drawImage(image, offsetBlock*15, (int)(offsetBlock*2.5), offsetBlock*10, offsetBlock*10, this);
		g.setColor(Color.black);
		g.setFont(new Font("Arial", Font.BOLD, 22));
		if (thisTurn==true) {
			g.setColor(Color.RED);
		}
		g.drawString(username, offsetBlock*30, offsetBlock*5);
		g.setColor(Color.black);
		g.drawString("Score: "+score, offsetBlock*30, offsetBlock*10);
	}
	public void updateScore(int score, int nextId){
		if (id==nextId) {
			thisTurn=true;
		}else{
			thisTurn=false;
		}
		this.score=score;
		this.level=LevelCalculation.calculateLevel(score);
		repaint();
	}
}
