/**
 * 
 */
package com.mambu.apisdk.services;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.core.shared.model.Currency;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;

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
	private static String CUSTOM_FIELDS = APIData.CUSTOM_FIELDS;
	private static String CUSTOM_FIELD_SETS = APIData.CUSTOM_FIELD_SETS;

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
		Currency[] currencies = GsonUtils.createGson().fromJson(jsonResponse, Currency[].class);

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

		Branch branches[] = (Branch[]) GsonUtils.createGson().fromJson(jsonResponse, Branch[].class);

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

		Branch branch = (Branch) GsonUtils.createGson().fromJson(jsonResponse, Branch.class);

		return branch;

	}

	/** Centres **/
	/**
	 * Requests a centre by their Mambu ID
	 * 
	 * @param centreId
	 * @return the Mambu centre model
	 * @throws MambuApiException
	 */
	public Centre getCentre(String centreId) throws MambuApiException {

		// Replace spaces with url-encoding symbol "+". Spaces in IDs crash API wrappers
		centreId = centreId.trim().replace(" ", "+");

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(APIData.CENTRES + "/" + centreId));

		ParamsMap params = new ParamsMap();

		params.put(FULL_DETAILS, "true");
		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Centre centre = (Centre) GsonUtils.createGson().fromJson(jsonResponse, Centre.class);

		return centre;

	}

	/**
	 * Get paginated list of centres
	 * 
	 * @param branchID
	 *            Centers for the specified branch are returned. If NULL, all centres are searched
	 * 
	 * @param offset
	 *            the offset of the response. If not set a value of 0 is used by default
	 * @param limit
	 *            the maximum number of response entries. If not set a value of 50 is used by default
	 * @return an array of Centres
	 * 
	 * @throws MambuApiException
	 */

	public Centre[] getCentres(String branchId, String offset, String limit) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(APIData.CENTRES));
		String jsonResponse;
		ParamsMap params = new ParamsMap();

		params.addParam(APIData.BRANCH_ID, branchId); // if null, all centres are searched
		params.put(OFFSET, offset);
		params.put(LIMIT, limit);

		jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Centre centres[] = (Centre[]) GsonUtils.createGson().fromJson(jsonResponse, Centre[].class);

		return centres;
	}

	// Custom Fields and Custom Field Sets
	/**
	 * Get CustomFieldValue object details by Custom Field ID
	 * 
	 * @param fieldId
	 *            The id of the required CustomFieldValue
	 * 
	 * @return CustomFieldValue
	 * 
	 * @throws MambuApiException
	 */
	// TODO: to be tested with Mambu 3.3, see MBU-2486
	public CustomFieldValue getCustomField(String fieldId) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CUSTOM_FIELDS + "/" + fieldId));

		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET, ContentType.JSON);

		CustomFieldValue caustomFiledValue = (CustomFieldValue) GsonUtils.createGson().fromJson(jsonResponse,
				CustomFieldValue.class);

		return caustomFiledValue;
	}
	/**
	 * Get Custom Field Sets
	 * 
	 * @param customFieldType
	 *            The type of the required CustomField Set. Example CLIENT_INFO, GROUP_INFO, LOAN_ACCOUNT_INFO,
	 *            SAVINGS_ACCOUNT_INFO, BRANCH_INFO, USER_INFO Can be null - all types requested.
	 * 
	 * @return List of CustomFieldSet sets
	 * 
	 * @throws MambuApiException
	 */
	// TODO: to be tested with Mambu 3.3, see MBU-2486
	public List<CustomFieldSet> getCustomFieldSets(CustomField.Type customFieldType) throws MambuApiException {

		// create the api call

		String urlString = new String(mambuAPIService.createUrl(CUSTOM_FIELD_SETS));
		// Add Custom Filed Type Param
		ParamsMap params = new ParamsMap();
		String customFieldTypeString = (customFieldType == null) ? null : customFieldType.name();

		params.addParam(APIData.CUSTOM_FIELD_SETS_TYPE, customFieldTypeString); // if null, all types are requested

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<CustomFieldSet>>() {}.getType();
		List<CustomFieldSet> sustomFieldSets = GsonUtils.createGson().fromJson(jsonResponse, collectionType);

		return sustomFieldSets;
	}
}
