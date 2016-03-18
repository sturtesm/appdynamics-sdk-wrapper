package com.appdynamics.test.unit;

import java.util.Date;

import org.appdynamics.appdrestapi.RESTAccess;
import org.appdynamics.appdrestapi.data.Application;
import org.appdynamics.appdrestapi.data.Applications;
import org.appdynamics.appdrestapi.data.Event;
import org.appdynamics.appdrestapi.data.Events;
import org.appdynamics.appdrestapi.data.MetricData;
import org.appdynamics.appdrestapi.data.MetricDatas;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import com.appdynamics.sdk.Metric;

import junit.framework.Assert;

public class RestAPITest {
	Logger logger = Logger.getLogger(getClass());
	RESTAccess access = null;

	@BeforeTest
	public void setup() {
		access = new RESTAccess("127.0.0.1", "8090", false, "admin", "admin");
	}

	@Test 
	public void getApplications() {
		assert (access != null);

		Applications apps = access.getApplications();

		for (Application a : apps.getApplications()) {
			System.out.println("Found application:\n " + a);
			logger.info("Found Application: " + a);
		}
	}

	@Test 
	public void getEvents() {
		assert (access != null);

		Events events = access.getEvents("82", "CUSTOM", "INFO,WARN,ERROR", 0, new Date().getTime());

		for (Event e : events.getEvents()) {
			System.out.println("Found event:\n " + e);
			logger.info("Found Event: " + e);
		}
	}
	
	@Test
	public void getMetrics() {
		
		/** should pull data for the last 5-minutes */
		Date stop = new Date();
		Date start = new Date(stop.getTime() - 300000);
		
		String metricPath = 
				"Application Infrastructure Performance|JUnitTest|Custom Metrics|MetricReporter/Response_Time_ms";
		
		Assert.assertNotNull(access);
		
		Metric metric = new Metric("MetricReporter/Response_Time_ms", null);
		
		for (int i = 0; i < 100; i++) {
			metric.addObservation(i);
		}
		metric.reportQuantile(95);
		
		try {
			/** give the agent time to push the data */
			Thread.sleep(90000);
		}catch (Exception e) { 
			//stub 
		}
		
		MetricDatas datas = access.getRESTMetricQueryCustom(
				"JavaUnitTest", metricPath, start.getTime(), stop.getTime(), false);
		
		for (MetricData d : datas.getMetric_data()) {
			System.out.println("Found Metric Data: " + d);
		}
				
		Assert.assertNotNull(datas);
	}
}
