package com.txg.scrabble.view;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import org.json.JSONException;
import org.json.JSONObject;

import com.txg.scrabble.config.Config;
import com.txg.scrabble.model.MessageController;

public class GamePanel extends JPanel implements MouseListener{

	private int panelSize;
	private int blockSize;
	public char[][] characters=new char[20][20];
	public GamePanel(int panelSize) {
		// TODO Auto-generated constructor stub
		this.panelSize=panelSize/Config.MATRIXNUM*Config.MATRIXNUM;
		this.addMouseListener(this);
	}
	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		drawBoard((Graphics2D) g);
		updateCharacters((Graphics2D) g);
	}
	public void drawBoard(Graphics2D g){
		panelSize=this.getWidth();
		blockSize=panelSize/20;
		g.setStroke(new BasicStroke(2.0f));
		for (int i = 0; i <= Config.MATRIXNUM; i++) {
			g.drawLine(0, i*blockSize, Config.MATRIXNUM*blockSize, i*blockSize);
			g.drawLine(i*blockSize, 0, i*blockSize, Config.MATRIXNUM*blockSize);
		}
		
	}
	public void updateCharacters(Graphics2D g){
		for(int i=0;i<20;i++){
			for(int j=0;j<20;j++){
				g.drawString(characters[i][j]+"", blockSize*i, blockSize*j);
			}
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		int x=e.getX();
		int y=e.getY();
		int xPosition=x/blockSize;
		int yPosition=y/blockSize;
		System.out.println(xPosition+"   "+yPosition);
		JSONObject object=new JSONObject();
		try {
			object.put("operation", "ADDCHAR");
			object.put("player_id", Config.user.getId());
			object.put("colum", xPosition);
			object.put("row", yPosition);
			object.put("character", GameView.gameView.character.getText());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MessageController.controller.sendMessage(object);
		 
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void setCharacters(char[][] characters) {
		this.characters = characters;
	}
}
