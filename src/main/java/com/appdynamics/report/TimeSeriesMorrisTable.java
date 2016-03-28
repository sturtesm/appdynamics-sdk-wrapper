package com.appdynamics.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.appdynamics.appdrestapi.data.MetricData;
import org.appdynamics.appdrestapi.data.MetricDatas;
import org.appdynamics.appdrestapi.data.MetricValue;
import org.appdynamics.appdrestapi.data.MetricValues;

import com.appdynamics.rest.AppdRESTHelper;
import com.appdynamics.sdk.Metric;
import com.appdynamics.sdk.MetricOperations.METRIC_OPERATIONS;
import com.appdynamics.sdk.Observation;

import edu.emory.mathcs.backport.java.util.Collections;

public class TimeSeriesMorrisTable extends MorrisChartPlot {

	
	
	
	protected String createMorrisTableFromObservations() {

		StringBuffer buffer = new StringBuffer ();

		if (getObservations() == null || getObservations().size() == 0) {
			return new String ("[]");
		}

		List<Long> keyList = new ArrayList<Long> ();

		keyList.addAll(getObservations().keySet());

		/** sort our list of keys */
		Collections.sort(keyList, new Comparator<Long> () {
			
			public int compare(Long o1, Long o2) {
				/** sort in descending order */
				return (-1 * o1.compareTo(o2));
			}
			
		});

		/** expected datetime format: 2012-02-24 15:00 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		/***
		 * <tr>
	        <td>3326</td>
	        <td>10/21/2013</td>
	        <td>3:29 PM</td>
	        <td>$321.33</td>
    	   </tr>
		 * 
		 * 
		 */
		for (Long l : keyList) {
			ArrayList<Observation> pointInTimeObservations = getObservations().get(l);
			
			buffer.append("<tr>\n");
			
			boolean addedTimePeriod = false;
			
			for (Observation o : pointInTimeObservations) {
				if (!addedTimePeriod) {

					String formattedTime = sdf.format(o.getTime());

					buffer.append("<td>" + formattedTime + "</td>\n");

					addedTimePeriod = true;
				}
				buffer.append("<td>" + o.getValue() + "</td>\n");
			}
			buffer.append("</tr>");
		}
		return buffer.toString();
	}

	private String createTableHeader() {
		/**
		 * <th>#</th>
	       <th>Date</th>
	       <th>Time</th>
	       <th>Amount</th>
	       <th>Test Column</th>
	       <th>Test Column #2</th>
		 */
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<th>Date</th>");
		
		for (MetricDataSeries metrics : getSeries()) {
			Metric m = metrics.getMetric();
			
			buffer.append("<th>");
			buffer.append(m.getPrettyName() + " (" + 
					metrics.getOperation().getDescription() + ")");
			buffer.append("</th>");
		}
		
		return buffer.toString();
	}
	
	/**
	 * Parse the metric history into a table
	 * 
	 * @param metric the metric we're parsing / adding
	 * @param operation the operation we should apply to the metric
	 * @param series the metric history / data
	 */
	protected void parseMetricHistoryToObservations(Metric m, METRIC_OPERATIONS op, MetricDatas metricDatas) {

		/** iterate through the metric history, and add each observation to our list */
		for (MetricData d : metricDatas.getMetric_data()) {

			/** create our historical observations */
			for (MetricValues mValues : d.getMetricValues()) {
				ArrayList<MetricValue> mList = mValues.getMetricValue();

				for (MetricValue mv : mList) {
					Date metricTime = getMetricTime(mv.getStartTimeInMillis());
					Long metricValue = getMetricValue(mv, op);

					addMetricToObservations(m, metricTime, metricValue, op);
				}
			}
		}

		/** now get the latest observation as it most likely has not been persisted yet in AppD */
		long metricValue = (long) m.getValue(op);
		Date metricTime = getMetricTime(new Date().getTime());

		addMetricToObservations(m, metricTime, metricValue, op);
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
			
			String tableHeader = createTableHeader();
			String tableBody = createMorrisTableFromObservations();
			
			updatedTemplate = updatedTemplate.replace("$metric_history_table_headers", tableHeader);
			updatedTemplate = updatedTemplate.replace("$metric_history_table_body", tableBody);
			
		}
		
		return updatedTemplate;
	}
}
