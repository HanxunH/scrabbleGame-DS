package com.txg.scrabble.model;

public class LevelCalculation {

	public static int calculateLevel(int score){
		if (score<=50) {
			return 1;
		}else if(score<=100){
			return 2;
		}else if(score<=250){
			return 3;
		}else if(score<=600){
			return 4;
		}else if(score<=1000){
			return 5;
		}else if(score<=2000){
			return 6;
		}else if(score<=3000){
			return 7;
		}
		return 1;
	}
}
