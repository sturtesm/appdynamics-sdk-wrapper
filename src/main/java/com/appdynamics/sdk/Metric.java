package com.appdynamics.sdk;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class Metric {
	String metricName = null;

	/** our unsorted observations */
	ArrayList<Double> unsortedObservations = new ArrayList<Double> ();

	/** a sorted primited array */
	double[] sortedPrimitive;

	boolean unsorted = true;

	public Metric(String name) {
		this.metricName = name;
	}

	/**
	 * adds a metric observation
	 * 
	 * @param observation
	 */
	public void addObservation(long observation) {
		unsortedObservations.add(new Double(observation));

		unsorted = true;
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
	 * report the metrics we've observed to date, will result in a SUM, AVG, MIN, MAX and COUNT
	 * for the metric reported.  If quantiles is non-null, then also reports a percentile metric
	 * for all quantiles.
	 * 
	 */
	public void report() {
		MetricReporter reporter = new MetricReporter();

		if (unsortedObservations == null || unsortedObservations.isEmpty()) {
			return;
		}

		for (Double l : unsortedObservations) {
			reporter.reportMetricInstance(metricName, l.longValue());
		}
	}

	/**
	 * report the metrics we've observed to date, will result in a SUM, AVG, MIN, MAX and COUNT
	 * for the metric reported.  If quantiles is non-null, then also reports a percentile metric
	 * for all quantiles.
	 * 
	 * @param quantiles list of percentile metrics to report for the observations, or NULL and no
	 * percentiles will be reported. Quantiles should be non-floating point.
	 */
	public void evaluateAndReport(ArrayList<Long> quantiles) {
		/** report the metric instances */
		report();

		reportQuantiles(quantiles);
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
				String percentileMetricName = metricName +  "_" + d.intValue() + "_percentile";

				reporter.reportMetricInstance(percentileMetricName, stat.longValue());
			}
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
}