package com.mambu.apisdk.util;

import java.util.List;

import com.mambu.accounts.shared.model.TransactionChannel;
import com.mambu.accounts.shared.model.TransactionChannel.ChannelField;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.core.shared.model.CustomFieldValue;

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

	/**
	 * Validate Custom Field ID and make ParamsMap for Update Custom Field value API requests
	 * 
	 * @param customFieldId
	 *            the ID or the encoded key of the custom field to be updated. Must be not null and not empty
	 * @param fieldValue
	 *            the new value of the custom field
	 * 
	 * @return params
	 */
	public static ParamsMap makeParamsForUpdateCustomField(String customFieldId, String fieldValue) {

		// Verify that customFieldId is not null
		if (customFieldId == null || customFieldId.trim().isEmpty()) {
			throw new IllegalArgumentException("Custom Field ID must not be null or empty");
		}

		// Create JSON string to be used in the PATCH request
		// The JSON string for this API must have the following format: {"value":"newFieldValue"}. See MBU-6661

		// Make CustomFieldValue object to create this JSON
		CustomFieldValue customFieldValue = new CustomFieldValue();
		customFieldValue.setValue(fieldValue);

		// Set all other parameters to null, they are not needed in this JSON
		customFieldValue.setCustomField(null);
		customFieldValue.setToBeDeleted(null);
		customFieldValue.setIndexInList(null);
		customFieldValue.setAmount(null);

		final String patchJson = GsonUtils.createGson().toJson(customFieldValue, CustomFieldValue.class);

		ParamsMap params = new ParamsMap();
		params.put(APIData.JSON_OBJECT, patchJson);

		return params;

	}

	/**
	 * Convenience method to add to the ParamsMap input parameters common to most account transactions. Such common
	 * parameters include transaction amount, transaction date, transaction notes and transactionDetails object
	 * 
	 * @param params
	 *            input ParamsMap map to which transactionDetails shall be added. Must not be null
	 * @param amount
	 *            transaction amount
	 * @param date
	 *            transaction date
	 * @param notes
	 *            transaction notes
	 * @param transactionDetails
	 *            TransactionDetails object containing information about the transaction channel and the channel fields
	 */
	public static void addAccountTransactionParams(ParamsMap params, String amount, String date, String notes,
			TransactionDetails transactionDetails) {

		// Params map must not be null
		if (params == null) {
			throw new IllegalArgumentException("Params Map cannot be null");
		}
		params.addParam(APIData.AMOUNT, amount);
		params.addParam(APIData.DATE, date);
		params.addParam(APIData.NOTES, notes);

		// Add transactionDetails to the paramsMap
		addParamsForTransactionDetails(params, transactionDetails);

		return;
	}

	/**
	 * Add TransactionDetails to the input ParamsMap required for account transactions (e.g. disburseLoanAccount(),
	 * makeLoanRepayment(), etc.)
	 * 
	 * @param params
	 *            input ParamsMap to which transactionDetails shall be added. Must be not null
	 * 
	 * @param transactionDetails
	 *            TransactionDetails object containing information about the transaction channel and the channel fields
	 */
	private static void addParamsForTransactionDetails(ParamsMap params, TransactionDetails transactionDetails) {

		if (transactionDetails == null) {
			// Nothing to add
			return;
		}
		// Params must not be null
		if (params == null) {
			throw new IllegalArgumentException("params Map cannot be null");
		}

		// Get Channel ID
		TransactionChannel channel = transactionDetails.getTransactionChannel();
		String channelId = (channel == null) ? null : channel.getId();
		params.addParam(APIData.PAYMENT_METHOD, channelId);

		if (channel == null || channel.getChannelFields() == null) {
			// If channel was not specified or channel has no fields then there is nothing more to add
			return;
		}
		// Get Channel Fields configured for the provided channel
		List<ChannelField> channelFields = channel.getChannelFields();

		// Get field's value from the transactionDetails and add each field to the ParamsMap
		for (ChannelField field : channelFields) {
			switch (field) {
			case ACCOUNT_NAME:
				params.addParam(APIData.ACCOUNT_NAME, transactionDetails.getAccountName());
				break;
			case ACCOUNT_NUMBER:
				params.addParam(APIData.BANK_ACCOUNT_NUMBER, transactionDetails.getAccountNumber());
				break;
			case BANK_NUMBER:
				params.addParam(APIData.BANK_NUMBER, transactionDetails.getBankNumber());
				break;
			case CHECK_NUMBER:
				params.addParam(APIData.CHECK_NUMBER, transactionDetails.getCheckNumber());
				break;
			case IDENTIFIER:
				params.addParam(APIData.IDENTIFIER, transactionDetails.getIdentifier());
				break;
			case RECEPIT_NUMBER:
				params.addParam(APIData.RECEIPT_NUMBER, transactionDetails.getReceiptNumber());
				break;
			case ROUTING_NUMBER:
				params.addParam(APIData.BANK_ROUTING_NUMBER, transactionDetails.getRoutingNumber());
				break;
			}
		}
	}

}
