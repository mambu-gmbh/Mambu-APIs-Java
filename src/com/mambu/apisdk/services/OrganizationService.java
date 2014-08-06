/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.List;

import com.google.inject.Inject;
import com.mambu.accounts.shared.model.TransactionChannel;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.ServiceHelper;
import com.mambu.core.shared.model.Currency;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;

/**
 * Service class which handles API operations available for the organizations like getting it's currency
 * 
 * @author ipenciuc
 * 
 */
public class OrganizationService {

	private static String OFFSET = APIData.OFFSET;
	private static String LIMIT = APIData.LIMIT;

	private ServiceHelper serviceHelper;

	// Create API definitions for services provided by ClientService

	private final static ApiDefinition getBranchDetails = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, Branch.class);
	private final static ApiDefinition getBranches = new ApiDefinition(ApiType.GET_LIST, Branch.class);

	private final static ApiDefinition getCentreDetails = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, Centre.class);
	private final static ApiDefinition getCentres = new ApiDefinition(ApiType.GET_LIST, Centre.class);

	private final static ApiDefinition getCustomField = new ApiDefinition(ApiType.GET_ENTITY, CustomField.class);
	private final static ApiDefinition getCustomFieldSets = new ApiDefinition(ApiType.GET_LIST, CustomFieldSet.class);

	private final static ApiDefinition getCurrencies = new ApiDefinition(ApiType.GET_LIST, Currency.class);

	private final static ApiDefinition getTransactionChannels = new ApiDefinition(ApiType.GET_LIST,
			TransactionChannel.class);

	/***
	 * Create a new organization service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public OrganizationService(MambuAPIService mambuAPIService) {
		this.serviceHelper = new ServiceHelper(mambuAPIService);
	}

	/**
	 * Requests the organization currency
	 * 
	 * @return the Mambu base currency
	 * 
	 * @throws MambuApiException
	 */
	public final static String baseCurrencyMustBeDefined = "Base Currency must be defined";

	public Currency getCurrency() throws MambuApiException {

		List<Currency> currencies = serviceHelper.execute(getCurrencies);
		if (currencies != null && currencies.size() > 0) {
			return currencies.get(0);
		} else {
			// At least base currency must be defined for an organization
			throw new MambuApiException(-1, baseCurrencyMustBeDefined);
		}
	}

	/**
	 * Get a paginated list of branches
	 * 
	 * @param offset
	 *            the offset of the response. If not set a value of 0 is used by default
	 * @param limit
	 *            the maximum number of response entries. If not set a value of 50 is used by default
	 * 
	 * @return List<Branch>
	 * 
	 * @throws MambuApiException
	 */
	public List<Branch> getBranches(String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();

		params.put(OFFSET, offset);
		params.put(LIMIT, limit);
		return serviceHelper.execute(getBranches, params);
	}

	/**
	 * Requests a branch by their Mambu ID
	 * 
	 * @param branchId
	 * 
	 * @return the Mambu branch model
	 * 
	 * @throws MambuApiException
	 */
	public Branch getBranch(String branchId) throws MambuApiException {
		return serviceHelper.execute(getBranchDetails, branchId);
	}

	/**
	 * Requests a centre details by their Mambu ID
	 * 
	 * @param centreId
	 * 
	 * @return the Mambu centre model (with full details)
	 * 
	 * @throws MambuApiException
	 */
	public Centre getCentre(String centreId) throws MambuApiException {
		return serviceHelper.execute(getCentreDetails, centreId);
	}

	/**
	 * Get paginated list of centres
	 * 
	 * @param branchId
	 *            Centers for the specified branch are returned. If NULL, all centres are searched
	 * @param offset
	 *            the offset of the response. If not set a value of 0 is used by default
	 * @param limit
	 *            the maximum number of response entries. If not set a value of 50 is used by default
	 * 
	 * @return an array of Centres
	 * 
	 * @throws MambuApiException
	 */
	public List<Centre> getCentres(String branchId, String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam(APIData.BRANCH_ID, branchId); // if branchId is null then all centres are searched
		params.put(OFFSET, offset);
		params.put(LIMIT, limit);

		return serviceHelper.execute(getCentres, params);
	}

	/**
	 * Get CustomField object details by Custom Field ID
	 * 
	 * @param fieldId
	 *            The id of the required CustomField
	 * 
	 * @return CustomField
	 * 
	 * @throws MambuApiException
	 */
	public CustomField getCustomField(String fieldId) throws MambuApiException {
		return serviceHelper.execute(getCustomField, fieldId);
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
	public List<CustomFieldSet> getCustomFieldSets(CustomField.Type customFieldType) throws MambuApiException {

		ParamsMap params = null;
		// if customFieldType is null then all types are requested
		if (customFieldType != null) {
			// Add Custom Filed Type Param
			params = new ParamsMap();
			params.addParam(APIData.CUSTOM_FIELD_SETS_TYPE, customFieldType.name());
		}

		return serviceHelper.execute(getCustomFieldSets, params);
	}

	/**
	 * Get Transaction Channels
	 * 
	 * @return List of all Transaction Channels for the organization
	 * 
	 * @throws MambuApiException
	 */
	public List<TransactionChannel> getTransactionChannels() throws MambuApiException {
		ParamsMap params = null;
		return serviceHelper.execute(getTransactionChannels, params);
	}
}
