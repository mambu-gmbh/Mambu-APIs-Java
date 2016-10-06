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
import com.mambu.savings.shared.model.SavingsAccount;

/**
 * SettlementAccountPatchSerializer implements custom JsonSerializer for PATCH Loan accounts API request. It specifies
 * PATCH API fields inclusion strategy, as well as providing custom JSON formating as expected by Mambu API specified
 * for this API.
 * 
 * For more details on Mambu PATCH Loans API specification see MBU-14409 and @see
 * <a href="https://developer.mambu.com/customer/en/portal/articles/1617482-loans-api">Loans API</a>
 * 
 * @author acostros
 *
 */
public class SettlementAccountPatchSerializer implements JsonSerializer<SavingsAccount> {

	/**
	 * A list of SavingsAccount class fields supported by the PATCH settlement account on a loan. See MBU-14409.
	 * 
	 */
	private final static Set<String> modifiableSavingsAccountFields = new HashSet<String>(Arrays.asList(
			APIData.ENCODED_KEY, APIData.ID));

	// Create Inclusion Strategy with only those fields supported by PATCH loans's settlements account API
	private final JsonFieldsInclusionStrategy settlementsPatchInclusionStrategy;
	{
		settlementsPatchInclusionStrategy = new JsonFieldsInclusionStrategy(SavingsAccount.class,
				modifiableSavingsAccountFields);
	}

	// Serialize using custom Inclusion Strategy
	@Override
	public JsonElement serialize(SavingsAccount savingsAccount, Type typeOfSrc, JsonSerializationContext context) {

		GsonBuilder gsonBuilder = GsonUtils.createGsonBuilder();
		// Add savingsPatchInclusionStrategy exclusionStrategy
		gsonBuilder.addSerializationExclusionStrategy(settlementsPatchInclusionStrategy);

		Gson gson = gsonBuilder.create();
		JsonElement savingsAccountJsonElement = gson.toJsonTree(savingsAccount);
		JsonObject result = savingsAccountJsonElement.getAsJsonObject();

		// Return result as "{\"savingsAccount\":" +{ id or encodedKey and its value + "}}";
		JsonObject savingsPatchObject = new JsonObject();
		savingsPatchObject.add(APIData.SAVINGS_ACCOUNT, result);
		return savingsPatchObject;
	}

}
