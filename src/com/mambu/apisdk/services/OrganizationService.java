/**
 * 
 */
package com.mambu.apisdk.services;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.core.shared.model.Currency;

import com.mambu.organization.shared.model.Branch;

/**
 * Service class which handles API operations available for the organizations like getting it's currency
 * 
 * @author ipenciuc
 * 
 */
public class OrganizationService {

	private static String BRANCHES = "branches";
	private static String CURRENCIES = "currencies";
	private static String OFFSET = "offset";
	private static String LIMIT = "limit";

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
		String urlString = new String(mambuAPIService.createUrl(CURRENCIES));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		// convert to collection
		Currency[] currencies = GsonUtils.createResponse().fromJson(jsonResponse, Currency[].class);

		if (currencies != null && currencies.length > 0) {
			return currencies[0];
		} else {
			return null;
		}
	}

	/**
	 * Get a paginated list of branches
	 * 
	 * @param offset
	 *            the offset of the response. If not set a value of 0 is used by default
	 * @param limit
	 *            the maximum number of response entries. If not set a value of 50 is used by default
	 * @return
	 * @throws MambuApiException
	 */

	public Branch[] getBranches(String offset, String limit) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(BRANCHES));
		String jsonResponse;
		ParamsMap params = new ParamsMap();

		params.put(OFFSET, offset);
		params.put(LIMIT, limit);

		jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Branch branches[] = (Branch[]) GsonUtils.createResponse().fromJson(jsonResponse, Branch[].class);

		return branches;
	}

	/**
	 * Requests a branch by their Mambu ID
	 * 
	 * @param branchId
	 * @return the Mambu branch model
	 * @throws MambuApiException
	 */
	// TODO: This API is not implemented YET- for no this call returns all branches
	public Branch getBranch(String branchId) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(BRANCHES + "/" + branchId));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		Branch branches[] = (Branch[]) GsonUtils.createResponse().fromJson(jsonResponse, Branch[].class);

		return branches[0];

	}
}
