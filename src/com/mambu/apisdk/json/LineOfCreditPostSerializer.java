package com.mambu.apisdk.json;

import java.lang.reflect.Type;

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
 * LineOfCreditPostSerializer implements custom JsonSerializer for POST API requirements. It provides custom JSON
 * formatting as expected by Mambu API specification.
 * 
 * For more details on Mambu POST LineOfCredit API specification see MBU-13767 and
 * {@link https://mambucom.jira.com/browse/MBU-13767}
 * 
 * @author acostros
 *
 */

public class LineOfCreditPostSerializer implements JsonSerializer<LineOfCredit> {

	/**
	 * Serializes LineOfCredit using custom inclusion strategy as expected by Mambu lineOfCredit API.
	 * 
	 */
	@Override
	public JsonElement serialize(LineOfCredit lineOfCredit, Type typeOfSrc, JsonSerializationContext context) {

		GsonBuilder gsonBuilder = GsonUtils.createGsonBuilder();

		Gson gson = gsonBuilder.create();
		JsonElement loanAccountJsonElement = gson.toJsonTree(lineOfCredit);
		JsonObject lineOfcreditResult = loanAccountJsonElement.getAsJsonObject();

		// Return as "{\"lineOfCredit\":" +{ lineOfCreditDetails + "}}";
		JsonObject loanSubsetObject = new JsonObject();
		loanSubsetObject.add(APIData.LINE_OF_CREDIT, lineOfcreditResult);
		return loanSubsetObject;
	}

}
