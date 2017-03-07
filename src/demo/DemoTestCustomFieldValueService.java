package demo;

import java.util.ArrayList;
import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.CustomFieldValueService;
import com.mambu.apisdk.services.OrganizationService;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.core.shared.model.CustomFieldType;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanTransaction;

/**
 * Test class to show example usage for custom field values API
 * 
 * @author mdanilkis
 * 
 */
public class DemoTestCustomFieldValueService {

	private static String methodName = null; // print method name on exception

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			testUpdateAndDeleteCustomFieldValues();
			testUpdateAndDeleteTransactionCustomFieldValues(); // Available since Mambu 4.1
			testUpdateMultipleCustomFields(); // Available since 4.2. For more details see 12231

			// Available since 3.8
			// Support for Grouped Custom fields available since 3.11
			// Support for Linked Custom fields available since 3.11

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Custom Field Values");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	}

	// Test updating and deleting custom field values.
	private static void testUpdateAndDeleteCustomFieldValues() throws MambuApiException {

		System.out.println(methodName = "\nIn testUpdateAndDeleteCustomFieldValues");

		// Iterate through supported entity types and Update a field first and then delete field
		// This API is available for Client, Group. LoanAccount, SavingsAccount, Branch, Centre entities
		MambuEntityType[] supportedEntities = CustomFieldValueService.getSupportedEntities();

		for (MambuEntityType parentEntity : supportedEntities) {

			testUpdateDeleteEntityCustomFields(parentEntity);
		}

	}

	/**
	 * Test Updating and Deleting Custom Field value for a MambuEntity
	 * 
	 * @param parentEntity
	 *            Mambu entity for which custom fields are updated or deleted
	 * @throws MambuApiException
	 */
	public static void testUpdateDeleteEntityCustomFields(MambuEntityType parentEntity) throws MambuApiException {

		System.out.println(methodName = "\nIn testUpdateDeleteEntityCustomFields");

		// Get ID of the parent entity. Use demo entity
		DemoEntityParams entityParams = DemoEntityParams.getEntityParams(parentEntity);
		String parentId = entityParams.getId();
		String parentName = entityParams.getName();

		System.out.println("\n\nTesting Custom Fields APIs for " + parentEntity + " " + parentName + " with ID="
				+ parentId);
		// Execute test cases to update and delete custom fields
		testUpdateAddDeleteEntityCustomFields(parentEntity, entityParams);
	}

	/**
	 * Convenience method to Test Updating and Deleting Custom Field value for a MambuEntity by providing required
	 * entityParms
	 * 
	 * @param parentEntity
	 *            Mambu entity for which custom fields are updated or deleted
	 * @entityParams entity params. Must be not null and have not null entity id
	 * @throws MambuApiException
	 */
	public static void testUpdateAddDeleteEntityCustomFields(MambuEntityType parentEntity,
			DemoEntityParams entityParams) throws MambuApiException {

		System.out.println(methodName = "\nIn testUpdateAddDeleteEntityCustomFields");

		// Get ID of the parent entity. Use demo entity
		if (entityParams == null || entityParams.getId() == null) {
			throw new IllegalArgumentException("Entity params must be not null and have an ID");
		}
		String parentId = entityParams.getId();
		String parentName = entityParams.getName();

		System.out.println("\n\nTesting Custom Fields APIs for " + parentEntity + " " + parentName + " with ID="
				+ parentId);
		// Test Update API
		List<CustomFieldValue> customFieldValues = updateCustomFieldValues(parentEntity, entityParams);

		// Test addGroupedCustom fields API
		testAddGroupedCustomFields(parentEntity, entityParams);

		// Test Delete Custom Field API
		deleteCustomField(parentEntity, parentId, customFieldValues);

	}

	// Test updating and deleting custom field values for Transactions
	private static void testUpdateAndDeleteTransactionCustomFieldValues() throws MambuApiException {

		System.out.println(methodName = "\nIn testUpdateAndDeleteTransactionCustomFieldValues");

		// Get test LoanTransaction
		LoanAccount demoAccount = DemoUtil.getDemoLoanAccount();
		String accountId = demoAccount.getId();
		LoanTransaction transaction = DemoUtil.getDemoLoanTransactionWithDetails(accountId);
		if (transaction == null) {
			System.out.println("WARNING: no test transactions with details found for account " + accountId);
			return;
		}

		String transactionId = transaction.getTransactionId().toString();
		String channelKey = transaction.getDetails().getTransactionChannelKey();

		// Test Updating Transaction Custom fields: since Mambu model for 4.1 transactions can have CustomFieldValues
		List<CustomFieldValue> transactionFields = transaction.getCustomFieldValues();
		if (transactionFields == null || transactionFields.size() == 0) {
			// Make new test fields
			transactionFields = DemoUtil.makeForEntityCustomFieldValues(CustomFieldType.TRANSACTION_CHANNEL_INFO,
					channelKey, false);
		}
		if (transactionFields == null || transactionFields.size() == 0) {
			System.out.println("WARNING: no custom fields available for transaction " + transactionId + " channel="
					+ channelKey);
			return;
		}
		// Update custom fields values
		CustomFieldValueService service = MambuAPIFactory.getCustomFieldValueService();
		boolean updateFieldstatus = service.update(MambuEntityType.LOAN_ACCOUNT, accountId,
				MambuEntityType.LOAN_TRANSACTION, transactionId, transactionFields.get(0));
		System.out.println("Update status=" + updateFieldstatus);

		// Test Deleting Custom Field Value. Delete the first one
		boolean deleteStatus = service.delete(MambuEntityType.LOAN_ACCOUNT, accountId,
				MambuEntityType.LOAN_TRANSACTION, transactionId, transactionFields.get(0));
		System.out.println("Delete status=" + deleteStatus);
	}

	// Test Adding new Grouped custom fields API. Available since 4.1. See MBU-12228
	private static void testAddGroupedCustomFields(MambuEntityType parentEntity, DemoEntityParams entityParams)
			throws MambuApiException {

		System.out.println(methodName = "\nIn testAddGroupedCustomFields");

		// Get Custom field set of Grouped type first
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();
		CustomFieldType customFieldType = CustomFieldValueService.getCustomFieldType(parentEntity);
		// Get all sets and find a Grouped custom fields Set for testing
		List<CustomFieldSet> sets = organizationService.getCustomFieldSets(customFieldType);
		if (sets == null) {
			return;
		}
		CustomFieldSet groupedSet = null;
		for (CustomFieldSet set : sets) {
			if (set.isGrouped()) {
				groupedSet = set;
				break;
			}
		}
		if (groupedSet == null || groupedSet.getCustomFields() == null || groupedSet.getCustomFields().size() == 0) {
			System.out.println("\nWARNING: No Grouped Custom Field Sets with fields found for " + customFieldType);
			return;
		}
		// Make test custom fields values for the set. Get only applicable custom fields (e.g. applicable to product)
		List<CustomField> groupFields = DemoUtil.getForEntityCustomFields(groupedSet, entityParams.getLinkedTypeKey());
		if (groupFields.size() == 0) {
			System.out.println("\nWARNING: No Applicable Grouped Custom Fields found for " + customFieldType);
			return;
		}
		List<CustomFieldValue> customFieldValues = new ArrayList<>();
		for (CustomField field : groupFields) {
			// Create test value matching field's data type
			CustomFieldValue fieldValue = DemoUtil.makeNewCustomFieldValue(groupedSet, field, null);
			customFieldValues.add(fieldValue);
		}
		// Execute Add Grouped Custom Fields Values API
		CustomFieldValueService service = MambuAPIFactory.getCustomFieldValueService();
		System.out.println("Adding Grouped Custom Fields for " + parentEntity + "\tID=" + entityParams.getId());
		boolean addStatus = service.addGroupedFields(parentEntity, entityParams.getId(), customFieldValues);
		System.out.println("Added Grouped Fields. Status=" + addStatus);

		// Test Updating the same group of fields now
		// Add group index to the fields we are updating. Update in group zero (it must be present after or "add group"
		// test)
		Integer updateGroup = 0;
		for (CustomFieldValue fieldValue : customFieldValues) {
			fieldValue.setCustomFieldSetGroupIndex(updateGroup);
		}
		// Execute Update Grouped Custom Field Values API request
		System.out.println("Updating Grouped Custom Fields for " + parentEntity + "\tID=" + entityParams.getId());
		boolean updateStatus = service.updateGroupedFields(parentEntity, entityParams.getId(), customFieldValues);
		System.out.println("Updated Grouped Fields. Status=" + updateStatus);
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
	private static List<CustomFieldValue> updateCustomFieldValues(MambuEntityType parentEntity,
			DemoEntityParams entityParams) throws MambuApiException {

		System.out.println(methodName = "\nIn updateCustomFieldValues");

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

			// Create valid new value for a custom field
			CustomFieldValue customFieldValue = DemoUtil.makeNewCustomFieldValue(value);
			String fieldId = value.getCustomFieldId();

			// Update Custom Field value
			boolean updateStatus;
			System.out.println("\nUpdating Custom Field with ID=" + fieldId + " for " + entityName + " with ID="
					+ entityId);

			// Test API to update Custom Fields Value
			updateStatus = customFieldsService.update(parentEntity, entityId, customFieldValue);
			// Log results
			String statusMessage = (updateStatus) ? "Success" : "Failure";
			System.out.println(statusMessage + " updating Custom Field, ID=" + fieldId + " for demo " + entityName
					+ " with ID=" + entityId + " Value=" + customFieldValue.getValue() + " Linked Key="
					+ customFieldValue.getLinkedEntityKeyValue());

		}

		return customFieldValues;
	}

	// Tests PATCHing a list of custom fields
	private static void testUpdateMultipleCustomFields() throws MambuApiException {

		System.out.println(methodName = "\nIn testUpdateMultiplaCustomFields");

		// get supported entities
		MambuEntityType[] supportedEntities = CustomFieldValueService.getSupportedEntities();
		// Iterate through supported entity types and Update custom fields
		for (MambuEntityType parentEntity : supportedEntities) {
			System.out.println("\nStart testing update of custom field values for entity: " + parentEntity);
			testUpdateAndGetFieldsForParentEntity(parentEntity);
		}
	}

	/**
	 * Test Updating Custom Field values. Test getting Custom Field Values by custom field ID
	 * 
	 * 
	 * @param parentEntityType
	 *            The ParentEntityType whose custom field values will be updated
	 * @throws MambuApiException
	 */
	public static void testUpdateAndGetFieldsForParentEntity(MambuEntityType parentEntityType) throws MambuApiException {

		System.out.println(methodName = "\nIn testUpdateAndGetFieldsForParentEntity");
		DemoEntityParams entityParams = DemoEntityParams.getEntityParams(parentEntityType);

		List<CustomFieldValue> customFieldValues = DemoEntityParams
				.getCustomFieldValues(parentEntityType, entityParams);

		// the updated custom field values
		List<CustomFieldValue> updatedCustomFieldValues = new ArrayList<>();

		// return if there are no custom field values for the entity
		if (customFieldValues.isEmpty()) {

			System.out.println("No Custom Field Values were found for: " + parentEntityType);

			CustomFieldType customFieldType = CustomFieldValueService.getCustomFieldType(parentEntityType);
			System.out.println("Creating new fields");
			List<CustomFieldValue> clientCustomInformation = DemoUtil.makeForEntityCustomFieldValues(customFieldType,
					entityParams.getLinkedTypeKey(), false);
			// Add All custom fields
			updatedCustomFieldValues.addAll(clientCustomInformation);
			if (clientCustomInformation.isEmpty()) {
				System.out.println("WARNING: cannot test update, no custom fields defined for " + parentEntityType);
				return;
			}
		} else {
			for (CustomFieldValue customFieldValue : customFieldValues) {
				customFieldValue = DemoUtil.makeNewCustomFieldValue(customFieldValue);
				updatedCustomFieldValues.add(customFieldValue);
			}
		}
		// Use Try and catch to allow tests for other entities to continue
		try {
			CustomFieldValueService customFieldsService = MambuAPIFactory.getCustomFieldValueService();

			boolean updateCustomFieldStatus = customFieldsService.update(parentEntityType, entityParams.getId(),
					updatedCustomFieldValues);

			System.out.println("Update a list of custom fields for " + parentEntityType + " status = "
					+ updateCustomFieldStatus);

			// Test API to GET Custom Field Values for an entity by Custom Field ID
			testGetCustomFieldValues(parentEntityType, entityParams, updatedCustomFieldValues);

		} catch (MambuApiException e) {
			DemoUtil.logException(methodName, e);
			System.out.println("Failed updating a list of custom fields for " + parentEntityType);

		}
	}

	/**
	 * Tests getting custom fields and print their details to the console.
	 * 
	 * @param parentEntityType
	 *            The MambuParentEntityType
	 * @param entityParams
	 *            The DemoEntityParams
	 * @param customFieldValues
	 *            A list of custom field values
	 * @throws MambuApiException
	 */
	private static void testGetCustomFieldValues(MambuEntityType parentEntityType, DemoEntityParams entityParams,
			List<CustomFieldValue> customFieldValues) throws MambuApiException {

		// Available since 4.2. More details on MBU-13211
		System.out.println(methodName = "\nIn testGetCustomFieldValues");

		CustomFieldValueService customFieldsService = MambuAPIFactory.getCustomFieldValueService();

		for (CustomFieldValue customFieldValue : customFieldValues) {
			// call Mambu to get the details of the custom field
			List<CustomFieldValue> updatedCustomFieldValue = customFieldsService.getCustomFieldValue(parentEntityType,
					entityParams.getId(), customFieldValue.getCustomFieldId());

			DemoUtil.logCustomFieldValues(updatedCustomFieldValue, parentEntityType.toString(),
					customFieldValue.getCustomFieldId());
		}
	}

	/**
	 * Private helper to Delete the first custom field value for MambuEntity
	 * 
	 * @param parentEntity
	 *            MambuEntity for custom field values
	 * @param entityId
	 *            parent entity id
	 * @param customFieldValues
	 *            custom field values for this entity. The first one will be deleted
	 * @throws MambuApiException
	 */
	private static void deleteCustomField(MambuEntityType parentEntity, String entityId,
			List<CustomFieldValue> customFieldValues) throws MambuApiException {

		System.out.println(methodName = "\nIn deleteCustomField");

		Class<?> entityClass = parentEntity.getEntityClass();
		String entityName = entityClass.getSimpleName();

		if (customFieldValues == null || customFieldValues.size() == 0) {
			System.out.println("WARNING: No Custom fields defined for demo " + entityName + " with ID=" + entityId
					+ ". Nothing to delete");
			return;
		}
		// Get the first Custom Field Value
		CustomFieldValue customFieldValue = customFieldValues.get(0);

		// Required custom fields cannot be deleted. Using try block to continue testing
		String customFieldId = customFieldValue.getCustomFieldId();
		System.out.println("Deleting field with ID=" + customFieldId + " and Group Number="
				+ customFieldValue.getCustomFieldSetGroupIndex());
		try {
			// Test Delete API
			CustomFieldValueService customFieldsService = MambuAPIFactory.getCustomFieldValueService();
			boolean deleteStatus = customFieldsService.delete(parentEntity, entityId, customFieldValue);
			// Log results
			String statusMessage = (deleteStatus) ? "Success" : "Failure";
			System.out.println(statusMessage + " deleting Custom Field, ID=" + customFieldId + " for demo "
					+ entityName + " with ID=" + entityId);
		} catch (MambuApiException e) {
			System.out.println("Exception deleting field: " + customFieldId + " Message:" + e.getMessage());
		}

	}

}
