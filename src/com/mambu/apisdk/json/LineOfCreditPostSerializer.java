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
import com.mambu.linesofcredit.shared.model.LineOfCredit;

/**
 * LineOfCreditPostSerializer implements custom JsonSerializer for POST API requirements. It specifies the PATCH API
 * fields inclusion strategy as well as providing custom JSON formatting as expected by Mambu API specification.
 * 
 * For more details on Mambu POST LineOfCredit API specification see MBU-13767 and
 * {@link https://mambucom.jira.com/browse/MBU-13767}
 * 
 * @author acostros
 *
 */

public class LineOfCreditPostSerializer implements JsonSerializer<LineOfCredit> {

	/**
	 * A list of fields from LineOfCredit supported by POST LineOfCredit API. See MBU-13757.
	 * 
	 * As of Mambu 4.2 the following fields can be posted: id, groupKey, clientKey, notes, startDate, expireDate,
	 * lastModifiedDate, creationDate, state, amount.
	 * 
	 */
	private final static Set<String> modifiableLineOfCreditFields = new HashSet<String>(
			Arrays.asList(APIData.ID, APIData.GROUP_KEY, APIData.CLIENT_KEY, APIData.START_DATE, APIData.EXPIRE_DATE,
					APIData.AMOUNT, APIData.NOTES, APIData.STATE, APIData.CREATION_DATE, APIData.LAST_MODIFIED_DATE));

	// Create inclusion strategy for LineOfCredit POST API. Include allowed LineOfCredit fields
	private final static JsonFieldsInclusionStrategy lineOfCreditPatchStrategy;
	static {
		lineOfCreditPatchStrategy = new JsonFieldsInclusionStrategy(LineOfCredit.class, modifiableLineOfCreditFields);
	}

	/**
	 * Serializes LineOfCredit using custom inclusion strategy as expected by Mambu lineOfCredit API.
	 * 
	 */
	@Override
	public JsonElement serialize(LineOfCredit lineOfCredit, Type typeOfSrc, JsonSerializationContext context) {

		GsonBuilder gsonBuilder = GsonUtils.createGsonBuilder();
		// Add our lineOfCreditPatchStrategy
		gsonBuilder.addSerializationExclusionStrategy(lineOfCreditPatchStrategy);

		Gson gson = gsonBuilder.create();
		JsonElement loanAccountJsonElement = gson.toJsonTree(lineOfCredit);
		JsonObject lineOfcreditResult = loanAccountJsonElement.getAsJsonObject();

		// Return as "{\"lineOfCredit\":" +{ lineOfCreditDetails + "}}";
		JsonObject loanSubsetObject = new JsonObject();
		loanSubsetObject.add(APIData.LINE_OF_CREDIT, lineOfcreditResult);
		return loanSubsetObject;
	}

}
