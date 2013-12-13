package com.example.easyauthentication;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

	private TextView authDisplay;
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
	
	private List<ArrayList<Float>> fileOne;
	private List<ArrayList<Float>> fileTwo;
	private List<ArrayList<Float>> fileThree;
	private List<ArrayList<Float>> fileFour;
	
	private List<Float> accel_x;
	private List<Float> accel_y;
	private List<Float> accel_z;
	private List<Float> gyro_x;
	private List<Float> gyro_y;
	private List<Float> gyro_z;
	private List<Float> velocity_x;
	private List<Float> velocity_y;
	private List<Float> fingerSize;

	
	private List< ArrayList<Float> > curInstance = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);     
        authDisplay = (TextView)findViewById(R.id.auth);
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
					System.out.println("CLEAR");
					try {
						DynamicTimeWarpingCalibration();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	} else {
					curInstance = new ArrayList<ArrayList<Float>>();
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
        		Velo_x = VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId);
        		Velo_y =  VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);
        		Size = event.getSize(pointerId);
                break;
            case MotionEvent.ACTION_UP:
            	running = false;
            	if (touchNum > 6) {
            		boolean gnbValid = testGestureWithGNB();
                	boolean dwtValid = DynamicTimeWarpingComparison();
                	if (gnbValid && dwtValid)
                		authDisplay.setText("VALID GESTURE");
                	else
                		authDisplay.setText("INVALID GESTURE");
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

		return curProb > 50;
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
    
    private boolean DynamicTimeWarpingComparison()
    {
		Iterator< ArrayList<Float> > i = curInstance.iterator();
		List<Float> tempAccelX = new ArrayList<Float>();
		List<Float> tempAccelY = new ArrayList<Float>();
		List<Float> tempAccelZ = new ArrayList<Float>();
		List<Float> tempGyroX = new ArrayList<Float>();
		List<Float> tempGyroY = new ArrayList<Float>();
		List<Float> tempGyroZ = new ArrayList<Float>();
		List<Float> tempVelocityX = new ArrayList<Float>();
		List<Float> tempVelocityY = new ArrayList<Float>();
		List<Float> tempFingerSize = new ArrayList<Float>();
		float numPoints = 0;
		while (i.hasNext()) {
			int numIdx = 0;
			numPoints++;
			Iterator<Float> j = i.next().iterator();
			while (j.hasNext()) {
				if (numIdx == 1) {
					tempAccelX.add(j.next());
				} else if (numIdx == 2) {
					tempAccelY.add(j.next());
				} else if (numIdx == 3) {
					tempAccelZ.add(j.next());
				} else if (numIdx == 4) {
					tempGyroX.add(j.next());
				} else if (numIdx == 5) {
					tempGyroY.add(j.next());
				} else if (numIdx == 6) {
					tempGyroZ.add(j.next());
				} else if (numIdx == 7) {
					tempVelocityX.add(j.next());
				} else if (numIdx == 8) {
					tempVelocityY.add(j.next());
				} else if (numIdx == 9) {
					tempFingerSize.add(j.next());
				}
				else
				{
					j.next();
				}
				numIdx++;
			}
		}
    	float currAccelX = DTWDistance(fileOne.get(0), tempAccelX);
    	currAccelX = currAccelX + DTWDistance(fileTwo.get(0), tempAccelX);
    	currAccelX = currAccelX + DTWDistance(fileThree.get(0), tempAccelX);
    	currAccelX = currAccelX + DTWDistance(fileFour.get(0), tempAccelX);
    	currAccelX = currAccelX/4;
    	float currAccelY = DTWDistance(fileOne.get(1), tempAccelY);
    	currAccelY = currAccelY + DTWDistance(fileTwo.get(1), tempAccelY);
    	currAccelY = currAccelY + DTWDistance(fileThree.get(1), tempAccelY);
    	currAccelY = currAccelY + DTWDistance(fileFour.get(1), tempAccelY);
    	currAccelY = currAccelY/4;
    	float currAccelZ = DTWDistance(fileOne.get(2), tempAccelZ);
    	currAccelZ = currAccelZ + DTWDistance(fileTwo.get(2), tempAccelZ);
    	currAccelZ = currAccelZ + DTWDistance(fileThree.get(2), tempAccelZ);
    	currAccelZ = currAccelZ + DTWDistance(fileFour.get(2), tempAccelZ);
    	currAccelZ = currAccelZ/4;
    	float currGyroX = DTWDistance(fileOne.get(3), tempGyroX);
    	currGyroX = currGyroX + DTWDistance(fileTwo.get(3), tempGyroX);
    	currGyroX = currGyroX + DTWDistance(fileThree.get(3), tempGyroX);
    	currGyroX = currGyroX + DTWDistance(fileFour.get(3), tempGyroX);
    	currGyroX = currGyroX/4;
    	float currGyroY = DTWDistance(fileOne.get(4), tempGyroY);
    	currGyroY = currGyroY + DTWDistance(fileTwo.get(4), tempGyroY);
    	currGyroY = currGyroY + DTWDistance(fileThree.get(4), tempGyroY);
    	currGyroY = currGyroY + DTWDistance(fileFour.get(4), tempGyroY);
    	currGyroY = currGyroY/4;
    	float currGyroZ = DTWDistance(fileOne.get(5), tempGyroZ);
    	currGyroZ = currGyroZ + DTWDistance(fileTwo.get(5), tempGyroZ);
    	currGyroZ = currGyroZ + DTWDistance(fileThree.get(5), tempGyroZ);
    	currGyroZ = currGyroZ + DTWDistance(fileFour.get(5), tempGyroZ);
    	currGyroZ = currGyroZ/4;
    	float currVelocityX = DTWDistance(fileOne.get(6), tempVelocityX);
    	currVelocityX = currVelocityX + DTWDistance(fileTwo.get(6), tempVelocityX);
    	currVelocityX = currVelocityX + DTWDistance(fileThree.get(6), tempVelocityX);
    	currVelocityX = currVelocityX + DTWDistance(fileFour.get(6), tempVelocityX);
    	currVelocityX = currVelocityX/4;
    	float currVelocityY = DTWDistance(fileOne.get(7), tempVelocityY);
    	currVelocityY = currVelocityY + DTWDistance(fileTwo.get(7), tempVelocityY);
    	currVelocityY = currVelocityY + DTWDistance(fileThree.get(7), tempVelocityY);
    	currVelocityY = currVelocityY + DTWDistance(fileFour.get(7), tempVelocityY);
    	currVelocityY = currVelocityY/4;
    	float currFingerSize = DTWDistance(fileOne.get(8), tempFingerSize);
    	currFingerSize = currFingerSize + DTWDistance(fileTwo.get(8), tempFingerSize);
    	currFingerSize = currFingerSize + DTWDistance(fileThree.get(8), tempFingerSize);
    	currFingerSize = currFingerSize + DTWDistance(fileFour.get(8), tempFingerSize);
    	currFingerSize = currFingerSize/4;
    	float accelXAvg = 0;
    	float accelYAvg = 0;
    	float accelZAvg = 0;
    	float gyroXAvg = 0;
    	float gyroYAvg = 0;
    	float gyroZAvg = 0;
    	float velocityXAvg = 0;
    	float velocityYAvg = 0;
    	float fingerSizeAvg = 0;
    	for(int k = 0; k < 6; k++)
    	{
    		accelXAvg = accelXAvg + accel_x.get(k);
    		accelYAvg = accelYAvg + accel_y.get(k);
    		accelZAvg = accelZAvg + accel_z.get(k);
    		gyroXAvg = gyroXAvg + gyro_x.get(k);
    		gyroYAvg = gyroYAvg + gyro_y.get(k);
    		gyroZAvg = gyroZAvg + gyro_z.get(k);
    		velocityXAvg = velocityXAvg + velocity_x.get(k);
    		velocityYAvg = velocityYAvg + velocity_y.get(k);
    		fingerSizeAvg = fingerSizeAvg + fingerSize.get(k);
    	}
    	accelXAvg = accelXAvg/6;
    	accelYAvg = accelYAvg/6;
    	accelZAvg = accelZAvg/6;
    	gyroXAvg = gyroXAvg/6;
    	gyroYAvg = gyroYAvg/6;
    	gyroZAvg = gyroZAvg/6;
    	velocityXAvg = velocityXAvg/6;
    	velocityYAvg = velocityYAvg/6;
    	fingerSizeAvg = fingerSizeAvg/6;

    	float accelXStdDev = 0;
    	float accelYStdDev = 0;
    	float accelZStdDev = 0;
    	float gyroXStdDev = 0;
    	float gyroYStdDev = 0;
    	float gyroZStdDev = 0;
    	float velocityXStdDev = 0;
    	float velocityYStdDev = 0;
    	float fingerSizeAvgStdDev = 0;
    	for(int l = 0; l < 6; l++)
    	{
    		accelXStdDev = accelXStdDev + (float)(Math.pow((accel_x.get(l)-accelXAvg),2));
    		accelYStdDev = accelYStdDev + (float)(Math.pow((accel_y.get(l)-accelYAvg),2));
    		accelZStdDev = accelZStdDev + (float)(Math.pow((accel_z.get(l)-accelZAvg),2));
    		gyroXStdDev = gyroXStdDev + (float)(Math.pow((gyro_x.get(l)-gyroXAvg),2));
    		gyroYStdDev = gyroYStdDev + (float)(Math.pow((gyro_y.get(l)-gyroYAvg),2));
    		gyroZStdDev = gyroZStdDev + (float)(Math.pow((gyro_z.get(l)-gyroZAvg),2));
    		velocityXStdDev = velocityXStdDev + (float)(Math.pow((velocity_x.get(l)-velocityXAvg),2));
    		velocityYStdDev = velocityYStdDev + (float)(Math.pow((velocity_y.get(l)-velocityYAvg),2));
    		fingerSizeAvgStdDev = fingerSizeAvgStdDev + (float)(Math.pow((fingerSize.get(l)-fingerSizeAvg),2));
    	}
    	
    	accelXStdDev = accelXStdDev/5;
    	accelYStdDev = accelYStdDev/5;
    	accelZStdDev = accelZStdDev/5;
    	gyroXStdDev = gyroXStdDev/5;
    	gyroYStdDev = gyroYStdDev/5;
    	gyroZStdDev = gyroZStdDev/5;
    	velocityXStdDev = velocityXStdDev/5;
    	velocityYStdDev = velocityYStdDev/5;
    	fingerSizeAvgStdDev = fingerSizeAvgStdDev/5;

    	accelXStdDev = (float)(Math.sqrt(accelXStdDev));
    	accelYStdDev = (float)(Math.sqrt(accelYStdDev));
    	accelZStdDev = (float)(Math.sqrt(accelZStdDev));
    	gyroXStdDev = (float)(Math.sqrt(gyroXStdDev));
    	gyroYStdDev = (float)(Math.sqrt(gyroYStdDev));
    	gyroZStdDev = (float)(Math.sqrt(gyroZStdDev));
    	velocityXStdDev = (float)(Math.sqrt(velocityXStdDev));
    	velocityYStdDev = (float)(Math.sqrt(velocityYStdDev));
    	fingerSizeAvgStdDev = (float)(Math.sqrt(fingerSizeAvgStdDev));
    	
		System.out.println("Accel_x: " + accelXAvg + " " + accelXStdDev + " " + currAccelX);
		System.out.println("Accel_y: " + accelYAvg + " " + accelYStdDev + " " + currAccelY);
		System.out.println("Accel_z: " + accelZAvg + " " + accelZStdDev + " " + currAccelZ);
		System.out.println("Gyro_x: " + gyroXAvg + " " + gyroXStdDev + " " + currGyroX);
		System.out.println("Gyro_y: " + gyroYAvg + " " + gyroYStdDev + " " + currGyroY);
		System.out.println("Gyro_z: " + gyroZAvg + " " + gyroZStdDev + " " + currGyroZ);
		System.out.println("velocity_x: " + velocityXAvg + " " + velocityXStdDev + " " + currVelocityX);
		System.out.println("velocity_y: " + velocityYAvg + " " + velocityYStdDev + " " + currVelocityY);
		System.out.println("fingerSize: " + fingerSizeAvg + " " + fingerSizeAvgStdDev + " " + currFingerSize);
		
		float accelXMargin = accelXAvg + accelXStdDev;
		float accelYMargin = accelYAvg + accelYStdDev;
		float accelZMargin = accelZAvg + accelZStdDev;
		float gyroXMargin = gyroXAvg + gyroXStdDev;
		float gyroYMargin = gyroYAvg + gyroYStdDev;
		float gyroZMargin = gyroZAvg + gyroZStdDev;
		float velocityXMargin = velocityXAvg + velocityXStdDev;
		float velocityYMargin = velocityYAvg + velocityYStdDev;
		float fingerSizeMargin = fingerSizeAvg + fingerSizeAvgStdDev;
		
		if(currFingerSize < fingerSizeMargin && currVelocityX < velocityXMargin && currVelocityY < velocityYMargin)
		{
			return true;
		}

		return false;
    }
    
    private void DynamicTimeWarpingCalibration() throws IOException
    {
    	File fileDir = context.getExternalFilesDir(null);
    	fileOne = new ArrayList<ArrayList<Float>>();
    	fileTwo = new ArrayList<ArrayList<Float>>();
    	fileThree = new ArrayList<ArrayList<Float>>();
    	fileFour = new ArrayList<ArrayList<Float>>();
    	accel_x = new ArrayList<Float>();
    	accel_y = new ArrayList<Float>();
    	accel_z = new ArrayList<Float>();
    	gyro_x = new ArrayList<Float>();
    	gyro_y = new ArrayList<Float>();
    	gyro_z = new ArrayList<Float>();
    	velocity_x = new ArrayList<Float>();
    	velocity_y = new ArrayList<Float>();
    	fingerSize = new ArrayList<Float>();
    	for(int i = 0; i < 9; i++)
    	{
    		fileOne.add(new ArrayList<Float>());
    		fileTwo.add(new ArrayList<Float>());
    		fileThree.add(new ArrayList<Float>());
    		fileFour.add(new ArrayList<Float>());
    	}
    	for (int i = 2; i <= NUM_CAL_FILES; i++) {
    		String curFile = "calibration" + i + ".csv";
    		System.out.println(curFile);
    		File cur = new File(fileDir, curFile);
    		BufferedReader br = new BufferedReader(new FileReader(cur));
    		String line;
    		while ((line = br.readLine()) != null) {
    			String[] tokens = line.split(",");
    			if(i==2)
    			{
        			fileOne.get(0).add(Float.parseFloat(tokens[1]));
        			fileOne.get(1).add(Float.parseFloat(tokens[2]));
        			fileOne.get(2).add(Float.parseFloat(tokens[3]));
        			fileOne.get(3).add(Float.parseFloat(tokens[4]));
        			fileOne.get(4).add(Float.parseFloat(tokens[5]));
        			fileOne.get(5).add(Float.parseFloat(tokens[6]));
        			fileOne.get(6).add(Float.parseFloat(tokens[7]));
        			fileOne.get(7).add(Float.parseFloat(tokens[8]));
        			fileOne.get(8).add(Float.parseFloat(tokens[9]));
    			}
    			else if(i == 3)
    			{
        			fileTwo.get(0).add(Float.parseFloat(tokens[1]));
        			fileTwo.get(1).add(Float.parseFloat(tokens[2]));
        			fileTwo.get(2).add(Float.parseFloat(tokens[3]));
        			fileTwo.get(3).add(Float.parseFloat(tokens[4]));
        			fileTwo.get(4).add(Float.parseFloat(tokens[5]));
        			fileTwo.get(5).add(Float.parseFloat(tokens[6]));
        			fileTwo.get(6).add(Float.parseFloat(tokens[7]));
        			fileTwo.get(7).add(Float.parseFloat(tokens[8]));
        			fileTwo.get(8).add(Float.parseFloat(tokens[9]));
    			}
    			else if (i==4)
    			{
        			fileThree.get(0).add(Float.parseFloat(tokens[1]));
        			fileThree.get(1).add(Float.parseFloat(tokens[2]));
        			fileThree.get(2).add(Float.parseFloat(tokens[3]));
        			fileThree.get(3).add(Float.parseFloat(tokens[4]));
        			fileThree.get(4).add(Float.parseFloat(tokens[5]));
        			fileThree.get(5).add(Float.parseFloat(tokens[6]));
        			fileThree.get(6).add(Float.parseFloat(tokens[7]));
        			fileThree.get(7).add(Float.parseFloat(tokens[8]));
        			fileThree.get(8).add(Float.parseFloat(tokens[9]));
    			}
    			else if (i==5)
    			{
        			fileFour.get(0).add(Float.parseFloat(tokens[1]));
        			fileFour.get(1).add(Float.parseFloat(tokens[2]));
        			fileFour.get(2).add(Float.parseFloat(tokens[3]));
        			fileFour.get(3).add(Float.parseFloat(tokens[4]));
        			fileFour.get(4).add(Float.parseFloat(tokens[5]));
        			fileFour.get(5).add(Float.parseFloat(tokens[6]));
        			fileFour.get(6).add(Float.parseFloat(tokens[7]));
        			fileFour.get(7).add(Float.parseFloat(tokens[8]));
        			fileFour.get(8).add(Float.parseFloat(tokens[9]));
    			}
    		}
    		br.close();
    	}
		for(int i = 0; i<9; i++)
		{
    		if(i == 0)
    		{
    			accel_x.add(DTWDistance(fileOne.get(i), fileTwo.get(i)));
    			accel_x.add(DTWDistance(fileOne.get(i), fileThree.get(i)));
    			accel_x.add(DTWDistance(fileOne.get(i), fileFour.get(i)));
    			accel_x.add(DTWDistance(fileTwo.get(i), fileThree.get(i)));
    			accel_x.add(DTWDistance(fileTwo.get(i), fileFour.get(i)));
    			accel_x.add(DTWDistance(fileThree.get(i), fileFour.get(i)));
    		}
    		else if(i == 1)
    		{
    			accel_y.add(DTWDistance(fileOne.get(i), fileTwo.get(i)));
    			accel_y.add(DTWDistance(fileOne.get(i), fileThree.get(i)));
    			accel_y.add(DTWDistance(fileOne.get(i), fileFour.get(i)));
    			accel_y.add(DTWDistance(fileTwo.get(i), fileThree.get(i)));
    			accel_y.add(DTWDistance(fileTwo.get(i), fileFour.get(i)));
    			accel_y.add(DTWDistance(fileThree.get(i), fileFour.get(i)));
    		}
    		else if (i == 2)
    		{
    			accel_z.add(DTWDistance(fileOne.get(i), fileTwo.get(i)));
    			accel_z.add(DTWDistance(fileOne.get(i), fileThree.get(i)));
    			accel_z.add(DTWDistance(fileOne.get(i), fileFour.get(i)));
    			accel_z.add(DTWDistance(fileTwo.get(i), fileThree.get(i)));
    			accel_z.add(DTWDistance(fileTwo.get(i), fileFour.get(i)));
    			accel_z.add(DTWDistance(fileThree.get(i), fileFour.get(i)));
    		}
    		else if (i == 3)
    		{
    			gyro_x.add(DTWDistance(fileOne.get(i), fileTwo.get(i)));
    			gyro_x.add(DTWDistance(fileOne.get(i), fileThree.get(i)));
    			gyro_x.add(DTWDistance(fileOne.get(i), fileFour.get(i)));
    			gyro_x.add(DTWDistance(fileTwo.get(i), fileThree.get(i)));
    			gyro_x.add(DTWDistance(fileTwo.get(i), fileFour.get(i)));
    			gyro_x.add(DTWDistance(fileThree.get(i), fileFour.get(i)));
    		}
    		else if (i == 4)
    		{
    			gyro_y.add(DTWDistance(fileOne.get(i), fileTwo.get(i)));
    			gyro_y.add(DTWDistance(fileOne.get(i), fileThree.get(i)));
    			gyro_y.add(DTWDistance(fileOne.get(i), fileFour.get(i)));
    			gyro_y.add(DTWDistance(fileTwo.get(i), fileThree.get(i)));
    			gyro_y.add(DTWDistance(fileTwo.get(i), fileFour.get(i)));
    			gyro_y.add(DTWDistance(fileThree.get(i), fileFour.get(i)));
    		}
    		else if (i == 5)
    		{
    			gyro_z.add(DTWDistance(fileOne.get(i), fileTwo.get(i)));
    			gyro_z.add(DTWDistance(fileOne.get(i), fileThree.get(i)));
    			gyro_z.add(DTWDistance(fileOne.get(i), fileFour.get(i)));
    			gyro_z.add(DTWDistance(fileTwo.get(i), fileThree.get(i)));
    			gyro_z.add(DTWDistance(fileTwo.get(i), fileFour.get(i)));
    			gyro_z.add(DTWDistance(fileThree.get(i), fileFour.get(i)));
    		}
    		else if (i == 6)
    		{
    			velocity_x.add(DTWDistance(fileOne.get(i), fileTwo.get(i)));
    			velocity_x.add(DTWDistance(fileOne.get(i), fileThree.get(i)));
    			velocity_x.add(DTWDistance(fileOne.get(i), fileFour.get(i)));
    			velocity_x.add(DTWDistance(fileTwo.get(i), fileThree.get(i)));
    			velocity_x.add(DTWDistance(fileTwo.get(i), fileFour.get(i)));
    			velocity_x.add(DTWDistance(fileThree.get(i), fileFour.get(i)));
    		}
    		else if (i == 7)
    		{
    			velocity_y.add(DTWDistance(fileOne.get(i), fileTwo.get(i)));
    			velocity_y.add(DTWDistance(fileOne.get(i), fileThree.get(i)));
    			velocity_y.add(DTWDistance(fileOne.get(i), fileFour.get(i)));
    			velocity_y.add(DTWDistance(fileTwo.get(i), fileThree.get(i)));
    			velocity_y.add(DTWDistance(fileTwo.get(i), fileFour.get(i)));
    			velocity_y.add(DTWDistance(fileThree.get(i), fileFour.get(i)));
    		}
    		else if (i == 8)
    		{
    			fingerSize.add(DTWDistance(fileOne.get(i), fileTwo.get(i)));
    			fingerSize.add(DTWDistance(fileOne.get(i), fileThree.get(i)));
    			fingerSize.add(DTWDistance(fileOne.get(i), fileFour.get(i)));
    			fingerSize.add(DTWDistance(fileTwo.get(i), fileThree.get(i)));
    			fingerSize.add(DTWDistance(fileTwo.get(i), fileFour.get(i)));
    			fingerSize.add(DTWDistance(fileThree.get(i), fileFour.get(i)));
    		}
		}
		for(int i = 0; i < 6; i++)
			System.out.println("Accel_x: " + accel_x.get(i));
		for(int i = 0; i < 6; i++)
			System.out.println("Accel_y: " + accel_y.get(i));
		for(int i = 0; i < 6; i++)
			System.out.println("Accel_z: " + accel_z.get(i));
		for(int i = 0; i < 6; i++)
			System.out.println("Gyro_x: " + gyro_x.get(i));
		for(int i = 0; i < 6; i++)
			System.out.println("Gyro_y: " + gyro_y.get(i));
		for(int i = 0; i < 6; i++)
			System.out.println("Gyro_z: " + gyro_z.get(i));
		for(int i = 0; i < 6; i++)
			System.out.println("velocity_x: " + velocity_x.get(i));
		for(int i = 0; i < 6; i++)
			System.out.println("velocity_y: " + velocity_y.get(i));
		for(int i = 0; i < 6; i++)
			System.out.println("fingerSize: " + fingerSize.get(i));
    }
    
    public static float DTWDistance(List<Float> a, List<Float> b) {
	    float[][] DTW = new float[a.size()][b.size()];
	    
	    for(int i =1 ;i<a.size(); i++)
	    {
	    	DTW[i][0] = 2147483647;
	    }
	    for(int i =1 ;i<b.size(); i++)
	    {
	    	DTW[0][i] = 2147483647;
	    }
	    DTW[0][0] = 0;
	    
	    for(int i = 1; i < a.size(); i++)
	    {
	    	for(int j = 1; j<b.size(); j++)
	    	{
	    		float cost = Math.abs(a.get(i)-b.get(j));
	    		DTW[i][j] = cost + minimum(DTW[i-1][j], DTW[i][j-1], DTW[i-1][j-1]);
	    	}
	    }
	    return DTW[a.size()-1][b.size()-1];
	}
	
	public static float minimum(float x, float y, float z)
	{
		if(x <= y && x <= z)
		{
			return x;
		}
		else if(y <= x && y <= z)
		{
			return y;
		}
		else
		{
			return z;
		}
	}
}
