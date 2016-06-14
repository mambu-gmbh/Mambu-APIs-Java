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
import com.mambu.clients.shared.model.Group;

public class GroupPatchSerializer implements JsonSerializer<Group>{
	
	
	/**
	 * A list of fields supported by PATCH Group API. See MBU-12985
	 * 
	 * As of Mambu 4.2 the following fields can be patched: id, groupName, notes, emailAddress, mobilePhone1
	 * homePhone, preferredLanguage, assignedBranchKey, assignedCentreKey
	 * 
	 */
	private final static Set<String> groupPatchFields = new HashSet<String>(Arrays.asList(APIData.ID,
			APIData.GROUP_NAME, APIData.NOTES, APIData.EMAIL_ADDRESS, APIData.MOBILE_PHONE_1, APIData.HOME_PHONE,
			APIData.PREFERRED_LANGUAGE, APIData.ASSIGNED_BRANCH_KEY, APIData.ASSIGNED_CENTRE_KEY 
			));

	/* Create API Fields InclusionStrategy */
	private final static JsonFieldsInclusionStrategy groupPatchInclusionStrategy = new JsonFieldsInclusionStrategy(
			Group.class, groupPatchFields);

	public GroupPatchSerializer() {
	}

	@Override
	public JsonElement serialize(Group group, Type typeOfSrc, JsonSerializationContext context) {
		GsonBuilder gsonBuilder = GsonUtils.createGsonBuilder();
		gsonBuilder.addSerializationExclusionStrategy(groupPatchInclusionStrategy);
		Gson gson = gsonBuilder.create();
		JsonElement groupJsonElement = gson.toJsonTree(group, typeOfSrc);
		
		JsonObject groupObject = new JsonObject();
		groupObject.add(APIData.GROUP, groupJsonElement);
		return groupObject;
		
	}

}
