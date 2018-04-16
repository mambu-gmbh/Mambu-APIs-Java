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
import com.mambu.accounts.shared.model.InterestAccountSettings;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.savings.shared.model.SavingsAccount;

/**
 * SavingsAccountPatchJsonSerializer implements custom JsonSerializer for PATCH Savings Account API requests. It
 * specifies PATCH API fields inclusion strategy, as well as providing custom JSON formating as expected by Mambu API
 * specification for this API
 * 
 * For more details on Mambu PATCH Savings API specification see MBU-10447 and @see <a
 * href="https://developer.mambu.com/customer/en/portal/articles/1616216-savings-api">Savings API</a>
 * 
 * @author mdanilkis
 * 
 */
public class SavingsAccountPatchJsonSerializer implements JsonSerializer<SavingsAccount> {

	/**
	 * A list of SavingsAccount class fields supported by the PATCH savings account API. See MBU-10447.
	 * 
	 * Note in Mambu 4.1 the interest rate and overdraft interest rate related fields were moved inside the new
	 * "interestRateSettings" and "overdraftInterestRateSettings" classes respectively. The interest rate related fields
	 * now need to be copied from these classes to be placed at the top, account level for the PATCH savings API
	 * 
	 */
	private final static Set<String> modifiableSavingsAccountFields = new HashSet<String>(Arrays.asList(
			APIData.MAX_WITHDRAWAL_AMOUNT, 
			APIData.RECOMMENDED_DEPOSIT_AMOUNT, 
			APIData.TARGET_AMOUNT,
			APIData.OVERDRAFT_LIMIT, 
			APIData.OVERDRAFT_EXPIRY_DATE
			));

	// Create Inclusion Strategy with only those fields supported by PATCH Savings API
	private final static JsonFieldsInclusionStrategy savingsPatchInclusionStrategy;
	static {
		savingsPatchInclusionStrategy = new JsonFieldsInclusionStrategy(SavingsAccount.class,
				modifiableSavingsAccountFields);

	}

	public SavingsAccountPatchJsonSerializer() {
	}

	// Serialize using custom Inclusion Strategy
	@Override
	public JsonElement serialize(SavingsAccount savingsAccount, Type typeOfSrc, JsonSerializationContext context) {
		GsonBuilder gsonBuilder = GsonUtils.createGsonBuilder();
		// Add savingsPatchInclusionStrategy exclusionStrategy
		gsonBuilder.addSerializationExclusionStrategy(savingsPatchInclusionStrategy);

		Gson gson = gsonBuilder.create();
		JsonElement savingsAccountJsonElement = gson.toJsonTree(savingsAccount);
		JsonObject result = savingsAccountJsonElement.getAsJsonObject();

		// Adjust Interest Rate fields: copy them from the "interestRateSettings" (InterestAccountSettings.class)
		adjustInterestRateFields(result);

		// Adjust Overdraft Interest Rate fields: copy them from "overdraftInterestSettings"
		// (InterestAccountSettings.class)
		adjustOverdraftInterestRateFields(result);

		// Return result as "{\"savingsAccount\":" +{ accountFields + "}}";
		JsonObject savingsPatchObject = new JsonObject();
		savingsPatchObject.add(APIData.SAVINGS_ACCOUNT, result);
		return savingsPatchObject;
	}

	/**
	 * Add fields needed from the "interestRateSettings" to the message. Place them at the account level. Remove
	 * "interestRateSettings" from the JSON
	 * 
	 * @param jsonResult
	 *            JsonObject where the fields from the interestRateSettings are copied into
	 */
	private void adjustInterestRateFields(JsonObject jsonResult) {
		// Get "interestRateSettings"
		JsonObject interestRateSettings = jsonResult.getAsJsonObject(APIData.INTEREST_SETTINGS);
		if (interestRateSettings != null) {
			JsonElement interestRate = interestRateSettings.get(APIData.INTEREST_RATE);
			JsonElement interestRateSpread = interestRateSettings.get(APIData.INTEREST_RATE_SPREAD);
			// Add to the our flat JSON
			JsonHelper.addValueIfNotNullValue(jsonResult, APIData.INTEREST_RATE, interestRate);
			JsonHelper.addValueIfNotNullValue(jsonResult, APIData.INTEREST_RATE_SPREAD, interestRateSpread);
			// Remove interestRateSettings:{}. It's not supported by API
			jsonResult.remove(APIData.INTEREST_SETTINGS);
		}
	}

	/**
	 * Add fields needed from the overdraftInterestSettings to the message. Place them at acount level. Remove
	 * overdraftInterestSettings from the JSON
	 * 
	 * @param jsonResult
	 *            JsonObject where the fields from the overdraftInterestRateSettings are copied into
	 */
	private void adjustOverdraftInterestRateFields(JsonObject jsonResult) {
		// Get "overdraftInterestSettings"
		JsonObject overdraftRateSettings = jsonResult.getAsJsonObject(APIData.OVERDRAFT_INTEREST_SETTINGS);
		if (overdraftRateSettings != null) {
			JsonElement overdraftRate = overdraftRateSettings.get(APIData.INTEREST_RATE);
			JsonElement overdraftRateSpread = overdraftRateSettings.get(APIData.INTEREST_RATE_SPREAD);
			// Copy overdraft fields using different field names, as required by PATCH savings API:
			// as OVERDRAFT_INTEREST_RATE and OVERDRAFT_SPREAD at account level
			JsonHelper.addValueIfNotNullValue(jsonResult, APIData.OVERDRAFT_INTEREST_RATE, overdraftRate);
			JsonHelper.addValueIfNotNullValue(jsonResult, APIData.OVERDRAFT_SPREAD, overdraftRateSpread);
			// Remove overdraftInterestSettings:{}. It's not supported by API
			jsonResult.remove(APIData.OVERDRAFT_INTEREST_SETTINGS);
		}
	}
}
