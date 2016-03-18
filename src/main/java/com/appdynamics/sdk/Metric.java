package com.appdynamics.sdk;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import com.appdynamics.sdk.MetricOperations.METRIC_OPERATIONS;

public class Metric {
	private static final String METRIC_PREFIX="SDK_METRIC";
	private String metricName = null;

	/** our unsorted observations */
	private ArrayList<Double> unsortedObservations = new ArrayList<Double> ();
	
	/** sum of the observations */
	private long sum = 0;
	
	/** the metric floor (min) / max */
	private long min = Long.MAX_VALUE;
	private long max = Long.MIN_VALUE;

	/** a sorted primited array */
	double[] sortedPrimitive;

	boolean unsorted = true;
	
	/** a description of the units */
	private String units = null;
	
	/** the reporter reports metrics to the SDK */
	MetricReporter reporter = null;

	/**
	 * Construct a new Metric for observations, the units is a descriptor only and will not
	 * result in any calculations or transformations.
	 * 
	 * @param name
	 * @param units
	 */
	public Metric(String name, String units) {
		this.metricName = METRIC_PREFIX + "/" + name;
		this.units = units;
		this.reporter = new MetricReporter();
	}

	/**
	 * adds a metric observation
	 * 
	 * @param observation
	 */
	public void addObservation(long observation) {
		unsortedObservations.add(new Double(observation));

		setMinAndMax(observation);
		setSum(observation);
		
		unsorted = true;
		
		reporter.reportMetricInstance(metricName, observation);
	}

	private void setSum(long observation) {
		sum += observation;
	}

	private void setMinAndMax(long observation) {
		min = (observation < min) ? observation : min;
		max = (observation > max) ? observation : max;
	}

	/**
	 * creates a primitive copy and sorts the data, will set {@link #unsorted} to false
	 */
	private void sortData() {
		Double[] d = unsortedObservations.toArray(new Double[0]);
		sortedPrimitive = ArrayUtils.toPrimitive(d);

		Arrays.sort(sortedPrimitive);

		unsorted = false;
	}

	private boolean isUnsorted() {
		return this.unsorted;
	}

	/**
	 * reports a percentile metric based on our observations added with {@link #addObservation(long)}
	 * 
	 * @param quantile the percentile metric to report
	 */
	public void reportQuantile(long quantile) {
		ArrayList<Long> quantiles = new ArrayList<Long> ();

		quantiles.add(quantile);

		reportQuantiles(quantiles);
	}

	/**
	 * If quantiles is non-null, then also reports a percentile metric for all quantiles.
	 * 
	 * @param quantiles list of percentile metrics to report for the observations
	 */
	public void reportQuantiles(ArrayList<Long> quantiles) {

		MetricReporter reporter = new MetricReporter();

		if (unsortedObservations == null || unsortedObservations.isEmpty()) {
			return;
		}

		if (quantiles != null && !quantiles.isEmpty()) {

			if (isUnsorted()) {
				sortData();
			}

			Percentile p = new Percentile();

			/** setting the sorted observations */
			p.setData(this.sortedPrimitive);

			for (Long d : quantiles) {
				Double stat = p.evaluate(d);
				String percentileMetricName = getPercentileMetricName(d.intValue(), false);

				reporter.reportMetricInstance(percentileMetricName, stat.longValue());
			}
		}		
	}

	/**
	 * gets the metric name for a percentile metric
	 * 
	 * @param quantile
	 * @return
	 */
	public String getPercentileMetricName(int quantile, boolean pretty) {
		if (pretty) {
			return getPrettyName() + "_" + quantile + "_pct";
		}
		else {
			return metricName +  "_" + quantile + "_percentile";
		}
	}

	/**
	 * get a percentile value of the observations to date, if the observations is empty
	 * then returns {#Double.Nan}
	 * 
	 * @param quantile
	 * @return
	 */
	public double getPercentileValue(double quantile) {

		if (unsortedObservations == null || unsortedObservations.isEmpty()) {
			return Double.NaN;
		}

		if (isUnsorted()) {
			sortData();
		}

		Percentile p = new Percentile();

		/** setting the sorted observations */
		p.setData(this.sortedPrimitive);

		return p.evaluate(quantile);
	}

	public String getMetricName() {
		return metricName;
	}
	
	public String getPrettyName() {
		String s = getMetricName();
		
		return s.replace(METRIC_PREFIX + "/", "");
	}
	
	/**
	 * get the avg metric value.
	 * 
	 * @return
	 */
	public long getAvg() {
		
		if (getCount() == 0) {
			return 0;
		}
		
		return getSum() / getCount();
	}
	
	/**
	 * get the number of observations we've made
	 * 
	 * @return
	 */
	public long getCount() {
		return unsortedObservations.size();
	}

	public long getSum() {
		return sum;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}
	
	public double getValue(METRIC_OPERATIONS operation) {
		switch (operation) {
		case AVG: return getAvg();
		case MIN: return getMin();
		case MAX: return getMax();
		case COUNT: return getCount();
		case SUM: return getSum();
		case PCT_90: return getPercentileValue(90);
		case PCT_95: return getPercentileValue(95);
		case PCT_99: return getPercentileValue(99);
		default: return Double.NaN;
		}
	}

	public String getUnits() {
		return (this.units == null) ? "" : this.units;
	}

	public void setUnits(String units) {
		this.units = units;
	}
}