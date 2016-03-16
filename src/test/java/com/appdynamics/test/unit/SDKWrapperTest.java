package com.appdynamics.test.unit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.appdynamics.sdk.EventReporter;
import com.appdynamics.sdk.EventReporter.EVENT_TYPE;
import com.appdynamics.sdk.Metric;
import com.appdynamics.sdk.MetricReporter;
import com.appdynamics.sdk.TransactionReporter;

public class SDKWrapperTest {

	private Logger logger = Logger.getLogger(SDKWrapperTest.class);

	@Test (priority=1, invocationCount = 100)
	public void metricAvgTest() {
		Metric metric = new Metric("SampleUnitTest/Response_Time_ms");

		for (int i = 1; i <= 100; i++) {
			metric.addObservation(i);
		}

		metric.report();

		double percentile = metric.getPercentileValue(95);

		Assert.assertTrue(new Double(percentile).intValue() == 95);

		metric.reportQuantile(95);

		sleep(500);
	}

	//@Test (priority=1, invocationCount = 200)
	public void eventInfoTest() {
		EventReporter wrapper = new EventReporter();
		Map<String, String> map = new HashMap<String, String> ();

		logger.debug("Running Test eventTest()");

		map.put("key1", "value1");
		map.put("key2", "value2");

		wrapper.generateEvent("SampleUnitTest_Info_Event", map, EVENT_TYPE.INFO);

		sleep(500);
	}

	private void sleep(int delayMs) {
		try {
			Thread.sleep(delayMs);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

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

}
