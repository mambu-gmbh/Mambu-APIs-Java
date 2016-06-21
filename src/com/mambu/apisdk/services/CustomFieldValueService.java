package com.mambu.apisdk.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.core.shared.model.CustomFieldType;
import com.mambu.core.shared.model.CustomFieldValue;

/**
 * Service class which handles API operations for patching and deleting Custom Field Values for supported Mambu
 * Entities. Currently supported entities include: Client, Group. LoanAccount, SavingsAccount,Branch, Centre, User
 * 
 * 
 * @author mdanilkis
 * 
 */

public class CustomFieldValueService {

	// Mambu Entity managed by this class
	private static MambuEntityType serviceEntity = MambuEntityType.CUSTOM_FIELD_VALUE;

	// Service helper
	protected ServiceExecutor serviceExecutor;

	// Specify Mambu entities supported by the Custom Field Value API: Client, Group. LoanAccount, SavingsAccount,
	// Branch, Centre
	private final static MambuEntityType[] supportedEntities = new MambuEntityType[] { MambuEntityType.CLIENT,
			MambuEntityType.GROUP, MambuEntityType.LOAN_ACCOUNT, MambuEntityType.SAVINGS_ACCOUNT,
			MambuEntityType.BRANCH, MambuEntityType.CENTRE, MambuEntityType.USER };

	// Map MambuEntityType to a CustomFieldType to support testing using CustomFieldType
	// Example: MambuEntityType.CLIENT=> CustomFieldType.CLIENT_INFO
	private final static HashMap<MambuEntityType, CustomFieldType> customFieldTypes;
	static {
		customFieldTypes = new HashMap<>();
		customFieldTypes.put(MambuEntityType.CLIENT, CustomFieldType.CLIENT_INFO);
		customFieldTypes.put(MambuEntityType.GROUP, CustomFieldType.GROUP_INFO);
		customFieldTypes.put(MambuEntityType.LOAN_ACCOUNT, CustomFieldType.LOAN_ACCOUNT_INFO);
		customFieldTypes.put(MambuEntityType.LOAN_TRANSACTION, CustomFieldType.TRANSACTION_CHANNEL_INFO);
		customFieldTypes.put(MambuEntityType.SAVINGS_ACCOUNT, CustomFieldType.SAVINGS_ACCOUNT_INFO);
		customFieldTypes.put(MambuEntityType.SAVINGS_TRANSACTION, CustomFieldType.TRANSACTION_CHANNEL_INFO);
		customFieldTypes.put(MambuEntityType.BRANCH, CustomFieldType.BRANCH_INFO);
		customFieldTypes.put(MambuEntityType.CENTRE, CustomFieldType.CENTRE_INFO);
		customFieldTypes.put(MambuEntityType.USER, CustomFieldType.USER_INFO);
	}

	// Custom Field Values API supports Updating (PATCH) and Deleting (DELETE). Note, custom field values cannot be
	// retrieved separately from the parent entity. (Use GET Entity with full details to retrieve all custom field
	// values)

	/***
	 * Create a new service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public CustomFieldValueService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}

	/***
	 * Update custom field value entity.
	 * 
	 * @param parentEntity
	 *            Mambu entity for which the custom field value is updated. Example: MambuEntity.CLIENT,
	 *            MambuEntity.BRANCH
	 * @param parentEntityId
	 *            entity id or encoded key for the parent entity
	 * @param customFieldValue
	 *            custom field value to be updated
	 * 
	 * @return true if updated successfully
	 * @throws MambuApiException
	 */
	public boolean update(MambuEntityType parentEntity, String parentEntityId, CustomFieldValue customFieldValue)
			throws MambuApiException {
		// Update Custom Field Values API examples:
		// Execute request for PATCH API to update custom field value for a Loan Account. See MBU-6661
		// e.g. PATCH "{ "value": "10" }" /host/api/loans/accointId/custominformation/{customFieldId}

		// PATCH linked Entity value for a custom field value (see MBU-8514)
		// PATCH '{ "linkedEntityKeyValue": "40288a13...." }'// /api/clients/abc123/custominformation/{customFieldId}

		// For Grouped field: PATCH {"value": "10"} /api/clients/abc123/custominformation/{customFieldId}/{groupNumber}
		// See MBU-8340

		// Make custom field value id path. The id path must include group number for grouped fields
		String customFieldIdPath = makeCustomFieldIdPath(customFieldValue);

		// Make Custom Field value which contains only fields needed in API request
		CustomFieldValue apiFieldValue = makePatchApiCustomField(customFieldValue);

		// Submit API request
		return serviceExecutor.updateOwnedEntity(parentEntity, parentEntityId, apiFieldValue, customFieldIdPath);

	}

	/***
	 * Delete custom field value for a Mambu parent entity
	 * 
	 * @param parentEntity
	 *            Mambu entity for which custom field value is deleted. Example: MambuEntity.CLIENT, MambuEntity.BRANCH
	 * @param parentEntityId
	 *            entity id or encoded key for the parent entity
	 * @param customFieldValue
	 *            custom field value to be deleted
	 * @return true if successful
	 * @throws MambuApiException
	 */
	public boolean delete(MambuEntityType parentEntity, String parentEntityId, CustomFieldValue customFieldValue)
			throws MambuApiException {

		// Exammple:Execute request for DELETE API to delete custom field value for a client See MBU-6661
		// e.g. DELETE /host/api/clients/clientId/custominformation/{customFieldId}

		// Example Delete grouped customfield value. See MBU-8340. Need to specify group number
		// DELETE /api/clients/abc123/custominformation/{customFieldId}/{groupNumber}

		// Create custom field value id. The id must include group number for grouped fields
		String customFieldIdPath = makeCustomFieldIdPath(customFieldValue);

		return serviceExecutor.deleteOwnedEntity(parentEntity, parentEntityId, serviceEntity, customFieldIdPath);

	}

	/**
	 * Make Custom Field Value for a PATCH API request. Only certain fields need to be present in the API request.
	 * Specifically, "value" and "linkedEntityKeyValue"
	 * 
	 * @param customFieldValue
	 *            original custom field value
	 * @return api custom field value
	 */
	private CustomFieldValue makePatchApiCustomField(CustomFieldValue customFieldValue) {
		CustomFieldValue apiField = new CustomFieldValue(customFieldValue);

		// Only "value" and "linkedEntityKeyValue" fields need to be present in API request, if defined
		// Set all other fields to null
		apiField.setAmount(null);
		apiField.setToBeDeleted(null);
		apiField.setCustomField(null);
		apiField.setCustomFieldSetGroupIndex(null);
		apiField.setEncodedKey(null);
		apiField.setParentKey(null);
		apiField.setCustomFieldId(null);
		apiField.setIndexInList(null);
		apiField.setLinkedEntitySummary(null);
		apiField.setSkipUniqueValidation(null);

		return apiField;
	}

	/***
	 * Update custom field value for owned entity. This methods allows to update custom fields for entities like
	 * Transaction, for example, where a parent entity is (e.g. loan) and the actual entity (e.g. transaction) as well
	 * as their IDs must be specified.
	 * 
	 * E.g. PATCH "{ "value": "10" }" /api/loans/accountId/transactions/transId/custominformation/{customFieldId}
	 * 
	 * 
	 * Note: as of Mambu 4.1 updating custom fields only for Loan and Savings transactions is supported
	 * 
	 * @param parentEntity
	 *            the parent Mambu entity for which the custom field value is updated. Example: MambuEntity.LOAN_ACCOUNT
	 * @param parentEntityId
	 *            entity id or encoded key for the parent entity (e.g. loan account id)
	 * @param ownedEntity
	 *            Mambu entity for which the custom field value is updated. Example: MambuEntity.LOAN_TRANSACTOION
	 * @param ownedEntityId
	 *            the entity id or encoded key for the owned entity (e.g transaction id)
	 * 
	 * @param customFieldValue
	 *            custom field value to be updated
	 * @return true if updated successfully
	 * @throws MambuApiException
	 */
	public boolean update(MambuEntityType parentEntity, String parentEntityId, MambuEntityType ownedEntity,
			String ownedEntityId, CustomFieldValue customFieldValue) throws MambuApiException {
		// Update Custom Field Values API for Owned entities. Currently used to update custom fields for a specific
		// transaction for a loan or savings account.

		// Available since Mambu 4.1. See MBU-11984
		// Execute PATCH API request to update custom field value for a Loan Account Transaction.
		// E.g. PATCH "{ "value": "10" }" /api/loans/accountId/transactions/transId/custominformation/{customFieldId}

		// E.g. PATCH linked Entity value for a custom field value (see MBU-8514)
		// PATCH '{ "linkedEntityKeyValue": "40"}'
		// /api/savings/accountId/transactions/{id}/custominformation/{customFieldId}

		// Make custom field value id path. May contain group number in a path. E.g. customFieldId.
		String customFieldIdPath = makeCustomFieldIdPath(customFieldValue);

		// Make Custom Field value for API request which contains only fields needed in the request
		CustomFieldValue apiFieldValue = makePatchApiCustomField(customFieldValue);

		// Create Api Definition for this API
		ApiDefinition apiDefinition = makePatchApiDefintion(parentEntity, parentEntityId, ownedEntity, ownedEntityId,
				customFieldIdPath);
		// Submit API request
		return serviceExecutor.executeJson(apiDefinition, apiFieldValue);

	}

	/***
	 * Delete custom field value for owned entity. Used to delete a custom field value where both the parent entity id
	 * and type (e.g. loans) and the owned entity id and type (e.g. transaction) must be specified
	 * 
	 * @param parentEntity
	 *            the parent Mambu entity for which the custom field value is deleted. Example: MambuEntity.LOAN_ACCOUNT
	 * @param parentEntityId
	 *            entity id or encoded key for the parent entity (e.g. loan account id)
	 * @param ownedEntity
	 *            Mambu entity for which the custom field value is deleted. Example: MambuEntity.LOAN_TRANSACTOION
	 * @param ownedEntityId
	 *            the entity id or encoded key for the owned entity (e.g transaction id)
	 * 
	 * @param customFieldValue
	 *            custom field value to be updated
	 * @return true if updated successfully
	 * @throws MambuApiException
	 */
	public boolean delete(MambuEntityType parentEntity, String parentEntityId, MambuEntityType ownedEntity,
			String ownedEntityId, CustomFieldValue customFieldValue) throws MambuApiException {

		// Delete Custom Field Values API for Owned entities. Currently used to delete custom fields for a specific
		// transaction for a loan or savings account.

		// Available since Mambu 4.1. See MBU-11984

		// Exammple:Execute request for DELETE API to delete custom field value for a client See MBU-6661
		// e.g. DELETE /api/loans/abc123/transactions/{id}/custominformation/{customFieldId}

		// Example Delete grouped customfield value. See MBU-8340. Need to specify group number
		// DELETE/api/loans/abc123/transactions/{id}/custominformation/{groupNumber}

		// Make custom field value id path. The id path must include group number for grouped fields
		String customFieldIdPath = makeCustomFieldIdPath(customFieldValue);

		ApiDefinition apiDefinition = makeDeleteApiDefintion(parentEntity, parentEntityId, ownedEntity, ownedEntityId,
				customFieldIdPath);
		// Submit API request
		return serviceExecutor.execute(apiDefinition);

	}

	/***
	 * Add new grouped custom field values for an entity.
	 * 
	 * See MBU-12228- As a Developer, I Need to Add Grouped Custom Fields via PATCH APIs
	 * 
	 * @param parentEntity
	 *            Mambu entity for which the custom field values are added. Example: MambuEntity.CLIENT,
	 *            MambuEntity.BRANCH
	 * @param parentEntityId
	 *            id or encoded key for the parent entity
	 * @param customFieldValues
	 *            custom field value to be added. Must not be null. All custom field values must belong to the same
	 *            Grouped custom field set
	 * 
	 * @return true if updated successfully
	 * @throws MambuApiException
	 */
	public boolean addGroupedFields(MambuEntityType parentEntity, String parentEntityId,
			List<CustomFieldValue> customFieldValues) throws MambuApiException {
		// Add Grouped Custom Field Values
		// Available since Mambu 4.1. See MBU-12228
		// Example: Execute request for PATCH API to add new group of custom field values for a Client:
		// PATCH /api/clients/clientId/custominformation/
		// {"customInformation":[{"customFieldID":"IBAN","value":"DE123456789121243546783" },{"customFieldID":"BIC",
		// "value":"1234566441"}]}

		return patchGroupedCustomFields(parentEntity, parentEntityId, customFieldValues, true);

	}

	/***
	 * Update grouped custom field values for an entity.
	 * 
	 * See MBU-12229- As a Developer, I need to Edit Multiple Grouped Custom Fields via PATCH APIs
	 * 
	 * @param parentEntity
	 *            Mambu entity for which the custom field values are added. Example: MambuEntity.CLIENT,
	 *            MambuEntity.BRANCH
	 * @param parentEntityId
	 *            id or encoded key for the parent entity
	 * @param customFieldValues
	 *            custom field value to be added. Must not be null. All custom field values must belong to the same
	 *            Grouped custom field set. customFieldSetGroupIndex must be present in each field
	 * 
	 * @return true if updated successfully
	 * @throws MambuApiException
	 */
	public boolean updateGroupedFields(MambuEntityType parentEntity, String parentEntityId,
			List<CustomFieldValue> customFieldValues) throws MambuApiException {

		// Available since Mambu 4.1. See MBU-12229.
		// Example: Execute request for PATCH API to Update group custom field values for a Client:
		// PATCH /api/clients/clientId/custominformation/
		// {"customInformation":[{"customFieldID":"IBAN","value":"DE123456789121243546783", "customFieldSetGroupIndex" :
		// "0"},{"customFieldID":"BIC", "value":"1234566441", "customFieldSetGroupIndex" : "0"}]}

		return patchGroupedCustomFields(parentEntity, parentEntityId, customFieldValues, false);
	}

	/***
	 * Helper to submit adding or updating grouped custom field values for an entity.
	 * 
	 * @param parentEntity
	 *            Mambu entity for which the custom field values are added. Example: MambuEntity.CLIENT,
	 *            MambuEntity.BRANCH
	 * @param parentEntityId
	 *            id or encoded key for the parent entity
	 * @param customFieldValues
	 *            custom field value to be added. Must not be null. All custom field values must belong to the same
	 *            Grouped custom field set. The "customFieldSetGroupIndex" must not be null in each field
	 * 
	 * @return true if API request was successful
	 * @throws MambuApiException
	 */
	private boolean patchGroupedCustomFields(MambuEntityType parentEntity, String parentEntityId,
			List<CustomFieldValue> customFieldValues, boolean forCreate) throws MambuApiException {

		// Add or Update Grouped Custom Field Values
		// Available since Mambu 4.1. See MBU-12228 and MBU-12229

		// Create. See MBU-12228. customFieldSetGroupIndex must NOT be present
		// Example: Execute request for PATCH API to Add (create) new group of custom field values for a Client:
		// PATCH /api/clients/clientId/custominformation/
		// {"customInformation":[{"customFieldID":"IBAN","value":"DE123456789121243546783" },{"customFieldID":"BIC",
		// "value":"1234566441"}]}

		// Update. See MBU-12229. customFieldSetGroupIndex must be present
		// Example: Execute request for PATCH API to Update group custom field values for a Client:
		// PATCH /api/clients/clientId/custominformation/
		// {"customInformation":[{"customFieldID":"IBAN","value":"DE123456789121243546783", "customFieldSetGroupIndex" :
		// "0"},{"customFieldID":"BIC",
		// "value":"1234566441", "customFieldSetGroupIndex" : "0"}]}

		if (customFieldValues == null) {
			throw new IllegalArgumentException("Custom field values must not be null");
		}
		// Make API-versions of the provided custom fields - values with only the field ID and field value going into
		// request
		List<CustomFieldValue> apiCustomFieldValues = makePatchGroupApiCustomFields(customFieldValues, forCreate);

		// Create JSON for customFieldValues and add it to the ParamsMap
		ParamsMap paramsMap = makeCustomInformationParams(apiCustomFieldValues);

		// Create API definition
		ApiDefinition apiDefinition = new ApiDefinition(ApiType.PATCH_OWNED_ENTITY, parentEntity.getEntityClass(),
				serviceEntity.getEntityClass());
		// Submit API request
		return serviceExecutor.execute(apiDefinition, parentEntityId, paramsMap);

	}

	/**
	 * Helper to make Custom Field Value for a PATCH Group custom fields values API request. This API supported creating
	 * new grouped custom field values or updating existent ones. When the existent values are updated the group number
	 * must be present. It should not be present when creating new fields
	 * 
	 * See MBU-12228- As a Developer, I Need to Add Grouped Custom Fields via PATCH APIs
	 * 
	 * Create new group example: /api/clients/clientId/custominformation/{"customInformation":[{"customFieldID" :
	 * "ABC","value A"}, {"customFieldID" : "DEF","value B"}]}
	 * 
	 * See MBU-12229 -As a Developer, I need to Edit Multiple Grouped Custom Fields via PATCH APIs PATCH
	 * 
	 * Edit group fields example : /api/clients/clientId/custominformation/{"customInformation":[{"customFieldID" :
	 * "ABC","value" : "value A", "customFieldSetGroupIndex" : "0"},{"customFieldID" : "DEFG","value" : "value B",
	 * "customFieldSetGroupIndex" : "0"]}
	 * 
	 * 
	 * Only certain fields need to be present in the API request. Specifically, "value" and "linkedEntityKeyValue" and
	 * optionally customFieldSetGroupIndex
	 * 
	 * @param customFieldValues
	 *            custom field values to be created or updated
	 * @param forCreate
	 *            true if the request if for creating new values, false otherwise
	 * @return list of the custom fields values with a subset of fields populated as need for the API
	 */
	private List<CustomFieldValue> makePatchGroupApiCustomFields(List<CustomFieldValue> customFieldValues,
			boolean forCreate) {

		// Make Api Custom Field but keep the group index when request is for edit
		List<CustomFieldValue> apiFeilds = new ArrayList<>();
		for (CustomFieldValue field : customFieldValues) {
			CustomFieldValue apiField = makePatchApiCustomField(field);
			apiField.setCustomFieldId(field.getCustomFieldId());
			// Keep the group index when updating grouped custom fields. Set to null otherwise
			Integer groupIndex = forCreate ? null : field.getCustomFieldSetGroupIndex();
			apiField.setCustomFieldSetGroupIndex(groupIndex);

			apiFeilds.add(apiField);
		}
		return apiFeilds;
	}

	/**
	 * Make custom filed id URL path for updating and deleting custom field values. Custom field id URL path for grouped
	 * custom fields must include the group number of the custom field value to be updated or deleted
	 * 
	 * @param customFieldValue
	 *            custom field value to be updated or deleted
	 * @return custom field id path
	 */
	private String makeCustomFieldIdPath(CustomFieldValue customFieldValue) {
		if (customFieldValue == null || customFieldValue.getCustomFieldId() == null) {
			throw new IllegalArgumentException("Custom Field Value and its Field ID cannot be null");
		}

		// For non-grouped custom fields specify custom field id
		String customFieldIdPath = customFieldValue.getCustomFieldId();

		// For Grouped custom field add group number to the URL request: {custofieldId}/{groupNumber} See MBU-8340
		Integer groupIndex = customFieldValue.getCustomFieldSetGroupIndex();
		if (groupIndex != null && groupIndex >= 0) {
			// Add group number to the path
			customFieldIdPath = customFieldIdPath.concat("/").concat(String.valueOf(groupIndex));
		}

		return customFieldIdPath;
	}

	/**
	 * Helper to make URL path for patching and deleting owned custom field values.
	 * 
	 * Path example: /api/loans/{accountId}/transactions/{id}/custominformation/{customFieldId}
	 * 
	 * @param parentEntity
	 *            parent entity. Example MambuEntityType.LOAN_ACCOUNT
	 * @param parentEntityId
	 *            the id or encoded key of the parent entity
	 * @param ownedEntity
	 *            owned entity. Example MambuEntityType.LOAN_TRANSACTION
	 * @param ownedEntityId
	 *            the id or encoded key of the owned entity
	 * @param customFieldId
	 *            custom field id
	 * @return URL patch for DELETE custom field values API
	 */
	private String makeOwnedEntityUrlPath(MambuEntityType parentEntity, String parentEntityId,
			MambuEntityType ownedEntity, String ownedEntityId, String customFieldId) {
		if (parentEntity == null || parentEntityId == null || ownedEntity == null || ownedEntityId == null) {
			throw new IllegalArgumentException("Parameters must not be null");
		}
		// Get API end point for parent (e.g. "loans")
		String entityPath = ApiDefinition.getApiEndPoint(parentEntity.getEntityClass());
		// Get API end point for owned entity (e.g. "transactions")
		String relatedEentityPath = ApiDefinition.getApiEndPoint(ownedEntity.getEntityClass());

		// Example: /api/loans/{accountId}/transactions/{id}/custominformation/{customFieldId}
		String urlPath = entityPath + "/" + parentEntityId + "/" + relatedEentityPath + "/" + ownedEntityId + "/"
				+ APIData.CUSTOM_INFORMATION + "/" + customFieldId;
		return urlPath;
	}

	/**
	 * Helper to make ApiDefinition for patching owned custom field values
	 * 
	 * @param parentEntity
	 *            parent entity. Example MambuEntityType.LOAN_ACCOUNT
	 * @param parentEntityId
	 *            the id or encoded key of the parent entity
	 * @param ownedEntity
	 *            owned entity. Example MambuEntityType.LOAN_TRANSACTION
	 * @param ownedEntityId
	 *            the id or encoded key of the owned entity
	 * @param customFieldId
	 *            custom field id
	 * @return api definition for PATCH custom field values API
	 */
	private ApiDefinition makePatchApiDefintion(MambuEntityType parentEntity, String parentEntityId,
			MambuEntityType ownedEntity, String ownedEntityId, String customFieldId) {

		if (parentEntity == null || parentEntityId == null || ownedEntity == null || ownedEntityId == null) {
			throw new IllegalArgumentException("Parameters must not be null");
		}
		// Make URL path
		String urlPath = makeOwnedEntityUrlPath(parentEntity, parentEntityId, ownedEntity, ownedEntityId, customFieldId);

		// Create ApiDefintion
		return new ApiDefinition(urlPath, ContentType.JSON, Method.PATCH, Boolean.class, ApiReturnFormat.BOOLEAN);

	}

	/**
	 * Helper to make ApiDefinition for deleting owned custom field values
	 * 
	 * @param parentEntity
	 *            parent entity. Example MambuEntityType.LOAN_ACCOUNT
	 * @param parentEntityId
	 *            the id or encoded key of the parent entity
	 * @param ownedEntity
	 *            owned entity. Example MambuEntityType.LOAN_TRANSACTION
	 * @param ownedEntityId
	 *            the id or encoded key of the owned entity
	 * @param customFieldId
	 *            custom field id
	 * @return api definition for DELETE custom field values API
	 */
	private ApiDefinition makeDeleteApiDefintion(MambuEntityType parentEntity, String parentEntityId,
			MambuEntityType ownedEntity, String ownedEntityId, String customFieldId) {

		if (parentEntity == null || parentEntityId == null || ownedEntity == null || ownedEntityId == null) {
			throw new IllegalArgumentException("Parameters must not be null");
		}

		String urlPath = makeOwnedEntityUrlPath(parentEntity, parentEntityId, ownedEntity, ownedEntityId, customFieldId);

		return new ApiDefinition(urlPath, ContentType.WWW_FORM, Method.DELETE, Boolean.class, ApiReturnFormat.BOOLEAN);

	}

	/**
	 * Create JSON for CustomFieldValues API and add it to the ParamsMap
	 * 
	 * @param customFieldValues
	 *            custom field values for an API request
	 * @return params map with request JSON in the format expected by RequestExecutor for jSON requests
	 */
	// TODO: in future updates consider changing implementation to using custom serializer in ApiDefitnion for
	// generating JSON for Custom Field Value API
	private ParamsMap makeCustomInformationParams(List<CustomFieldValue> customFieldValues) {
		// customInformation":[{ "customFieldID":"IBAN","value":"DE123456789121243546783"},{ "customFieldID":"BIC","value":"1234566441"}]

		Gson gson = GsonUtils.createGson();
		JsonElement customFieldsJson = gson.toJsonTree(customFieldValues);

		JsonObject jsonObject = new JsonObject();
		jsonObject.add(APIData.CUSTOM_INFORMATION_FIELD, customFieldsJson);
		String jsonRequest = gson.toJson(jsonObject);

		// Add resulting JSON to ParamsMap
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(APIData.JSON_OBJECT, jsonRequest);

		return paramsMap;

	}

	/**
	 * Get Mambu Entities supporting Custom Field Value API
	 * 
	 * @return supported entities
	 */
	public static MambuEntityType[] getSupportedEntities() {
		return supportedEntities;
	}

	/**
	 * Is parent entity type supported by the Custom Field Value API
	 * 
	 * @param parentEntityType
	 *            Mambu Entity type
	 * @return true if supported
	 */
	public static boolean isSupported(MambuEntityType parentEntityType) {
		if (parentEntityType == null) {
			return false;
		}

		Set<MambuEntityType> set = new HashSet<MambuEntityType>(Arrays.asList(supportedEntities));
		return (set.contains(parentEntityType)) ? true : false;

	}

	/**
	 * Get CustomFieldType for the corresponding MambuEntityType
	 * 
	 * @param mambuEntityType
	 *            Mambu Entity Type
	 * @return Custom Field Type
	 */
	public static CustomFieldType getCustomFieldType(MambuEntityType mambuEntityType) {

		return mambuEntityType != null ? customFieldTypes.get(mambuEntityType) : null;
	}

}
