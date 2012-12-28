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

	private static String BRANCH_ID = "RICHMOND_001"; //

	public static void main(String[] args) {

		try {
			MambuAPIFactory.setUp("demo.mambucloud.com", "api", "api");
			
			testGetCurrency();			
			
			testGetBranches();
			
			//testGetBranch(); // API not implemented 

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Organization Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testGetBranches() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		Branch branches[] = organizationService.getBranches(null, null);

		System.out.println(" Total branches=" + branches.length + ".  Firts branch=" + branches[0].getName()
				+ "   Key= " + branches[0].getEncodedKey());

	}

	public static void testGetBranch() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		Branch branch = organizationService.getBranch(BRANCH_ID);

		System.out.println("Branch id=" + branch.getId() + "   Name=" + branch.getName());

	}
	public static void testGetCurrency() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		Currency currency = organizationService.getCurrency();

		System.out.println("Currency code=" + currency.getCode() + "   Name=" + currency.getName());

	}

}
