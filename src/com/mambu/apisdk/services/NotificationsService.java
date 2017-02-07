package com.mambu.apisdk.services;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.NotificationsToBeResent;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.ServiceExecutor;

/**
 * Service class which handles /notification API operations. See full
 * Mambu Notifications API documentation at: https://developer.mambu.com/customer/en/portal/articles/2240968-notifications-api?b_id=874
 * 
 * @author acostros
 *
 */
@Singleton
public class NotificationsService {
	
	private static final String RESEND_ACTION = "resend";
	
	private ServiceExecutor serviceExecutor;
	
	private static final ApiDefinition resendNotifications;
	static {
		resendNotifications = new ApiDefinition(ApiType.CREATE_JSON_ENTITY, NotificationsToBeResent.class);
		//this endpoint returns boolean in case of success
		resendNotifications.setApiReturnFormat(ApiReturnFormat.BOOLEAN);
	}
	
	/**
	 * Create a new notifications service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public NotificationsService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}
	
	/**
	 * Resends the failed messages.
	 * 
	 * Available since 4.5 Call sample: POST { "action":"resend", "identifiers":[ "8a80808a5317de22015317de5b94034c",
	 * "8a80806852f38c860152f38de0ad0019"]} /api/notifications/messages
	 * 
	 * @param identifiers
	 *            a list of identifiers (i.e encodedKeys) of the notifications that need to be resent. Only unique
	 *            encoded keys can be specified within the list.
	 * @return true if all the messages get successfully resent and false otherwise
	 * @throws MambuApiException
	 */
	public boolean resendFailedNotifications(List<String> identifiers) throws MambuApiException{
		
		NotificationsToBeResent notificationsToBeResent = new NotificationsToBeResent(RESEND_ACTION, identifiers);
		
		return serviceExecutor.executeJson(resendNotifications, notificationsToBeResent);
	}

}
