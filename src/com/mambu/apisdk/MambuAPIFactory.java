package com.mambu.apisdk;

import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.AccountingService;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.apisdk.services.IntelligenceService;
import com.mambu.apisdk.services.LoansService;
import com.mambu.apisdk.services.OrganizationService;
import com.mambu.apisdk.services.RepaymentsService;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.services.UsersService;

/**
 * Factory for creating Mambu API Services in a singleton fashion.
 * 
 * @author edanilkis
 * 
 */
public class MambuAPIFactory {

	/***
	 * The Guice injector used for the creation of each service
	 */
	private static Injector injector;
	private final static Logger LOGGER = Logger.getLogger(MambuAPIFactory.class.getName());

	// An 'Application Key' can be optionally set by some Applications (e.g. Mambu Android)
	// Mambu may handle API requests differently depending on the Application Key
	private static String applicationKey = null;

	/**
	 * The defined error code for an invalid authentication. In this case this will be used if the user didn't set up
	 * the factory properly before getting a service
	 */
	private static Integer INVALID_BASIC_AUTHORIZATION = 1;

	/***
	 * Set up the Guice Module with data required for accessing the remote server
	 * 
	 * @param domain
	 *            the domain where the server is found
	 * @param username
	 *            the name of the user
	 * @param password
	 *            the password used by the user
	 */
	public static void setUp(String domain, String username, String password) {
		injector = Guice.createInjector(new MambuAPIModule(domain, username, password));
	}

	/***
	 * Throw a MambuAPIException if the injector is null, meaning the user didn't set up the factory
	 * 
	 * @throws MambuApiException
	 *             the thrown exception
	 */
	private static void validateFactorySetUp() throws MambuApiException {

		if (injector == null) {

			LOGGER.severe("validateFactorySetUp - Failed");
			throw new MambuApiException(INVALID_BASIC_AUTHORIZATION, "The factory wasn't set up properly!");
		}

	}
	/***
	 * Get an instance of the ClientService class
	 * 
	 * @return the obtained instance
	 * @throws MambuApiException
	 */
	public static ClientsService getClientService() throws MambuApiException {
		validateFactorySetUp();
		return injector.getInstance(ClientsService.class);
	}

	/***
	 * Get an instance of the LoanService class
	 * 
	 * @return the obtained instance
	 * @throws MambuApiException
	 */
	public static LoansService getLoanService() throws MambuApiException {
		validateFactorySetUp();
		return injector.getInstance(LoansService.class);
	}

	/***
	 * Get an instance of the SavingsService class
	 * 
	 * @return the obtained instance
	 * @throws MambuApiException
	 */
	public static SavingsService getSavingsService() throws MambuApiException {
		validateFactorySetUp();
		return injector.getInstance(SavingsService.class);
	}

	/***
	 * Get an instance of the IntelligenceService class
	 * 
	 * @return the obtained instance
	 * @throws MambuApiException
	 */
	public static IntelligenceService getIntelligenceService() throws MambuApiException {
		validateFactorySetUp();
		return injector.getInstance(IntelligenceService.class);
	}

	/***
	 * Get an instance of the RepaymentsService class
	 * 
	 * @return the obtained instance
	 * @throws MambuApiException
	 */
	public static RepaymentsService getRepaymentsService() throws MambuApiException {
		validateFactorySetUp();
		return injector.getInstance(RepaymentsService.class);
	}

	/***
	 * Get an instance of the OrganizationService class
	 * 
	 * @return the obtained instance
	 * @throws MambuApiException
	 */
	public static OrganizationService getOrganizationService() throws MambuApiException {
		validateFactorySetUp();
		return injector.getInstance(OrganizationService.class);
	}

	/***
	 * Get an instance of the AccountingService class
	 * 
	 * @return the obtained instance
	 * @throws MambuApiException
	 */
	public static AccountingService getAccountingService() throws MambuApiException {
		validateFactorySetUp();
		return injector.getInstance(AccountingService.class);
	}

	/***
	 * Get an instance of the UsersService class
	 * 
	 * @return the obtained instance
	 * @throws MambuApiException
	 */
	public static UsersService getUsersService() throws MambuApiException {
		validateFactorySetUp();
		return injector.getInstance(UsersService.class);
	}
	/***
	 * Setter for an Application Key
	 * 
	 * @param applicationKey
	 * @return void
	 */
	public static void setApplicationKey(String appKey) {

		applicationKey = appKey;
	}
	/***
	 * Getter for an Application Key
	 * 
	 * @return String
	 */
	public static String getApplicationKey() {

		return applicationKey;
	}
}
