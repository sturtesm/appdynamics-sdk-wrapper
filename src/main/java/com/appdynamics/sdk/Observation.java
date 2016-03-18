package com.appdynamics.sdk;

import java.util.Date;

import com.appdynamics.sdk.MetricOperations.METRIC_OPERATIONS;

public class Observation {
	private String metricName = null;
	private METRIC_OPERATIONS operation = null;
	private Date time = null;
	private long value = 0;

	public Observation(long value, String metricName, METRIC_OPERATIONS op, Date time) {
		this.value = value;
		this.metricName = metricName;
		this.operation = op;
		this.time = time;
	}

	public String getMetricName() {
		return metricName;
	}

	public METRIC_OPERATIONS getOperation() {
		return operation;
	}

	public Date getTime() {
		return time;
	}

	public long getValue() {
		return value;
	}
} 