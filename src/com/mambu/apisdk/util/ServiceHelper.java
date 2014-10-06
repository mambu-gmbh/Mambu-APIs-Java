package com.mambu.apisdk.util;


/**
 * ServiceHelper class provides helper methods for validating and building parameters and API definitions required for
 * executing API requests common across different services. For example, there might be common operations required to
 * build transaction parameters for both LoanService and SavingsService, or for API requests which are supported by
 * multiple services (such as getting a list of entities for a Custom View, updating custom field value for an entity,
 * etc.)
 * 
 * @author mdanilkis
 * 
 */
public class ServiceHelper {

	/**
	 * Validate Input params and make ParamsMap for GET Mambu entities for a custom view API requests
	 * 
	 * @param customViewKey
	 *            the encoded key of the Custom View to filter entities
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null the must be an integer greater than zero
	 * 
	 * @return params
	 */
	public static ParamsMap makeParamsForGetByCustomView(String customViewKey, String offset, String limit) {

		// Verify that the customViewKey is not null or empty
		if (customViewKey == null || customViewKey.trim().isEmpty()) {
			throw new IllegalArgumentException("customViewKey must not be null or empty");
		}
		// Validate pagination parameters
		if ((offset != null && Integer.parseInt(offset) < 0) || ((limit != null && Integer.parseInt(limit) < 1))) {
			throw new IllegalArgumentException("Invalid pagination parameters");
		}

		ParamsMap params = new ParamsMap();
		params.addParam(APIData.VIEW_FILTER, customViewKey);
		params.addParam(APIData.OFFSET, offset);
		params.addParam(APIData.LIMIT, limit);

		return params;

	}

}
