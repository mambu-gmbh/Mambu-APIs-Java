package demo;

import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.LinesOfCreditService;
import com.mambu.apisdk.util.MambuEntity;
import com.mambu.linesofcredit.shared.model.AccountsFromLineOfCredit;
import com.mambu.linesofcredit.shared.model.LineOfCredit;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.savings.shared.model.SavingsAccount;

/**
 * Test class to show example usage for Line Of Credit (LoC) API
 * 
 * @author mdanilkis
 * 
 */
public class DemoTestLoCService {

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testGetLinesOfCredit(); // Available since 3.11

			String lineOfCrediId = testGetCustomerLinesOfCredit(); // Available since 3.11

			testGetAccountsForLineOfCredit(lineOfCrediId);// Available since 3.11

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Lines of Credit Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	/**
	 * Test Get paginated list of all lines of credit and LoC details
	 * 
	 * @throws MambuApiException
	 */
	public static void testGetLinesOfCredit() throws MambuApiException {
		System.out.println("\nIn testGetLinesOfCredit");

		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();
		Integer offset = 0;
		Integer limit = 30;

		// Test getting all lines of credit
		List<LineOfCredit> linesOfCredit = linesOfCreditService.getAllLinesOfCredit(offset, limit);
		System.out.println("Total Lines of Credit=" + linesOfCredit.size());
		if (linesOfCredit.size() == 0) {
			System.out.println("*** No Lines of Credit to test ***");
			return;
		}
		// Test get Line Of Credit details
		String lineofcreditId = linesOfCredit.get(0).getId();
		System.out.println("Getting details for Line of Credit ID=" + lineofcreditId);
		LineOfCredit lineOfCredit = linesOfCreditService.getLineOfCredit(lineofcreditId);
		// Log returned LoC
		System.out.println("Line of Credit. ID=" + lineOfCredit.getId() + "\tAmount=" + lineOfCredit.getAmount()
				+ "\tOwnerType=" + lineOfCredit.getOwnerType() + "\tHolderKey="
				+ lineOfCredit.getAccountHolder().getAccountHolderKey());
	}

	/**
	 * Test Get lines of credit for a Client and Group
	 * 
	 * @return any of the LoC IDs
	 */
	public static String testGetCustomerLinesOfCredit() throws MambuApiException {
		System.out.println("\nIn testGetCustomerLinesOfCredit");

		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();
		Integer offset = 0;
		Integer limit = 30;
		// Test Get line of credit for a Client
		// Get Demo Client ID first
		DemoEntityParams entityParams = DemoEntityParams.getEntityParams(MambuEntity.CLIENT);
		final String clientId = entityParams.getId();
		// Get Lines of Credit for a client
		List<LineOfCredit> clientLoCs = linesOfCreditService.getClientLinesOfCredit(clientId, offset, limit);
		System.out.println(clientLoCs.size() + " lines of credit  for Client " + entityParams.getName() + " "
				+ entityParams.getId());

		String clientLoCId = (clientLoCs.size() > 0) ? clientLoCs.get(0).getId() : null;
		// Test Get line of credit for a Group
		// Get Demo Group ID first
		entityParams = DemoEntityParams.getEntityParams(MambuEntity.GROUP);
		final String groupId = entityParams.getId();
		// Get Lines of Credit for a group
		List<LineOfCredit> groupLoCs = linesOfCreditService.getGroupLinesOfCredit(groupId, offset, limit);
		System.out.println(groupLoCs.size() + " lines of credit for Group " + entityParams.getName() + " "
				+ entityParams.getId());
		String groupLoCId = (groupLoCs.size() > 0) ? groupLoCs.get(0).getId() : null;

		// return the Id for one of the LoC for subsequent tests
		return (clientLoCId != null) ? clientLoCId : groupLoCId;
	}

	/**
	 * Test Get Accounts for a line of Credit
	 * 
	 * @param lineOfCreditId
	 *            an id or encoded key for a Line of Credit
	 * @throws MambuApiException
	 */
	public static void testGetAccountsForLineOfCredit(String lineOfCreditId) throws MambuApiException {
		System.out.println("\nIn testGetAccountsForLineOfCredit");
		// Test Get Accounts for a line of credit

		if (lineOfCreditId == null) {
			System.out.println("No Line of credit to get accounts");
			return;
		}
		LinesOfCreditService linesOfCreditService = MambuAPIFactory.getLineOfCreditService();

		System.out.println("\nGetting all accounts for LoC with ID= " + lineOfCreditId);
		AccountsFromLineOfCredit accountsForLoC = linesOfCreditService.getAccountsForLineOfCredit(lineOfCreditId);
		// Log returned results
		List<LoanAccount> loanAccounts = accountsForLoC.getLoanAccounts();
		List<SavingsAccount> savingsAccounts = accountsForLoC.getSavingsAccounts();
		System.out.println("Total Loan Accounts=" + loanAccounts.size() + "\tTotal Savings Accounts="
				+ savingsAccounts.size() + " for LoC=" + lineOfCreditId);
	}
}
