package com.appdynamics.report;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.appdynamics.report.ReportGenerator.SUMMARY_INDEX;

public class ReportSummary {
	
	public enum TEST_STATUS { NORMAL, WARNING, CRITICAL };
	
	public final static String FA_CHECK="fa-check-circle-o";
	public final static String FA_WARNING="fa-exclamation-triangle";
	public final static String FA_CRITICAL="fa-times-circle";
	
	public final static String BS_PANEL_CRITICAL="panel-red";
	public final static String BS_PANEL_WARNING="panel-yellow";
	public final static String BS_PANEL_GREEN="panel-green";

	private Hashtable<SUMMARY_INDEX, SummaryMetric> goalSummary = new Hashtable<SUMMARY_INDEX, SummaryMetric> ();

	private SummaryMetric notApplicableMetric = new SummaryMetric("N/A", "N/A", 0, 0, 100, 100);
	
	private class SummaryMetric {
		private String shortDescription = null;
		private String description = null;
		private double value = Double.NaN;
		private double goalValue = Double.NaN;
		private long percentWarnTolerance = 100;
		private long percentCriticalTolerance = 100;
		
		public SummaryMetric(String shortDescription, String description, 
				double value, double goalValue, long percentWarnTolerance, long percentCriticalTolerance)
		{
			this.shortDescription = shortDescription;
			this.description = description;
			this.value = value;
			this.goalValue = goalValue;
			this.percentWarnTolerance = percentWarnTolerance;
			this.percentCriticalTolerance = percentCriticalTolerance;
		}

		public String getShortDescription() {
			return shortDescription;
		}

		public void setShortDescription(String shortDescription) {
			this.shortDescription = shortDescription;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}

		public long getPercentWarnTolerance() {
			return percentWarnTolerance;
		}

		public void setPercentWarnTolerance(long percentWarnTolerance) {
			this.percentWarnTolerance = percentWarnTolerance;
		}

		public long getPercentCriticalTolerance() {
			return percentCriticalTolerance;
		}

		public void setPercentCriticalTolerance(long percentCriticalTolerance) {
			this.percentCriticalTolerance = percentCriticalTolerance;
		}

		public double getGoalValue() {
			return goalValue;
		}

		public void setGoalValue(double goalValue) {
			this.goalValue = goalValue;
		}
	}
	
	/**
	 * Add a summary goal to our report.
	 * 
	 * @param index
	 * @param shortDescription
	 * @param description
	 * @param value
	 * @param percentWarnTolerance
	 * @param percentCriticalTolerance
	 */
	public void addSummaryGoal(SUMMARY_INDEX index, 
			String shortDescription, String description, double value, double goalValue, long percentWarnTolerance, long percentCriticalTolerance)
	{
		SummaryMetric sm = new SummaryMetric(shortDescription, description, value, goalValue, percentWarnTolerance, percentCriticalTolerance);
		
		goalSummary.put(index, sm);
	}
	
	protected String addSummarySection(final String reportTemplate) {
		assert (reportTemplate != null);
		
		String updatedTemplate = reportTemplate;
		
		for (SUMMARY_INDEX index : SUMMARY_INDEX.values()) {
			updatedTemplate = addTestGoal(index, goalSummary.get(index), updatedTemplate);
		}
		
		return updatedTemplate;
	}

	private String addTestGoal(SUMMARY_INDEX index, SummaryMetric sm, String reportTemplate) {
		if (sm == null) {
			sm = notApplicableMetric;
		}
		
		String icon = "$fa_goal_" + index.getValue() + "_icon";
		String panel = "$goal_" + index.getValue() + "_panel_color";
		String summary = "$goal_" + index.getValue() + "_summary";
		String description = "$goal_" + index.getValue() + "_description";
		String actual = "$goal_" + index.getValue() + "_actual";
		
		String iconValue = getIconValue(sm);
		String panelValue = getPanelColor(sm);
		
		reportTemplate = reportTemplate.replace(icon, iconValue);
		reportTemplate = reportTemplate.replace(panel, panelValue);
		reportTemplate = reportTemplate.replace(summary, sm.getShortDescription());
		reportTemplate = reportTemplate.replace(description, sm.getDescription());
		reportTemplate = reportTemplate.replace(actual, "Test value is " + sm.getValue());
		
		return reportTemplate;
	}
	
	private String getPanelColor(SummaryMetric sm) {
		if (isCritical(sm)) {
			return BS_PANEL_CRITICAL;
		}
		else if (isWarning(sm)) {
			return BS_PANEL_WARNING;
		}
		else {
			return BS_PANEL_GREEN;
		}
	}

	private String getIconValue(SummaryMetric sm) {
		
		if (isCritical(sm)) {
			return FA_CRITICAL;
		}
		else if (isWarning(sm)) {
			return FA_WARNING;
		}
		else {
			return FA_CHECK;
		}
	}

	private boolean isCritical(SummaryMetric sm) {
		double value = sm.getValue();
		double goal = sm.getGoalValue();
		double allowance = (sm.getPercentCriticalTolerance() / 100.00d) * goal;
		
		return (allowance < Math.abs(value - goal));
	}
	
	private boolean isWarning(SummaryMetric sm) {
		double value = sm.getValue();
		double goal = sm.getGoalValue();
		double allowance = (sm.getPercentWarnTolerance() / 100.00d) * goal;
		
		return (allowance < Math.abs(value - goal));
	}
	
	/**
	 * Based on the goals set, return the state of the test.  The state of the test will be determined
	 * by the most critical state of the {@link #goalSummary} summary goals.
	 * 
	 * @return TEST_STATUS
	 */
	public TEST_STATUS getTestStatus() {
		TEST_STATUS status = TEST_STATUS.NORMAL;
		
		if (goalSummary == null || goalSummary.size() <= 0) {
			return status;
		}
		
		Iterator<SummaryMetric> c = goalSummary.values().iterator();
		
		while (c.hasNext()) {
			SummaryMetric sm = c.next();
			
			if (isWarning(sm)) {
				status = TEST_STATUS.WARNING;
			}
			if (isCritical(sm)) {
				return TEST_STATUS.CRITICAL;
			}
		}
				
		return status;
	}
	
	
	public List<String> getGoalStatusSummary() {
		 List<String> description = new ArrayList<String> ();
		 
		 if (goalSummary == null) {
			 return description;
		 }
		 
		 Iterator<SummaryMetric> i = goalSummary.values().iterator();
		 
		 while (i.hasNext()) {
			 SummaryMetric sm = i.next();
			 
			 getTestStatus(sm);
			 
			 String d = String.format("%s: %s", 
					 getTestStatus(sm).toString(), sm.getDescription());
			 
			 description.add(d);
		 }
		 
		 return description;
	}

	private TEST_STATUS getTestStatus(SummaryMetric sm) {
		if (isWarning(sm)) {
			return TEST_STATUS.WARNING;
		}
		else if (isCritical(sm)) {
			return TEST_STATUS.CRITICAL;
		}
		else {
			return TEST_STATUS.NORMAL;
		}
	}
}
