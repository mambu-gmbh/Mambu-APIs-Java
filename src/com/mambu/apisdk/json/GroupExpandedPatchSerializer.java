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

/**
 * GroupExpandedPatchSerializer implements custom JsonSerializer for PATCH Group API requests. It specifies PATCH API
 * fields inclusion strategy as well as providing custom JSON formating as expected by Mambu API specification for this
 * API
 * 
 * For more details on Mambu PATCH Group API specification see MBU-11443, MBU-12985 and 
 * {@link https://mambucom.jira.com/browse/MBU-12985}
 * 
 * @author acostros
 * 
 */

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

	/*
	 * Serialize GroupExpanded using custom inclusion strategy as well as helper method to replace "theGroup" with "group"
	 * as expected by Mambu group API.
	 * 
	 * (non-Javadoc)
	 * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
	 */
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

	/**
	 * Adjusts the GroupExpanded, JSON Object, by replacing "theGroup" element with "group" element.
	 * Also copies all the properties from "theGroup" element into "group". 
	 * Adjustment needed to be compliant with specifications for the Group API,
	 * which expects "group" not "theGroup" element. 
	 * 
	 * @param groupExpandedObject
	 *            GroupExpanded as JSON object
	 */
	private void adjustGroupElement(JsonObject groupExpandedObject) {
		JsonObject theGroup = groupExpandedObject.getAsJsonObject(APIData.THE_GROUP);
		if(theGroup != null){
			JsonElement theGroupElement = groupExpandedObject.get(APIData.THE_GROUP);
			groupExpandedObject.add(APIData.GROUP, theGroupElement);
			groupExpandedObject.remove(APIData.THE_GROUP);
		}
	}
}
