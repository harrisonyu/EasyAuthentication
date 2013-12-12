package com.example.easyauthentication;

import java.io.File;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;

//TODO: use: size, graph velocity, time, gestureslibrary, accelerometer, gyroscope
public class MainActivity extends Activity implements SensorEventListener{
	

	private float Accel_x;
	private float Accel_y;
	private float Accel_z;
	private float Gyro_x;
	private float Gyro_y;
	private float Gyro_z;
	private float Velo_x;
	private float Velo_y;
	private float Size;
	private static Context context;


	private SensorManager senMan;
	private Sensor accel;
	private Sensor gyro;

	private static boolean running = false;
	
	private static final String DEBUG_TAG = "Velocity";

	private TextView velocityDisplay;
	private TextView calibrationDisplay;
	private int touchNum;
	
	private VelocityTracker mVelocityTracker = null;

	File file;
	CSVWriter writer;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);     
        velocityDisplay = (TextView)findViewById(R.id.velocity);
        calibrationDisplay = (TextView)findViewById(R.id.calibrate);
        touchNum = 0;
    	Accel_x = 0;
    	Accel_y = 0;
    	Accel_z = 0;
    	Gyro_x = 0;
    	Gyro_y = 0;
    	Gyro_z = 0;
    	senMan = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    	accel = senMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	gyro = senMan.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        context = getApplicationContext();
        /*
    	File filedir = context.getExternalFilesDir(null);
    	String filename = "calibration" + touchNum + ".csv";
        file = new File(filedir, filename);
    	try {
    		writer = new CSVWriter(new FileWriter(file), ',');
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                if(mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                }
                else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(event);
                calibrationDisplay.setText("Touch Number: " + touchNum++);
                context = getApplicationContext();
            	File filedir = context.getExternalFilesDir(null);
            	String filename = "calibration" + touchNum + ".csv";
                file = new File(filedir, filename);
            	try {
            		writer = new CSVWriter(new FileWriter(file), ',', CSVWriter.NO_QUOTE_CHARACTER);
            	} catch (IOException e) {
            		e.printStackTrace();
            	}
                running = true;             
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                // When you want to determine the velocity, call 
                // computeCurrentVelocity(). Then call getXVelocity() 
                // and getYVelocity() to retrieve the velocity for each pointer ID. 
                mVelocityTracker.computeCurrentVelocity(1000);
                // Log velocity of pixels per second
                // Best practice to use VelocityTrackerCompat where possible.
                Log.d("", "X velocity: " + 
                        VelocityTrackerCompat.getXVelocity(mVelocityTracker, 
                        pointerId));	
                Log.d("", "Y velocity: " + 
                        VelocityTrackerCompat.getYVelocity(mVelocityTracker,
                        pointerId));
        		velocityDisplay.setText("X velocity: " + VelocityTrackerCompat.getXVelocity(mVelocityTracker, 
                        pointerId) + " Y velocity: " + VelocityTrackerCompat.getYVelocity(mVelocityTracker,
                                pointerId));
        		Velo_x = VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId);
        		Velo_y =  VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);
        		Size = event.getSize(pointerId);
                break;
            case MotionEvent.ACTION_UP:
            	running = false;
            	try {
            		writer.close();
            	} catch (IOException e) {
            		e.printStackTrace();
            	}
            case MotionEvent.ACTION_CANCEL:
                // Return a VelocityTracker object back to be re-used by others.
                mVelocityTracker.recycle();
                break;
        }
        return true;
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    
    @Override
    public void onSensorChanged(SensorEvent event)
    {
    	if(running == true)
    	{
	    	float timestamp = event.timestamp * 1000000; // milliseconds
	    	float[] values = event.values;
	        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
	        	Accel_x = values[0];
	        	Accel_y = values[1];
	        	Accel_z = values[2];
	        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
	        	Gyro_x = values[0];
	        	Gyro_y = values[1];
	        	Gyro_z = values[2];
	        }
	            String[] entry = new String[1];
	            entry[0] = timestamp + "," + Accel_x + ","
	            		+ Accel_y + "," + Accel_z + ","
	            		+ Gyro_x + "," + Gyro_y + ","
	            		+ Gyro_z + "," + Velo_x + "," + Velo_y + "," + Size;
	            System.out.println(entry[0]);
	            writer.writeNext(entry);
    	}
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        senMan.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        senMan.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
    }
 
    @Override
    protected void onPause() {
        super.onPause(); 
        senMan.unregisterListener(this);
    }
   
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	try {
    		writer.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}
