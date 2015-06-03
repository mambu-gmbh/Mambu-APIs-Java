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

		return apiField;
	}

	/**
	 * Make custom filed id path for updating and deleting custom field values. Custom field id path for grouped custom
	 * fields must include the group number of the custom field value to be deleted
	 * 
	 * @param customFieldValue
	 *            custom field value to be deleted
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
		if (groupIndex != null) {
			// Add group number to the path
			customFieldIdPath = customFieldIdPath.concat("/").concat(String.valueOf(groupIndex));
		}

		return customFieldIdPath;
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
