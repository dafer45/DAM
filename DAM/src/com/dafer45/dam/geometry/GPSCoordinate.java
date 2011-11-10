package com.dafer45.dam.geometry;

import java.io.Serializable;

public class GPSCoordinate implements Serializable{
	public double latitude;
	public double longitude;
	public double altitude;

	public GPSCoordinate(double latitude, double longitude, double altitude){
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}
}
