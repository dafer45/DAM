package com.dafer45.dam.geometry;

import java.io.Serializable;

public class Coordinate implements Serializable{
	public Coordinate(){
	}

	public Coordinate(double setX, double setY){
		x = setX;
		y = setY;
	}

	public double x;
	public double y;
}
