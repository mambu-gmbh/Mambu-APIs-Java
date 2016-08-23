package com.mambu.apisdk.json;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.loans.shared.model.DisbursementDetails;
import com.mambu.loans.shared.model.LoanAccount;

/**
 * LoanProductScheduleJsonSerializer implements custom JsonSerializer for GET Loan Product Schedule preview API
 * requests. It specifies API fields inclusion strategies, as well as providing custom JSON formating as expected by
 * Mambu API specification
 * 
 * For more details on Mambu GET Loan Product schedule preview API specification see MBU-6789, MBU-7676, MBU-10802 and
 * MBU-11481 and {@link https://developer.mambu.com/customer/en/portal/articles/1616164-loan-products-api}
 * 
 * @author mdanilkis
 * 
 */
public class LoanProductScheduleJsonSerializer implements JsonSerializer<LoanAccount> {

	/**
	 * A list of fields in the LoanAccount supported by the GET loan schedule preview API. See MBU-6789, MBU-7676,
	 * MBU-10802 and MBU-11481.
	 * 
	 * Note, since 4.0 the EXPECTED_DISBURSEMENT_DATE and FIRST_REPAYMENT_DATE are part of the DisbursementDetails class
	 */
	private final static Set<String> loanSchedulePreviewFields = new HashSet<String>(Arrays.asList(APIData.LOAN_AMOUNT,
			APIData.INTEREST_RATE, APIData.REPAYMENT_INSTALLMENTS, APIData.REPAYMENT_PERIOD_COUNT,
			APIData.REPAYMENT_PERIOD_UNIT, APIData.GRACE_PERIOD, APIData.PRNICIPAL_REPAYMENT_INTERVAL,
			APIData.PERIODIC_PAYMENT, APIData.FIXED_DAYS_OF_MONTH, APIData.DISBURSEMENT_DETAILS));

	/**
	 * A list of fields from the DisbursementDetails supported by the GET loan schedule preview API. See MBU-11481
	 */
	private final static Set<String> disbursementFields = new HashSet<String>(Arrays.asList(
			APIData.EXPECTED_DISBURSEMENT_DATE, APIData.FIRST_REPAYMENT_DATE));

	// Specify Inclusion Strategy: specify fields to use from the LoanAccount.class and from the
	// DisbursementDetails.class
	private final static JsonFieldsInclusionStrategy getLoanScheduleInclusionStrategy;
	static {
		getLoanScheduleInclusionStrategy = new JsonFieldsInclusionStrategy(LoanAccount.class, loanSchedulePreviewFields);
		getLoanScheduleInclusionStrategy.addInclusion(DisbursementDetails.class, disbursementFields);
	}

	public LoanProductScheduleJsonSerializer() {

	}

	@Override
	public JsonElement serialize(LoanAccount loanAccount, Type typeOfSrc, JsonSerializationContext context) {
		// GET schedule API is a x-www-form-urlencoded API. Need to specify "yyyyMmddFormat" date time format as
		// expected by this API
		GsonBuilder gsonBuilder = GsonUtils.createGsonBuilder(APIData.yyyyMmddFormat);
		gsonBuilder.addSerializationExclusionStrategy(getLoanScheduleInclusionStrategy);
		Gson gson = gsonBuilder.create();

		JsonElement loanAccountJsonElement = gson.toJsonTree(loanAccount);
		JsonObject loanResult = loanAccountJsonElement.getAsJsonObject();
		// Get loan disbursement dates from the DisbursementDetails class and place them at the account level
		adjustDisbursementDetails(loanResult);
		// Adjust Fixed Days value to the format expected by the API. E.g. 1.15
		adjustFixedDays(loanResult);
		return loanResult;
	}

	/**
	 * Add fields needed from the DisbursementDetails to the jsonResult. Remove DisbursementDetails
	 * 
	 * @param jsonResult
	 *            with expectedDisbursementDate and firstRepaymentDate fields copied from DisbursementDetails
	 */
	private void adjustDisbursementDetails(JsonObject jsonResult) {
		JsonObject disbursementDetails = jsonResult.getAsJsonObject(APIData.DISBURSEMENT_DETAILS);
		if (disbursementDetails != null) {
			// Get disbursementDetails
			JsonElement expectedDisbursementDate = disbursementDetails.get(APIData.EXPECTED_DISBURSEMENT_DATE);
			// Get firstRepaymentDate
			JsonElement firstRepaymentDate = disbursementDetails.get(APIData.FIRST_REPAYMENT_DATE);

			// Place expectedDisbursementDate value at account level
			// Note the "expectedDisbursementDate" value is expected by this particular API as "anticipatedDisbursement"
			JsonHelper.addValueIfNotNullValue(jsonResult, APIData.ANTICIPATE_DISBURSEMENT, expectedDisbursementDate);
			// Place firstRepaymentDate value at account level
			JsonHelper.addValueIfNotNullValue(jsonResult, APIData.FIRST_REPAYMENT_DATE, firstRepaymentDate);
			// Now remove disbursementDetails: {} field
			jsonResult.remove(APIData.DISBURSEMENT_DETAILS);
		}
	}

	/**
	 * Modify JsonObject for FixedDays value format as required by Mambu API: from default [2,15] to 2.15
	 * 
	 * @param jsonResult
	 *            JsonObject with adjusted value for fixed days
	 */
	private void adjustFixedDays(JsonObject jsonResult) {
		// FIXED_DAYS_OF_MONTH field is an Integer array with the data in the format [2,15]. But for this url-encoded
		// API it needs to be converted into a string with no array square brackets: Mambu expects it in this format:
		// "fixedDaysOfMonth"="2,15" See MBU-10802.

		// Get an array and convert it into a string with no square brackets
		JsonElement fixedDaysElement = jsonResult.get(APIData.FIXED_DAYS_OF_MONTH);
		if (fixedDaysElement != null && fixedDaysElement.isJsonArray()
				&& fixedDaysElement.getAsJsonArray().toString().length() >= 2) {
			String arrayData = fixedDaysElement.getAsJsonArray().toString();
			arrayData = arrayData.substring(1, arrayData.length() - 1); // remove surrounding []

			// Replace the original value with the updated value
			jsonResult.remove(APIData.FIXED_DAYS_OF_MONTH);
			if (arrayData.length() > 0) {
				jsonResult.addProperty(APIData.FIXED_DAYS_OF_MONTH, arrayData);
			}
		}
	}

}
