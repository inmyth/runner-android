package jp.hanatoya.training.accel;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import jp.hanatoya.training.json.RunnerData;
import jp.hanatoya.training.util.RunnerMath;
import jp.hanatoya.training.util.Smoothing;
import jp.hanatoya.training.util.Smoothing.FilterType;

public class AccelerometerAdapter implements SensorEventListener {
	private SensorManager manager;
	private StepListener listener;
	private float oldx = 0f;
	private float oldy = 0f;
	private float oldz = 0f;
	public float dx = 0f;
	public float dy = 0f;
	public float dz = 0f;
	
	private int  counter = 0;
	private int  firstSteps = 1;

	int thresholdTime = 190;
	int thresholdPower = 40;

	private RunnerData runnerData;
	
	private int senseInterval;
	private int accSenseInterval;	
	public int accStepInterval;
	private long startTime;
	private long lastStepTime;
	private long lastSenseTime;
	private long dataPointCount;
	private long relativeSenseTime; // for csv
	private long initiateSmoothStopTime;
	private boolean smoothStopExecuted;
	private double prevslope;
	private double filterAverage;
	private double lastFilterAverage = 0.0d;

	public long stopTimeFromCoach = 0l;

	
	private BufferedWriter out;
//	private boolean killListener;
	private boolean forceStop;
	private Smoothing smoothing;
	

	private float stride;
	private float pitch;
	
	private Sensor accSensor;
	
	
	private static String newLineChar = System.getProperty("line.separator");


	public AccelerometerAdapter(SensorManager manager, StepListener listener, RunnerData runnerData){
		this.listener = listener;
		this.runnerData = runnerData;
		this.manager = manager;
		List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			accSensor = sensors.get(0);
			manager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);
		}
		smoothing = new Smoothing(FilterType.HANNING);
	}
	
	public AccelerometerAdapter(SensorManager manager, StepListener listener, RunnerData runnerData, File dump) {
		this(manager, listener, runnerData);
		
		try {
			out = new BufferedWriter(new FileWriter(dump));
			out.write("t,dx,dy,dz,power");
			out.write(newLineChar);

		} catch(IOException e){ // runner still runs even if file is na
			e.printStackTrace();
		} 

	}

	public void forceStop() {
		this.forceStop = true;
	}
	
	public void smoothStop(){
		smoothStopExecuted = true;
		initiateSmoothStopTime = System.currentTimeMillis();			
	}
	
	private void killSensor(){
		if (manager != null) {
			manager.unregisterListener(this, accSensor);
			manager = null;
		}
	}

	public void closeBuffer(){
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				out = null;
			}
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (forceStop){
			killSensor();
			closeBuffer();
			return;
		}
		
		if (smoothStopExecuted){
			if (System.currentTimeMillis() - initiateSmoothStopTime > 200){  // let the sensor run for 200ms extra to detect the last step in the last window
				killSensor();
				closeBuffer();
				wrapUp();
				listener.onSmoothStopCompleted();
				return;
			}
		}
		
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if (startTime == 0l) {
				startTime = lastSenseTime = lastStepTime = System.currentTimeMillis();
			} else {
				long nowSenseTime = System.currentTimeMillis();
				senseInterval = (int) (nowSenseTime - lastSenseTime);
				relativeSenseTime = nowSenseTime - startTime;

				
				lastSenseTime = nowSenseTime;
				accSenseInterval += senseInterval;
				
			}
			
			
			dx = event.values[0] - oldx;
			dy = event.values[1] - oldy;
			dz = event.values[2] - oldz;
			
			double dpower = (double) (dx * dx + dy * dy + dz * dz);
			
			if (!smoothStopExecuted)
				listener.onSensed(accSenseInterval, dx, dy, dz, filterAverage);
			
			Double[] smootheds = smoothing.oneFilter(dpower);
			if (smootheds != null && smootheds[0] != null){
				filterAverage = smootheds[0];
			}else{
				filterAverage = 0.0d;
			}

			double slope = filterAverage - lastFilterAverage; 			
			long nowStepTime = System.currentTimeMillis();
			long stepdt = (nowStepTime - lastStepTime);

			write(accSenseInterval, dx, dy, dz, dpower);
			
			boolean isStep = (filterAverage > 50) && (slope < 0 ) && (prevslope > 0) && stepdt > thresholdTime ? true : false;
//			boolean isStep = ((filterAverage > thresholdPower) && (lastFilterAverage < thresholdPower)) ? true : false; 
	
			
			
			if (isStep) {				
				lastStepTime = nowStepTime;
				accStepInterval += stepdt;	
				if (firstSteps <= 0){
					counter++;
					runnerData.setStep(counter);
					listener.onStep(accStepInterval, stepdt, counter);		
				}else{
					firstSteps--;
				}
			}
			lastFilterAverage = filterAverage;
			prevslope = slope;

			
			oldx = event.values[0];
			oldy = event.values[1];
			oldz = event.values[2];
		}
	}
	

	public void wrapUp() {
		if (counter == 0)
			counter = 1;
		
		runnerData.setSpeed(RunnerMath.speed(runnerData.getDistance(), runnerData.getStopMsCoach()));
		runnerData.setPitch(RunnerMath.pitch(System.currentTimeMillis() - startTime, counter));
//		runnerData.setStride(RunnerMath.stride(runnerData.getDistance(), counter));
		runnerData.setStride(RunnerMath.stride(runnerData.getSpeed(), runnerData.getPitch()));

	}

	public boolean isSmoothStopping(){
		return smoothStopExecuted;
	}
	
	private void write(long t, float ax, float ay, float az, double ap) {
		if (out == null) {
			return;
		}
		try { // at worst the file will be cut short
			out.write(String.format("%d,%.5f,%.5f,%.5f,%.5f", t, ax, ay, az, ap));
			out.write(newLineChar);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
}

