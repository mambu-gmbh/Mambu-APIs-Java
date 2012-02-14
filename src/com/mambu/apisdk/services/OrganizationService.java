/**
 * 
 */
package com.mambu.apisdk.services;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.core.shared.model.Currency;

/**
 * Service class which handles API operations available for the organizations like getting it's currency
 * 
 * @author ipenciuc
 * 
 */
public class OrganizationService {

	private MambuAPIService mambuAPIService;

	/***
	 * Create a new organization service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public OrganizationService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/**
	 * Requests the organization currency
	 * 
	 * @return the Mambu gl account
	 * @throws MambuApiException
	 */
	public Currency getCurrency() throws MambuApiException {

		// create the api call
		String url = "currencies";
		String urlString = new String(mambuAPIService.createUrl(url));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		// convert to collection
		Currency[] currencies = GsonUtils.createResponse().fromJson(jsonResponse, Currency[].class);

		if (currencies != null && currencies.length > 0) {
			return currencies[0];
		} else {
			return null;
		}

	}
}
