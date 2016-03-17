package com.appdynamics.sdk;

import java.util.Hashtable;

public class Metrics {

	private Hashtable<String, Metric> metricHash = null;
	
	public Metrics() {
		metricHash = new Hashtable<String, Metric> ();
	}
	
	public void addMetric(Metric metric) {
		if (metric == null) {
			throw new IllegalArgumentException("metric cannot be null");
		}
		
		metricHash.put(metric.getMetricName(), metric);
	}
	
	public Metric getMetric(String metricName) {
		return metricHash.get(metricName);
	}
}
