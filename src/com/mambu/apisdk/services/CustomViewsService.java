package com.mambu.apisdk.services;

import java.util.HashMap;
import java.util.List;

import com.google.inject.Inject;
import com.mambu.api.server.handler.customviews.model.ApiViewType;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
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

	// Custom Views API supports two types of returned values
	// See MBU-10842. Note: a third result type (COLUMNS) will be added in future releases
	public enum CustomViewResultType {
		BASIC, // returns entities without any extra details
		FULL_DETAILS, // returns entities with full details
	}

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
	 * @param fullDetails
	 *            boolean indicating if entities with fullDetails shall be returned. Applicable to Clients, Groups, Loan
	 *            Accounts and Savings Accounts
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return a list of entities for the custom view
	 * @throws MambuApiException
	 */
	// TODO: implement with branch/centre/officer filter parameters when MBU-7042 is dine in Mambu 4.0
	public <T> List<T> getCustomViewEntities(ApiViewType apiViewType, boolean fullDetails, String customViewKey,
			String offset, String limit) throws MambuApiException {
		// Example GET /api/clients?viewfilter=123&offset=0&limit=100&resultType=FULL_DETAILS
		// See MBU-4607, MBU-10842

		CustomViewResultType resultType = fullDetails ? CustomViewResultType.FULL_DETAILS : CustomViewResultType.BASIC;
		ApiDefinition apiDefinition = makeApiDefintion(apiViewType, resultType);

		// Create params map with all filtering parameters

		// Branch Id, Centre ID, and Credit officer name filer params are not applicable until Mambu 4.0
		// See MBU-7042
		String branchId = null;
		String centreId = null;
		String creditOfficerName = null;
		ParamsMap params = makeParamsForGetByCustomView(customViewKey, resultType, branchId, centreId,
				creditOfficerName, offset, limit);

		return serviceExecutor.execute(apiDefinition, params);

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
	 * @param centreId
	 *            centre id. Optional filter parameter
	 * @param creditOfficerName
	 *            credit officer name. Optional filter parameter
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return params params map
	 */
	public static ParamsMap makeParamsForGetByCustomView(String customViewKey, CustomViewResultType resultType,
			String branchId, String centreId, String creditOfficerName, String offset, String limit) {

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
			switch (resultType) {
			case BASIC:
				params.put(APIData.RESULT_TYPE, APIData.BASIC);
				break;
			case FULL_DETAILS:
				params.put(APIData.RESULT_TYPE, APIData.FULL_DETAILS);
			}
		}
		// Add supported filter parameters
		params.put(APIData.BRANCH_ID, branchId);
		params.put(APIData.CENTRE_ID, centreId);
		params.put(APIData.CREDIT_OFFICER_USER_NAME, creditOfficerName);
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
	private ApiDefinition makeApiDefintion(ApiViewType apiViewType, CustomViewResultType resultType) {

		if (resultType == null) {
			throw new IllegalArgumentException("Result Type must not be null");
		}
		// Get MambuEntityType for the requested View Type
		MambuEntityType forEntity = getMambuEntityType(apiViewType);

		Class<?> forEntityClass = forEntity.getEntityClass();
		String entityPath = ApiDefinition.getApiEndPoint(forEntityClass);
		String apiPath = entityPath;
		switch (apiViewType) {
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
