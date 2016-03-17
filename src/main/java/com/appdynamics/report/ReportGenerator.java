package com.appdynamics.report;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

import com.appdynamics.utils.FileUtils;

public class ReportGenerator {
	
	public enum SUMMARY_INDEX { 
		
		ONE(1), TWO(2), THREE(3); 
		
		int value = 0;
		SUMMARY_INDEX(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	};
	
	Logger logger = Logger.getLogger(getClass());
	
	/** has the report been init'd */
	private boolean init = false;
	
	private FileUtils utils = null;
	
	/** the source, where we get the template from */
	private String templateSource;
	
	/** the target output, where the report is going */
	private Path targetOutputPath;
	
	/** this will own the summary section of the report */
	ReportSummary reportSummary = null;
	
	
	public ReportGenerator(String reportTemplateSourcePath, String reportOutputPath) {
		this.templateSource = (reportTemplateSourcePath == null) ? "./bootstrap-admin-template" : reportTemplateSourcePath;
		this.targetOutputPath = Paths.get(reportOutputPath);
		
		this.reportSummary = new ReportSummary();
		this.utils = new FileUtils();
	}
	
	/**
	 * Initialize the report generator, primarily attempts to recursively copy the report
	 * template from the {@link #templateSource} to the {@link #targetOutputPath}.
	 * 
	 * @throws Exception 
	 */
	public void initialize() throws Exception {
		
		utils.recursivelyCopy(templateSource, targetOutputPath);
		
		this.init = true;
	}
	
	/**
	 * Add up to three summary goals at the top of the report.
	 * 
	 * @param index which goal are we adding, the goals are rendered left to right at the top of the report
	 * @param shortDescription a short (typically < 10-char) description; e.g. 95th %, AVG, MAX, etc...
	 * @param description a longer, but still short, description of the metric typically < 25-chars
	 * @param value the value from the test run
	 * @param value of the goal, what are we shooting for?
	 * @param percentWarnTolerance the percent we're allowed to deviate away from the goal before we highlight a warning error
	 * @param percentCriticalTolerance the percent we're allowed to deviate away from the goal before we highlight a critical error
	 */
	public void addSummaryGoal(SUMMARY_INDEX index, 
			String shortDescription, String description, 
			double value, double goalValue, 
			long percentWarnTolerance, long percentCriticalTolerance)
	{
		reportSummary.addSummaryGoal(index, shortDescription, description, value, goalValue, percentWarnTolerance, percentCriticalTolerance);
	}
	
	/**
	 * generates the test report
	 * 
	 * @throws Exception
	 */
	public void generateReport() throws Exception {
		if (! init ) {
			initialize();
		}
		
		String template = utils.readFile(utils.getDestination(), "unitTestReport_template.html");
		
		if (template == null) {
			throw new Exception ("Error reading report template, template == null");
		}
		
		logger.debug("Adding test summary section");
		
		template = reportSummary.addSummarySection(template);
		
		logger.info("Writing test report (appdynamics-unit-test-report.html) to " + targetOutputPath);
		
		utils.writeReport(targetOutputPath, "appdynamics-unit-test-report.html", template);
	}
}
