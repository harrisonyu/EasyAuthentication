package com.example.easyauthentication;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;

//TODO: use: size, graph velocity, time, gestureslibrary, accelerometer, gyroscope
public class MainActivity extends Activity implements SensorEventListener{
	
	final int NUM_CAL_FILES = 5;

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
	
	private float trainedMeanAx;
	private float trainedMeanAy;
	private float trainedMeanAz;
	private float trainedMeanGyrox;
	private float trainedMeanGyroy;
	private float trainedMeanGyroz;
	private float trainedMeanSize;
	
	private float trainedVarAx;
	private float trainedVarAy;
	private float trainedVarAz;
	private float trainedVarGyrox;
	private float trainedVarGyroy;
	private float trainedVarGyroz;
	private float trainedVarSize;
	
	private ArrayList< ArrayList<Float> > curInstance = null;
	
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
        long timestamp = event.getEventTime();

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                if(mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(event);
                calibrationDisplay.setText("Touch Number: " + ++touchNum);
               // context = getApplicationContext();
				if (touchNum < 6) {
					File filedir = context.getExternalFilesDir(null);
					String filename = "calibration" + touchNum + ".csv";
					file = new File(filedir, filename);
					try {
						writer = new CSVWriter(new FileWriter(file), ',', CSVWriter.NO_QUOTE_CHARACTER);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (touchNum == 6) {
					try {
						trainGaussianNaiveBayes();
						System.out.println(trainedMeanAx + " " + trainedMeanAy + " " + trainedMeanAz + " " +
							trainedMeanGyrox + " " + trainedMeanGyroy + " " + trainedMeanGyroz + " " + trainedMeanSize +
							" " + trainedVarAx + " " + trainedVarAy + " " + trainedVarAz + " " + trainedVarGyrox + " " +
							trainedVarGyroy + " " + trainedVarGyroz + " " + trainedVarSize);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
            	} else {
					curInstance = new ArrayList< ArrayList<Float> >();
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
            	if (touchNum > 6) {
            		boolean isValid = testGestureWithGNB();
            	}
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
	        
			if (touchNum < 7) {
	            String[] entry = new String[1];
	            entry[0] = timestamp + "," + Accel_x + ","
	            		+ Accel_y + "," + Accel_z + ","
	            		+ Gyro_x + "," + Gyro_y + ","
	            		+ Gyro_z + "," + Velo_x + "," + Velo_y + "," + Size;
	            writer.writeNext(entry);
			} else {
				ArrayList<Float> curSample = new ArrayList<Float>();
				curSample.add(timestamp);
				curSample.add(Accel_x);
				curSample.add(Accel_y);
				curSample.add(Accel_z);
				curSample.add(Gyro_x);
				curSample.add(Gyro_y);
				curSample.add(Gyro_z);
				curSample.add(Velo_x);
				curSample.add(Velo_y);
				curSample.add(Size);
				curInstance.add(curSample);
			}
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
    
    //TODO: learn how to generate reliable negative examples from only positive examples
    private void generateNeg() throws IOException {
    	File fileDir = getFilesDir();
    	for (int i = 2; i <= NUM_CAL_FILES; i++) {
    		String curFile = "calibration" + i + ".csv";
    		File cur = new File(fileDir, curFile);
    		BufferedReader br = new BufferedReader(new FileReader(cur));
    		String line;
    		while ((line = br.readLine()) != null) {
    			String[] tokens = line.split(",");
    			
    		}
    	}
    }

	private double probDensity(float sampleMean, float mean, float var) {
		double sqrtTwoPi = Math.sqrt(Math.PI * 2);
		double stdDev = Math.sqrt(var);
		double coEff = 1.0/(sqrtTwoPi * stdDev);
		double innerExp = -(Math.pow(sampleMean - var, 2)/(2 * var));

		return coEff * Math.exp(innerExp);
	}

	private boolean testGestureWithGNB() {
		//ArrayList<Float>[] instance = curInstance.toArray();
		Iterator< ArrayList<Float> > i = curInstance.iterator();
		float meanAx = 0;
		float meanAy = 0;
		float meanAz = 0;
		float meanGyrox = 0;
		float meanGyroy = 0;
		float meanGyroz = 0;
		float meanSize = 0;
		float numPoints = 0;
		while (i.hasNext()) {
			int numIdx = 0;
			numPoints++;
			Iterator<Float> j = i.next().iterator();
			while (j.hasNext()) {
				if (numIdx == 1) {
					meanAx += j.next();
				} else if (numIdx == 2) {
					meanAy += j.next();
				} else if (numIdx == 3) {
					meanAz += j.next();
				} else if (numIdx == 4) {
					meanGyrox += j.next();
				} else if (numIdx == 5) {
					meanGyroy += j.next();
				} else if (numIdx == 6) {
					meanGyroz += j.next();
				} else if (numIdx == 9) {
					meanSize += j.next();
				} else {
					j.next();
				}
				numIdx++;
			}
		}
		meanAx /= numPoints;
		meanAy /= numPoints;
		meanAz /= numPoints;
		meanGyrox /= numPoints;
		meanGyroy /= numPoints;
		meanGyroz /= numPoints;
		meanSize /= numPoints;
		
		double curProb = 0;
		curProb += probDensity(meanAx, trainedMeanAx, trainedVarAx);
		curProb += probDensity(meanAy, trainedMeanAy, trainedVarAy);
		curProb += probDensity(meanAz, trainedMeanAz, trainedVarAz);
		curProb += probDensity(meanGyrox, trainedMeanGyrox, trainedVarGyrox);
		curProb += probDensity(meanGyroy, trainedMeanGyroy, trainedVarGyroy);
		curProb += probDensity(meanGyroz, trainedMeanGyroz, trainedVarGyroz);
		curProb += probDensity(meanSize, trainedMeanSize, trainedVarSize);
		System.out.println("curProb : " + curProb);

		return false;
	}
    
    private void trainGaussianNaiveBayes() throws IOException {
    	File fileDir = context.getExternalFilesDir(null);
    	float meanAx = 0;
    	float meanAy = 0;
    	float meanAz = 0;
    	float meanGyrox = 0;
    	float meanGyroy = 0;
    	float meanGyroz = 0;
    	float meanSize = 0;
    	
    	float varAx = 0;
    	float varAy = 0;
    	float varAz = 0;
    	float varGyrox = 0;
    	float varGyroy = 0;
    	float varGyroz = 0;
    	float varSize = 0; 

    	float numExamples = 0;
		float numFiles = 0;
    	for (int i = 2; i <= NUM_CAL_FILES; i++) {
			numFiles ++;
    		String curFile = "calibration" + i + ".csv";
    		File cur = new File(fileDir, curFile);
    		BufferedReader br = new BufferedReader(new FileReader(cur));
    		String line;
    		while ((line = br.readLine()) != null) {
    			String[] tokens = line.split(",");
    			meanAx += Float.parseFloat(tokens[1]);
    			meanAy += Float.parseFloat(tokens[2]);
    			meanAz += Float.parseFloat(tokens[3]);
    			meanGyrox += Float.parseFloat(tokens[4]);
    			meanGyroy += Float.parseFloat(tokens[5]);
    			meanGyroz += Float.parseFloat(tokens[6]);
    			meanSize += Float.parseFloat(tokens[9]);
    			numExamples++;
    		}
    		br.close();
    	}
		meanAx /= numExamples;
		meanAy /= numExamples;
		meanAz /= numExamples;
		meanGyrox /= numExamples;
		meanGyroy /= numExamples;
		meanGyroz /= numExamples;
		meanSize /= numExamples;
    	for (int i = 2; i <= NUM_CAL_FILES; i++) {
			float curMeanAx = 0;
			float curMeanAy = 0;
			float curMeanAz = 0;
			float curMeanGyrox = 0;
			float curMeanGyroy = 0;
			float curMeanGyroz = 0;
			float curMeanSize = 0;
			float numPoints = 0;
    		String curFile = "calibration" + i + ".csv";
    		File cur = new File(fileDir, curFile);
    		BufferedReader br = new BufferedReader(new FileReader(cur));
    		String line;
    		while ((line = br.readLine()) != null) {
    			String[] tokens = line.split(",");
    			curMeanAx += Float.parseFloat(tokens[1]);
    			curMeanAy += Float.parseFloat(tokens[2]);
    			curMeanAz += Float.parseFloat(tokens[3]);
    			curMeanGyrox += Float.parseFloat(tokens[4]);
    			curMeanGyroy += Float.parseFloat(tokens[5]);
    			curMeanGyroz += Float.parseFloat(tokens[6]);
    			curMeanSize += Float.parseFloat(tokens[9]);
    			numPoints++;
    		}
			curMeanAx /= numPoints;
			curMeanAy /= numPoints;
			curMeanAz /= numPoints;
			curMeanGyrox /= numPoints;
			curMeanGyroy /= numPoints;
			curMeanGyroz /= numPoints;
			curMeanSize /= numPoints;
			varAx += Math.pow(meanAx - curMeanAx, 2);
			varAy += Math.pow(meanAy - curMeanAy, 2);
			varAz += Math.pow(meanAz - curMeanAz, 2);
			varGyrox += Math.pow(meanGyrox - curMeanGyrox, 2);
			varGyroy += Math.pow(meanGyroy - curMeanGyroy, 2);
			varGyroz += Math.pow(meanGyroz - curMeanGyroz, 2);
			varSize += Math.pow(meanSize - curMeanSize, 2);
		}
		varAx /= numFiles;
		varAy /= numFiles;
		varAz /= numFiles;
		varGyrox /= numFiles;
		varGyroy /= numFiles;
		varGyroz /= numFiles;
		varSize /= numFiles;

		trainedMeanAx = meanAx;
		trainedMeanAy = meanAy;
		trainedMeanAz = meanAz;
		trainedMeanGyrox = meanGyrox;
		trainedMeanGyroy = meanGyroy;
		trainedMeanGyroz = meanGyroz;
		trainedMeanSize = meanSize;

		trainedVarAx = varAx;
		trainedVarAy = varAy;
		trainedVarAz = varAz;
		trainedVarGyrox = varGyroz;
		trainedVarGyroy = varGyroy;
		trainedVarGyroz = varGyroz;
		trainedVarSize = varSize;
    }
}
