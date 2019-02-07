package com.mambu.apisdk.util;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mambu.accounts.shared.model.Account;
import com.mambu.accounts.shared.model.AccountState;
import com.mambu.accounts.shared.model.PredefinedFee;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.api.server.handler.loan.model.JSONApplyManualFee;
import com.mambu.api.server.handler.loan.model.JSONFeeRequest;
import com.mambu.api.server.handler.loan.model.JSONLoanAccount;
import com.mambu.api.server.handler.loan.model.JSONTransactionRequest;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.Money;
import com.mambu.loans.shared.model.CustomPredefinedFee;
import com.mambu.loans.shared.model.DisbursementDetails;
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
	 * Create JSONTransactionRequest for submitting JSON transaction API requests
	 * 
	 * @param amount
	 *            transaction amount
	 * @param backDate
	 *            transaction back date
	 * @param firstRepaymentDate
	 *            first repayment date
	 * @param transactionDetails
	 *            transaction details
	 * @param transactionFees
	 *            transaction fees
	 * @param customInformation
	 *            transaction custom fields
	 * @param notes
	 *            transaction notes
	 * @return JSON Transaction Request
	 */
	public static JSONTransactionRequest makeJSONTransactionRequest(Money amount, Date backDate,
			Date firstRepaymentDate, TransactionDetails transactionDetails, List<CustomPredefinedFee> transactionFees,
			List<CustomFieldValue> customInformation, String notes) {

		// Create JSONTransactionRequest object for POSTing JSON Transactions. See MBU-11837
		// Example:POST { "type":"DISBURSMENT", "date":"2012-10-04T11:03:31", "notes":"API comments",
		// "method":"PayPoint", "customInformation":[ {"value":"Pending", "customFieldID":"Status" }]}
		// /api/loans/loanId/transactions

		JSONTransactionRequest request = new JSONTransactionRequest();
		// Add amount and notes
		BigDecimal bigDecimalAmount = amount == null ? null : amount.getAmount();
		request.setAmount(bigDecimalAmount);
		request.setNotes(notes);
		// Add Back Date and First Repayment Date
		request.setDate(backDate);
		request.setValueDate(backDate);
		request.setFirstRepaymentDate(firstRepaymentDate);
		// Add transaction custom fields
		request.setCustomInformation(customInformation);
		// Transaction Channel must be set separately
		String channelKey = transactionDetails != null ? transactionDetails.getTransactionChannelKey() : null;
		request.setMethod(channelKey);

		// Add Transaction Fees
		setTransactionFees(request, transactionFees);
		return request;

	}

	/**
	 * Convenience method to create JSONTransactionRequest specifying disbursement details
	 * 
	 * @param amount
	 *            transaction amount
	 * @param disbursementDetails
	 *            disbursement details
	 * @param customInformation
	 *            transaction custom fields
	 * @param notes
	 *            transaction notes
	 * @return JSON Transaction Request
	 */
	public static JSONTransactionRequest makeJSONTransactionRequest(Money amount,
			DisbursementDetails disbursementDetails, List<CustomFieldValue> customInformation, String notes) {

		Date backDate = null;
		Date firstRepaymentDate = null;
		List<CustomPredefinedFee> disbursementFees = null;
		TransactionDetails transactionDetails = null;
		// Get transaction request fields from the disbursementDetails
		if (disbursementDetails != null) {
			backDate = disbursementDetails.getExpectedDisbursementDate();
			firstRepaymentDate = disbursementDetails.getFirstRepaymentDate();
			transactionDetails = disbursementDetails.getTransactionDetails();
			disbursementFees = disbursementDetails.getFees();
		}

		return makeJSONTransactionRequest(amount, backDate, firstRepaymentDate, transactionDetails, disbursementFees,
				customInformation, notes);
	}

	/**
	 * Create JSON Transaction request for submitting applying predefined fee and specifying repayment number
	 * 
	 * Available since Mambu 4.1. See MBU-12271, MBU-12272 (loan fees), MBU-12273 (savings fees)
	 * 
	 * @param transactionFees
	 *            transaction fees
	 * @param repaymentNumber
	 *            repayment number. Applicable only for loan transactions. Must be set to null for savings apply fee
	 *            transaction
	 * @param notes
	 *            transaction notes
	 * @return JSON Apply Manual Fee Request
	 */
	public static JSONApplyManualFee makeJSONApplyManualFeeRequest(List<CustomPredefinedFee> transactionFees,
			Integer repaymentNumber, String notes) {

		// Applying Manual predefined fees for is available since Mambu 4.1. MBU-12271, MBU-12272. MBU-12273

		// Example: POST /api/loans/LOAN_ID/transactions
		// {"type":"FEE", "fees":[{"encodedKey":"8a80816752715c34015278bd4792084b", "amount":"20" }
		// ],"repayment":"2","notes":"test"}

		// Example: POST /api/savings/SAVINGS_ID/transactions
		// {"type":"FEE", "fees":[{"encodedKey":"8a80816752715c34015278bd4792084b", "amount":"25.50" }
		// ],"notes":"test"}

		// Create JSONApplyManualFee. It extends JSONTransactionRequest and supports repayment number parameter
		JSONApplyManualFee request = new JSONApplyManualFee();

		// Add Transaction Fees to the request
		setTransactionFees(request, transactionFees);

		// Set repayment number
		request.setRepayment(repaymentNumber);

		// Add notes
		request.setNotes(notes);
		return request;

	}

	/**
	 * Helper to add custom predefined fees to a JSON Transaction Request. CustomPredefinedFees must be converted into
	 * JSONFeeRequest format when POSTing JSONTransactionRequest
	 * 
	 * @param request
	 *            JSON Transaction Request
	 * @param transactionFees
	 *            transaction fees to be added to the request
	 */
	private static void setTransactionFees(JSONTransactionRequest request, List<CustomPredefinedFee> transactionFees) {

		if (request == null) {
			return;
		}
		// Specifying disbursement predefined Fees in POST JSON transaction is available since Mambu 4.0. See MBU-11853
		// Example : POST {"type":"DISBURSMENT",
		// "fees":[{"encodedKey":"8a80816752715c34015278bd4792084b" },{
		// "encodedKey":"8a808167529f477a0152a1d3fe390336","amount":"11"}]}

		request.setPredefinedFeeInfo(null);
		if (transactionFees != null && transactionFees.size() > 0) {
			// Add fees converting CustomPredefinedFee to an expected JSONFeeRequest
			List<JSONFeeRequest> fees = new ArrayList<>();
			for (CustomPredefinedFee custFee : transactionFees) {
				PredefinedFee predefinedFee = custFee.getFee();
				String feeEncodedKey = predefinedFee != null ? predefinedFee.getEncodedKey() : null;

				// Make JSONFeeRequest
				JSONFeeRequest jsonFee = new JSONFeeRequest();
				jsonFee.setEncodedKey(feeEncodedKey); // set key from PredefinedFee
				// Set amount. Must be not null only for fees with no amount defined in the product. See MBU-8811
				jsonFee.setAmount(custFee.getAmount()); // set amount from CustomPredefinedFee
				fees.add(jsonFee);
			}
			request.setPredefinedFeeInfo(fees);
		}
	}

	/**
	 * Create params map with as JSON request message for a transaction type
	 * 
	 * @param transactionType
	 *            transaction type string. Example: "DISBURSEMENT"
	 * @param transactionRequest
	 *            jSON transaction request. If null then the JSON string with only the transactionType is returned.
	 * @return params map with a JSON_OBJECT included
	 */
	public static ParamsMap makeParamsForTransactionRequest(String transactionType,
			JSONTransactionRequest transactionRequest) {
		// JSONTransactionRequest contains all required for a request fields except the transaction type
		// Create JSON string for a transactionReqest and add type parameter in a format of: "type":"DISBURSEMENT"

		// Make "type":"transactionType" to be added to the JSON string
		JsonObject jsonObject;
		Gson gson = GsonUtils.createGson();
		if (transactionRequest == null) {
			// if nothing on the request, allow sending just the transaction type as a JSON
			jsonObject = new JsonObject();
			jsonObject.addProperty(APIData.TYPE, transactionType);
		} else {
			// Create JSON string for the JSONTransactionRequest first
			JsonElement transactionJson = gson.toJsonTree(transactionRequest, transactionRequest.getClass());
			// Add transaction type
			jsonObject = transactionJson.getAsJsonObject();
			jsonObject.addProperty(APIData.TYPE, transactionType);
		}

		String jsonRequest = gson.toJson(jsonObject);
		// return params map with the generated JSON
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(APIData.JSON_OBJECT, jsonRequest);
		return paramsMap;

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
	 * Create ParamsMap with a map of fields for the GET loan schedule for the product API. Only fields applicable to
	 * the API are added to the params map
	 * 
	 * @param account
	 *            input loan account
	 * @param apiDefinition
	 *            api definition containing custom serializer for loan account to support generating request only with
	 *            those fields required by the GET schedule API
	 * @return params map with fields for an API request
	 */
	public static ParamsMap makeParamsForLoanSchedule(LoanAccount account, ApiDefinition apiDefinition) {

		// Verify that account is not null
		if (account == null) {
			throw new IllegalArgumentException("Loan Account must not be null");
		}
		// Create API JSON string using apiDefinition with a custom serializer
		JsonElement object = ServiceHelper.makeApiJsonElement(account, apiDefinition);
		// For this GET API we need to create params map with all individual params separately. This API uses
		// "x-www-form-urlencoded" content type
		// Convert Json object with the applicable fields into a ParamsMap.
		Type type = new TypeToken<ParamsMap>() {}.getType();
		ParamsMap params = GsonUtils.createGson(APIData.yyyyMmddFormat).fromJson(object.getAsJsonObject(), type);
		return params;

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

		// Use provided dateTimeFormat. Default format will be used if null
		return GsonUtils.createGson(dateTimeFormat).toJson(object, object.getClass());
	}

	/**
	 * Generate a JSON string for an object and with the specified ApiDefinition
	 * 
	 * @param object
	 *            object
	 * @param apiDefinition
	 *            API definition
	 * @return JSON string for the object
	 */
	public static <T> String makeApiJson(T object, ApiDefinition apiDefinition) {

		return GsonUtils.createSerializerGson(apiDefinition).toJson(object, object.getClass());

	}

	/**
	 * Generate a JsonElement for an object and with the specified ApiDefinition
	 * 
	 * @param object
	 *            object
	 * @param apiDefinition
	 *            API definition
	 * @return JsonElement for an object
	 */
	public static <T> JsonElement makeApiJsonElement(T object, ApiDefinition apiDefinition) {

		return GsonUtils.createSerializerGson(apiDefinition).toJsonTree(object, object.getClass());

	}

	/**
	 * Convenience helper to make parameters map which contains only pagination parameters: offset and limit
	 * 
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null the must be an integer greater than zero
	 * @return ParamsMap
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
			return JSONLoanAccount.class;
		case SAVINGS_ACCOUNT:
			return JSONSavingsAccount.class;
		default:
			return null;

		}
	}

	/**
	 * Helper to determine the type of the Undo Closer Transaction for a closed loan account. The transaction type
	 * needed in UNDO closer API transactions. Can be also used to determine ahead of time if the UNDO Closer
	 * transaction can be performed via API for the specified account
	 * 
	 * See MBU-13190. As of Mambu 4.2 the following UNDO closer types are supported "UNDO_REJECT", "UNDO_WITHDRAWN",
	 * "UNDO_CLOSE"
	 * 
	 * @param account
	 *            loan account. Must not be null and its state must not be null.
	 * @return UNDO close transaction type. Return null if account is not closed or if its closer type is not supported
	 *         by Mambu API
	 */
	public static String getUndoCloserTransactionType(LoanAccount account) {

		// UNDO Closing loan accounts is available since Mambu 4.4. See MBU-13190

		if (account == null || account.getState() == null) {
			throw new IllegalArgumentException("Account and its state must not be null");
		}
		// Get current state and sub-state
		AccountState accountState = account.getState();
		AccountState accountSubState = account.getSubState();
		// Determine the UNDO Transaction Type parameter based on how the account was closed
		switch (accountState) {
		case CLOSED:
			// Null sub-state is set by Mambu if for accounts closed with all obligations met
			if (accountSubState == null) {
				// Account was closed with all obligations met
				return APIData.UNDO_CLOSE;
			}
			// WITHDRAW sub-state is supported by Mambu API for accounts in CLOSED state
			switch (accountSubState) {
			case WITHDRAWN:
				// Account was closed withdrawn
				return APIData.UNDO_WITHDRAWN;
			default:
				return null;
			}
		case CLOSED_REJECTED:
			// Account was closed rejected. No need to check sub-state
			return APIData.UNDO_REJECT;
		default:
			// Only CLOSED and CLOSED_REJECTED states are supported by loan API
			return null;
		}

	}

	/**
	 * Helper to determine the type of the Undo Closer Transaction for a closed savings account. The transaction type
	 * needed in UNDO closer API transactions. Can be also used to determine ahead of time if the UNDO Closer
	 * transaction can be performed via API for the specified account
	 * 
	 * See MBU-13193. As of Mambu 4.2 the following UNDO closer types are supported "UNDO_REJECT", "UNDO_WITHDRAWN",
	 * "UNDO_CLOSE"
	 * 
	 * @param account
	 *            savings account. Must not be null and its state must not be null.
	 * @return UNDO close transaction type. Return null if account is not closed or if its closer type is not supported
	 *         by Mambu API
	 */
	public static String getUndoCloserTransactionType(SavingsAccount account) {

		// UNDO Closing loan accounts is available since Mambu 4.4. See MBU-13193

		if (account == null || account.getAccountState() == null) {
			throw new IllegalArgumentException("Account and its state must not be null");
		}
		// Get current state and sub-state
		AccountState accountState = account.getAccountState();
		// Determine the UNDO Transaction Type parameter based on how the account was closed
		switch (accountState) {
		case CLOSED:
			// Account was closed with all zero balance
			return APIData.UNDO_CLOSE;
		case WITHDRAWN:
			// Account was closed withdrawn
			return APIData.UNDO_WITHDRAWN;
		case CLOSED_REJECTED:
			// Account was closed rejected
			return APIData.UNDO_REJECT;
		default:
			// Only CLOSED, WITHDRAWN and CLOSED_REJECTED states are supported by Savings API
			return null;
		}
	}

	/**
	 * Gets the encodedKey or the ID from the account passed as parameter in a call to this method.
	 * 
	 * @param account
	 *            The account used to obtain the ID or the encoded key from.
	 * @return a String key representing the encodedKey or the ID or null if both are null.
	 */
	public static String getKeyForAccount(Account account) {

		if (account == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		String encodedKey = account.getEncodedKey();
		String accountId = account.getId();

		return encodedKey != null ? encodedKey : accountId;
	}
}
