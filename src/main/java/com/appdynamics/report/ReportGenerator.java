package com.appdynamics.report;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.log4j.Logger;

import com.appdynamics.rest.AppdRESTHelper;
import com.appdynamics.sdk.Metric;
import com.appdynamics.sdk.MetricOperations.METRIC_OPERATIONS;
import com.appdynamics.sdk.ResponseTimeMetric;
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

	/** the time series report */
	TimeSeriesMetricPlot metricHistorySeries = null;

	/** the summary pie chart plot */
	MorrisPieChart metricSummaryPlot = null;
	
	/** the metric table plot */
	TimeSeriesMorrisTable metricTablePlot = null;

	private AppdRESTHelper restHelper = null;
	
	/** test start / stop times */
	private Date testStart = null;
	private Date testStop = null;

	public ReportGenerator(String reportTemplateSourcePath, String reportOutputPath) {
		this.templateSource = (reportTemplateSourcePath == null) ? "./bootstrap-admin-template" : reportTemplateSourcePath;
		this.targetOutputPath = Paths.get(reportOutputPath);

		this.metricHistorySeries = new TimeSeriesMetricPlot();
		this.reportSummary = new ReportSummary();
		this.metricSummaryPlot = new MorrisPieChart();
		this.metricTablePlot = new TimeSeriesMorrisTable();

		this.utils = new FileUtils();
		this.testStart = new Date();
	}

	/**
	 * mark the start of the test
	 */
	public void startTest() {
		testStart = new Date();
	}

	/** 
	 * mark the stop of the test
	 */
	public void stopTest() {
		testStop = new Date();
	}
	
	public void addMetricTimeSeriesPlot(Metric metric, METRIC_OPERATIONS operation) throws Exception {

		if (! init ) {
			initialize();
		}

		metricHistorySeries.addMetricToTimeSeries(metric, operation);
	}

	private void addMetricTimeSeriesSection() throws URISyntaxException, IOException {
		String morrisData = 
				utils.readFile(utils.getDestination(), "./js/morris-data-line-chart-template.js");

		morrisData = metricHistorySeries.writeMetricsToTemplate(morrisData, restHelper);

		utils.writeReport(utils.getDestination(), "./js/morris-data-line-chart.js", morrisData);
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
	 * generates the test report, this should be called last, after the other sections are added.
	 * 
	 * @throws Exception
	 */
	public void generateReport() throws Exception {
		
		if (testStop == null) {
			stopTest();
		}
		
		if (! init ) {
			initialize();
		}
		

		/** ingest the template file from the destination path */
		String template = 
				utils.readFile(utils.getDestination(), "templates/unitTestReport_template.html");

		if (template == null) {
			throw new Exception ("Error reading report template, template == null");
		}

		logger.debug("Adding test summary section");

		/** adds the top section, the goals and progress against goals */
		template = reportSummary.addSummarySection(template);
		
		/** adds the event notification and timeline summary */
		template = addEventTimeline(template);

		try {
			/** adds the metrics in a timeseries form to the chart */
			addMetricTimeSeriesSection();
		}catch (Exception e) {
			logger.error("Error adding time series chart to report: " + e.getMessage());

			e.printStackTrace();
		}

		try {
			addMetricSummarySeriesSection();
		}catch (Exception e) {
			logger.error("Error adding summary series chart to report: " + e.getMessage());

			e.printStackTrace();
		}

		/** writes the time series table plot to out report */
		template = metricTablePlot.writeMetricsToTemplate(template, restHelper);
		
		logger.info("Writing test report (appdynamics-unit-test-report.html) to " + targetOutputPath);

		utils.writeReport(targetOutputPath, "appdynamics-unit-test-report.html", template);
	}

	private String addEventTimeline(String template) {
		/** report the this test as an event in appdynamics */
		TestSummaryEvent event = 
				new TestSummaryEvent(testStart, testStop, reportSummary);
		
		event.reportTestEvent();
		template = event.insertResponsiveTimeline(restHelper, template);
		
		return template;
	}

	/**
	 * attempts to write the morris donut chart to our report
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private void addMetricSummarySeriesSection() throws URISyntaxException, IOException {
		String morrisData = 
				utils.readFile(utils.getDestination(), "./js/morris-data-pie-chart-template.js");

		morrisData = metricSummaryPlot.writeMetricsToTemplate(morrisData);

		utils.writeReport(utils.getDestination(), "./js/morris-data-pie-chart.js", morrisData);		
	}

	/**
	 * Initialize the report generator, primarily attempts to recursively copy the report
	 * template from the {@link #templateSource} to the {@link #targetOutputPath}.
	 * 
	 * @throws Exception 
	 */
	public void initialize() throws Exception {

		utils.recursivelyCopy(templateSource, targetOutputPath);

		try {
			this.restHelper = new AppdRESTHelper();

			/** loads our properties so we can connect to appdynamics */
			restHelper.loadProperties();
		}
		catch (Exception e) {
			logger.error("Error initializing REST API, access to historical data will be disabled.");

			e.printStackTrace();
		}

		this.init = true;
	}

	/**
	 * Adds a metric to be summarized into the pie chart / plot.
	 * 
	 * @param metricTransaction
	 * @param metricStep1
	 * @param avg
	 */
	public void addMetricSummaryPlot(ResponseTimeMetric metric, METRIC_OPERATIONS operation) 
	{
		metricSummaryPlot.addSummaryMetric(metric, operation);
	}
	
	/**
	 * Adds a metric to be summarized into the pie chart / plot.
	 * 
	 * @param metricTransaction
	 * @param metricStep1
	 * @param avg
	 */
	public void addMetricTablePlot(ResponseTimeMetric metric, METRIC_OPERATIONS operation) 
	{
		metricTablePlot.addMetricToTimeSeries(metric, operation);
	}
}
