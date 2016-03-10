package com.appdynamics.sdk;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.appdynamics.apm.appagent.api.IMetricAndEventReporter;

public class MetricReporter extends AppDynamicsSDKWrapper {
	private Logger logger = Logger.getLogger(MetricReporter.class);
	
	private Hashtable<String, Metric> metrics = new Hashtable<String, Metric> ();

	public MetricReporter() {
		super();
	}

	@Override
	protected void isInitialized() {
		setEnabled(getMetricReporter() != null);
	}

	/**
	 * reports a metric instance for the current 1-minute interval.  will result in
	 * a min, max, avg, sum, count and observed (last instance reported) also being reported.
	 * 
	 * @param metricName cannot be null
	 * @param value
	 */
	public void reportMetricInstance(String metricName, long value) {
		
		if (metricName == null || metricName.trim().length() <= 0) {
			return;
		}
		
		IMetricAndEventReporter metricReporter = getMetricReporter();

		metricReporter.reportAverageMetric(metricName, value);
	}

	public void reportSumMetric(String metricName, long metricValue) {
		IMetricAndEventReporter metricReporter = getMetricReporter();

		metricReporter.reportAverageMetric(metricName, metricValue);
	}
}
