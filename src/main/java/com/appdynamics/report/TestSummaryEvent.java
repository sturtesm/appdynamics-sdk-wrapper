package com.appdynamics.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.appdynamics.appdrestapi.data.Event;
import org.appdynamics.appdrestapi.data.Events;

import com.appdynamics.report.ReportSummary.TEST_STATUS;
import com.appdynamics.rest.AppdRESTHelper;
import com.appdynamics.sdk.EventReporter;
import com.appdynamics.sdk.EventReporter.EVENT_TYPE;
import com.appdynamics.utils.Pair;

public class TestSummaryEvent extends MorrisChartPlot {



	private final static String TIMELINE_EVENT_TEMPLATE = 
			"<li $timeline-class-inverted> "
					+ "<div class=\"timeline-badge $badge-status-color\"><i class=\"fa $fa-status-icon\"></i></div> "
					+ ""
					+ "<div class=\"timeline-panel\"> " 
					+ "	  <div class=\"timeline-heading\"> "
					+ "      <h4 class=\"timeline-title\">AppDynamics SDK Test Event</h4> "
					+ "      <p><small class=\"text-muted\"><i class=\"fa fa-clock-o\"></i>$timeline-date</small></p> "
					+ "   </div> "
					+ "   <div class=\"timeline-body\"> "
					+ "      <p>Link to build information - <a href=\"$timeline_event_link\">Build Details</a></p> "
					+ "   </div>"
					+ "</div> "
					+ "</li>";

	private Logger logger = Logger.getLogger(getClass());

	private static final String CUSTOM_EVENT_TYPE="SDK Test Event";
	private static final String START_TIME_KEY="Start Time";
	private static final String STOP_TIME_KEY="Stop Time";
	private static final String TEST_STATUS_KEY = "TEST_STATUS";

	private Date testStart = null;
	private Date testStop = null;

	private List<Pair<String, String>> 
	testProperties = new ArrayList<Pair<String, String>>();

	private ReportSummary testSummary;

	public TestSummaryEvent(Date start, Date stop, ReportSummary summary) {
		this.testStart = start;
		this.testStop = stop;
		this.testSummary = summary;
	}

	private Map<String, String> getEventProperties() {
		Map<String, String> properties = new LinkedHashMap<String, String> ();
		TEST_STATUS status = null;

		if (testSummary == null) {
			status = TEST_STATUS.NORMAL;
		}
		else {
			status = testSummary.getTestStatus();
		}

		properties.put(START_TIME_KEY + " (GMT)", testStart.toString());
		properties.put(STOP_TIME_KEY + " (GMT)", testStart.toString());
		properties.put(TEST_STATUS_KEY, status.toString());

		List<String> goalDetails = testSummary.getGoalStatusSummary();

		for (int i = 0; i < goalDetails.size(); i++) {
			properties.put("TEST_GOAL_" + i, goalDetails.get(i));
		}

		return properties;
	}

	/** 
	 * Report the status of the test as an event in appdynamics
	 */
	public void reportTestEvent() {
		EventReporter er = new EventReporter();

		Map<String, String> props = getEventProperties();

		EVENT_TYPE severity;
		
		switch (testSummary.getTestStatus()) {
		
		case WARNING: severity = EVENT_TYPE.WARNING; break;
		case CRITICAL: severity = EVENT_TYPE.CRITICAL; break;
		
		default: severity = EVENT_TYPE.INFO;
		}
		
		er.generateEvent(CUSTOM_EVENT_TYPE, props, severity);
	}


	/**
	 * Insert the responsive timeline into the 
	 * @param restHelper
	 * @param template
	 * @return
	 */
	public String insertResponsiveTimeline(AppdRESTHelper restHelper, String template) {

		Events filteredEvents = getTestHistory(restHelper);

		if (filteredEvents == null || filteredEvents.getEvents() == null || filteredEvents.getEvents().size() <= 0) {
			return template.replace("$responsive-timeline-events", "");
		}

		String timelineEvents = buildTimelineEventsList(filteredEvents);

		return template.replace("$responsive-timeline-events", timelineEvents);
	}

	private String buildTimelineEventsList(Events filteredEvents) {

		boolean doReverse = false;
		
		StringBuffer timelineEventList = new StringBuffer();

		for (Event e : filteredEvents.getEvents()) {
			String timelineClass = null;
			Date eventTime = new Date(e.getEventTime());
			String severity = e.getSeverity();
			String link = e.getDeepURL();

			if (doReverse) {
				timelineClass = "class=\"timeline-inverted\"";
			}
			else {
				timelineClass = "";
			}
			doReverse = !(doReverse);
			
			String severityIcon = ReportSummary.FA_CHECK;
			String badgeColor = "success";
			
			if (severity.compareTo("WARN") == 0) {
				severityIcon = ReportSummary.FA_WARNING;
				badgeColor = "warning";
			}
			else if (severity.compareTo("ERROR") == 0) {
				severityIcon = ReportSummary.FA_CRITICAL;
				badgeColor = "danger";
			}

			String eventTemplate = new String(TIMELINE_EVENT_TEMPLATE);
			
			eventTemplate = eventTemplate.replace("$badge-status-color", badgeColor); 
			eventTemplate = eventTemplate.replace("$timeline-class-inverted", timelineClass);
			eventTemplate = eventTemplate.replace("$fa-status-icon", severityIcon);
			eventTemplate = eventTemplate.replace("$timeline-date", eventTime.toString());
			eventTemplate = eventTemplate.replace("$timeline_event_link", link);
			
			timelineEventList.append(eventTemplate);
			timelineEventList.append("\n");
		}
		
		return timelineEventList.toString();
	}

	private Events getTestHistory(AppdRESTHelper restHelper) {
		Date eventStop = new Date();
		Date eventStart = new Date(eventStop.getTime() - ONE_WEEK_MILLIS);

		Events events = restHelper.getCustomEvents(eventStart, eventStop, "INFO,WARN,ERROR");
		Events filteredEvents = new Events();
		
		if (events == null) {
			return filteredEvents;
		}

		for (Event e : events.getEvents()) {
			if (e.getSummary().compareTo(CUSTOM_EVENT_TYPE) == 0) {
				filteredEvents.getEvents().add(e);

				logger.info("Found Test Event: " + e);
			}
			else {
				logger.trace("Found custom event " + e.getSummary() + ", passing on non-matching event");
			}

		}

		return filteredEvents;
	}
}
