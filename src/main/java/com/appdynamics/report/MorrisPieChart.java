package com.appdynamics.report;

import java.util.ArrayList;
import java.util.List;

import com.appdynamics.sdk.Metric;
import com.appdynamics.sdk.MetricOperations.METRIC_OPERATIONS;

public class MorrisPieChart extends MorrisChartPlot {

	private List<MetricDataSeries> summaryMetrics = new ArrayList<MetricDataSeries> ();
	
	public MorrisPieChart() {
		
	}
	
	public void addSummaryMetric(Metric m, METRIC_OPERATIONS op) {
		MetricDataSeries ds = new MetricDataSeries(m, op);
		
		summaryMetrics.add(ds);
	}
	
	public String writeMetricsToTemplate(String templateContent) {
		
		String data = getJSONData();
		
		return templateContent.replace("$morris-donut-chart-data", data);
	}
	
	protected String getJSONData() {

		StringBuffer buffer = new StringBuffer ("[");

		if (summaryMetrics == null || summaryMetrics.size() == 0) {
			return new String ("[]");
		}

		boolean firstMetric = true;
		
		for (MetricDataSeries mds : summaryMetrics) {
			
			if (firstMetric) {
				buffer.append("\n{\n");
				firstMetric = false;
			}
			else {
				buffer.append("\n,{\n");
			}

			String name = super.getMetricName(mds.getMetric(), mds.getOperation());
			long value = (long) mds.getMetric().getValue(mds.getOperation());
			
			buffer.append(String.format("label : \"%s\",\n", sanitizeMetricName(name)));
			buffer.append(String.format("value : %s}", value));
		}
		buffer.append("]\n");
		return buffer.toString();
	}
}
