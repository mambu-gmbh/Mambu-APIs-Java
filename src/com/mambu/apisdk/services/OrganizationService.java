/**
 * 
 */
package com.mambu.apisdk.services;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
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

	private static String BRANCHES = APIData.BRANCHES;
	private static String CURRENCIES = APIData.CURRENCIES;
	private static String OFFSET = APIData.OFFSET;
	private static String LIMIT = APIData.LIMIT;
	private static String FULL_DETAILS = APIData.FULL_DETAILS;

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
	public Branch getBranch(String branchId) throws MambuApiException {

		// Replace spaces with url-encoding symbol "+". Spaces in IDs crash API wrappers
		branchId = branchId.trim().replace(" ", "+");

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(BRANCHES + "/" + branchId));

		ParamsMap params = new ParamsMap();

		params.put(FULL_DETAILS, "true");
		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Branch branch = (Branch) GsonUtils.createResponse().fromJson(jsonResponse, Branch.class);

		return branch;

	}
}
