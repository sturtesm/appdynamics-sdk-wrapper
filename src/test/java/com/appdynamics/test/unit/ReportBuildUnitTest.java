package com.appdynamics.test.unit;

import java.io.File;
import java.nio.file.Paths;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.appdynamics.sdk.Metric;
import com.appdynamics.utils.FileUtils;

public class ReportBuildUnitTest {
	private FileUtils utils = null;
	private String reportTemplate = null;
	
	@BeforeTest
	public void testRecursiveJarCopy() throws Exception {
		utils = new FileUtils();
		
		String resourceName = "./target/test-classes/bootstrap-admin-template";

		utils.recursivelyCopy(resourceName, Paths.get(".", "appd-unit-test-report"));
	}
	
	@Test
	public void readTemplate() throws Exception {
		reportTemplate = 
				utils.readFile(utils.getDestination(), "unitTestReport_template.html");
		
		Assert.assertNotNull (reportTemplate != null, "Error reading template");
	}
	
	@Test (dependsOnMethods = {"readTemplate"})
	public void metricAvgTest() {
		Metric metricOne = new Metric("SampleUnitTest/MetricOne/Response_Time_ms", null);
		

		for (int i = 1; i <= 1000; i++) {
			metricOne.addObservation(i);
		}

		double percentile = metricOne.getPercentileValue(95);

		reportTemplate = reportTemplate.replace("$percentile_goal_panel", "panel-green");
		reportTemplate = reportTemplate.replace("$percentile_goal_description", "95th");
		reportTemplate = reportTemplate.replace("$percentile_goal_value", "95th % goal is 950 (ms)");
		reportTemplate = reportTemplate.replace("$percentile_actual", "Actual 95th % value is " + percentile);
		
		Assert.assertTrue(new Double(percentile).intValue() == 950);
	}
	
	@Test (dependsOnMethods = {"metricAvgTest"}) 
	public void writeReport() throws Exception 
	{
		String reportName = "unitTestReport.html";
		
		assert (reportTemplate != null);
		assert (utils.getDestination() != null);
		
		utils.writeReport (utils.getDestination(), reportName, reportTemplate);
		
		String s = utils.getDestination().toString() + File.separator + reportName;
		
		Assert.assertTrue(new File(s).exists() == true, "Error validating that file " + s + " exists.");
		
		System.out.println("Wrote " + s + " unit test report");
	}
}
