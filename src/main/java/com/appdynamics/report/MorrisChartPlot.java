package com.appdynamics.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.appdynamics.appdrestapi.data.MetricData;
import org.appdynamics.appdrestapi.data.MetricDatas;
import org.appdynamics.appdrestapi.data.MetricValue;
import org.appdynamics.appdrestapi.data.MetricValues;
import org.json.simple.JSONArray;

import com.appdynamics.rest.AppdRESTHelper;
import com.appdynamics.sdk.Metric;
import com.appdynamics.sdk.MetricOperations.METRIC_OPERATIONS;
import com.appdynamics.sdk.Observation;

import edu.emory.mathcs.backport.java.util.Collections;

public class MorrisChartPlot {

	public class MetricDataSeries {
		private Metric metric = null;
		private METRIC_OPERATIONS operation = null;
		
		public MetricDataSeries(Metric metric, METRIC_OPERATIONS operation) {
			this.metric = metric;
			this.operation = operation;
		}

		public Metric getMetric() {
			return metric;
		}

		public METRIC_OPERATIONS getOperation() {
			return operation;
		}

		public void setMetric(Metric metric) {
			this.metric = metric;
		}

		public void setOperation(METRIC_OPERATIONS operation) {
			this.operation = operation;
		}
	}
	public static final long ONE_WEEK_MILLIS = 604800000;

	public static final long ONE_DAY_MILLIS = 86400000;
	
	public Logger logger = Logger.getLogger(getClass());
	
	private ArrayList<MetricDataSeries> series = new ArrayList<MetricDataSeries> ();
		
	
	/** we have to group our observations by date in order to format them for the morris plot */
	private Hashtable<Long, ArrayList<Observation>> observations = new Hashtable<Long, ArrayList<Observation>> ();
	
	public MorrisChartPlot() {

	}

	private void addMetricToObservations(Metric m, Date metricTime, Long metricValue, METRIC_OPERATIONS op) {
		Long millisTimeKey = metricTime.getTime();

		String metricName = getMetricName(m, op);

		/** create a new observation at this time */
		Observation observation = new Observation(
				metricValue, metricName, op, metricTime);

		if (!observations.containsKey(millisTimeKey)) {
			ArrayList<Observation> timeObservations = new ArrayList<Observation> ();

			/** add our list of observations to this time in the hash */
			observations.put(millisTimeKey, timeObservations);
		}

		/** add this observation to the correct time period */
		observations.get(millisTimeKey).add(observation);
	}

	/** adds a new metric to the time series that we can plot or report on */
	public void addMetricToTimeSeries(Metric metric, METRIC_OPERATIONS operation) {
		series.add(new MetricDataSeries(metric, operation));
	}

	protected String createJsonArrayFromObservations() {

		StringBuffer buffer = new StringBuffer ("[");

		if (observations == null || observations.size() == 0) {
			return new String ("[]");
		}

		List<Long> keyList = new ArrayList<Long> ();

		keyList.addAll(observations.keySet());

		/** sort our list of keys */
		Collections.sort(keyList);

		/** expected datetime format: 2012-02-24 15:00 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		boolean firstMetric = true;

		for (Long l : keyList) {
			ArrayList<Observation> pointInTimeObservations = observations.get(l);

			boolean addedTimePeriod = false;

			if (firstMetric) {
				buffer.append("\n{\n");
				firstMetric = false;
			}
			else {
				buffer.append("\n,{\n");
			}

			for (Observation o : pointInTimeObservations) {
				if (!addedTimePeriod) {

					String formattedTime = sdf.format(o.getTime());

					buffer.append("\tperiod: '" + formattedTime + "'\n");

					addedTimePeriod = true;
				}

				String sanitizedName = sanitizeMetricName(o.getMetricName());

				buffer.append(", " + sanitizedName + ": " + o.getValue() + "\n");
			}
			buffer.append("}\n");
		}
		buffer.append("]\n");
		return buffer.toString();
	}

	/** we'll get the last week of data, hard coded time frame for now */
	private MetricDatas getMetricData(AppdRESTHelper restHelper, String metricPath) {
		Date stop = new Date();
		Date start = new Date(stop.getTime() - ONE_DAY_MILLIS);

		/** get metric data for the last day, don't rollup the results */
		return restHelper.getMetricData(metricPath, start, stop, false);	
	}

	/**
	 * gets the metric history, asserts the rest helper is non-null && not disabled.
	 * 
	 * @param restHelper the helper to pull the data from appdynamics
	 * @param metric the metric we want to extract from the repository

	 * @return an arraylist of metric data 
	 */
	protected MetricDatas getMetricHistory(AppdRESTHelper restHelper, Metric m, METRIC_OPERATIONS op) {
		assert(restHelper != null && !restHelper.isDisabled());

		String metricPath = getMetricPath(restHelper, m, op);

		MetricDatas metrics = getMetricData(restHelper, metricPath);

		for (MetricData d : metrics.getMetric_data()) {
			logger.trace("Found Metric Data for (" + m.getMetricName() + ") \n" + d);
		}

		return metrics;
	}

	/** 
	 * Gets the metric labels which will go into the chart 
	 * 
	 * @return
	 */
	protected JSONArray getMetricLabels() {
		JSONArray labels = new JSONArray();

		for (MetricDataSeries ms : series) {

			/** includes logic to support getting the % metric name if needed */
			String metricName = getMetricName(ms.getMetric(), ms.getOperation());

			labels.add(sanitizeMetricName(metricName));
		}

		return labels;
	}

	protected String getMetricName(Metric m, METRIC_OPERATIONS op) {
		if (op.isPercentileMetric()) {
			return m.getPercentileMetricName((int) op.getQuantile());
		}
		else {
			return m.getMetricName();
		}
	}

	private String getMetricPath(AppdRESTHelper restHelper, Metric m, METRIC_OPERATIONS op) {

		/** 
		 * an example metric path:
		 * 	"Application Infrastructure Performance|JUnitTest|Custom Metrics|MetricReporter/Response_Time_ms"; 
		 */
		StringBuilder buf = new StringBuilder();

		String metricName = m.getMetricName();

		/** get the correct metric name, includes logic if this is a percentile metric */
		metricName = getMetricName(m, op);

		buf.append("Application Infrastructure Performance|");
		buf.append(restHelper.getTier());
		buf.append("|Custom Metrics|" + metricName);

		String path = buf.toString();

		logger.debug("Built metric path " + buf + " for metric " + m.getMetricName());

		return path;
	}

	private Date getMetricTime(long startTimeInMillis) {
		Calendar c = Calendar.getInstance();

		/** set the time, round to the nearest minute */
		c.setTimeInMillis(startTimeInMillis);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTime();
	}

	private Long getMetricValue(MetricValue mv, METRIC_OPERATIONS op) {
		/** by default we'll pick the average time */
		long value = mv.getValue();

		/** for percentil metrics, we use the avg value which is set above */
		if (!op.isPercentileMetric()) {
			switch (op) {
			case SUM: value = mv.getSum(); break;
			case MAX: value = mv.getMax(); break;
			case MIN: value = mv.getMin(); break;
			case AVG: value = mv.getValue(); break;
			case COUNT: value = mv.getCount(); break;
			default:
				break;
			}
		}

		return value;
	}

	public ArrayList<MetricDataSeries> getSeries() {
		return series;
	}

	/**
	 * Parse the metric history into a json array
	 * 
	 * @param metric the metric we're parsing / adding
	 * @param operation the operation we should apply to the metric
	 * @param series the metric history / data
	 * 
	 * @return a JSON Array that will be added to our tet report
	 */
	protected void parseMetricHistoryToObservations(Metric m, METRIC_OPERATIONS op, MetricDatas metricDatas) {

		/** iterate through the metric history, and add each observation to our list */
		for (MetricData d : metricDatas.getMetric_data()) {
			String frequency = d.getFrequency();

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

	protected String sanitizeMetricName(String name) {

		/** at least in morris charts, the escaped chars are unexpected */
		String s = name.replace("/", "_");

		return s;
	}

}
