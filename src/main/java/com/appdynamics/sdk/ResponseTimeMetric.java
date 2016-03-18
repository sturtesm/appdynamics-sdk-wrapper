package com.appdynamics.sdk;

import java.util.Date;

public class ResponseTimeMetric extends Metric {
	private long startTime = 0;
	private long stopTime = 0;
	private long durationMilliseconds = 0;
	
	
	public ResponseTimeMetric(String name, String units) {
		super (name, units);
	}
	
	public void startTimer() {
		startTime = new Date().getTime();
		
		durationMilliseconds = 0;
	}
	
	public void stopTimer(boolean addObservation) {
		stopTime = new Date().getTime();
		
		durationMilliseconds = stopTime - startTime;
		
		if (addObservation) {
			this.addObservation();
		}
	}
	
	public long getDurationMillis() {
		return this.durationMilliseconds;
	}
	
	public void addObservation() {
		super.addObservation(durationMilliseconds);
	}
}
