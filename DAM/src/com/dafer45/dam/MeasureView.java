package com.dafer45.dam;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.LocationListener;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import java.util.Vector;
import com.dafer45.dam.geometry.Polygon;
import com.dafer45.dam.geometry.Coordinate;
import com.dafer45.dam.geometry.GPSCoordinate;

public class MeasureView extends View{
	Context c;

	public static final int LENGTH_UNITS_AUTO_METRIC = 0;
	public static final int LENGTH_UNITS_AUTO_IMPERIAL = 1;
	public static final int LENGTH_UNITS_METER = 2;
	public static final int LENGTH_UNITS_KILOMETER = 3;
	public static final int LENGTH_UNITS_FOOT = 4;
	public static final int LENGTH_UNITS_YARD = 5;
	public static final int LENGTH_UNITS_CHAIN = 6;
	public static final int LENGTH_UNITS_MILE = 7;
	private int lengthUnits;
	public static final int AREA_UNITS_AUTO_METRIC = 0;
	public static final int AREA_UNITS_AUTO_IMPERIAL = 1;
	public static final int AREA_UNITS_SQUARE_METER = 2;
	public static final int AREA_UNITS_HECTARE = 3;
	public static final int AREA_UNITS_SQUARE_KILOMETER = 4;
	public static final int AREA_UNITS_SQUARE_FOOT = 5;
	public static final int AREA_UNITS_SQUARE_YARD = 6;
	public static final int AREA_UNITS_ACRE = 7;
	public static final int AREA_UNITS_SQUARE_MILE = 8;
	private int areaUnits;

	public static final double FOOT_IN_METER = 0.3048;
	public static final double YARD_IN_METER = 0.9144;
	public static final double CHAIN_IN_METER = 20.1168;
	public static final double MILE_IN_METER = 1609.344;
	public static final double ACRE_IN_SQUARE_METER = 4840*YARD_IN_METER*YARD_IN_METER;

	public static final int MODE_MEASURE = 0;
	public static final int MODE_VIEW = 1;
	private int mode;

	public static final int LOGGING_MODE_AUTO = 0;
	public static final int LOGGING_MODE_MANUAL = 1;
	private int loggingMode;

	private Polygon polygon;
	Coordinate origo;
	private double rotation;
	private double zoom;

	private Paint backgroundPaint;
	private Paint linePaint;
	private Paint closingLinePaint;
	private Paint textPaint;
	private Paint gpsOnPaint;
	private Paint gpsOffPaint;
	private Paint gpsViewPaint;
	private Paint positionPaint;
	private Paint translatedByPaint;
	private Paint loggingLinePaint;

	LocationManager locationManager;
	GPSLocationListener gps;

	SensorManager sensorManager;
	SensorEventListener sensorEventListener;

	private static double longitude = 0;
	private static double latitude = 0;
	private static double altitude = 0;
	private class GPSLocationListener implements LocationListener{
		private Context context;
		private View view;
		private int currentStatus = LocationProvider.OUT_OF_SERVICE;
		private int numSatellites = 0;

		private boolean hasRecievedSignal;
		private boolean isStarted;

		private static final int radiusEarth = 6378137;

		private double flatXUnitX;
		private double flatXUnitY;
		private double flatXUnitZ;
		private double flatYUnitX;
		private double flatYUnitY;
		private double flatYUnitZ;
		private boolean origoIsFixed = false;

		public Coordinate coordinate;

		public GPSLocationListener(Context sContext, View v){
			context = sContext;
			view = v;

			coordinate = new Coordinate(0, 0);

			origoIsFixed = false;
			hasRecievedSignal = false;
			isStarted = false;
		}

		public void start(){
			isStarted = true;
		}

		public void reset(){
			origoIsFixed = false;
		}

		public boolean isActive(){
			return hasRecievedSignal;
		}

		public void onLocationChanged(Location location){
			hasRecievedSignal = true;

			if(mode == MODE_MEASURE /*&& (isStarted || loggingMode == LOGGING_MODE_MANUAL)*/){
				longitude = location.getLongitude();
				latitude = location.getLatitude();
				altitude = location.getAltitude();

				double x = (radiusEarth + altitude)*Math.cos(latitude*Math.PI/180)*Math.cos(longitude*Math.PI/180);
				double y = (radiusEarth + altitude)*Math.cos(latitude*Math.PI/180)*Math.sin(longitude*Math.PI/180);
				double z = (radiusEarth + altitude)*Math.sin(latitude*Math.PI/180);

				if(!origoIsFixed){
					flatXUnitX = -y/Math.sqrt(x*x + y*y);
					flatXUnitY = x/Math.sqrt(x*x + y*y);
					flatXUnitZ = 0;
					flatYUnitX = (y*flatXUnitZ - z*flatXUnitY);
					flatYUnitY = (z*flatXUnitX - x*flatXUnitZ);
					flatYUnitZ = (x*flatXUnitY - y*flatXUnitX);
					double length = Math.sqrt(flatYUnitX*flatYUnitX + flatYUnitY*flatYUnitY + flatYUnitZ*flatYUnitZ);
					flatYUnitX = flatYUnitX/length;
					flatYUnitY = flatYUnitY/length;
					flatYUnitZ = flatYUnitZ/length;
				}

				double spaceX = (radiusEarth + altitude)*Math.cos(latitude*Math.PI/180)*Math.cos(longitude*Math.PI/180);
				double spaceY = (radiusEarth + altitude)*Math.cos(latitude*Math.PI/180)*Math.sin(longitude*Math.PI/180);
				double spaceZ = (radiusEarth + altitude)*Math.sin(latitude*Math.PI/180);

				coordinate.x = spaceX*flatXUnitX + spaceY*flatXUnitY + spaceZ*flatXUnitZ;
				coordinate.y = spaceX*flatYUnitX + spaceY*flatYUnitY + spaceZ*flatYUnitZ;

				if(loggingMode == LOGGING_MODE_AUTO && isStarted)
					((MeasureView)view).importFromGPS();

				setOrigo(coordinate.x, coordinate.y);

				view.postInvalidate();
			}
		}

		public void onProviderDisabled(String provider){
		}

		public void onProviderEnabled(String provider){
		}

		public void onStatusChanged(String provider, int status, Bundle extras){
			currentStatus = status;
			numSatellites = extras.getInt("satellites");
		}

		public int getStatus(){
			return currentStatus;
		}

		public int getNumSatellites(){
			return numSatellites;
		}

		public void ensureOrigoIsFixed(){
			if(!origoIsFixed){
				polygon.setOrigo(origo);
				origoIsFixed = true;
			}
		}
	};

	private double directionInRadians = 0;
	private class CompassSensorEventListener implements SensorEventListener{
		View view;

		private static final int AVERAGING_INTERVAL = 10;
		private int magneticFieldCounter = 0;
		private Vector<Vector<Float>> magneticFieldValues;

		public CompassSensorEventListener(View v){
			magneticFieldValues = new Vector<Vector<Float>>();
			magneticFieldValues.add(new Vector<Float>());
			magneticFieldValues.add(new Vector<Float>());
			magneticFieldValues.add(new Vector<Float>());
			for(int n = 0; n < AVERAGING_INTERVAL; n++){
				magneticFieldValues.elementAt(0).add(new Float(0));
				magneticFieldValues.elementAt(1).add(new Float(0));
				magneticFieldValues.elementAt(2).add(new Float(0));
			}

			view = v;
		}

		public void onSensorChanged(SensorEvent event){
			magneticFieldValues.elementAt(0).setElementAt(new Float(event.values[0]),magneticFieldCounter);
			magneticFieldValues.elementAt(1).setElementAt(new Float(event.values[1]),magneticFieldCounter);
			magneticFieldValues.elementAt(2).setElementAt(new Float(event.values[2]),magneticFieldCounter);
			if(++magneticFieldCounter == AVERAGING_INTERVAL)
				magneticFieldCounter = 0;
			float mx = 0;
			float my = 0;
			float mz = 0;
			for(int n = 0; n < AVERAGING_INTERVAL; n++){
				mx += magneticFieldValues.elementAt(0).elementAt(n);
				my += magneticFieldValues.elementAt(1).elementAt(n);
				mz += magneticFieldValues.elementAt(2).elementAt(n);
			}

			mx /= AVERAGING_INTERVAL;
			my /= AVERAGING_INTERVAL;
			mz /= AVERAGING_INTERVAL;

			directionInRadians = Math.acos(my/Math.sqrt(mx*mx + my*my));
			if(mx < 0)
				directionInRadians *= -1;

			setRotation(- directionInRadians);
			view.postInvalidate();
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy){
		}
	};

	public MeasureView(Context context, AttributeSet attrs){
		super(context, attrs);

		c = context;

		origo = new Coordinate(0, 0);

		setPolygon(new Polygon());

		backgroundPaint = new Paint();
		backgroundPaint.setColor(0xFFFFFFFF);

		linePaint = new Paint();
		linePaint.setColor(0xFF000000);
		linePaint.setAntiAlias(true);

		closingLinePaint = new Paint();
		closingLinePaint.setColor(0x440000FF);
		closingLinePaint.setAntiAlias(true);

		textPaint = new Paint();
		textPaint.setColor(0xDD0000FF);
		textPaint.setTextSize(20);
		textPaint.setAntiAlias(true);

		gpsOnPaint = new Paint();
		gpsOnPaint.setColor(0xDD00FF00);
		gpsOnPaint.setAntiAlias(true);
		gpsOnPaint.setTextSize(20);

		gpsOffPaint = new Paint();
		gpsOffPaint.setColor(0xDDFF0000);
		gpsOffPaint.setAntiAlias(true);
		gpsOffPaint.setTextSize(20);

		gpsViewPaint = new Paint();
		gpsViewPaint.setColor(0xDD0000FF);
		gpsViewPaint.setAntiAlias(true);
		gpsViewPaint.setTextSize(20);

		positionPaint = new Paint();
		positionPaint.setColor(0x88000000);
		positionPaint.setAntiAlias(true);
		positionPaint.setStyle(Style.STROKE);

		translatedByPaint = new Paint();
		translatedByPaint.setColor(0x44888888);
		translatedByPaint.setAntiAlias(true);

		loggingLinePaint = new Paint();
		loggingLinePaint.setColor(0xFFFF0000);
		loggingLinePaint.setAntiAlias(true);

		zoom = 1;

		init();
	}

	public void setPolygon(Polygon sPolygon){
		polygon = sPolygon;
		if(polygon.getSize() > 0)
			setZoom(1/polygon.getSize());
		else
			setZoom(1);
		origo = polygon.getOrigo();
	}

	public Polygon getPolygon(){
		return polygon;
	}

/*	public void changeFigure(int numVertices){
		figure = new Figure(numVertices);
	}*/

	public float convertRelativeToView(double x){
		double scale = Math.max(getWidth()/2, getHeight()/2);

		return (float)(x*zoom*scale);
	}

	public double convertViewToRelative(float x){
		double scale = Math.min(getWidth()/2, getHeight()/2);

		return x/(zoom*scale);
	}

	protected float calculateInViewPositionX(double x, double y){
		double scale = Math.min(getWidth()/2, getHeight()/2);

		float rotatedX = (float)(Math.cos(rotation)*(x - origo.x) - Math.sin(rotation)*(y - origo.y));
		float rotatedY = (float)(Math.sin(rotation)*(x - origo.x) + Math.cos(rotation)*(y - origo.y));
		return (float)(getWidth()/ 2 + zoom*scale*rotatedX);
	}

	protected float calculateInViewPositionY(double x, double y){
		double scale = Math.min(getWidth()/2, getHeight()/2);

		float rotatedX = (float)(Math.cos(rotation)*(x - origo.x) - Math.sin(rotation)*(y - origo.y));
		float rotatedY = (float)(Math.sin(rotation)*(x - origo.x) + Math.cos(rotation)*(y - origo.y));
		return (float)(getHeight()/ 2 - zoom*scale*rotatedY);
	}

	protected double calculateRelativePositionX(int x, int y){
		double scale = Math.min(getWidth()/2, getHeight()/2);

		double nonBackRotatedX = (x - getWidth()/2)/(scale*zoom);
		double nonBackRotatedY = (y - getHeight()/2)/(scale*zoom);
		return Math.cos(-rotation)*nonBackRotatedX - Math.sin(-rotation)*nonBackRotatedY + origo.x;
	}

	protected double calculateRelativePositionY(int x, int y){
		double scale = Math.min(getWidth()/2, getHeight()/2);

		double nonBackRotatedX = (x - getWidth()/2)/(scale*zoom);
		double nonBackRotatedY = (-y + getHeight()/2)/(scale*zoom);
		return Math.sin(-rotation)*nonBackRotatedX + Math.cos(-rotation)*nonBackRotatedY + origo.y;
	}

	public void moveOrigo(double moveX, double moveY){
		origo.x -= moveX;
		origo.y -= moveY;
		postInvalidate();
	}

	public void setOrigo(double setX, double setY){
		origo.x = setX;
		origo.y = setY;
	}

	public void setZoom(double sZoom){
		zoom = sZoom;
	}

	public double getZoom(){
		return zoom;
	}

	public void setRotation(double sRotation){
		rotation = sRotation;
	}

	public void init(){
		sensorManager = (SensorManager)getContext().getSystemService(Context.SENSOR_SERVICE);
		sensorEventListener = new CompassSensorEventListener(this);
		sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);

		locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
		gps = new GPSLocationListener(getContext(), this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gps);

		mode = MODE_MEASURE;
		setLoggingMode(LOGGING_MODE_AUTO);
	}

	public void destroy(){
		sensorManager.unregisterListener(sensorEventListener);
		locationManager.removeUpdates(gps);
	}

	private int numPointsToPlot = 1000;
	public void increaseResolution(){
		numPointsToPlot *= 10;
	}

	public void decreaseResolution(){
		if(numPointsToPlot > 10)
			numPointsToPlot /= 10;
	}

	@Override
	public void onDraw(Canvas canvas){
		canvas.drawPaint(backgroundPaint);

		Polygon figure = getPolygon();

		//Draw lines and vertice markers
		int plotPointStep = 1;
		int temp = figure.getNumVertices();
		temp /= numPointsToPlot;
		while(temp != 0){
				plotPointStep *= 10;
				temp /= 10;
		}
		for(int n = 0; n < figure.getNumVertices(); n += plotPointStep){
			float thisVertexX = calculateInViewPositionX(figure.getVertexCoordinateX(n), figure.getVertexCoordinateY(n));
			float thisVertexY = calculateInViewPositionY(figure.getVertexCoordinateX(n), figure.getVertexCoordinateY(n));
			float nextVertexX;
			float nextVertexY;

			if(n < figure.getNumVertices() - plotPointStep){
				nextVertexX = calculateInViewPositionX(figure.getVertexCoordinateX(n + plotPointStep), figure.getVertexCoordinateY(n + plotPointStep));
				nextVertexY = calculateInViewPositionY(figure.getVertexCoordinateX(n + plotPointStep), figure.getVertexCoordinateY(n + plotPointStep));
			}
			else{
				nextVertexX = calculateInViewPositionX(figure.getVertexCoordinateX(0), figure.getVertexCoordinateY(0));
				nextVertexY = calculateInViewPositionY(figure.getVertexCoordinateX(0), figure.getVertexCoordinateY(0));
			}
			if(n < figure.getNumVertices() - plotPointStep)
				canvas.drawLine(thisVertexX, thisVertexY, nextVertexX, nextVertexY, linePaint);
			else
				canvas.drawLine(thisVertexX, thisVertexY, nextVertexX, nextVertexY, closingLinePaint);
		}
		if(loggingMode == LOGGING_MODE_MANUAL && figure.getNumVertices() == 1)
			canvas.drawLine(calculateInViewPositionX(figure.getVertexCoordinateX(0), figure.getVertexCoordinateY(0)),
							calculateInViewPositionY(figure.getVertexCoordinateX(0), figure.getVertexCoordinateY(0)),
							calculateInViewPositionX(gps.coordinate.x, gps.coordinate.y), calculateInViewPositionY(gps.coordinate.x, gps.coordinate.y), loggingLinePaint);

		Coordinate coord = gps.coordinate;
		if(mode == MODE_MEASURE){
			canvas.drawCircle((float)(calculateInViewPositionX(coord.x, coord.y)), (float)(calculateInViewPositionY(coord.x, coord.y)), 5, positionPaint);
			canvas.drawCircle((float)(calculateInViewPositionX(coord.x, coord.y)), (float)(calculateInViewPositionY(coord.x, coord.y)), 1, positionPaint);
		}

		canvas.drawText(c.getString(R.string.distance), 10, getHeight() - 35, textPaint);
		canvas.drawText(c.getString(R.string.area), 10, getHeight() - 10, textPaint);
		canvas.drawText("" + Math.round(100*getLength())/(double)100 + getLengthUnit(), /*105*/145, getHeight() - 35, textPaint);
		canvas.drawText("" + Math.round(100*getArea())/(double)100 + getAreaUnit(), /*105*/145, getHeight() - 10, textPaint);

		if(mode == MODE_MEASURE){
			if(gps.isStarted && loggingMode == LOGGING_MODE_AUTO){
				canvas.drawCircle(20, 20, 10, gpsOnPaint);
				canvas.drawText(c.getString(R.string.running), 40, 27, gpsOnPaint);
			}
			else if(gps.isActive()){
//		else if(gps.getStatus() == LocationProvider.AVAILABLE){
				canvas.drawCircle(20, 20, 10, gpsOnPaint);
				canvas.drawText(c.getString(R.string.ready), 40, 27, gpsOnPaint);
			}
//		else if(gps.getStatus() == LocationProvider.TEMPORARILY_UNAVAILABLE){
//			canvas.drawCircle(20, 20, 10, gpsOffPaint);
//			canvas.drawText("Temporarly unavailable", 40, 27, gpsOffPaint);
//		}
			else{
				canvas.drawCircle(20, 20, 10, gpsOffPaint);
				canvas.drawText(c.getString(R.string.awaiting_signal), 40, 27, gpsOffPaint);
			}
		}
		else{
			canvas.drawCircle(20, 20, 10, gpsViewPaint);
			canvas.drawText(c.getString(R.string.view_mode), 40, 27, gpsViewPaint);
		}
		canvas.drawText(c.getString(R.string.translated_by), 20, 47, translatedByPaint);
//		canvas.drawText("" + gps.getNumSatellites(), 20, 50, gpsOffPaint);
//		canvas.drawText("" + zoom, 20, 70, gpsOffPaint);
//		if(getFigure().getNumVertices() > 0)
//			canvas.drawText("" + getFigure().getVertexCoordinateX(0) + " " + getFigure().getVertexCoordinateY(0), 20, 90, gpsOffPaint);

//		canvas.drawText("" + Math.round((180/Math.PI)*directionInRadians) + "°", 10, 20, textPaint);

		super.onDraw(canvas);
	}

	public String getLengthUnit(){
		double length = getPolygon().getLength();
		int scale = 1;
		if(lengthUnits == LENGTH_UNITS_METER){
			return c.getString(R.string.length_unit_meter);
		}
		else if(lengthUnits == LENGTH_UNITS_KILOMETER){
			return c.getString(R.string.length_unit_kilometer);
		}
		else if(lengthUnits == LENGTH_UNITS_AUTO_METRIC){
			if(length > 1000)
				return c.getString(R.string.length_unit_kilometer);
			else
				return c.getString(R.string.length_unit_meter);
		}
		else if(lengthUnits == LENGTH_UNITS_FOOT){
			return c.getString(R.string.length_unit_foot);
		}
		else if(lengthUnits == LENGTH_UNITS_YARD){
			return c.getString(R.string.length_unit_yard);
		}
		else if(lengthUnits == LENGTH_UNITS_CHAIN){
			return c.getString(R.string.length_unit_chain);
		}
		else if(lengthUnits == LENGTH_UNITS_MILE){
			return c.getString(R.string.length_unit_mile);
		}
		else if(lengthUnits == LENGTH_UNITS_AUTO_IMPERIAL){
			if(length > MILE_IN_METER)
				return c.getString(R.string.length_unit_mile);
			else
				return c.getString(R.string.length_unit_yard);
		}

		return "undefined";
	}

	public String getAreaUnit(){
		double area = getPolygon().getArea();
		double scale = 1;
		if(areaUnits == AREA_UNITS_SQUARE_METER){
			return c.getString(R.string.area_unit_square_meter);
		}
		else if(areaUnits == AREA_UNITS_HECTARE){
			return c.getString(R.string.area_unit_hectare);
		}
		else if(areaUnits == AREA_UNITS_SQUARE_KILOMETER){
			return c.getString(R.string.area_unit_square_kilometermeter);
		}
		else if(areaUnits == AREA_UNITS_AUTO_METRIC){
			if(area >= 1000000)
				return c.getString(R.string.area_unit_square_kilometermeter);
			else if(area >= 10000)
				return c.getString(R.string.area_unit_hectare);
			else
				return c.getString(R.string.area_unit_square_meter);
		}
		else if(areaUnits == AREA_UNITS_SQUARE_FOOT){
			return c.getString(R.string.area_unit_square_foot);
		}
		else if(areaUnits == AREA_UNITS_SQUARE_YARD){
			return c.getString(R.string.area_unit_square_yard);
		}
		else if(areaUnits == AREA_UNITS_ACRE){
			return c.getString(R.string.area_unit_acre);
		}
		else if(areaUnits == AREA_UNITS_SQUARE_MILE){
			return c.getString(R.string.area_unit_square_mile);
		}
		else if(areaUnits == AREA_UNITS_AUTO_IMPERIAL){
			if(area > MILE_IN_METER*MILE_IN_METER)
				return c.getString(R.string.area_unit_square_mile);
			else if(area > ACRE_IN_SQUARE_METER)
				return c.getString(R.string.area_unit_acre);
			else
				return c.getString(R.string.area_unit_square_yard);
		}

		return "undefined";
	}

	public double getLength(){
		double length = getPolygon().getLength();
		double scale = 1;
		if(lengthUnits == LENGTH_UNITS_METER){
		}
		else if(lengthUnits == LENGTH_UNITS_KILOMETER){
			scale = 1000;
		}
		else if(lengthUnits == LENGTH_UNITS_AUTO_METRIC){
			if(length > 1000)
				scale = 1000;
		}
		else if(lengthUnits == LENGTH_UNITS_FOOT){
			scale = FOOT_IN_METER;
		}
		else if(lengthUnits == LENGTH_UNITS_YARD){
			scale = YARD_IN_METER;
		}
		else if(lengthUnits == LENGTH_UNITS_CHAIN){
			scale = CHAIN_IN_METER;
		}
		else if(lengthUnits == LENGTH_UNITS_MILE){
			scale = MILE_IN_METER;
		}
		else if(lengthUnits == LENGTH_UNITS_AUTO_IMPERIAL){
			if(length > MILE_IN_METER)
				scale = MILE_IN_METER;
			else
				scale = YARD_IN_METER;
		}

		return length/scale;
	}

	public double getArea(){
		double area = getPolygon().getArea();
		double scale = 1;
		if(areaUnits == AREA_UNITS_SQUARE_METER){
		}
		else if(areaUnits == AREA_UNITS_HECTARE){
			scale = 10000;
		}
		else if(areaUnits == AREA_UNITS_SQUARE_KILOMETER){
			scale = 1000000;
		}
		else if(areaUnits == AREA_UNITS_AUTO_METRIC){
			if(area >= 1000000)
				scale = 1000000;
			else if(area >= 10000)
				scale = 10000;
		}
		else if(areaUnits == AREA_UNITS_SQUARE_FOOT){
			scale = FOOT_IN_METER*FOOT_IN_METER;
		}
		else if(areaUnits == AREA_UNITS_SQUARE_YARD){
			scale = YARD_IN_METER*YARD_IN_METER;
		}
		else if(areaUnits == AREA_UNITS_ACRE){
			scale = ACRE_IN_SQUARE_METER;
		}
		else if(areaUnits == AREA_UNITS_SQUARE_MILE){
			scale = MILE_IN_METER*MILE_IN_METER;
		}
		else if(areaUnits == AREA_UNITS_AUTO_IMPERIAL){
			if(area > MILE_IN_METER*MILE_IN_METER)
				scale = MILE_IN_METER*MILE_IN_METER;
			else if(area > ACRE_IN_SQUARE_METER)
				scale = ACRE_IN_SQUARE_METER;
			else
				scale = YARD_IN_METER*YARD_IN_METER;
		}

		return area/scale;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
		return super.onTouchEvent(event);
	}

	public void start(){
//		reset();
		gps.start();
	}

	public void reset(){
		setPolygon(new Polygon());

		gps.reset();
	}

	public void stop(){
		gps.isStarted = false;
	}

	public boolean isReadyToStart(){
		if(gps.isActive() && !gps.isStarted)
			return true;
		else
			return false;
	}

	public boolean isRunning(){
		if(gps.isStarted)
			return true;
		else
			return false;
	}

	public void importFromGPS(){
		gps.ensureOrigoIsFixed();
		getPolygon().addVertex(new Coordinate(gps.coordinate.x, gps.coordinate.y),
								new GPSCoordinate(latitude, longitude, altitude));
		if(getPolygon().getSize() > 0)
			setZoom(1/getPolygon().getSize());
		else
			setZoom(1);
	}

	public void setLengthUnits(int sUnits){
		lengthUnits = sUnits;
	}

	public void setAreaUnits(int sUnits){
		areaUnits = sUnits;
	}

	public void setMode(int mode){
		this.mode = mode;
	}

	public int getMode(){
		return mode;
	}

	public void setLoggingMode(int loggingMode){
		this.loggingMode = loggingMode;
	}

	public int getLoggingMode(){
		return loggingMode;
	}

	public String estimatedAreaError(int estimatedGPSAccuracy){
		if(getPolygon().getNumVertices() > 0){
			double length = getPolygon().getLength();
			double area = getPolygon().getArea();

			double estimatedAreaError = length*estimatedGPSAccuracy;

			double relativeError = estimatedAreaError/area;

			return "" + ((int)(relativeError*getArea()*100))/(float)100 + getAreaUnit() + "\n" + (int)(relativeError*100) + "%";
		}
		else{
			return "0" + getAreaUnit() + "\n0%";
		}
	}
}
