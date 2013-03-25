package demo;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.OrganizationService;
import com.mambu.core.shared.model.Currency;
import com.mambu.organization.shared.model.Branch;

/**
 * Test class to show example usage of the api calls
 * 
 * @author edanilkis
 * 
 */
public class DemoTestOrganizationService {

	private static String BRANCH_ID = "Richmond01"; // 414659806 RICHMOND_001

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testGetCurrency();

			testGetAllBranches();

			testGetBranchesByPage();

			testGetBranch();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Organization Service");
			System.out.println("Exception Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}
	public static void testGetAllBranches() throws MambuApiException {
		System.out.println("\nIn testGetBranches");
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String offset = null;
		String limit = null;
		Branch branches[] = organizationService.getBranches(offset, limit);

		System.out.println("All Total=" + branches.length);
		for (Branch branch : branches) {
			System.out.println(" Name=" + branch.getName() + "\tId=" + branch.getId());
		}
		System.out.println();

	}

	public static void testGetBranchesByPage() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		String offset = "1";
		String limit = "1";
		System.out.println("\nIn testGetBranchesByPage" + "  Offset=" + offset + "  Limit=" + limit);
		Branch branches[] = organizationService.getBranches(offset, limit);

		System.out.println("Total=" + branches.length);
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
	public static void testGetCurrency() throws MambuApiException {
		System.out.println("\nIn testGetCurrency");
		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		Currency currency = organizationService.getCurrency();

		System.out.println("Currency code=" + currency.getCode() + "   Name=" + currency.getName());

	}

}
