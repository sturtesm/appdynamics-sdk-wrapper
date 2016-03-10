package com.appdynamics.sdk;

import java.util.Map;

import org.apache.log4j.Logger;

import com.appdynamics.apm.appagent.api.IMetricAndEventReporter;

public class EventReporter extends AppDynamicsSDKWrapper {
	
	public enum EVENT_TYPE  {
			INFO, WARNING, CRITICAL;
	};

	private Logger logger = Logger.getLogger(EventReporter.class);

	public EventReporter() {
		super();
	}

	/** 
	 * Generate an error using the AppDynamics agent
	 * 
	 * @param brief summary of the event
	 * @param map of <key (string), value (string)> details describing attributes of the event
	 * @param is the event critical, or 
	 */
	public void generateEvent(String eventSummary, Map<String, String> details, EVENT_TYPE type) {

		if (isEnabled()) {
			
			logger.debug("Publishing new AppDynamics Event, (" + type.toString() + "): " + eventSummary);

			
			IMetricAndEventReporter reporter = getMetricReporter();
			
			switch (type) {
			case INFO: 
				reporter.publishInfoEvent(eventSummary, details); break;
			case WARNING:
				reporter.publishErrorEvent(eventSummary, details, false); break;
			case CRITICAL:
				reporter.publishErrorEvent(eventSummary, details, true); break;
			}
		}
	}

	@Override
	protected void isInitialized() {
		setEnabled(getMetricReporter() != null);
	}
}
