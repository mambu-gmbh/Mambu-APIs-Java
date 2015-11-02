package com.mambu.apisdk.util;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mambu.accounts.shared.model.TransactionChannel;
import com.mambu.accounts.shared.model.TransactionChannel.ChannelField;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.model.LoanAccountExpanded;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.savings.shared.model.SavingsAccount;

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

	/***
	 * Create ParamsMap with a JSON string for the JSONDocument object
	 * 
	 * @param document
	 *            JSONDocument document containing Document object and documentContent string
	 * @return params map with the document JSON string
	 */
	public static ParamsMap makeParamsForDocumentJson(JSONDocument document) {

		// Use custom parsing for the potentially very large document object. JSONDocument object
		// contains a Document object and also the encoded documentContent part, which can be a very large string. For
		// memory management efficiency reasons it is better no to be asking Gson to parse the whole JSONDocument
		// object (with this potentially very large string included) but to parse only the Document object with
		// the blank content part and then insert the actual document content value into the resulting JSON ourselves
		// (there were reports of out of memory errors during Gson parsing JSONDocument objects with a large document
		// content)

		// Create JSON string in two steps: a) parse document object with the blank document content value and b) insert
		// document content value into the JSON string. This approach requires less memory than just using
		// Gson.toJson(document) parser

		// Create Json with the same document but with empty content
		JSONDocument copy = new JSONDocument();
		copy.setDocument(document.getDocument());
		copy.setDocumentContent("");

		// Parse modified JSONDocument with the blank content value
		String jsonData = makeApiJson(copy);

		// Add AppKey here - to avoid inserting it after the full string is made
		String applicationKey = MambuAPIFactory.getApplicationKey();
		if (applicationKey != null && applicationKey.length() > 0) {
			jsonData = addAppkeyValueToJson(applicationKey, jsonData);
		}

		// Now insert back document content value into the generated JSON string
		final String documentContent = document.getDocumentContent();
		StringBuffer finalJson = new StringBuffer(jsonData.length() + documentContent.length());
		finalJson.append(jsonData);

		// Now find the position to insert document content (into the "" part of the "documentContent":"")
		final String contentPair = "\"documentContent\":\"\"";
		int insertPosition = finalJson.indexOf(contentPair) + contentPair.length() - 1;

		// Insert document content
		finalJson.insert(insertPosition, documentContent);

		String documentJson = finalJson.toString();

		// Add generated JSON string to the ParamsMap
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(APIData.JSON_OBJECT, documentJson);

		return paramsMap;
	}

	/**
	 * Get Base64 encoded content from the API message containing bas64 encoding indicator and base64 encoded content
	 * 
	 * Mambu API returns encoded files in the following format: "data:image/jpg;base64,/9j...." The encoded string is
	 * base64 encoded with CRLFs, E.g. 9j/4AAQSkZJR...\r\nnHBwgJC4nICIsIxwcKDcpL... This methods returns just the
	 * content part without the base64 indicator
	 * 
	 * @param apiResponse
	 *            api response message containing base64 indicator and base64 encoded string
	 * 
	 * @return base64 encoded message content
	 */
	public static String getContentForBase64EncodedMessage(String apiResponse) {

		// Check if the response format is as expected and return null otherwise
		final String encodingStartsAfter = APIData.BASE64_ENCODING_INDICATOR;
		if (apiResponse == null || !apiResponse.contains(encodingStartsAfter)) {
			return null;
		}

		final int dataStart = apiResponse.indexOf(encodingStartsAfter) + encodingStartsAfter.length();
		// Get the actual encoded string part. From dataStart till the end (without the enclosing double quote char)
		String base64EncodedString = apiResponse.substring(dataStart, apiResponse.length() - 1);

		return base64EncodedString;
	}

	/**
	 * A list of fields supported by the PATCH loan account API. See MBU-7758
	 */
	private final static Set<String> modifiableLoanAccountFields = new HashSet<String>(Arrays.asList(
			APIData.LOAN_AMOUNT, APIData.INTEREST_RATE, APIData.INTEREST_RATE_SPREAD, APIData.REPAYMENT_INSTALLMENTS,
			APIData.REPAYMENT_PERIOD_COUNT, APIData.REPAYMENT_PERIOD_UNIT, APIData.EXPECTED_DISBURSEMENT_DATE,
			APIData.FIRST_REPAYMENT_DATE, APIData.GRACE_PERIOD, APIData.PRNICIPAL_REPAYMENT_INTERVAL,
			APIData.PENALTY_RATE, APIData.PERIODIC_PAYMENT));

	/**
	 * Create ParamsMap with a JSON string for the PATCH loan account API. Only fields applicable to the API are added
	 * to the output JSON
	 * 
	 * @param account
	 *            input loan account
	 * @return params map
	 */
	public static ParamsMap makeParamsForLoanTermsPatch(LoanAccount account) {

		// Verify that account is not null
		if (account == null) {
			throw new IllegalArgumentException("Loan Account must not be null");
		}

		// Create JSON expected by the PATCH loan account API:
		JsonObject accountFields = makeJsonObjectForFields(account, modifiableLoanAccountFields);
		if (accountFields == null) {
			return null;
		}

		// Create JSON string. Format: { "loanAccount":{"loanAmount":"1000", "repaymentPeriodCount":"10"}}'
		String json = "{\"loanAccount\":" + accountFields.toString() + "}";

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(APIData.JSON_OBJECT, json);

		return paramsMap;
	}

	/**
	 * A list of fields supported by the PATCH savings account API. See MBU-10447 for a list of fields supported in 3.14
	 * 
	 */
	private final static Set<String> modifiableSavingsAccountFields = new HashSet<String>(Arrays.asList(
			APIData.INTEREST_RATE, APIData.INTEREST_RATE_SPREAD, APIData.MAX_WITHDRAWAL_AMOUNT,
			APIData.RECOMMENDED_DEPOSIT_AMOUNT, APIData.TARGET_AMOUNT, APIData.OVERDRAFT_INTEREST_RATE,
			APIData.OVERDRAFT_LIMIT, APIData.OVERDRAFT_EXPIRY_DATE));

	/**
	 * Create ParamsMap with a JSON string for the PATCH savings account API. Only fields applicable to the API are
	 * added to the output JSON
	 * 
	 * @param account
	 *            input savings account
	 * @return params map
	 */
	public static ParamsMap makeParamsForSavingsTermsPatch(SavingsAccount account) {

		// Verify that account is not null
		if (account == null) {
			throw new IllegalArgumentException("Loan Account must not be null");
		}

		// Create JSON expected by the PATCH loan account API:
		JsonObject accountFields = makeJsonObjectForFields(account, modifiableSavingsAccountFields);
		if (accountFields == null) {
			return null;
		}

		// Create JSON string. Format: { "loanAccount":{"loanAmount":"1000", "repaymentPeriodCount":"10"}}'
		String json = "{\"savingsAccount\":" + accountFields.toString() + "}";

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(APIData.JSON_OBJECT, json);

		return paramsMap;
	}

	/**
	 * A list of fields supported by get loan schedule preview API. See MBU-6789 and MBU-7676
	 */
	private final static Set<String> loanSchedulePreviewFields = new HashSet<String>(Arrays.asList(APIData.LOAN_AMOUNT,
			APIData.INTEREST_RATE, APIData.REPAYMENT_INSTALLMENTS, APIData.REPAYMENT_PERIOD_COUNT,
			APIData.REPAYMENT_PERIOD_UNIT, APIData.EXPECTED_DISBURSEMENT_DATE, APIData.FIRST_REPAYMENT_DATE,
			APIData.GRACE_PERIOD, APIData.PRNICIPAL_REPAYMENT_INTERVAL, APIData.PERIODIC_PAYMENT));

	/**
	 * Create ParamsMap with a map of fields for the GET loan schedule for the product API. Only fields applicable to
	 * the API are added to the params map
	 * 
	 * @param account
	 *            input loan account
	 * @return params map
	 */
	public static ParamsMap makeParamsForLoanSchedule(LoanAccount account) {

		// Verify that account is not null
		if (account == null) {
			throw new IllegalArgumentException("Loan Account must not be null");
		}

		// Make JsonObject with the applicable fields only
		// Loan schedule API uses URL encoded params, so the dates should be in "yyyy-MM-dd" date format
		JsonObject loanTermsObject = makeJsonObjectForFields(account, loanSchedulePreviewFields, APIData.yyyyMmddFormat);

		// For this GET API we need to create params map with all individual params separately
		// Convert Json object with the applicable fields into a ParamsMap.
		Type type = new TypeToken<ParamsMap>() {
		}.getType();
		ParamsMap params = GsonUtils.createGson().fromJson(loanTermsObject.toString(), type);
		if (params == null || params.size() == 0) {
			return params;
		}
		// Mambu API expects "anticipatedDisbursement" parameter in place of "expectedDisbursementDate" loan field when
		// getting loan account schedule. See MBU-6789
		// Send expectedDisbursementdate value as APIData.ANTICIPATE_DISBURSEMENT parameter
		String expectedDisbursement = params.get(APIData.EXPECTED_DISBURSEMENT_DATE);
		if (expectedDisbursement != null) {
			params.remove(APIData.EXPECTED_DISBURSEMENT_DATE);
			params.put(APIData.ANTICIPATE_DISBURSEMENT, expectedDisbursement);
		}
		return params;

	}

	/**
	 * Helper to create a JSON object with a sub-set of the applicable object fields with the Mambu's default JSON date
	 * time format ("yyyy-MM-dd'T'HH:mm:ssZ")
	 * 
	 * @param object
	 *            object
	 * @param applicableFields
	 *            a set of applicable fields
	 * @return JSON object
	 */
	public static <T> JsonObject makeJsonObjectForFields(T object, Set<String> applicableFields) {

		return makeJsonObjectForFields(object, applicableFields, GsonUtils.defaultDateTimeFormat);
	}

	/**
	 * Helper to create a JSON object with a sub-set of the applicable object fields
	 * 
	 * @param object
	 *            object
	 * @param applicableFields
	 *            a set of applicable fields
	 * @param dateTimeFormat
	 *            a string representing Mambu API date time format
	 * @return JSON object
	 */
	public static <T> JsonObject makeJsonObjectForFields(T object, Set<String> applicableFields, String dateTimeFormat) {

		if (dateTimeFormat == null) {
			dateTimeFormat = GsonUtils.defaultDateTimeFormat;
		}
		// Create JsonObject for the full loan account and then extract only the fields present in the applicableFields
		// set
		JsonObject loanAccountJson = GsonUtils.createGson(dateTimeFormat).toJsonTree(object).getAsJsonObject();

		// Make JsonObject with the applicable fields only. Add only those which are not NULL
		JsonObject loanSubsetObject = new JsonObject();
		for (String fieldName : applicableFields) {
			JsonElement element = loanAccountJson.get(fieldName);
			if (element == null) {
				// Field value is NULL. Skipping
				continue;
			}
			loanSubsetObject.add(fieldName, element);
		}
		return loanSubsetObject;
	}

	/**
	 * Generate a JSON string for an object using Mambu's default date time format ("yyyy-MM-dd'T'HH:mm:ssZ")
	 * 
	 * @param object
	 *            object
	 * @return JSON string for the object
	 */
	public static <T> String makeApiJson(T object) {
		return GsonUtils.createGson().toJson(object, object.getClass());
	}

	/**
	 * Generate a JSON string for an object and with the specified format for date fields
	 * 
	 * @param object
	 *            object
	 * @param dateTimeFormat
	 *            date time format string. Example: "yyyy-MM-dd". If null then the default date time format is used
	 *            ("yyyy-MM-dd'T'HH:mm:ssZ")
	 * @return JSON string for the object
	 */
	public static <T> String makeApiJson(T object, String dateTimeFormat) {
		if (dateTimeFormat == null) {
			// Use default API formatter
			return GsonUtils.createGson().toJson(object, object.getClass());
		} else {
			// Use provided dateTimeFormat
			return GsonUtils.createGson(dateTimeFormat).toJson(object, object.getClass());
		}
	}

	/**
	 * Convenience helper to make params map which contains only pagination parameters: offset and limit
	 * 
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null the must be an integer greater than zero
	 * @return
	 */
	public static ParamsMap makePaginationParams(String offset, String limit) {

		if (offset == null && limit == null) {
			return null;
		}
		// Validate pagination parameters
		if ((offset != null && Integer.parseInt(offset) < 0) || ((limit != null && Integer.parseInt(limit) < 1))) {
			throw new IllegalArgumentException("Invalid pagination parameters. Offset=" + offset + " Limit=" + limit);
		}

		ParamsMap params = new ParamsMap();
		params.addParam(APIData.OFFSET, offset);
		params.addParam(APIData.LIMIT, limit);

		return params;

	}

	/**
	 * Add appKey value to the json string.
	 * 
	 * @param appKey
	 *            app key value. Can be null
	 * @param jsonString
	 *            json string.
	 * @return json string with the appKey parameter added. If the appKey parameter is already present then the
	 *         jsonString is not modified. See MBU-3892
	 */
	public static String addAppkeyValueToJson(String appKey, String jsonString) {

		// Example JSON request with the appKey parameter (see MBU-3892):
		// curl -H "Content-type: application/json" -X POST -d '{ "appkey":"appKeyValue", "client": {...}'

		if (appKey == null || appKey.length() == 0) {
			return jsonString;
		}

		// First compile the following string: {"appKey":"appKeyValue",
		String appKeyValue = APIData.APPLICATION_KEY + "\":\"" + appKey; // "appKey":"appKeyValue"
		// This formatted appKey string will be appended with the original json string (without the first '{')
		String appKeyString = "{\"" + appKeyValue + "\",";

		// Check if we have the string to insert into
		if (jsonString == null || jsonString.length() == 0) {
			// Nothing to insert into. Return just the appKey param (surrounded by the square brackets)
			return appKeyString.replace(',', '}');
		}

		// Check If the appkey is already present - do not add it again
		if (jsonString.contains(appKeyValue)) {
			// Appkey is already present, do not insert
			return jsonString;
		}

		// We need input json string without the first '{'
		String jsonStringToAdd = jsonString.substring(1);

		// Create initial String Buffer large enough to hold the resulting two strings
		StringBuffer jsonWithAppKey = new StringBuffer(jsonStringToAdd.length() + appKeyString.length());

		// Append the appkey and the the json string
		jsonWithAppKey.append(appKeyString);
		jsonWithAppKey.append(jsonStringToAdd);

		return jsonWithAppKey.toString();

	}

	/**
	 * Get class corresponding to the "full details" class for a Mambu Entity. For example, ClientExpanded.class for
	 * MambuEntityType.CLIENT;
	 * 
	 * @param entityType
	 *            entity type. Must not be null
	 * @return class representing full details class for the Mambu Entity or null if no such class exists
	 */
	public static Class<?> getFullDetailsClass(MambuEntityType entityType) {
		if (entityType == null) {
			throw new IllegalArgumentException("Entity type must not be null");
		}
		switch (entityType) {
		case CLIENT:
			return ClientExpanded.class;
		case GROUP:
			return GroupExpanded.class;
		case LOAN_ACCOUNT:
			return LoanAccountExpanded.class;
		case SAVINGS_ACCOUNT:
			return JSONSavingsAccount.class;
		default:
			return null;

		}
	}
}
