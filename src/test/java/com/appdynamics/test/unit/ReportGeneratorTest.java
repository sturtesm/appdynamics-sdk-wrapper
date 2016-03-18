package com.appdynamics.test.unit;

import java.util.ArrayList;
import java.util.Random;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.appdynamics.report.ReportGenerator;
import com.appdynamics.report.ReportGenerator.SUMMARY_INDEX;
import com.appdynamics.rest.AppdRESTHelper;
import com.appdynamics.sdk.MetricOperations.METRIC_OPERATIONS;
import com.appdynamics.sdk.ResponseTimeMetric;

public class ReportGeneratorTest {
	
	ReportGenerator generator = null;
	
	ResponseTimeMetric metricTransaction = new ResponseTimeMetric("SampleUnitTest/TestCycle/Response_Time_ms", "ms");
	ResponseTimeMetric metricStep1 = new ResponseTimeMetric("SampleUnitTest/StepOne/Response_Time_ms", "ms");
	ResponseTimeMetric metricStep2 = new ResponseTimeMetric("SampleUnitTest/StepTwo/Response_Time_ms", "ms");
	
	@BeforeTest
	public void testSetup() {
		generator = new ReportGenerator("./target/test-classes/bootstrap-admin-template", "appd-unit-test-report");
	}
	
	@AfterTest
	public void generateTestReport() throws Exception {
		/** sleep for 1+ minutes to allow appd agent to flush its buffer */
		//doSleep(90000);
		
		/** we can add up to three summary goals for our test */
		generator.addSummaryGoal(SUMMARY_INDEX.ONE, "95th", "95th % Goal is 550", metricTransaction.getPercentileValue(95), 550, 3, 10);
		generator.addSummaryGoal(SUMMARY_INDEX.TWO, "Avg", "Avg (ms) Goal is 287.5", metricStep1.getAvg(), 50, 5, 10);
		generator.addSummaryGoal(SUMMARY_INDEX.THREE, "Count", "Observations Goal is 10000", metricStep2.getCount(), 10000, 1, 2);
		
		/** this will add the metrics to our time series plot */
		generator.addMetricTimeSeriesPlot(metricTransaction, METRIC_OPERATIONS.PCT_95);
		generator.addMetricTimeSeriesPlot(metricStep1, METRIC_OPERATIONS.AVG);
		generator.addMetricTimeSeriesPlot(metricStep2, METRIC_OPERATIONS.AVG);
		
		/** this will add the metrics to our test summary pie chart */
		generator.addMetricSummaryPlot(metricTransaction, metricStep1, METRIC_OPERATIONS.AVG);
		generator.addMetricSummaryPlot(metricTransaction, metricStep2, METRIC_OPERATIONS.AVG);

		generator.generateReport();
	}
	
	@Test (invocationCount = 100)
	public void simulateUnitTest() {
		
		int stepOneSleepCeiling = new Random().nextInt(400) + 1;
		int stepTwoSleepCeiling = new Random().nextInt(200) + stepOneSleepCeiling;
		
		/** time how long it takes to complete a full iteration, and also the sub-steps */
		metricTransaction.startTimer();

		doStep(metricStep1, stepOneSleepCeiling);
		
		doStep(metricStep2, stepTwoSleepCeiling);
		
		doSleep(new Random().nextInt(450));
		
		metricTransaction.stopTimer(true);
	}
	
	@Test (dependsOnMethods = {"simulateUnitTest"}) 
	public void reportUnitTestMetrics() {
		ArrayList<Long> quantiles = new ArrayList<Long>();
		quantiles.add(90l);
		quantiles.add(95l);
		
		/** report the percentil metrics as part of the test */
		metricTransaction.reportQuantiles(quantiles);
		metricStep1.reportQuantiles(quantiles);
		metricStep2.reportQuantiles(quantiles);
	}
	
	private void doStep(ResponseTimeMetric metric, int maxSleepMillis) {
		metric.startTimer();
		
		doSleep(maxSleepMillis);
		
		metric.stopTimer(true);
	}
	
	private void doSleep(int maxSleepMillis) {
		try {
			Thread.sleep(new Random().nextInt(maxSleepMillis));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test (dependsOnMethods = {"reportUnitTestMetrics"})
	public void loadAppdProperties() throws Exception
	{
		AppdRESTHelper restHelper = new AppdRESTHelper();
		
		restHelper.loadProperties();
	}
}
