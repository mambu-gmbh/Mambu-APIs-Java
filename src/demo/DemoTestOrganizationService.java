package demo;

import java.util.Date;
import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.OrganizationService;
import com.mambu.core.shared.model.Currency;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;

/**
 * Test class to show example usage of the api calls
 * 
 * @author edanilkis
 * 
 */
public class DemoTestOrganizationService {

	private static String BRANCH_ID = "Richmond01"; // Richmond01 TAK 001

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			// TODO:Custom Fields API to be tested with Mambu 3.3, see MBU-2486
			// testCustomField();
			// testGetCustomFieldSetsByType();

			testGetCentresByPage();
			testGetCentre();

			testGetCurrency();

			testGetAllBranches();
			testGetCentresByBranch();

			testGetBranchesByPage();

			testGetBranch();

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
		for (Branch branch : branches) {
			System.out.println(" Name=" + branch.getName() + "\tId=" + branch.getId());
		}
		System.out.println();

	}

	public static void testGetBranch() throws MambuApiException {
		System.out.println("\nIn testGetBranch");
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		Branch branch = organizationService.getBranch(BRANCH_ID); // BRANCH_ID

		if (branch != null)
			System.out.println("Branch id=" + BRANCH_ID + " found. Returned:  ID=" + branch.getId() + "   Name="
					+ branch.getName());
		else
			System.out.println("Not Found Branch id=" + BRANCH_ID);
	}
	public static void testGetCentre() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String centreId = "Richmond_Center 1"; // Richmond_Center_1 CanWest_001
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
		for (Centre centre : centres) {
			System.out.println(" Name=" + centre.getName() + "\tId=" + centre.getId());
		}
		System.out.println();

	}
	public static void testGetCentresByBranch() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String branchId = BRANCH_ID; // "Richmond01"
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
	public static void testCustomField() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String fieldId = "Family_Size_Clients";
		System.out.println("\nIn testCustomField by ID." + "  Field ID=" + fieldId);

		Date d1 = new Date();
		CustomFieldValue customFieldValue = organizationService.getCustomField(fieldId);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("CustomFieldValue: ID=" + customFieldValue.getCustomFieldId() + " Value="
				+ customFieldValue.getValue() + " Name=" + customFieldValue.getCustomField().getName() + " Total time="
				+ diff);

	}

	// Get CustomFieldSets by Type
	public static void testGetCustomFieldSetsByType() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		CustomField.Type customFieldType = CustomField.Type.CLIENT_INFO; //

		System.out.println("\nIn testGetCustomFieldSetsByType");

		Date d1 = new Date();
		List<CustomFieldSet> sustomFieldSets = organizationService.getCustomFieldSets(customFieldType);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Total Sets returned=" + sustomFieldSets.size() + " Total time=" + diff);
		for (CustomFieldSet set : sustomFieldSets) {
			System.out.println(" Name=" + set.getName() + "\tType=" + set.getType().toString());
		}
		System.out.println();

	}
}
