package com.appdynamics.rest;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.appdynamics.appdrestapi.RESTAccess;
import org.appdynamics.appdrestapi.data.Events;
import org.appdynamics.appdrestapi.data.MetricDatas;

public class AppdRESTHelper {
	Logger logger = Logger.getLogger(getClass());

	Properties appDynamicsProperties = new Properties();

	private String hostName = null;
	private String port = null;
	private String username = null;
	private String password = null;
	private String application = null;
	private String tier = null;
	private Boolean useSSL = false;

	private boolean isInit = false;
	private boolean disabled = false;

	private RESTAccess restAccess = null;

	public AppdRESTHelper() {

	}

	public String getApplication() {
		return application;
	}

	
	/** 
	 * returns a list of all the custom events with the specified severity between
	 * the time frames provided.
	 * 
	 * @param startTime
	 * @param stopTime
	 * @param severity
	 * 
	 * @return
	 */
	public Events getCustomEvents(Date startTime, Date stopTime, String severity) {
		/** no-op if already initialized */
		initializeRestHelper();
		
		if (! disabled ) {
			return restAccess.getEvents(application, "CUSTOM", severity, 
					startTime.getTime(), stopTime.getTime());
		}
		else {
			return null;
		}
	}
	
	/**
	 * get the metric data between the time frames
	 * 
	 * @param metricPath
	 * @param start
	 * @param stop
	 * @param rollup
	 * 
	 * @return
	 */
	public MetricDatas getMetricData(String metricPath, Date start, Date stop, boolean rollup) {
		if (!disabled) {
			return restAccess.getRESTMetricQueryCustom(
				application, metricPath, start.getTime(), stop.getTime(), rollup);
		}
		else {
			return null;
		}
	}

	public String getTier() {
		return tier;
	}

	public void initializeRestHelper() {

		if (!isInit && !disabled) {
			try {
				
				restAccess = new RESTAccess(hostName, port, useSSL, username, password);
			}
			catch (Exception e) {
				logger.error("Error initialzing REST Helper, historical data access is disabled.");
				e.printStackTrace();
				
				isInit = false;
				disabled = true;
			}
		}
	}

	public boolean isDisabled() {
		return disabled;
	}

	public boolean isInit() {
		return isInit;
	}

	public void loadProperties() {
		try {
			final InputStream stream =
					ClassLoader.getSystemResourceAsStream("appdynamics.properties");

			appDynamicsProperties.load(stream);
			stream.close();

			/**
			 * appdynamics.rest.host="127.0.0.1"
			 * appdynamics.rest.port="8090"
			 * appdynamics.rest.userName="admin"
			 * appdynamics.rest.password="admin"
			 * appdynamics.rest.application="JavaUnitTest"
			 */
			hostName = loadProperty("appdynamics.rest.host", true);
			port = loadProperty("appdynamics.rest.port", true);
			username = loadProperty("appdynamics.rest.username", true);
			password = loadProperty("appdynamics.rest.password", true);
			application = loadProperty("appdynamics.rest.application", true);
			tier = loadProperty("appdynamics.rest.tier", true);
			useSSL = new Boolean(loadProperty("appdynamics.rest.ssl", true));

		}catch (Exception e) {
			logger.error("Error loading appdynamics properties, REST access to historical data will be disabled.");

			e.printStackTrace();
		}
	}

	/**
	 * Read and return a property from our Properties object
	 * 
	 * @param propertyName
	 * @param throwExceptionIfMissing
	 * @return the property value
	 * 
	 * @throws Exception 
	 */
	private String loadProperty(String propertyName, boolean throwExceptionIfMissing) throws Exception {
		String p = appDynamicsProperties.getProperty(propertyName);

		if (p == null && throwExceptionIfMissing) {
			throw new Exception ("Error finding property " + propertyName + " unable to initialize REST Access to Historical data");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Loaded property: " + propertyName + "=" + p);
		}

		return p;
	}
}
