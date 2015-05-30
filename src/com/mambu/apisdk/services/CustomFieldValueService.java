package com.mambu.apisdk.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ServiceExecutor;
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
	 *            owned entity object
	 * @param customFieldValueId
	 *            the encoded key or id of the custom field value to be updated
	 * 
	 * @return true if updated successfully
	 * @throws MambuApiException
	 */
	public boolean update(MambuEntityType parentEntity, String parentEntityId, CustomFieldValue customFieldValue,
			String customFieldValueId) throws MambuApiException {
		// Update Custom Field Values API examples:
		// Execute request for PATCH API to update custom field value for a Loan Account. See MBU-6661
		// e.g. PATCH "{ "value": "10" }" /host/api/loans/accointId/custominformation/customFieldId

		// PATCH linked Entity value for a custom field value (see MBU-8514)
		// PATCH '{ "linkedEntityKeyValue": "40288a13...." }'// /api/clients/abc123/custominformation/customFieldId

		// Update Grouped field: PATCH '{ "value": "10" }' /api/clients/abc123/custominformation/family_members/1
		// See MBU-8340

		if (customFieldValue == null || customFieldValueId == null) {
			throw new IllegalArgumentException("Custom Field Value and Field ID cannot be null");
		}
		// For Grouped custom field group number must be added to URL request
		Integer groupIndex = customFieldValue.getCustomFieldSetGroupIndex();
		if (groupIndex != null) {
			customFieldValueId = customFieldValueId.concat("/").concat(String.valueOf(groupIndex));
		}
		// Make Custom Field value which contains only fields needed in API request
		CustomFieldValue apiFieldValue = makePatchApiCustomField(customFieldValue);

		// Submit API request
		return serviceExecutor.updateOwnedEntity(parentEntity, parentEntityId, apiFieldValue, customFieldValueId);

	}

	/***
	 * Delete custom field value for a Mambu parent entity
	 * 
	 * @param parentEntity
	 *            Mambu entity for which custom field value is deleted. Example: MambuEntity.CLIENT, MambuEntity.BRANCH
	 * @param parentEntityId
	 *            entity id or encoded key for the parent entity
	 * @param customFieldValueId
	 *            the encoded key or id of the custom field value to be deleted
	 * @return true if successful
	 * @throws MambuApiException
	 */
	public boolean delete(MambuEntityType parentEntity, String parentEntityId, String ownedEntityId)
			throws MambuApiException {

		// Exammple:Execute request for DELETE API to delete custom field value for a client See MBU-6661
		// e.g. DELETE /host/api/clients/clientId/custominformation/customFieldId

		return serviceExecutor.deleteOwnedEntity(parentEntity, parentEntityId, serviceEntity, ownedEntityId);

	}

	/**
	 * Make Custom Field Value for a PATCH Api request. Only certain fields need to be present in the API request.
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

		return apiField;
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
}
