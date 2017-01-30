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
import com.mambu.clients.shared.model.Client;
import com.mambu.core.shared.model.ClientRole;

/**
 * ClientPatchJsonSerializer implements custom JsonSerializer for PATCH Client API requests. It specifies PATCH API
 * fields inclusion strategy as well as providing custom JSON formating as expected by Mambu API specification for this
 * API
 * 
 * For more details on Mambu PATCH Client API specification see MBU-11443, MBU-11868 and @see
 * <a href ="https://developer.mambu.com/customer/en/portal/articles/1617472-clients-api">Clients API</a>
 * 
 * @author mdanilkis
 * 
 */
public class ClientPatchJsonSerializer implements JsonSerializer<Client> {

	/**
	 * A list of fields supported by PATCH Client API. See MBU-11443, MBU-11868
	 * 
	 * As of Mambu 4.1 the following fields can be patched: id, clientRoleId, firstName, lastName, middleName,
	 * homePhone, mobilePhone1, birthDate, emailAddress, gender, state, notes, preferredLanguage
	 * 
	 */
	private final static Set<String> clientPatchFields = new HashSet<String>(Arrays.asList(APIData.ID,
			APIData.FIRST_NAME, APIData.LAST_NAME, APIData.MIDDLE_NAME, APIData.HOME_PHONE, APIData.MOBILE_PHONE_1,
			APIData.EMAIL_ADDRESS, APIData.BIRTH_DATE, APIData.GENDER, APIData.STATE, APIData.NOTES,
			APIData.PREFERRED_LANGUAGE, APIData.ASSIGNED_BRANCH_KEY, APIData.ASSIGNED_CENTRE_KEY, APIData.ASSIGNED_USER_KEY));

	// Create API Fields InclusionStrategy
	private final static JsonFieldsInclusionStrategy clientPatchInclusionStrategy = new JsonFieldsInclusionStrategy(
			Client.class, clientPatchFields);

	public ClientPatchJsonSerializer() {

	}

	// Serialize API request using custom InclusionStrategy and adjusting Client Role fields
	@Override
	public JsonElement serialize(Client client, Type typeOfSrc, JsonSerializationContext context) {
		GsonBuilder gsonBuilder = GsonUtils.createGsonBuilder();
		// Add inclusion strategy
		gsonBuilder.addSerializationExclusionStrategy(clientPatchInclusionStrategy);

		Gson gson = gsonBuilder.create();
		JsonElement clientJsonElement = gson.toJsonTree(client);
		JsonObject clientResult = clientJsonElement.getAsJsonObject();

		// Adjust request for ClientRole ID: ClientRole in this PATCH API is not sent as a clientRole:{encodedKey:"12"}.
		// Mambu expects it in a format: "clientRoleId":"12"
		adjustPatchClientRole(client, clientResult);

		// Send as "client:{client fields}" as per Mambu API specification
		JsonObject clientObject = new JsonObject();
		clientObject.add(APIData.CLIENT, clientJsonElement);
		return clientObject;
	}

	/**
	 * Adjust ClientRole ID field: ClientRole's encoded key must be specified in a separate field, "clientRoleId" field.
	 * See MBU-11868
	 * 
	 * @param client
	 *            client
	 * @param jsonResult
	 *            JSON object where the whole "ClientRole" object is replaced with "clientRoleId":"123"
	 */
	private void adjustPatchClientRole(Client client, JsonObject jsonResult) {
		ClientRole clientRole = client.getClientRole();
		if (clientRole != null) {
			String clientRoleKey = clientRole.getEncodedKey();
			// Add role's encoded key as clientRoleId. Example: "clientRoleId:"12345"
			jsonResult.addProperty(APIData.CLIENT_ROLE_ID, clientRoleKey);
			// Now we need to remove "clientRole" object
			jsonResult.remove(APIData.CLIENT_ROLE);
		}
	}
}
