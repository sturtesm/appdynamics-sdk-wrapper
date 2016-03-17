package com.appdynamics.test.unit;

import org.testng.annotations.Test;

import com.appdynamics.report.ReportGenerator;
import com.appdynamics.report.ReportGenerator.SUMMARY_INDEX;
import com.appdynamics.sdk.Metric;

public class ReportGeneratorTest {
	
	
	@Test
	public void metricAvgTest() throws Exception {
		Metric metricOne = new Metric("SampleUnitTest/MetricOne/Response_Time_ms", null);

		for (int i = 1; i <= 1000; i++) {
			metricOne.addObservation(i);
		}

		double percentile = metricOne.getPercentileValue(95);

		ReportGenerator generator = new ReportGenerator("./target/test-classes/bootstrap-admin-template", "appd-unit-test-report");
		
		generator.addSummaryGoal(SUMMARY_INDEX.ONE, "95th", "95th % Goal is 950", percentile, 950, 10, 20);
		generator.addSummaryGoal(SUMMARY_INDEX.TWO, "Avg", "Avg (ms) Goal is 575", metricOne.getAvg(), 575, 10, 20);
		generator.addSummaryGoal(SUMMARY_INDEX.THREE, "Count", "Observations Goal is 10", metricOne.getCount(), 100, 10, 20);

		generator.generateReport();
	}
}
