package demo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.mambu.accounts.shared.model.TransactionChannel;
import com.mambu.accounts.shared.model.TransactionChannel.ChannelField;
import com.mambu.api.server.handler.settings.organization.model.JSONOrganization;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.OrganizationService;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.clients.shared.model.IdentificationDocumentTemplate;
import com.mambu.core.shared.model.Address;
import com.mambu.core.shared.model.Currency;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.core.shared.model.CustomFieldType;
import com.mambu.core.shared.model.DateFormatType;
import com.mambu.core.shared.model.GeneralSettings;
import com.mambu.core.shared.model.IndexRate;
import com.mambu.core.shared.model.ObjectLabel;
import com.mambu.core.shared.model.Organization;
import com.mambu.core.shared.model.Role;
import com.mambu.core.shared.model.UsageRights;
import com.mambu.core.shared.model.User;
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

	private static User demoUser;
	private static Centre demoCentre;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			demoUser = DemoUtil.getDemoUser();
			demoCentre = DemoUtil.getDemoCentre();

			testGetOrganizationDetails();// Available since 3.11

			testPostIndexInterestRate(); // Available since 3.10

			testGetTransactionChannels(); // Available since 3.7

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

			testUpdateDeleteCustomFields(); // Available since 3.8

			testGetDocumentTemplates(); // Available since 3.10.5

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
		CustomFieldType customFieldType = CustomFieldType.CLIENT_INFO;

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
				CUSTOM_FIELD_ID = DemoUtil.logCustomField(field);
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

			// Transaction channels also have UsageRights since Mambu 3.13. See MBU-9562
			String demoUserRoleKey = (demoUser.getRole() == null) ? null : demoUser.getRole().getEncodedKey();
			UsageRights rights = channel.getUsageRights();
			if (rights != null) {
				List<Role> roles = rights.getRoles();
				int totalRoles = (roles == null) ? 0 : roles.size();
				System.out.println("Is Accessible By All Users=" + rights.isAccessibleByAllUsers() + "\tTotal Roles="
						+ totalRoles + "\tDemo User Role Key=" + demoUserRoleKey);
				if (roles != null) {
					for (Role role : roles) {
						System.out.println("For Role Key=" + role.getEncodedKey());
					}
				}

			}
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

		// Delegate tests to new since 3.11 DemoTestCustomFiledValueService
		// Test fields for a Branch
		DemoTestCustomFiledValueService.testUpdateDeleteCustomFields(MambuEntityType.BRANCH);

		// Test fields for a Centre
		DemoTestCustomFiledValueService.testUpdateDeleteCustomFields(MambuEntityType.CENTRE);
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

	// Test getting Identification Document Templates
	public static void testGetDocumentTemplates() throws MambuApiException {
		System.out.println("\nIn testGetDocumentTemplates");

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();
		List<IdentificationDocumentTemplate> templates = organizationService.getIdentificationDocumentTemplates();

		System.out.println("Total Templates Returned=" + templates.size());
		// Print templates details
		for (IdentificationDocumentTemplate template : templates) {
			System.out.println("Template=" + template.getDocumentIdTemplate() + "\tType=" + template.getDocumentType()
					+ "\tAuthority=" + template.getIssuingAuthority() + "\tIs Mandatory="
					+ template.getMandatoryForClients() + "\tAllow Attachments=" + template.getAllowAttachments()
					+ "\tKey=" + template.getEncodedKey());

		}
	}

	// Get Organization details. Available since 3.11
	public static void testGetOrganizationDetails() throws MambuApiException {
		System.out.println("\nIn testGetOrganization");

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		// Test Get organization
		JSONOrganization jsonOrganization = organizationService.getOrganization();

		Organization organization = jsonOrganization.getOrganization();
		Address address = jsonOrganization.getAddress();
		System.out.println("Address=" + address.getLine1() + " " + address.getCity());

		System.out.println("Organization=" + organization.getName() + "\tTimeZoneId=" + organization.getTimeZoneID()
				+ "\tPhone=" + organization.getPhoneNo() + "\tEmail=" + organization.getEmailAddress());

		// Test Get General Settings API
		GeneralSettings generalSettings = organizationService.getGeneralSettings();
		System.out.println("\nSettings. BirthDateRequired=" + generalSettings.getBirthDateRequired() + "\tDATE_FORMAT="
				+ generalSettings.getDateFormats().get(DateFormatType.DATE_FORMAT) + "\tDATETIME_FORMAT="
				+ generalSettings.getDateFormats().get(DateFormatType.DATE_TIME_FORMAT) + "\tDecimalSeperator="
				+ generalSettings.getDecimalSeperator());

		// Test Get Mambu Object Labels
		List<ObjectLabel> objectLabels = organizationService.getObjectLabels();
		System.out.println("\nTotal Object Labels Returned=" + objectLabels.size());
		// Print ObjectLabel details
		for (ObjectLabel label : objectLabels) {
			System.out.println("Object Label. Type=" + label.getType() + "\tSingular=" + label.getSingularValue()
					+ "\tPlural=" + label.getPluralValue() + "\tLanguage=" + label.getLanguage()
					// Note, Encoded key is not returned in response.
					+ "\tHas Custom Value=" + label.hasCustomValue());

		}
		// Test Get Organization Logo
		String logo = organizationService.getBrandingLogo();
		System.out.println("\nEncoded Logo file=" + logo);

		// Test Get Organization Icon
		String icon = organizationService.getBrandingIcon();
		System.out.println("\nEncoded Icon file=" + icon);

	}
}
