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
	
	ResponseTimeMetric metricTransaction = new ResponseTimeMetric("TestCycle/Elapsed", "ms");
	ResponseTimeMetric metricStep1 = new ResponseTimeMetric("Step1/Elapsed", "ms");
	ResponseTimeMetric metricStep2 = new ResponseTimeMetric("Step2/Elapsed", "ms");
	
	@BeforeTest
	public void testSetup() {
		generator = new ReportGenerator("./target/test-classes/bootstrap-admin-template", "appd-unit-test-report");
		
		/** start the test timer */
		generator.startTest();
	}
	
	@AfterTest
	public void generateTestReport() throws Exception {
		
		generator.stopTest();
		
		/** we can add up to three summary goals for our test */
		generator.addSummaryGoal(SUMMARY_INDEX.ONE, "95th", "95th % Goal is 550", (long)metricTransaction.getPercentileValue(95), 550, 90, 100);
		generator.addSummaryGoal(SUMMARY_INDEX.TWO, "Avg", "Avg (ms) Goal is 287.5", metricStep1.getAvg(), 50, 50, 75);
		generator.addSummaryGoal(SUMMARY_INDEX.THREE, "Count", "Observations Goal is 110", metricStep2.getCount(), 125, 15, 25);
		
		/** this will add the metrics to our time series plot */
		generator.addMetricTimeSeriesPlot(metricTransaction, METRIC_OPERATIONS.PCT_95);
		generator.addMetricTimeSeriesPlot(metricStep1, METRIC_OPERATIONS.AVG);
		generator.addMetricTimeSeriesPlot(metricStep2, METRIC_OPERATIONS.AVG);
		
		/** this will add the metrics to our test summary pie chart */
		generator.addMetricSummaryPlot(metricStep1, METRIC_OPERATIONS.AVG);
		generator.addMetricSummaryPlot(metricStep2, METRIC_OPERATIONS.AVG);
		
		generator.addMetricTablePlot(metricTransaction, METRIC_OPERATIONS.MIN);
		generator.addMetricTablePlot(metricTransaction, METRIC_OPERATIONS.AVG);
		generator.addMetricTablePlot(metricTransaction, METRIC_OPERATIONS.PCT_95);
		generator.addMetricTablePlot(metricTransaction, METRIC_OPERATIONS.MAX);

		generator.generateReport();
		
		/** sleep for 1+ minutes to allow appd agent to flush its buffer */
		doSleep(90000);
	}
	
	@Test (invocationCount = 10//
			)
	public void simulateUnitTest() {
		
		int stepOneSleepCeiling = new Random().nextInt(400) + 1;
		int stepTwoSleepCeiling = new Random().nextInt(200) + stepOneSleepCeiling;
		
		/** time how long it takes to complete a full iteration, and also the sub-steps */
		metricTransaction.startTimer();

		doStep(metricStep1, stepOneSleepCeiling);
		
		doStep(metricStep2, stepTwoSleepCeiling);
		
		doSleepRandom(new Random().nextInt(450) + 1);
		
		metricTransaction.stopTimer(true);
	}
	
	@Test (dependsOnMethods = {"simulateUnitTest"}) 
	public void reportUnitTestMetrics() {
		ArrayList<Long> quantiles = new ArrayList<Long>();
		quantiles.add(90l);
		quantiles.add(95l);
		
		/** report the percentile metrics as part of the test */
		metricTransaction.reportQuantiles(quantiles);
		metricStep1.reportQuantiles(quantiles);
		metricStep2.reportQuantiles(quantiles);
	}
	
	private void doStep(ResponseTimeMetric metric, int maxSleepMillis) {
		metric.startTimer();
		
		doSleepRandom(maxSleepMillis);
		
		metric.stopTimer(true);
	}
	
	private void doSleepRandom(int maxSleepMillis) {
		try {
			Thread.sleep(new Random().nextInt(maxSleepMillis));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void doSleep(int millis) {
		try {
			Thread.sleep(millis);
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
