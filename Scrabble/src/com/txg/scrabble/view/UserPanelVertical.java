package com.txg.scrabble.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import com.txg.scrabble.config.Config;

public class UserPanelVertical extends JPanel{
	
	private int width=0;
	private int height=0;
	private String username;
	private int score;
	private int level;
	
	private int offsetBlock;
	
	public UserPanelVertical(int width,int height,String username,int score, int level) {
		// TODO Auto-generated constructor stub
		this.width=width;
		this.height=height;
		this.username=username;
		this.score=score;
		this.level=level;
		offsetBlock=width/15;
	}
	
	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		g.setColor(Config.bgColor);
		g.fillRect(0, 0, width, height);
		Image image=Toolkit.getDefaultToolkit().getImage("images/level_"+level+".png");
		g.drawImage(image, (int)(offsetBlock*2.5),offsetBlock*15, offsetBlock*10, offsetBlock*10, this);
		g.setColor(Color.black);
		Font font =new Font("Arial", Font.BOLD, 22);
		AffineTransform affineTransform = new AffineTransform();
		affineTransform.rotate(Math.toRadians(90), 0, 0);
		Font rotatedFont = font.deriveFont(affineTransform);
		g.setFont(rotatedFont);
		g.drawString(username, offsetBlock*10, offsetBlock*30);
		g.drawString("Score: "+score, offsetBlock*5, offsetBlock*30);
	
	}
}
