package com.appdynamics.report;

import org.apache.log4j.Logger;
import org.appdynamics.appdrestapi.data.MetricDatas;
import org.json.simple.JSONArray;

import com.appdynamics.rest.AppdRESTHelper;
import com.appdynamics.sdk.Metric;
import com.appdynamics.sdk.MetricOperations.METRIC_OPERATIONS;

public class TimeSeriesMetricPlot extends MorrisChartPlot {
	
	private Logger logger = Logger.getLogger(getClass());

	public TimeSeriesMetricPlot() {
		
	}

	/**
	 * Write the metrics to the template content, if there is an error NULL will be returned.
	 * 
	 * @param templateContent
	 * @param restHelper
	 * @return
	 */
	public String writeMetricsToTemplate(String templateContent, AppdRESTHelper restHelper) {
		if (restHelper == null) {
			restHelper = new AppdRESTHelper();
		}
		
		/** get a pointer to the content, we'll update this */
		String updatedTemplate = templateContent;
		
		/** no-op if it's already init'd */
		restHelper.initializeRestHelper();
		
		if (!restHelper.isDisabled()) {
			for (MetricDataSeries metrics : getSeries()) {
				Metric m = metrics.getMetric();
				METRIC_OPERATIONS op = metrics.getOperation();
				
				/** the history will come from appdynamics */
				MetricDatas history = getMetricHistory(restHelper, m, op);
				
				/** take the metric history, and convert it to observations */
				parseMetricHistoryToObservations(m, op, history);
			}
			
			String metrics = createJsonArrayFromObservations();
			JSONArray metricLabels = getMetricLabels();
			
			updatedTemplate = updatedTemplate.replace("$morris_area_data_placeholder", metrics);
			updatedTemplate = updatedTemplate.replace("$morris_area_ykeys_placeholder", metricLabels.toJSONString());
			
			logger.trace("Just created the following JSon "
					+ "from our metrics history: \n" + metrics);
		}
		
		return updatedTemplate;
	}
}
