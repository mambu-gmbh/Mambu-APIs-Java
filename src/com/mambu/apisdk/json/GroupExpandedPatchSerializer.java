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
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.clients.shared.model.GroupMember;
import com.mambu.clients.shared.model.GroupRole;

public class GroupExpandedPatchSerializer implements JsonSerializer<GroupExpanded> {

	/**
	 * A list of fields from GroupExpanded supported by PATCH Group API. See MBU-12985
	 * 
	 * As of Mambu 4.2 the following fields can be patched: id, groupName, notes, emailAddress, mobilePhone1
	 * homePhone, preferredLanguage, assignedBranchKey, assignedCentreKey. And it also allows the replacing of 
	 * the fields in groupMembers and groupRoles (Warning: these last two are just replacements operations not patch ones).
	 * 
	 */
	private final static Set<String> groupExpandedPatchFields = new HashSet<String>(Arrays.asList(
			APIData.THE_GROUP, APIData.GROUP_MEMBERS, APIData.GROUP_ROLES
			));
	
	/**
	 * A list of fields from the Group class supported by PATCH group API.
	 * 
	 */
	private final static Set<String> groupPatchFields = new HashSet<String>(Arrays.asList(APIData.ID,
			APIData.GROUP_NAME, APIData.NOTES, APIData.EMAIL_ADDRESS, APIData.MOBILE_PHONE_1, APIData.HOME_PHONE,
			APIData.PREFERRED_LANGUAGE, APIData.ASSIGNED_BRANCH_KEY, APIData.ASSIGNED_CENTRE_KEY 
			));
	
	/**
	 * A list of fields from the group roles supported by PATCH group API.
	 * 
	 */
	private final static Set<String> groupRolesFields = new HashSet<String>(Arrays.asList(APIData.GROUP_ROLE_NAME_KEY,
			APIData.CLIENT_KEY)); 
	
	/**
	 * A list of fields from the group members supported by PATCH group API.
	 * 
	 */
	private final static Set<String> groupMembersFields = new HashSet<String>(Arrays.asList(APIData.CLIENT_KEY));

	private final static JsonFieldsInclusionStrategy groupExpandedPatchInclusionStrategy;
	
	// Create inclusion strategy for Group PATCH API. Include allowed Group, GroupMembers  
	// and GroupRoles fields
	static{
		groupExpandedPatchInclusionStrategy = new JsonFieldsInclusionStrategy(GroupExpanded.class, groupExpandedPatchFields);
		groupExpandedPatchInclusionStrategy.addInclusion(Group.class, groupPatchFields);
		groupExpandedPatchInclusionStrategy.addInclusion(GroupRole.class, groupRolesFields);
		groupExpandedPatchInclusionStrategy.addInclusion(GroupMember.class, groupMembersFields);
	}
	public GroupExpandedPatchSerializer() {
	}

	// Serialize GroupExpanded using custom inclusion strategy as well as helper methods to adjust 
	// the "theGroup" field.
	@Override
	public JsonElement serialize(GroupExpanded groupExpanded, Type type, JsonSerializationContext context) {
		GsonBuilder gsonBuilder = GsonUtils.createGsonBuilder();
		// Add our groupExpandedPatchInclusionStrategy
		gsonBuilder.addSerializationExclusionStrategy(groupExpandedPatchInclusionStrategy);
		Gson gson = gsonBuilder.create();
		
		JsonElement groupJsonElement = gson.toJsonTree(groupExpanded, type);
		JsonObject groupExpandedObject = groupJsonElement.getAsJsonObject();
		
		// Adjust format for "theGroup" field, replaces it with "group" field 
		// as the group API expects it  
		adjustGroupElement(groupExpandedObject);
	
		return groupExpandedObject;
	}

	private void adjustGroupElement(JsonObject groupExpandedObject) {
		/* Adjustment needed to change the element "theGroup" to be "group" as per API input required */
		JsonObject theGroup = groupExpandedObject.getAsJsonObject(APIData.THE_GROUP);
		if(null != theGroup){
			JsonObject newGroup= new JsonObject();
			
			JsonElement id =theGroup.get(APIData.ID);
			JsonElement groupName =theGroup.get(APIData.GROUP_NAME);
			JsonElement notes =theGroup.get(APIData.NOTES);
			JsonElement emailAddress =theGroup.get(APIData.EMAIL_ADDRESS);
			JsonElement mobilePhone1 =theGroup.get(APIData.MOBILE_PHONE_1);
			JsonElement homePhone =theGroup.get(APIData.HOME_PHONE);
			JsonElement preferredLanguage =theGroup.get(APIData.PREFERRED_LANGUAGE);
			JsonElement assignedBranchKey =theGroup.get(APIData.ASSIGNED_BRANCH_KEY);
			JsonElement assignedCentreKey =theGroup.get(APIData.ASSIGNED_CENTRE_KEY);
			
			JsonHelper.addValueIfNotNullValue(newGroup, APIData.ID, id);
			JsonHelper.addValueIfNotNullValue(newGroup, APIData.GROUP_NAME, groupName);
			JsonHelper.addValueIfNotNullValue(newGroup, APIData.NOTES, notes);
			JsonHelper.addValueIfNotNullValue(newGroup, APIData.EMAIL_ADDRESS, emailAddress);
			JsonHelper.addValueIfNotNullValue(newGroup, APIData.MOBILE_PHONE_1, mobilePhone1);
			JsonHelper.addValueIfNotNullValue(newGroup, APIData.HOME_PHONE, homePhone);
			JsonHelper.addValueIfNotNullValue(newGroup, APIData.PREFERRED_LANGUAGE, preferredLanguage);
			JsonHelper.addValueIfNotNullValue(newGroup, APIData.ASSIGNED_BRANCH_KEY, assignedBranchKey);
			JsonHelper.addValueIfNotNullValue(newGroup, APIData.ASSIGNED_CENTRE_KEY, assignedCentreKey);

			groupExpandedObject.add(APIData.GROUP, newGroup);
			groupExpandedObject.remove(APIData.THE_GROUP);
		}
	}
}
