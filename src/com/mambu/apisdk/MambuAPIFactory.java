package com.mambu.apisdk;

import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.Protocol;
import com.mambu.apisdk.services.AccountingService;
import com.mambu.apisdk.services.ActivitiesService;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.apisdk.services.CommentsService;
import com.mambu.apisdk.services.CustomFieldValueService;
import com.mambu.apisdk.services.CustomViewsService;
import com.mambu.apisdk.services.DatabaseService;
import com.mambu.apisdk.services.DocumentTemplatesService;
import com.mambu.apisdk.services.DocumentsService;
import com.mambu.apisdk.services.IntelligenceService;
import com.mambu.apisdk.services.LinesOfCreditService;
import com.mambu.apisdk.services.LoansService;
import com.mambu.apisdk.services.NotificationsService;
import com.mambu.apisdk.services.OrganizationService;
import com.mambu.apisdk.services.RepaymentsService;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.services.SearchService;
import com.mambu.apisdk.services.TasksService;
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

	// An 'Application Key' can be (optionally) set by some Applications (e.g. Mambu Android)
	// Mambu can handle API verification requests differently depending on the presence
	// of the valid Application Key
	private static String applicationKey = null;

	/**
	 * The defined error code for an invalid authentication. In this case this will be used if the user didn't set up
	 * the factory properly before getting a service
	 */
	private static Integer INVALID_BASIC_AUTHORIZATION = 1;

	/***
	 * Convenience method for setting up the Guice Module with data required for accessing the remote server. The
	 * application protocol that is used is HTTPS
	 * 
	 * @param domain
	 *            the domain where the server is found
	 * @param username
	 *            the name of the user
	 * @param password
	 *            the password used by the user
	 */
	public static void setUp(String domain, String username, String password) {

		injector = Guice.createInjector(new MambuAPIModule(Protocol.HTTPS, domain, username, password));
	}

	/***
	 * Set up the Guice Module with data required for accessing the remote server
	 * 
	 * @param protocol
	 *            the protocol used for communication
	 * @param domain
	 *            the domain where the server is found
	 * @param username
	 *            the name of the user
	 * @param password
	 *            the password used by the user
	 */
	public static void setUp(Protocol protocol, String domain, String username, String password) {

		injector = Guice.createInjector(new MambuAPIModule(protocol, domain, username, password));
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
	 * @throws MambuApiException
	 */
	public static UsersService getUsersService() throws MambuApiException {

		validateFactorySetUp();
		return injector.getInstance(UsersService.class);
	}

	/***
	 * Get an instance of the SearchService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public static SearchService getSearchService() throws MambuApiException {

		validateFactorySetUp();
		return injector.getInstance(SearchService.class);
	}

	/***
	 * Get an instance of the {@link TasksService} class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public static TasksService getTasksService() throws MambuApiException {

		validateFactorySetUp();
		return injector.getInstance(TasksService.class);
	}

	/***
	 * Get an instance of the {@link DocumentsService} class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public static DocumentsService getDocumentsService() throws MambuApiException {

		validateFactorySetUp();
		return injector.getInstance(DocumentsService.class);
	}

	/***
	 * Get an instance of the ActivitiesService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public static ActivitiesService getActivitiesService() throws MambuApiException {

		validateFactorySetUp();
		return injector.getInstance(ActivitiesService.class);
	}

	/***
	 * Get an instance of the CommentsService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public static CommentsService getCommentsService() throws MambuApiException {

		validateFactorySetUp();
		return injector.getInstance(CommentsService.class);
	}

	/***
	 * Get an instance of the LinesOfCreditService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public static LinesOfCreditService getLineOfCreditService() throws MambuApiException {

		validateFactorySetUp();
		return injector.getInstance(LinesOfCreditService.class);
	}

	/***
	 * Get an instance of the CustomFieldValueService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public static CustomFieldValueService getCustomFieldValueService() throws MambuApiException {

		validateFactorySetUp();
		return injector.getInstance(CustomFieldValueService.class);
	}

	/***
	 * Get an instance of the CustomViewsService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public static CustomViewsService getCustomViewsService() throws MambuApiException {

		validateFactorySetUp();
		return injector.getInstance(CustomViewsService.class);
	}

	/***
	 * Get an instance of the DocumentTemplatesService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public static DocumentTemplatesService getDocumentTemplatesService() throws MambuApiException {

		validateFactorySetUp();
		return injector.getInstance(DocumentTemplatesService.class);
	}

	/***
	 * Get an instance of the DatabaseService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public static DatabaseService getDatabaseService() throws MambuApiException {

		validateFactorySetUp();
		return injector.getInstance(DatabaseService.class);
	}
	
	/***
	 * Get an instance of the NotificationsService class
	 * 
	 * @return the obtained NotificationsService instance
	 * 
	 * @throws MambuApiException
	 */
	public static NotificationsService getNotificationsService() throws MambuApiException {

		validateFactorySetUp();
		return injector.getInstance(NotificationsService.class);
	}
	
	

	//
	/***
	 * Setter for an Application Key
	 * 
	 * @param appKey
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
