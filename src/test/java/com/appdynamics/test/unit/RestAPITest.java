package com.appdynamics.test.unit;

import java.util.Date;

import org.appdynamics.appdrestapi.RESTAccess;
import org.appdynamics.appdrestapi.data.Application;
import org.appdynamics.appdrestapi.data.Applications;
import org.appdynamics.appdrestapi.data.Event;
import org.appdynamics.appdrestapi.data.Events;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

public class RestAPITest {
	Logger logger = Logger.getLogger(getClass());
	RESTAccess access = null;

	@BeforeTest
	public void setup() {
		access = new RESTAccess("127.0.0.1", "8090", false, "admin", "admin");
	}

	@Test 
	public void getApplications() {
		assert (access != null);

		Applications apps = access.getApplications();

		for (Application a : apps.getApplications()) {
			System.out.println("Found application:\n " + a);
			logger.info("Found Application: " + a);
		}
	}

	@Test 
	public void getEvents() {
		assert (access != null);

		Events events = access.getEvents("82", "CUSTOM", "INFO,WARN,ERROR", 0, new Date().getTime());

		for (Event e : events.getEvents()) {
			System.out.println("Found event:\n " + e);
			logger.info("Found Event: " + e);
		}
	}
}
