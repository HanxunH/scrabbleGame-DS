package com.txg.scrabble.domain;
//The domain class of the users
public class User {

	private String userName;
	private int id;
	private int score;
	private boolean state;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "User [userName=" + userName + ", id=" + id + ", score=" + score + ", getUserName()=" + getUserName()
				+ ", getId()=" + getId() + ", getScore()=" + getScore() + ", getClass()=" + getClass() + ", hashCode()="
				+ hashCode() + ", toString()=" + super.toString() + "]";
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public User(String userName, int id, int score) {
		super();
		this.userName = userName;
		this.id = id;
		this.score = score;
	}
	public User() {
		// TODO Auto-generated constructor stub
	}
	
}
