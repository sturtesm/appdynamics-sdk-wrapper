package com.appdynamics.test.unit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.appdynamics.apm.appagent.api.AgentDelegate;
import com.appdynamics.apm.appagent.api.IMetricAndEventReporter;
import com.appdynamics.sdk.EventReporter;
import com.appdynamics.sdk.EventReporter.EVENT_TYPE;
import com.appdynamics.sdk.Metric;

public class SDKWrapperTest {
	private Date startTime = null;
	private Logger logger = Logger.getLogger(SDKWrapperTest.class);
	
	@BeforeTest
	public void setup() {
		startTime = new Date();
		
		logger.info("Starting Test Now: " + startTime);
	}

	@AfterTest
	public void shutdown() {
		Date stopTime = new Date();
		
		logger.info("Closing Test Down Now: " + stopTime + ", Test Duration=" + 
				((stopTime.getTime() - startTime.getTime()) / 1000) + " seconds"); 
		
		/** 
		 * Sleeping for 2-minutes to allow 
		 * AppDynamics to flush its metric and event buffer
		 */
		sleep(120000);
	}
	
	//@Test (priority=1, invocationCount = 100)
	public void directSDKTest() {
		IMetricAndEventReporter metricReporter = 
				AgentDelegate.getMetricAndEventPublisher();

		for (int i = 1; i < 10; i++) {
			metricReporter.reportAverageMetric("directSDKTest", i);
			
			sleep(new Random().nextInt(125) + 125);
		}
	}
	
	@Test (priority=1, invocationCount = 100)
	public void metricAvgTest() {
		Metric metric = new Metric("MetricReporter/Response_Time_ms", null);
		
		IMetricAndEventReporter metricReporter = 
				AgentDelegate.getMetricAndEventPublisher();
		
		for (int i = 1; i <= 1000; i++) {
			metric.addObservation(i);
			
			metricReporter.reportAverageMetric("directSDKTest/Response_Time_ms", i);
			
			sleep(new Random().nextInt(1));
		}

		double percentile = metric.getPercentileValue(95);

		Assert.assertTrue(new Double(percentile).intValue() == 950);

		metric.reportQuantile(95);
	}

	@Test (priority=1, invocationCount = 5)
	public void eventInfoTest() {
		EventReporter wrapper = new EventReporter();
		Map<String, String> map = new HashMap<String, String> ();

		//logger.debug("Running Test eventTest()");

		map.put("key1", "value1");
		map.put("key2", "value2");

		wrapper.generateEvent("SampleUnitTest_Info_Event", map, EVENT_TYPE.INFO);

		sleep(100);
	}

	private void sleep(int delayMs) {
		try {
			Thread.sleep(delayMs);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	//@Test (priority=2, invocationCount = 25)
	public void eventWarningTest() {
		EventReporter wrapper = new EventReporter();
		Map<String, String> map = new HashMap<String, String> ();

		logger.debug("Running Test eventTest()");

		map.put("key1", "value1");
		map.put("key2", "value2");

		wrapper.generateEvent("SampleUnitTest_Warning_Event", map, EVENT_TYPE.WARNING);

		try {
			Thread.sleep(500);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	//@Test (priority=3, invocationCount = 25)
	public void eventCriticalTest() {
		EventReporter wrapper = new EventReporter();
		Map<String, String> map = new HashMap<String, String> ();

		logger.debug("Running Test eventCriticalTest()");

		map.put("Event Time", new Date().toString());

		try {
			Thread.sleep(500);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		wrapper.generateEvent("SampleUnitTest_Critical_Event", map, EVENT_TYPE.CRITICAL);
	}

	@Test (priority=2, invocationCount=100)	
	public void bizTransactionTest() {
		TransactionReporter wrapper = new TransactionReporter();
		String txName = "bizTransactionTest";

		logger.debug("Running Test bizTransactionTest()");

		String identifier = wrapper.startBusinesTransaction(txName);

		Assert.assertNotNull(identifier);

		try {
			Thread.sleep(500);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		wrapper.reportTransactionBackend("Memcache", "Memcache", true);
		wrapper.reportTransactionBackend("HTTP", "PayPal", true);

		boolean status = wrapper.stopBusinesTransaction(txName);

		Assert.assertTrue(status);
	}

	@Test (priority=1)
	public void metricTest() {
		MetricReporter metricWrapper = new MetricReporter();

		for (int i = 0; i < 10000; i++) {
			metricWrapper.reportMetricInstance("metricTest_iterations", 1);
			metricWrapper.reportMetricInstance("metricTest_response_time", new Random().nextInt(1000));
		}
	}
	*/
}
