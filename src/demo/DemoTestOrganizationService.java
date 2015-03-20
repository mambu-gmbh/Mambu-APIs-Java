package demo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.mambu.accounts.shared.model.TransactionChannel;
import com.mambu.accounts.shared.model.TransactionChannel.ChannelField;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.OrganizationService;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.Address;
import com.mambu.core.shared.model.Currency;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldLink;
import com.mambu.core.shared.model.CustomFieldLink.LinkType;
import com.mambu.core.shared.model.CustomFieldSelection;
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.CustomFilterConstraint;
import com.mambu.core.shared.model.IndexRate;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;

/**
 * Test class to show example usage of the api calls
 * 
 * @author edanilkis
 * 
 */
public class DemoTestOrganizationService {

	private static String BRANCH_ID;
	private static String CENTRE_ID;
	private static String CUSTOM_FIELD_ID;

	private static Branch demoBranch;
	private static Centre demoCentre;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			demoBranch = DemoUtil.getDemoBranch();
			demoCentre = DemoUtil.getDemoCentre();

			testPostIndexInterestRate(); // Available since 3.10

			// Available since 3.7
			testGetTransactionChannels();

			testGetAllBranches();

			testGetCustomFieldSetsByType();
			testGetCustomField();

			testGetCentresByPage();
			testGetCentre();

			testGetCurrency();

			testGetAllBranches();
			testGetCentresByBranch();

			testGetBranchesByPage();

			testGetBranch();

			// Available since 3.8
			testUpdateDeleteCustomFields();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Organization Service");
			System.out.println("Exception Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testGetAllBranches() throws MambuApiException {
		System.out.println("\nIn testGetAllBranches");
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String offset = null;
		String limit = null;
		Date d1 = new Date();
		List<Branch> branches = organizationService.getBranches(offset, limit);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("All Total=" + branches.size() + " Total time=" + diff);
		for (Branch branch : branches) {
			System.out.println(" Name=" + branch.getName() + "\tId=" + branch.getId());
			Address address = branch.getAddress();
			if (address != null)
				System.out.println(" And address=" + address.getLine1());
		}
		System.out.println();

	}

	public static void testGetBranchesByPage() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String offset = "0";
		String limit = "500";
		System.out.println("\nIn testGetBranchesByPage" + "  Offset=" + offset + "  Limit=" + limit);

		Date d1 = new Date();
		List<Branch> branches = organizationService.getBranches(offset, limit);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Total Branches=" + branches.size() + " Total time=" + diff);
		BRANCH_ID = null;
		for (Branch branch : branches) {
			if (BRANCH_ID == null) {
				BRANCH_ID = branch.getId();
			}
			System.out.println(" Name=" + branch.getName() + "\tId=" + branch.getId());
		}
		System.out.println();

	}

	public static void testGetBranch() throws MambuApiException {
		System.out.println("\nIn testGetBranch");
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		Branch branch = organizationService.getBranch(BRANCH_ID);

		if (branch != null)
			System.out.println("Branch id=" + BRANCH_ID + " found. Returned:  ID=" + branch.getId() + "   Name="
					+ branch.getName());
		else
			System.out.println("Not Found Branch id=" + BRANCH_ID);
	}

	public static void testGetCentre() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String centreId = demoCentre.getId();
		System.out.println("\nIn testGetCentre by ID." + "  Centre ID=" + centreId);

		Date d1 = new Date();
		Centre centre = organizationService.getCentre(centreId);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Centre: Name=" + centre.getName() + " BranchId=" + centre.getAssignedBranchKey()
				+ " Total time=" + diff);

	}

	public static void testGetCentresByPage() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String offset = "0";
		String limit = "500";
		String branchId = null;
		System.out.println("\nIn testGetCentresByPage" + "  Offset=" + offset + "  Limit=" + limit);

		Date d1 = new Date();
		List<Centre> centres = organizationService.getCentres(branchId, offset, limit);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Total Centres=" + centres.size() + " Total time=" + diff);
		CENTRE_ID = null;
		for (Centre centre : centres) {
			if (CENTRE_ID == null) {
				CENTRE_ID = centre.getId();
			}
			System.out.println(" Name=" + centre.getName() + "\tId=" + centre.getId());
		}
		System.out.println();

	}

	public static void testGetCentresByBranch() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String branchId = BRANCH_ID;
		String offset = "0";
		String limit = "500";
		System.out.println("\nIn testGetCentresByBranch" + "  BranchID=" + branchId + "  Offset=" + offset + "  Limit="
				+ limit);

		Date d1 = new Date();
		List<Centre> centres = organizationService.getCentres(branchId, offset, limit);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Total Centres=" + centres.size() + " for branch=" + branchId + ". Total time=" + diff);
		for (Centre centre : centres) {
			System.out.println(" Name=" + centre.getName() + "\tId=" + centre.getId());
		}
		System.out.println();

	}

	public static void testGetCurrency() throws MambuApiException {
		System.out.println("\nIn testGetCurrency");
		Date d1 = new Date();
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		Currency currency = organizationService.getCurrency();
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();
		System.out.println("Currency code=" + currency.getCode() + "   Name=" + currency.getName() + " Total time="
				+ diff);

	}

	// Get Custom Field by ID
	public static void testGetCustomField() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String fieldId = CUSTOM_FIELD_ID;
		System.out.println("\nIn testGetCustomField by ID." + "  Field ID=" + fieldId);

		Date d1 = new Date();

		CustomField customField = organizationService.getCustomField(fieldId);

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("CustomField: ID=" + customField.getId() + "\tName=" + customField.getName()
				+ " \tData Type=" + customField.getDataType().name() + " Total time=" + diff);

	}

	// Get CustomFieldSets by Type
	public static void testGetCustomFieldSetsByType() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		// E.g. CustomField.Type.CLIENT_INFO, CustomField.Type.LOAN_ACCOUNT_INFO, etc
		CustomField.Type customFieldType = CustomField.Type.CLIENT_INFO;

		System.out.println("\nIn testGetCustomFieldSetsByType for " + customFieldType);

		Date d1 = new Date();
		List<CustomFieldSet> sustomFieldSets = organizationService.getCustomFieldSets(customFieldType);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Total Sets returned=" + sustomFieldSets.size() + " Total time=" + diff);
		for (CustomFieldSet set : sustomFieldSets) {
			List<CustomField> customFields = set.getCustomFields();

			System.out.println("\nSet Name=" + set.getName() + "\tType=" + set.getType().toString() + "  Total Fields="
					+ customFields.size() + "\tUsage=" + set.getUsage());
			System.out.println("List of fields");
			for (CustomField field : customFields) {
				System.out.println("\nField ID=" + field.getId() + "\tField Name=" + field.getName() + "\tDataType="
						+ field.getDataType().toString() + "\tIsDefault=" + field.isDefault().toString() + "\tType="
						+ field.getType().toString() + "\tIs Active=" + !field.isDeactivated());

				// Remember one of the active CustomFields for testing testGetCustomField()
				if (!field.isDeactivated()) {
					CUSTOM_FIELD_ID = (CUSTOM_FIELD_ID == null) ? field.getId() : CUSTOM_FIELD_ID;
				}

				// As of Mambu 3.9, settings for custom fields are per entity type, see MBU-7034
				List<CustomFieldLink> links = field.getCustomFieldLinks();
				if (links == null) {
					System.out.println("Field's CustomFieldLinks are null");
					continue;
				}
				if (links.size() == 0) {
					System.out.println("Field's CustomFieldLinks are empty");
					continue;
				}
				for (CustomFieldLink link : links) {
					LinkType linkType = link.getLinkType(); // PRODUCT or CLIENT_ROLE
					String entityLinkedKey = link.getEntityLinkedKey();
					boolean isLinkDefault = link.isDefault();
					boolean isLinkRequired = link.isRequired();
					System.out.println("Link Data. Type=" + linkType + "\tEntity Key=" + entityLinkedKey
							+ "\tRequired=" + isLinkRequired + "\tDefault=" + isLinkDefault);

					// Test Get field properties for this entity
					boolean isAvailableForEntity = field.isAvailableForEntity(entityLinkedKey);
					boolean isRequiredForEntity = field.isRequired(entityLinkedKey);
					boolean isDefaultForEntity = field.isDefault(entityLinkedKey);
					System.out.println("Available =" + isAvailableForEntity + "\tRequired=" + isRequiredForEntity
							+ "\tDefault=" + isDefaultForEntity);
				}
				// Log Custom Field selection options and dependencies
				// Dependent Custom fields are available since 3.10 (see MBU-7914)
				List<CustomFieldSelection> customFieldSelectionOptions = field.getCustomFieldSelectionOptions();
				if (customFieldSelectionOptions != null && customFieldSelectionOptions.size() > 0) {
					for (CustomFieldSelection option : customFieldSelectionOptions) {
						System.out.println("\nSelection Options:");
						String value = option.getValue();
						System.out.println("Value =" + value + "\tKey=" + option.getEncodedKey());
						CustomFilterConstraint constraint = option.getConstraint();
						if (constraint != null) {
							CUSTOM_FIELD_ID = field.getId(); // Store this field's ID for testGetCustomField() test
							System.out.println("Value =" + value + "\tdepends on field="
									+ constraint.getCustomFieldKey() + "\twith valueKey=" + constraint.getValue());
						}
					}
				}
			}
		}
		System.out.println();

	}

	public static void testGetTransactionChannels() throws MambuApiException {
		System.out.println("\nIn testGetTransactionChannels");
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		List<TransactionChannel> transactionChannels = organizationService.getTransactionChannels();

		System.out.println("Total Channels=" + transactionChannels.size());
		for (TransactionChannel channel : transactionChannels) {
			List<ChannelField> fields = channel.getChannelFields();
			int channelFieldsCount = (fields == null) ? 0 : fields.size();
			System.out.println("Channel Name=" + channel.getName() + "\tId=" + channel.getId() + "\tTotal Fields="
					+ channelFieldsCount);
			System.out.println();
			for (ChannelField field : fields) {
				System.out.println("Field Name=" + field.name() + " ");
			}
			System.out.println();
		}
	}

	// Update Custom Field values for the demo Branch and for demo Centre and delete the first custom field
	public static void testUpdateDeleteCustomFields() throws MambuApiException {
		System.out.println("\nIn testUpdateDeleteCustomFields");

		List<CustomFieldValue> customFieldValues;
		System.out.println("\nUpdating demo Branch custom fields...");
		customFieldValues = updateCustomFields(Branch.class);

		System.out.println("\nDeleting first custom field for a demo Branch...");
		deleteCustomField(Branch.class, customFieldValues);

		System.out.println("\n\nUpdating demo Centre custom fields...");
		customFieldValues = updateCustomFields(Centre.class);

		System.out.println("\nDeleting first custom field for a demo Centre...");
		deleteCustomField(Group.class, customFieldValues);

	}

	// Test Posting Index Interest Rates. Available since 3.10
	public static void testPostIndexInterestRate() throws MambuApiException {
		System.out.println("\nIn testPostIndexInterestRate");
		// Note that there is no API yet to get Index Rate Sources. API developers need to know the rate source key to
		// post new rates. These keys can be obtained from Mambu. They can also be looked up from the getProduct API
		// response. See MBU-8059 for more details

		// Encoded key for the Index Interest Rate Source
		String indexRateSourceKey = "8a6c06384b47afd4014b480624e6003a";
		int dateOffset = (int) (Math.random() * 30) * 24 * 60 * 60 * 1000; // rate start dates cannot be duplicated. Use
																			// random offset for each test run
		Date startDate = new Date(new Date().getTime() + dateOffset);
		// Create new IndexRate
		IndexRate indexRate = new IndexRate(startDate, new BigDecimal(3.5));

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();
		IndexRate indexRateResult = organizationService.postIndexInterestRate(indexRateSourceKey, indexRate);

		System.out.println("Interest Rate updated. New Rate=" + indexRateResult.getRate() + " for source="
				+ indexRateResult.getRateSource().getName() + " Start date=" + indexRateResult.getStartDate());

	}

	// Private helper to Update all custom fields a Branch and for a Centre
	private static List<CustomFieldValue> updateCustomFields(Class<?> entityClass) throws MambuApiException {

		String entityName = entityClass.getSimpleName();
		boolean forBranch = (entityClass.equals(Branch.class)) ? true : false;
		String entityId = (forBranch) ? demoBranch.getId() : demoCentre.getId();

		// Get Current custom field values first
		List<CustomFieldValue> customFieldValues = (forBranch) ? demoBranch.getCustomFieldValues() : demoCentre
				.getCustomFieldValues();

		if (customFieldValues == null || customFieldValues.size() == 0) {
			System.out.println("WARNING: No Custom fields defined for demo " + entityName + " with ID=" + entityId
					+ ". Nothing to update");
			return null;
		}
		// Update custom field values
		OrganizationService orgService = MambuAPIFactory.getOrganizationService();
		for (CustomFieldValue value : customFieldValues) {
			// TODO: re-test get fieldID via getCustomFieldId() when MBU-6923 is fixed: Null for Branch, Centre and User
			String testFieldId = value.getCustomFieldId(); // returns null for Branch, Centre and User details

			// Use customFieldId from the CustomField, this always works
			CustomField field = value.getCustomField();
			String fieldId = field.getId();

			// Create valid new value for a custom field
			String newValue = DemoUtil.makeNewCustomFieldValue(value).getValue();

			// Update Custom Field value
			boolean updateStatus;
			System.out.println("\nUpdating Custom Field with ID=" + fieldId + " for " + entityName + " with ID="
					+ entityId + "\tField's other ID=" + testFieldId);

			updateStatus = (forBranch) ? orgService.updateBranchCustomField(entityId, fieldId, newValue) : orgService
					.updateCentreCustomField(entityId, fieldId, newValue);

			String statusMessage = (updateStatus) ? "Success" : "Failure";
			System.out.println(statusMessage + " updating Custom Field, ID=" + fieldId + " for demo " + entityName
					+ " with ID=" + entityId + " New value=" + newValue);

		}

		return customFieldValues;
	}

	// Private helper to Delete the first custom field for a client or group
	private static void deleteCustomField(Class<?> entityClass, List<CustomFieldValue> customFieldValues)
			throws MambuApiException {

		String entityName = entityClass.getSimpleName();
		boolean forBranch = (entityClass.equals(Branch.class)) ? true : false;
		String entityId = (forBranch) ? demoBranch.getId() : demoCentre.getId();

		if (customFieldValues == null || customFieldValues.size() == 0) {
			System.out.println("WARNING: No Custom fields defined for demo " + entityName + " with ID=" + entityId
					+ ". Nothing to delete");
			return;
		}

		// Delete the first field on the list
		String customFieldId = customFieldValues.get(0).getCustomField().getId();

		OrganizationService orgService = MambuAPIFactory.getOrganizationService();
		boolean deleteStatus = (forBranch) ? orgService.deleteBranchCustomField(entityId, customFieldId) : orgService
				.deleteCentreCustomField(entityId, customFieldId);

		String statusMessage = (deleteStatus) ? "Success" : "Failure";
		System.out.println(statusMessage + " deleting Custom Field, ID=" + customFieldId + " for demo " + entityName
				+ " with ID=" + entityId);
	}
}
