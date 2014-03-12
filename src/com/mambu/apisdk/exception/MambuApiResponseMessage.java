package com.mambu.apisdk.exception;

import com.google.gson.JsonSyntaxException;
import com.mambu.apisdk.util.APIExceptionData;
import com.mambu.apisdk.util.GsonUtils;

/**
 * Encapsulation the API's application exception's Response - which might be included within Mambu exception APIs
 * message. This Class is to represent Mambu API response message object in Java API SDK.
 * 
 * When a Mambu API application exception is thrown, a formatted application error message optionally added to the
 * exception by Mambu. The purpose of this class is to provide helpers to create and parse this message, which contains
 * the following pars: returnCode, returnStatus, errorSource (optionally)
 * 
 * e.g,{"returnCode" 902,"returnStatus":"REQUIRED_CUSTOM_FIELD_MISSING","errorSource":"Loan_Purpose_Loan_Accounts"}
 * e.g.{"returnCode":851,"returnStatus":"INVALID_CENTRE_ID"}
 * 
 * 
 * @author mdanilkis
 * 
 */

public class MambuApiResponseMessage {

	private int returnCode;
	private String returnStatus;
	private String errorSource;

	// Default constructor
	public MambuApiResponseMessage() {
		returnCode = -1;
		returnStatus = null;
		errorSource = null;
	}

	// Constructor with individual response parts
	public MambuApiResponseMessage(int code, String status, String source) {
		returnCode = code;
		returnStatus = status;
		errorSource = source;

	}

	// Constructor with the individual parts but without the returnCode. Look up Error code using APIExceptionData class
	// E,g, lookup 902 by REQUIRED_CUSTOM_FIELD_MISSING as in {"returnCode"
	// 902,"returnStatus":"REQUIRED_CUSTOM_FIELD_MISSING","errorSource":"Loan_Purpose_Loan_Accounts"}
	public MambuApiResponseMessage(String status, String source) {
		returnCode = APIExceptionData.getResponseCode(status);
		returnStatus = status;
		errorSource = source;

	}

	// Constructor with the actual Mambu's exception error message
	// e.g ,"returnStatus":"REQUIRED_CUSTOM_FIELD_MISSING","errorSource":"Loan_Purpose_Loan_Accounts"
	// Given an API error message the application can convert it into a MambuApiResponseMessage object
	public MambuApiResponseMessage(String mambuResponse) {
		this();

		if (mambuResponse == null) {
			return;
		}

		MambuApiResponseMessage obj = null;

		// Need to check for valid Json format: not all Mambu exceptions would have properly formatted response
		// (returnCode, returnStatus, errorSource). Mambu exceptions thrown for IO errors, for example, do not.

		try {

			obj = (MambuApiResponseMessage) GsonUtils.createGson().fromJson(mambuResponse,
					MambuApiResponseMessage.class);

		} catch (JsonSyntaxException e) {
			returnCode = -1;
			returnStatus = "";
			errorSource = "";
			return;
		}

		returnCode = obj.returnCode;
		returnStatus = obj.returnStatus;
		errorSource = obj.errorSource;

	}
	// Constructor with Mambu Api exception.
	// Given Mambu's APIexception the application can convert it into a MambuApiResponseMessage object
	public MambuApiResponseMessage(MambuApiException e) {

		this(e.getMessage());

	}

	// Copy constructor
	public MambuApiResponseMessage(MambuApiResponseMessage obj) {
		this(obj.getReturnCode(), obj.getReturnStatus(), obj.getErrorSource());

	}

	// getters
	public int getReturnCode() {
		return returnCode;
	}

	public String getReturnStatus() {
		return returnStatus;
	}

	public String getErrorSource() {
		return errorSource;
	}

	// Get the json error message back. Creates a formatted message which can be used for throwing API exceptions
	public String getMambuResponseMessage() {
		String jsonResponse = GsonUtils.createGson().toJson(this, MambuApiResponseMessage.class);
		return jsonResponse;

	}
}
