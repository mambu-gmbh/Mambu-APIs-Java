package demo;

import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.CustomFieldValueService;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.core.shared.model.CustomFieldValue;

/**
 * Test class to show example usage for custom field values API
 * 
 * @author mdanilkis
 * 
 */
public class DemoTestCustomFiledValueService {

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			// Available since 3.8
			// Support for Grouped Custom fields available since 3.11
			// Support for Linked Custom fields available since 3.11
			testUpdateAndDeleteCustomFieldValues();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Custom Field Values");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	}

	// Test updating and deleting custom field values.
	private static void testUpdateAndDeleteCustomFieldValues() throws MambuApiException {
		System.out.println("\nIn testUpdateAndDeleteCustomFieldValues");

		// Iterate through supported entity types and Update a field first and then delete field
		// This API is available for Client, Group. LoanAccount, SavingsAccount, Branch, Centre entities
		MambuEntityType[] supportedEntities = CustomFieldValueService.getSupportedEntities();

		for (MambuEntityType parentEntity : supportedEntities) {

			testUpdateDeleteCustomFields(parentEntity);
		}

	}

	/**
	 * Test Updating and Deleting Custom Field value for a MambuEntity
	 * 
	 * @param parentEntity
	 *            Mambu entity for which custom fields are updated or deleted
	 * @throws MambuApiException
	 */
	public static void testUpdateDeleteCustomFields(MambuEntityType parentEntity) throws MambuApiException {
		System.out.println("\nIn testUpdateDeleteCustomFields");

		// Get ID of the parent entity. Use demo entity
		DemoEntityParams entityParams = DemoEntityParams.getEntityParams(parentEntity);
		String parentId = entityParams.getId();
		String parentName = entityParams.getName();

		System.out.println("\n\nTesting Custom Fields for " + parentEntity + " " + parentName + " with ID=" + parentId);
		// Test Update API
		List<CustomFieldValue> customFieldValues = updateCustomFields(parentEntity, entityParams);

		// Test Delete API
		deleteCustomField(parentEntity, parentId, customFieldValues);

	}

	/**
	 * Private helper to Update all custom fields for a demo Mambu Entity
	 * 
	 * @param parentEntity
	 *            MambuEntity for custom field values
	 * @param entityParams
	 *            entity params for a demo entity
	 * @return custom field values for a demo entity
	 * @throws MambuApiException
	 */
	private static List<CustomFieldValue> updateCustomFields(MambuEntityType parentEntity, DemoEntityParams entityParams)
			throws MambuApiException {
		System.out.println("\nIn updateCustomFields");

		String entityId = entityParams.getId();
		Class<?> entityClass = parentEntity.getEntityClass();
		String entityName = entityClass.getSimpleName();

		// Get Current custom field values first for a Demo account
		List<CustomFieldValue> customFieldValues = DemoEntityParams.getCustomFieldValues(parentEntity, entityParams);
		System.out.println("Total Custom Fields " + customFieldValues.size());

		if (customFieldValues == null || customFieldValues.size() == 0) {
			System.out.println("WARNING: No Custom fields defined for demo " + entityName + " with ID=" + entityId
					+ ". Nothing to update");
			return null;
		}
		// Update custom field values
		CustomFieldValueService customFieldsService = MambuAPIFactory.getCustomFieldValueService();
		for (CustomFieldValue value : customFieldValues) {

			String fieldId = value.getCustomFieldId();
			// Create valid new value for a custom field
			CustomFieldValue customFieldValue = DemoUtil.makeNewCustomFieldValue(value);

			// Update Custom Field value
			boolean updateStatus;
			System.out.println("\nUpdating Custom Field with ID=" + fieldId + " for " + entityName + " with ID="
					+ entityId);

			// Test API to update Custom Fields Value
			updateStatus = customFieldsService.update(parentEntity, entityId, customFieldValue, fieldId);
			// Log results
			String statusMessage = (updateStatus) ? "Success" : "Failure";
			System.out.println(statusMessage + " updating Custom Field, ID=" + fieldId + " for demo " + entityName
					+ " with ID=" + entityId + " Value=" + customFieldValue.getValue() + " Linked Key="
					+ customFieldValue.getLinkedEntityKeyValue());

		}

		return customFieldValues;
	}

	/**
	 * Private helper to Delete the first custom field for MambuEntity
	 * 
	 * @param parentEntity
	 *            MambuEntity for custom field values
	 * @param entityId
	 *            parent entity id
	 * @param customFieldValues
	 *            custom field values for this entity
	 * @throws MambuApiException
	 */
	private static void deleteCustomField(MambuEntityType parentEntity, String entityId,
			List<CustomFieldValue> customFieldValues) throws MambuApiException {
		System.out.println("\nIn deleteCustomField");

		Class<?> entityClass = parentEntity.getEntityClass();
		String entityName = entityClass.getSimpleName();

		if (customFieldValues == null || customFieldValues.size() == 0) {
			System.out.println("WARNING: No Custom fields defined for demo " + entityName + " with ID=" + entityId
					+ ". Nothing to delete");
			return;
		}
		// Get the first Custom Field ID
		String customFieldId = customFieldValues.get(0).getCustomField().getId();

		// Required custom fields cannot be deleted. Using try block to continue testing
		System.out.println("Deleting field with ID=" + customFieldId);
		try {
			// Test Delete API
			CustomFieldValueService customFieldsService = MambuAPIFactory.getCustomFieldValueService();
			boolean deleteStatus = customFieldsService.delete(parentEntity, entityId, customFieldId);
			// Log results
			String statusMessage = (deleteStatus) ? "Success" : "Failure";
			System.out.println(statusMessage + " deleting Custom Field, ID=" + customFieldId + " for demo "
					+ entityName + " with ID=" + entityId);
		} catch (MambuApiException e) {
			System.out.println("Exception deleting field: " + customFieldId + " Message:" + e.getMessage());
		}

	}

}
