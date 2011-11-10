package com.dafer45.dam;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.content.Context;
import android.widget.Toast;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.dafer45.dam.MeasureView;
//import measureapp.measureapp.FigureStorageHandler;
import com.dafer45.dam.geometry.Polygon;
/*import com.dafer45.mutualsupport.MutualSupportView;
import com.dafer45.distanceandareameasurement.filehandler.FileHandler;*/

public class DAM2 extends Activity {
	MeasureView measureView;
	Button startStopButton;
	Button logButton;
	Context c;

	Dialog dialog;
/*	FigureStorageHandler figureStorageHandler;
	FileHandler fileHandler;*/

	private OnItemSelectedListener dropdown_length_units_listener = new OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
			switch(position){
			case 0:
				measureView.setLengthUnits(MeasureView.LENGTH_UNITS_AUTO_METRIC);
				break;
			case 1:
				measureView.setLengthUnits(MeasureView.LENGTH_UNITS_AUTO_IMPERIAL);
				break;
			case 2:
				measureView.setLengthUnits(MeasureView.LENGTH_UNITS_METER);
				break;
			case 3:
				measureView.setLengthUnits(MeasureView.LENGTH_UNITS_KILOMETER);
				break;
			case 4:
				measureView.setLengthUnits(MeasureView.LENGTH_UNITS_FOOT);
				break;
			case 5:
				measureView.setLengthUnits(MeasureView.LENGTH_UNITS_YARD);
				break;
			case 6:
				measureView.setLengthUnits(MeasureView.LENGTH_UNITS_CHAIN);
				break;
			case 7:
				measureView.setLengthUnits(MeasureView.LENGTH_UNITS_MILE);
				break;
			}
		}

		public void onNothingSelected(AdapterView<?> parent){
		}
	};

	private OnItemSelectedListener dropdown_area_units_listener = new OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
			switch(position){
			case 0:
				measureView.setAreaUnits(MeasureView.AREA_UNITS_AUTO_METRIC);
				break;
			case 1:
				measureView.setAreaUnits(MeasureView.AREA_UNITS_AUTO_IMPERIAL);
				break;
			case 2:
				measureView.setAreaUnits(MeasureView.AREA_UNITS_SQUARE_METER);
				break;
			case 3:
				measureView.setAreaUnits(MeasureView.AREA_UNITS_HECTARE);
				break;
			case 4:
				measureView.setAreaUnits(MeasureView.AREA_UNITS_SQUARE_KILOMETER);
				break;
			case 5:
				measureView.setAreaUnits(MeasureView.AREA_UNITS_SQUARE_FOOT);
				break;
			case 6:
				measureView.setAreaUnits(MeasureView.AREA_UNITS_SQUARE_YARD);
				break;
			case 7:
				measureView.setAreaUnits(MeasureView.AREA_UNITS_ACRE);
				break;
			case 8:
				measureView.setAreaUnits(MeasureView.AREA_UNITS_SQUARE_MILE);
				break;
			}
		}

		public void onNothingSelected(AdapterView<?> parent){
		}
	};

	private OnItemSelectedListener dropdown_logging_listener = new OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
			switch(position){
			case 0:
				measureView.setLoggingMode(MeasureView.LOGGING_MODE_AUTO);
				if(measureView.getMode() == MeasureView.MODE_MEASURE)
					startStopButton.setEnabled(true);
				logButton.setEnabled(false);
				break;
			case 1:
				measureView.setLoggingMode(MeasureView.LOGGING_MODE_MANUAL);
				startStopButton.setEnabled(false);
				if(measureView.getMode() == MeasureView.MODE_MEASURE)
					logButton.setEnabled(true);
				break;
			}
		}

		public void onNothingSelected(AdapterView<?> parent){
		}
	};

	private OnClickListener reset_button_listener = new OnClickListener(){
		@Override
		public void onClick(View v){
			measureView.reset();
		}
	};

	private OnClickListener start_button_listener = new OnClickListener(){
		@Override
		public void onClick(View v){
			if(measureView.isReadyToStart()){
				measureView.start();
				startStopButton.setText(c.getString(R.string.stop));
				startStopButton.postInvalidate();
			}
			else if(measureView.isRunning()){
				measureView.stop();
				startStopButton.setText(c.getString(R.string.start));
				startStopButton.postInvalidate();
			}
		}
	};

	private OnClickListener log_button_listener = new OnClickListener(){
		@Override
		public void onClick(View v){
			measureView.importFromGPS();
			measureView.postInvalidate();
		}
	};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        MutualSupportView.setNamespace("http://schemas.android.com/apk/res/measureapp.measureapp");
       	setContentView(R.layout.main);

       	c = this;

       	measureView = (MeasureView)findViewById(R.id.measureView);

       	Spinner lengthUnitsSpinner = (Spinner)findViewById(R.id.lengthUnitsSpinner);
       	ArrayAdapter lengthAdapter = ArrayAdapter.createFromResource(this, R.array.lengthUnits,
													android.R.layout.simple_spinner_item);
       	lengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       	lengthUnitsSpinner.setAdapter(lengthAdapter);
       	lengthUnitsSpinner.setOnItemSelectedListener(dropdown_length_units_listener);

       	Spinner areaUnitsSpinner = (Spinner)findViewById(R.id.areaUnitsSpinner);
       	ArrayAdapter areaAdapter = ArrayAdapter.createFromResource(this, R.array.areaUnits,
													android.R.layout.simple_spinner_item);
       	areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       	areaUnitsSpinner.setAdapter(areaAdapter);
       	areaUnitsSpinner.setOnItemSelectedListener(dropdown_area_units_listener);

       	Spinner loggingSpinner = (Spinner)findViewById(R.id.loggingSpinner);
       	ArrayAdapter loggingAdapter = ArrayAdapter.createFromResource(this, R.array.logging,
													android.R.layout.simple_spinner_item);
       	loggingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       	loggingSpinner.setAdapter(loggingAdapter);
       	loggingSpinner.setOnItemSelectedListener(dropdown_logging_listener);

       	Button reset_button = (Button)findViewById(R.id.resetButton);
       	reset_button.setOnClickListener(reset_button_listener);

       	logButton = (Button)findViewById(R.id.logButton);
       	logButton.setOnClickListener(log_button_listener);

       	startStopButton = (Button)findViewById(R.id.startButton);
       	startStopButton.setOnClickListener(start_button_listener);

       	dialog = new Dialog(this);
       	dialog.setOwnerActivity(this);

/*       	figureStorageHandler = new FigureStorageHandler(this, measureView, dialog);
       	fileHandler = new FileHandler(this, dialog);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main, menu);
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case R.id.measure:
    	{
    		if(measureView.getLoggingMode() == MeasureView.LOGGING_MODE_AUTO){
    			Button startButton = (Button)findViewById(R.id.startButton);
    			startButton.setEnabled(true);
    		}
    		else{
    			Button logButton = (Button)findViewById(R.id.logButton);
    			logButton.setEnabled(true);
    		}
    		Button resetButton = (Button)findViewById(R.id.resetButton);
    		resetButton.setEnabled(true);
    		measureView.setPolygon(new Polygon());
    		measureView.setMode(MeasureView.MODE_MEASURE);
    		measureView.reset();
    		return true;
    	}
    	case R.id.save:
/*    		if(measureView.getMode() == MeasureView.MODE_MEASURE){
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setMessage(c.getString(R.string.will_end_measuring_session))
    				.setCancelable(false)
    				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						dialog.cancel();
    			    		Button startButton = (Button)findViewById(R.id.startButton);
    			    		startButton.setEnabled(false);
    			    		Button resetButton = (Button)findViewById(R.id.resetButton);
    			    		resetButton.setEnabled(false);
    			    		Button logButton = (Button)findViewById(R.id.logButton);
    			    		logButton.setEnabled(false);
    			    		measureView.stop();
    						startButton.setText(c.getString(R.string.start));
    						measureView.setMode(MeasureView.MODE_VIEW);
    						figureStorageHandler.launch(FigureStorageHandler.MODE_SAVE);
    					}
    				})
    				.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						dialog.cancel();
    					}
    				});
    			AlertDialog alert = builder.create();
    			alert.show();
    		}
    		else{
	    		Button startButton = (Button)findViewById(R.id.startButton);
	    		startButton.setEnabled(false);
				figureStorageHandler.launch(FigureStorageHandler.MODE_SAVE);
	    		Button resetButton = (Button)findViewById(R.id.resetButton);
	    		resetButton.setEnabled(false);
	    		Button logButton = (Button)findViewById(R.id.logButton);
	    		logButton.setEnabled(false);
    		}*/
    		return true;
    	case R.id.view:
/*    		if(measureView.getMode() == MeasureView.MODE_MEASURE){
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setMessage(c.getString(R.string.will_end_measuring_session))
    				.setCancelable(false)
    				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						dialog.cancel();
    			    		Button startButton = (Button)findViewById(R.id.startButton);
    			    		startButton.setEnabled(false);
    			    		Button resetButton = (Button)findViewById(R.id.resetButton);
    			    		resetButton.setEnabled(false);
    			    		Button logButton = (Button)findViewById(R.id.logButton);
    			    		logButton.setEnabled(false);
    			    		measureView.stop();
    						startButton.setText(c.getString(R.string.start));
    						measureView.setMode(MeasureView.MODE_VIEW);
    						figureStorageHandler.launch(FigureStorageHandler.MODE_LOAD);
    					}
    				})
    				.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						dialog.cancel();
    					}
    				});
    			AlertDialog alert = builder.create();
    			alert.show();
    		}
    		else{
	    		Button startButton = (Button)findViewById(R.id.startButton);
	    		startButton.setEnabled(false);
	    		Button resetButton = (Button)findViewById(R.id.resetButton);
	    		resetButton.setEnabled(false);
				figureStorageHandler.launch(FigureStorageHandler.MODE_LOAD);
	    		Button logButton = (Button)findViewById(R.id.logButton);
	    		logButton.setEnabled(false);
    		}*/
    		return true;
    	case R.id.delete:
/*    		if(measureView.getMode() == MeasureView.MODE_MEASURE){
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setMessage(c.getString(R.string.will_end_measuring_session))
    				.setCancelable(false)
    				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						dialog.cancel();
    			    		Button startButton = (Button)findViewById(R.id.startButton);
    			    		startButton.setEnabled(false);
    			    		Button resetButton = (Button)findViewById(R.id.resetButton);
    			    		resetButton.setEnabled(false);
    			    		Button logButton = (Button)findViewById(R.id.logButton);
    			    		logButton.setEnabled(false);
    			    		measureView.stop();
    						startButton.setText(c.getString(R.string.start));
    						measureView.setMode(MeasureView.MODE_VIEW);
    						figureStorageHandler.launch(FigureStorageHandler.MODE_DELETE);
    					}
    				})
    				.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						dialog.cancel();
    					}
    				});
    			AlertDialog alert = builder.create();
    			alert.show();
    		}
    		else{
	    		Button startButton = (Button)findViewById(R.id.startButton);
	    		startButton.setEnabled(false);
	    		Button resetButton = (Button)findViewById(R.id.resetButton);
	    		resetButton.setEnabled(false);
				figureStorageHandler.launch(FigureStorageHandler.MODE_DELETE);
	    		Button logButton = (Button)findViewById(R.id.logButton);
	    		logButton.setEnabled(false);
    		}*/
    		return true;
    	case R.id.help:
    	{
    		dialog.setContentView(R.layout.help);
    		dialog.setTitle("Help");
    		dialog.setCancelable(true);

    		dialog.show();
    		return true;
    	}
    	case R.id.estimateError:
    	{
    		dialog.setContentView(R.layout.error_estimate);
    		dialog.setTitle(getString(R.string.estimated_error));
    		dialog.setCancelable(true);

    		TextView errorEstimate1m = (TextView)dialog.findViewById(R.id.errorEstimate1m);
    		TextView errorEstimate10m = (TextView)dialog.findViewById(R.id.errorEstimate10m);

    		errorEstimate1m.setText(measureView.estimatedAreaError(1));
    		errorEstimate10m.setText(measureView.estimatedAreaError(10));

    		dialog.show();
    		return true;
    	}
    	case R.id.import_file:
/*    		fileHandler.importDAMEF(new FileHandler.OnImportCallback(){
    			public void onImport(Figure figure){
    				measureView.setMode(MeasureView.MODE_VIEW);
    				measureView.setFigure(figure);
    	    		dialog.cancel();
    			}
    		});*/
    		return true;
    	case R.id.export:
//    		fileHandler.export(measureView.getFigure());
    		return true;
   		default:
   				return super.onOptionsItemSelected(item);
    	}
    }

    @Override
    public void onDestroy(){
//    	figureStorageHandler.destroy();
    	measureView.destroy();
    	super.onDestroy();
    }
}
