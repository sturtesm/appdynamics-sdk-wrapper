package com.appdynamics.sdk;

public class MetricOperations {

	public enum METRIC_OPERATIONS {
		AVG("avg"), MIN("min"), MAX("max"), SUM("sum"), COUNT("count"), MEDIAN("median"), 
		PCT_90("90th percentile", 90), PCT_95("95th percentile", 95), PCT_99("99th percentile", 99);
		
		private String description = null;
		private double quantile = Double.NaN;
		private boolean isPercentileMetric = false;
		
		METRIC_OPERATIONS(String description) {
			this.description = description;
		}
		METRIC_OPERATIONS(String description, double quantile) {
			this.description = description;
			this.quantile = quantile;
			this.isPercentileMetric = true;
			
		}
		public String getDescription() {
			return description;
		}
		public double getQuantile() {
			return quantile;
		}
		public boolean isPercentileMetric() {
			return isPercentileMetric;
		}
	};
}
