package com.appdynamics.sdk;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.appdynamics.apm.appagent.api.ITransactionDemarcator;

public class TransactionReporter extends AppDynamicsSDKWrapper {

	private Hashtable<String, String> txNameIdentifierMap = new Hashtable<String, String> ();

	Logger logger = Logger.getLogger(TransactionReporter.class);

	public TransactionReporter() {
		super();
	}

	@Override
	protected void isInitialized() {
		setEnabled(getTransactionReporter() != null);
	}

	/**
	 * Starts a new AppDynamics business transaction, and returns true if successful 
	 * or false if not.  This API requires that the transaction will be started and stopped within
	 * the same thread.
	 * 
	 * @param txName the name of the business transaction
	 * 
	 * @return unique String identifier for the started BT, or NULL
	 */
	public String startBusinesTransaction(String txName) {

		if (txName == null || txName.trim().length() <= 0) {
			logger.error("Error starting business transaction, transaction name must be non-null and non-empty.");

			return null;
		}

		String txIdentifier = getTransactionIdentifier(txName);

		return startBusinessTransaction(txName, txIdentifier);

	}


	/**
	 * Starts a new AppDynamics business transaction, and returns true if successful 
	 * or false if not.  This API requires a an identifier that can be used to uniquely
	 * identify this instance of the transaction.  This API allows for transactions to be
	 * started / stopped across threads, providing the same unique identifier is used.  
	 * 
	 * @param txName the name of the business transaction, cannot be NULL.
	 * @param uniqueIdentifier an identifier that uniquely identifies this instance of the transaction.
	 *   Cannot be NULL 
	 * 
	 * @return unique String identifier for the started BT, or NULL
	 */
	public String startBusinessTransaction(String txName, String uniqueIdentifier) {
		ITransactionDemarcator reporter = getTransactionReporter();

		if (txName == null || txName.trim().length() <= 0) {
			logger.error("Error starting business transaction, transaction name must be non-null and non-empty.");

			return null;
		}
		else if (uniqueIdentifier == null || uniqueIdentifier.trim().length() <= 0) {
			logger.error("Error starting business transaction, transaction identifier must be non-null and non-empty.");

			return null;
		}

		/** should return a non-null tx identifier, which is the same as we've passed, on success */
		String result = reporter.beginOriginatingTransaction(txName, uniqueIdentifier);

		if (result == null) {
			logger.error("Error starting business transaction, AppDynamics SDK returned a null transaction identifier.");

			return null;
		}
		else {
			logger.debug("Successfully started new Business Transaction (" + 
					txName +"), unique Transaction Identifier = " + uniqueIdentifier);

			return uniqueIdentifier;
		}
	}

	private String getTransactionIdentifier(String txName) {
		String threadName = Thread.currentThread().getName();

		return threadName + "_" + txName;
	}

	/**
	 * Starts a new AppDynamics business transaction, and returns true if successful 
	 * or false if not.
	 * 
	 * @param txName the name of the business transaction
	 * 
	 * @return true (success) or false (failure)
	 */
	public boolean stopBusinesTransaction(String txName) {
		String txIdentifier = getTransactionIdentifier(txName);

		return stopBusinesTransaction(txName, txIdentifier);
	}

	/**
	 * Starts a new AppDynamics business transaction, and returns true if successful 
	 * or false if not.
	 * 
	 * @param txName the name of the business transaction
	 * 
	 * @return true (success) or false (failure)
	 */
	public boolean stopBusinesTransaction(String txName, String txIdentifier) {
		ITransactionDemarcator reporter = getTransactionReporter();

		if (txName == null || txIdentifier == null) {
			logger.error("Error stopping business transaction, transaction "
					+ "name and identifier must be non-null and non-empty.");
		}

		boolean b = reporter.endOriginatingTransaction(txIdentifier);

		if (logger.isDebugEnabled()) {
			logger.debug("Stopped BT (" + txIdentifier + "), Status == " + b);
		}
		return b;
	}

	/**
	 * 
	 *  Marks the beginning of an external service call uniquely identified by the 'service identifying name' parameter. 
	 *  The external call is associated with the business transaction executed by the current thread. This method starts
	 *  the timer for measuring the execution time of the external call.  Refer Sample code SyncClient. On server side 
	 *  use beginContinuingTransactionAndAddCurrentThread API to continue the transaction.
	 *
	 *	If the caller is not waiting for external call invocation to complete (e.g. has a semantics similar to JMS 
	 *  send(Message msg) api) the synchronous boolean should be set to false. 
	 *
	 *	@param invokedServiceIdentifyingName - the name uniquely identifying the service being called
	 *	@param invokedServiceDisplayName - the display name of the service being called
	 *	@param synchronous - if the caller is waiting for external call to respond before proceeding
	 *	
	 *  @return the string containing the appdynamics transaction header to be propagated
	 *  
	 *	@throws java.lang.IllegalArgumentException - if invokedServiceIdentifyingName is null
	 */
	public String reportTransactionBackend(
			String externalServiceUniqueIdentifier,
			String invokedServiceDisplayName,
			boolean synchronous
			) 
	{
		ITransactionDemarcator reporter = getTransactionReporter();

		return reporter.beginExternalCall(externalServiceUniqueIdentifier, invokedServiceDisplayName, synchronous);
	}
}
