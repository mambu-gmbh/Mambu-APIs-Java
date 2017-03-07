package com.mambu.apisdk;

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
 * Factory for creating Mambu API Service objects that have fixed a tenant's Mambu credentials
 */
public final class MambuAPIServiceFactory {

	/***
	 * The Guice injector used for the creation of each service, hard-linked to a tenant's Mambu credentials
	 */
	private final Injector injector;

	/*
	 * hidden constructor to force using the getFactory() method
	 */
	private MambuAPIServiceFactory(Injector injector) {
		this.injector = injector;
	}

	/***
	 * Convenience method for setting up the Guice Module with data required for accessing the remote server, returning
	 * a factory object to retrieve Mambu API services that have Mambu credentials built-in. The application protocol
	 * that is used is HTTPS
	 * 
	 * @param domain
	 *            the domain where the server is found
	 * @param username
	 *            the name of the user
	 * @param password
	 *            the password used by the user
	 * 
	 * @return factory object to create API service objects which are bound to the given credentials
	 */
	public static MambuAPIServiceFactory getFactory(String domain, String username, String password) {

		Injector injector = Guice.createInjector(new MambuAPIModule(Protocol.HTTPS, domain, username, password));
		return new MambuAPIServiceFactory(injector);
	}

	/***
	 * Set up the Guice Module with data required for accessing the remote server, returning a factory object to
	 * retrieve Mambu API services that have Mambu credentials built-in
	 * 
	 * @param protocol
	 *            the protocol used for communication
	 * @param domain
	 *            the domain where the server is found
	 * @param username
	 *            the name of the user
	 * @param password
	 *            the password used by the user
	 * 
	 * @return factory object to create API service objects which are bound to the given credentials
	 */
	public static MambuAPIServiceFactory getFactory(Protocol protocol, String domain, String username,
			String password) {

		Injector injector = Guice.createInjector(new MambuAPIModule(protocol, domain, username, password));
		return new MambuAPIServiceFactory(injector);
	}

	/***
	 * Get an instance of the ClientService class, non-static version of {@link MambuAPIFactory#getClientService()}
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public ClientsService getClientService() throws MambuApiException {

		return injector.getInstance(ClientsService.class);
	}

	/***
	 * Get an instance of the LoanService class, non-static version of {@link MambuAPIFactory#getLoanService()}
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public LoansService getLoanService() throws MambuApiException {

		return injector.getInstance(LoansService.class);
	}

	/***
	 * Get an instance of the SavingsService class, non-static version of {@link MambuAPIFactory#getSavingsService()}
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public SavingsService getSavingsService() throws MambuApiException {

		return injector.getInstance(SavingsService.class);
	}

	/***
	 * Get an instance of the IntelligenceService class, non-static version of
	 * {@link MambuAPIFactory#getIntelligenceService()}
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public IntelligenceService getIntelligenceService() throws MambuApiException {

		return injector.getInstance(IntelligenceService.class);
	}

	/***
	 * Get an instance of the RepaymentsService class, non-static version of
	 * {@link MambuAPIFactory#getRepaymentsService()}
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public RepaymentsService getRepaymentsService() throws MambuApiException {

		return injector.getInstance(RepaymentsService.class);
	}

	/***
	 * Get an instance of the OrganizationService class, non-static version of
	 * {@link MambuAPIFactory#getOrganizationService()}
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public OrganizationService getOrganizationService() throws MambuApiException {

		return injector.getInstance(OrganizationService.class);
	}

	/***
	 * Get an instance of the AccountingService class, non-static version of
	 * {@link MambuAPIFactory#getAccountingService()}
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public AccountingService getAccountingService() throws MambuApiException {

		return injector.getInstance(AccountingService.class);
	}

	/***
	 * Get an instance of the UsersService class, non-static version of {@link MambuAPIFactory#getUsersService()}
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public UsersService getUsersService() throws MambuApiException {

		return injector.getInstance(UsersService.class);
	}

	/***
	 * Get an instance of the SearchService class, non-static version of {@link MambuAPIFactory#getSearchService()}
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public SearchService getSearchService() throws MambuApiException {

		return injector.getInstance(SearchService.class);
	}

	/***
	 * Get an instance of the {@link TasksService} class, non-static version of
	 * {@link MambuAPIFactory#getTasksService()}
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public TasksService getTasksService() throws MambuApiException {

		return injector.getInstance(TasksService.class);
	}

	/***
	 * Get an instance of the {@link DocumentsService} class, non-static version of
	 * {@link MambuAPIFactory#getDocumentsService()}
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public DocumentsService getDocumentsService() throws MambuApiException {

		return injector.getInstance(DocumentsService.class);
	}

	/***
	 * Get an instance of the ActivitiesService class, non-static version of
	 * {@link MambuAPIFactory#getActivitiesService()}
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public ActivitiesService getActivitiesService() throws MambuApiException {

		return injector.getInstance(ActivitiesService.class);
	}

	/***
	 * Get an instance of the CommentsService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public CommentsService getCommentsService() throws MambuApiException {

		return injector.getInstance(CommentsService.class);
	}

	/***
	 * Get an instance of the LinesOfCreditService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public LinesOfCreditService getLineOfCreditService() throws MambuApiException {

		return injector.getInstance(LinesOfCreditService.class);
	}

	/***
	 * Get an instance of the CustomFieldValueService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public CustomFieldValueService getCustomFieldValueService() throws MambuApiException {

		return injector.getInstance(CustomFieldValueService.class);
	}

	/***
	 * Get an instance of the CustomViewsService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public CustomViewsService getCustomViewsService() throws MambuApiException {

		return injector.getInstance(CustomViewsService.class);
	}

	/***
	 * Get an instance of the DocumentTemplatesService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public DocumentTemplatesService getDocumentTemplatesService() throws MambuApiException {

		return injector.getInstance(DocumentTemplatesService.class);
	}

	/***
	 * Get an instance of the DatabaseService class
	 * 
	 * @return the obtained instance
	 * 
	 * @throws MambuApiException
	 */
	public DatabaseService getDatabaseService() throws MambuApiException {

		return injector.getInstance(DatabaseService.class);
	}
	
	/***
	 * Get an instance of the NotificationsService class
	 * 
	 * @return newly obtained instance of NotificationsService
	 * 
	 * @throws MambuApiException
	 */
	public NotificationsService getNotificationsService() throws MambuApiException {

		return injector.getInstance(NotificationsService.class);
	}

}
