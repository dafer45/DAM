package com.dafer45.dam.geometry;

import java.util.Vector;
import java.io.Serializable;

public class Polygon{
	private double length = 0;
	private double area = 0;

	private double size = 1;

	public class InvalidAngleException extends Exception{
		public double validLowerBound;
		public double validUperBound;

		public InvalidAngleException(double sValidLowerBound, double sValidUperBound){
			validLowerBound = sValidLowerBound;
			validUperBound = sValidUperBound;
		}
	}

	private Vector<Coordinate> vertices;
	/** @since 1.11 */
	private Vector<GPSCoordinate> gpsCoordinates;

	private Coordinate origo;

	public Polygon(){
		vertices = new Vector<Coordinate>();
		gpsCoordinates = new Vector<GPSCoordinate>();

		origo = new Coordinate(0, 0);
	}

	/* Need to take care of copying of MultiReferenceableValues*/
	public Polygon(Polygon figure){
		vertices = new Vector<Coordinate>();
		gpsCoordinates = new Vector<GPSCoordinate>();

		for(int n = 0; n < figure.getNumVertices(); n++){
			vertices.add(new Coordinate(figure.vertices.elementAt(n).x, figure.vertices.elementAt(n).y));
			gpsCoordinates.add(new GPSCoordinate(figure.gpsCoordinates.elementAt(n).latitude, figure.gpsCoordinates.elementAt(n).longitude, figure.gpsCoordinates.elementAt(n).altitude));
		}
	}

	public Polygon(Vector<Coordinate> sVertices, Vector<GPSCoordinate> sGPSCoordinates){
		vertices = sVertices;
		gpsCoordinates = sGPSCoordinates;
	}

	public void addVertex(Coordinate vertexCoordinate, GPSCoordinate gpsCoordinate){
		vertices.add(vertexCoordinate);
		gpsCoordinates.add(gpsCoordinate);

		adjustSize(vertexCoordinate);
	}

	public void removeVertex(int vertex_num){
		vertices.removeElementAt(vertex_num);
		gpsCoordinates.removeElementAt(vertex_num);
	}

	public int getNumVertices(){
		return vertices.size();
	}

	public double getVertexCoordinateX(int vertex){
		return vertices.elementAt(vertex).x;
	}

	public double getVertexCoordinateY(int vertex){
		return vertices.elementAt(vertex).y;
	}

	public double getVertexLatitude(int vertex){
		if(gpsCoordinates == null || gpsCoordinates.size() != vertices.size()){
			//Figures measured with version earlier than 1.11 does not have
			//any gpsCoordinates.
			return 0;
		}
		else
			return gpsCoordinates.elementAt(vertex).latitude;
	}

	public double getVertexLongitude(int vertex){
		if(gpsCoordinates == null || gpsCoordinates.size() != vertices.size()){
			//Figures measured with version earlier than 1.11 does not have
			//any gpsCoordinates.
			return 0;
		}
		else
			return gpsCoordinates.elementAt(vertex).longitude;
	}

	public double getVertexAltitude(int vertex){
		if(gpsCoordinates == null || gpsCoordinates.size() != vertices.size()){
			//Figures measured with version earlier than 1.11 does not have
			//any gpsCoordinates.
			return 0;
		}
		else
			return gpsCoordinates.elementAt(vertex).altitude;
	}

	public double getScaleFactor(){
		double minX = 0;
		double minY = 0;
		double maxX = 0;
		double maxY = 0;
		for(int n = 0; n < getNumVertices(); n++){
			if(minX > vertices.elementAt(n).x)
				minX = vertices.elementAt(n).x;
			if(minY > vertices.elementAt(n).y)
				minY = vertices.elementAt(n).y;
			if(maxX < vertices.elementAt(n).x)
				maxX = vertices.elementAt(n).x;
			if(maxY < vertices.elementAt(n).y)
				maxY = vertices.elementAt(n).y;
		}

		return Math.sqrt((maxX - minX)*(maxX - minX) + (maxY - minY)*(maxY - minY));
	}

	double minX = 0;
	double minY = 0;
	double maxX = 0;
	double maxY = 0;
	private void adjustSize(Coordinate c){
		if(c.x < minX)
			minX = c.x;
		else if(c.x > maxX)
			maxX = c.x;

		if(c.y < minY)
			minY = c.y;
		else if(c.y > maxY)
			maxY = c.y;

		size = Math.max(maxX - minX, maxY - minY);
	}

	public double getSize(){
		return size;
	}

	public Polygon getScaledFigure(){
		if(getNumVertices() > 1){
			Polygon nFigure = new Polygon();

			double scale = getScaleFactor();

			for(int n = 0; n < getNumVertices(); n++){
				nFigure.addVertex(new Coordinate(vertices.elementAt(n).x/scale, vertices.elementAt(n).y/scale),
									new GPSCoordinate(gpsCoordinates.elementAt(n).latitude, gpsCoordinates.elementAt(n).longitude, gpsCoordinates.elementAt(n).altitude));
			}

			return nFigure;
		}
		else{
			return this;
		}
	}

	private int lastPointUsedForMeasure = 0;
	public void updateMeasure(){
		if(lastPointUsedForMeasure != getNumVertices()){
			if(getNumVertices() > 1){
				double vector1X;
				double vector1Y;
				double vector2X;
				double vector2Y;

				vector1X = vertices.elementAt(lastPointUsedForMeasure).x - vertices.elementAt(0).x;
				vector1Y = vertices.elementAt(lastPointUsedForMeasure).y - vertices.elementAt(0).y;

				while(lastPointUsedForMeasure < getNumVertices() - 1){
					vector2X = vertices.elementAt(lastPointUsedForMeasure + 1).x - vertices.elementAt(0).x;
					vector2Y = vertices.elementAt(lastPointUsedForMeasure + 1).y - vertices.elementAt(0).y;

					length += Math.sqrt((vector2X - vector1X)*(vector2X - vector1X) + (vector2Y - vector1Y)*(vector2Y - vector1Y));
					if(lastPointUsedForMeasure != 0)
						area += (vector1X*vector2Y - vector1Y*vector2X)/(double)2;

					vector1X = vector2X;
					vector1Y = vector2Y;

					lastPointUsedForMeasure++;
				}
			}
		}
	}

	public double getLength(){
		updateMeasure();
		return length;
	}

	public double getArea(){
		updateMeasure();
		return Math.abs(area);
	}

	public void setOrigo(Coordinate origo){
		this.origo = new Coordinate(origo.x, origo.y);
	}

	public Coordinate getOrigo(){
		return origo;
	}
}
