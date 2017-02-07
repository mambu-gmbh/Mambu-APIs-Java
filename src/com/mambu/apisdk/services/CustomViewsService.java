package com.mambu.apisdk.services;

import java.util.HashMap;
import java.util.List;

import com.google.inject.Inject;
import com.mambu.api.server.handler.customviews.model.ApiViewType;
import com.mambu.api.server.handler.customviews.model.CustomViewEntitiesSummaryWrapper;
import com.mambu.api.server.handler.customviews.model.ResultType;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.JSONCustomViewEntitiesSummaryWrapper;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.apisdk.util.ServiceHelper;

public class CustomViewsService {

	// Service helper
	protected ServiceExecutor serviceExecutor;

	// Specify Mambu entities supported by the Custom View Value API: Client, Group. LoanAccount, SavingsAccount,
	// LoanTranscation, SavingsTransaction, Activity
	private final static HashMap<ApiViewType, MambuEntityType> supportedApiViewTypes = new HashMap<>();
	static {
		supportedApiViewTypes.put(ApiViewType.CLIENTS, MambuEntityType.CLIENT);
		supportedApiViewTypes.put(ApiViewType.GROUPS, MambuEntityType.GROUP);
		supportedApiViewTypes.put(ApiViewType.LOANS, MambuEntityType.LOAN_ACCOUNT);
		supportedApiViewTypes.put(ApiViewType.DEPOSITS, MambuEntityType.SAVINGS_ACCOUNT);
		supportedApiViewTypes.put(ApiViewType.LOAN_TRANSACTIONS, MambuEntityType.LOAN_TRANSACTION);
		supportedApiViewTypes.put(ApiViewType.DEPOSIT_TRANSACTIONS, MambuEntityType.SAVINGS_TRANSACTION);
		supportedApiViewTypes.put(ApiViewType.SYSTEM_ACTIVITIES, MambuEntityType.ACTIVITY);
		supportedApiViewTypes.put(ApiViewType.LINES_OF_CREDIT, MambuEntityType.LINE_OF_CREDIT);
	}

	/***
	 * Create a new service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public CustomViewsService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}

	/**
	 * Get entities for a Custom View
	 * 
	 * @param apiViewType
	 *            API view type. Example, ApiViewType.LOANS, or ApiViewType.CLIENTS
	 * @param branchId
	 *            an optional branch ID filtering parameter. If null, entities for all branches managed by the API user
	 *            are retrieved
	 * @param fullDetails
	 *            boolean indicating if entities with fullDetails shall be returned. Applicable to Clients, Groups, Loan
	 *            Accounts and Savings Accounts
	 * @param customViewKey
	 *            the encoded key for the custom view. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return a list of entities for the custom view
	 * @throws MambuApiException
	 */
	public <T> List<T> getCustomViewEntities(ApiViewType apiViewType, String branchId, boolean fullDetails,
			String customViewKey, String offset, String limit) throws MambuApiException {
		// Example GET /api/clients?viewfilter=123&branchId=b123&offset=0&limit=100&resultType=FULL_DETAILS
		// See MBU-4607, MBU-10842, MBU-7042

		ResultType resultType = fullDetails ? ResultType.FULL_DETAILS : ResultType.BASIC;
		ApiDefinition apiDefinition = makeApiDefintion(apiViewType, resultType);

		// Create params map with all filtering parameters
		ParamsMap params = makeParamsForGetByCustomView(customViewKey, resultType, branchId, offset, limit);

		return serviceExecutor.execute(apiDefinition, params);
	}

	/**
	 * Convenience method to get "Basic" entities for a Custom View (entities without full details)
	 * 
	 * @param apiViewType
	 *            API view type. Example, ApiViewType.LOANS, or ApiViewType.CLIENTS
	 * @param branchId
	 *            an optional branch ID filtering parameter. If null, entities for all branches managed by the API user
	 *            are retrieved
	 * @param customViewKey
	 *            the encoded key for the custom view. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return a list of basic entities for the custom view
	 * @throws MambuApiException
	 */
	public <T> List<T> getCustomViewEntities(ApiViewType apiViewType, String branchId, String customViewKey,
			String offset, String limit) throws MambuApiException {
		// Example GET /api/clients?viewfilter=123&branchId=b123&offset=0&limit=100&resultType=BASIC
		// See MBU-4607, MBU-10842, MBU-7042
		boolean fullDetails = false;
		return getCustomViewEntities(apiViewType, branchId, fullDetails, customViewKey, offset, limit);
	}

	/**
	 * Get summary for a custom view
	 * 
	 * @param apiViewType
	 *            API view type. Example, ApiViewType.LOANS, or ApiViewType.CLIENTS
	 * @param branchId
	 *            an optional branch ID filtering parameter. If null, summary for entities in all branches managed by
	 *            the API user are retrieved
	 * @param customViewKey
	 *            the encoded key for the custom view. Must not be null
	 * @return custom view summary
	 * @throws MambuApiException
	 */
	public CustomViewEntitiesSummaryWrapper getCustomViewSummary(ApiViewType apiViewType, String branchId,
			String customViewKey) throws MambuApiException {
		// Available since Mambu 4.1 . See MBU-11879
		// Example: Example GET /api/clients?viewfilter=123&branchId=b123&resultType=SUMMARY
		// Response example: { "summary":{ "count":"2", "totals":[{ "dataItemType":"CLIENT", "values":{
		// "LOAN_AMOUNT":"1900","PRINCIPAL_DUE":"0}, "customFieldValues":{"LOAN_AMOUNT":"1900", "PRINCIPAL_DUE":"0"
		// }}]}}

		// Make apiDefinition for a ResultType.SUMMARY
		final ResultType resultType = ResultType.SUMMARY;
		ApiDefinition apiDefinition = makeApiDefintion(apiViewType, resultType);

		// Create params map with filtering parameters. Offset and limit are not applicable when getting summaries
		ParamsMap params = makeParamsForGetByCustomView(customViewKey, resultType, branchId, null, null);

		// Execute API
		JSONCustomViewEntitiesSummaryWrapper jsonSummary = serviceExecutor.execute(apiDefinition, params);

		// Return the summary (CustomViewEntitiesSummaryWrapper)
		return jsonSummary != null ? jsonSummary.getSummary() : null;
	}

	/**
	 * Make ParamsMap for GET Mambu entities for a custom view API requests
	 * 
	 * @param customViewKey
	 *            the encoded key of the Custom View to filter entities. Must not be null
	 * @param resultType
	 *            custom View Result Type
	 * @param branchId
	 *            branch id. Optional filter parameter
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return params params map
	 */
	private static ParamsMap makeParamsForGetByCustomView(String customViewKey, ResultType resultType, String branchId,
			String offset, String limit) {

		// Verify that the customViewKey is not null or empty
		if (customViewKey == null || customViewKey.trim().isEmpty()) {
			throw new IllegalArgumentException("customViewKey must not be null or empty");
		}
		// Validate pagination parameters
		if ((offset != null && Integer.parseInt(offset) < 0) || ((limit != null && Integer.parseInt(limit) < 1))) {
			throw new IllegalArgumentException("Invalid pagination parameters");
		}
		// Create ParamsMap with supported filter parameters
		ParamsMap params = new ParamsMap();
		params.put(APIData.VIEW_FILTER, customViewKey);
		// Add result type, if provided. Mambu would default to BASIC
		if (resultType != null) {
			params.put(APIData.RESULT_TYPE, resultType.name());
		}
		// Add supported filter parameters
		params.put(APIData.BRANCH_ID, branchId);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		return params;
	}

	/**
	 * Make Api Definition for retrieving entities for a custom view
	 * 
	 * @param apiViewType
	 *            custom view's API view type
	 * @param resultType
	 *            required result type
	 * @return api definition
	 */
	private ApiDefinition makeApiDefintion(ApiViewType apiViewType, ResultType resultType) {

		if (resultType == null) {
			throw new IllegalArgumentException("Result Type must not be null");
		}
		// Get MambuEntityType for the requested View Type
		MambuEntityType forEntity = getMambuEntityType(apiViewType);

		Class<?> forEntityClass = forEntity.getEntityClass();
		String entityPath = ApiDefinition.getApiEndPoint(forEntityClass);
		String apiPath = entityPath;
		switch (apiViewType) {
		case LINES_OF_CREDIT:
		case CLIENTS:
		case GROUPS:
		case LOANS:
		case DEPOSITS:
		case SYSTEM_ACTIVITIES:
			// for these views the /api/entityPath is already good as is (e.g. /api/clients, /api/activities)
			break;
		case LOAN_TRANSACTIONS:
			// Make api paths in a form loans/transactions
			apiPath = APIData.LOANS + "/" + entityPath;
			break;
		case DEPOSIT_TRANSACTIONS:
			// Make api paths in a form savings/transactions
			apiPath = APIData.SAVINGS + "/" + entityPath;
			break;

		}
		// Set the Result class based on requested resultType
		Class<?> resultClass = forEntityClass;
		ApiReturnFormat returnFormat = ApiReturnFormat.COLLECTION;
		switch (resultType) {
		case BASIC:
			// forEntityClass is the required result class for case BASIC
			break;
		case FULL_DETAILS:
			// Get Full Details class
			resultClass = ServiceHelper.getFullDetailsClass(forEntity);
			if (resultClass == null) {
				throw new IllegalArgumentException("Full Details entities are not supported for " + forEntity);
			}
			break;

		case SUMMARY:
			// For summaries, the API returns JSONCustomViewEntitiesSummaryWrapper
			resultClass = JSONCustomViewEntitiesSummaryWrapper.class;
			returnFormat = ApiReturnFormat.OBJECT;
			break;
		}

		// Create API Definition
		ApiDefinition apiDefinition = new ApiDefinition(apiPath, ContentType.WWW_FORM, Method.GET, resultClass,
				returnFormat);

		return apiDefinition;
	}

	/**
	 * Get Mambu Entity type for the ApiViewType. Internally we use MambuEntityType to create an API request
	 * 
	 * @param apiViewType
	 *            API view type
	 * @return Mambu entity type
	 */
	private MambuEntityType getMambuEntityType(ApiViewType apiViewType) {
		if (apiViewType == null) {
			throw new IllegalArgumentException("API View type must not be null");
		}

		// Convert ApiViewType to MambuEntityType for building API call definitions
		if (supportedApiViewTypes.containsKey(apiViewType)) {
			return supportedApiViewTypes.get(apiViewType);
		}
		throw new IllegalArgumentException("Custom Views API is not supported for " + apiViewType);
	}

}
