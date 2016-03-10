package com.appdynamics.sdk;

import org.apache.log4j.Logger;

import com.appdynamics.apm.appagent.api.AgentDelegate;
import com.appdynamics.apm.appagent.api.IMetricAndEventReporter;
import com.appdynamics.apm.appagent.api.ITransactionDemarcator;

public abstract class AppDynamicsSDKWrapper {

	private Logger logger = Logger.getLogger(AppDynamicsSDKWrapper.class);

	private IMetricAndEventReporter metricReporter = null;
	private ITransactionDemarcator transactionReporter;
	
	/** is the agent delegate enabled? */
	boolean enabled = false;


	public AppDynamicsSDKWrapper() {
		metricReporter = AgentDelegate.getMetricAndEventPublisher();
		transactionReporter = AgentDelegate.getTransactionDemarcator();
		
		isInitialized();
	}

	protected IMetricAndEventReporter getMetricReporter() {
		return metricReporter;
	}

	public ITransactionDemarcator getTransactionReporter() {
		return transactionReporter;
	}

	protected boolean isEnabled() {
		return enabled;
	}
	
	/** 
	 * All child classes must determine if they fell the SDK Wrapper is initialized.  The
	 * expectation is they will call {@link #setEnabled(boolean)} to enable / disable the
	 * SDK based on their own init criteria.
	 */
	protected abstract void isInitialized();

	protected void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
}
