package demo;

import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.LoansService;
import com.mambu.loans.shared.model.LoanAccount;

/**
 * Test class to show example usage of the api calls
 * 
 * @author edanilkis
 * 
 */
public class DemoTestLoanService {

	private static String CLIENT_ID = "1000000043";
	private static String GROUP_ID = "445076768";
	private static String LOAN_ACCOUNT_ID = "BNGP767";

	public static void main(String[] args) {

		try {
			MambuAPIFactory.setUp("demo.mambucloud.com", "api", "1");

			testGetLoanAccount();
			testGetLoanAccountsForClient();
			testGetLoanAccountsForGroup();
			testApproveLoanAccount();

		} catch (MambuApiException e) {
			System.out.println(e.getCause());
			System.out.println(e.getMessage());
			System.out.println(e.getErrorCode());
		}

	}

	public static void testGetLoanAccount() throws MambuApiException {

		LoansService loanService = MambuAPIFactory.getLoanService();

		System.out.println("Got loan account: " + loanService.getLoanAccount(LOAN_ACCOUNT_ID).getName());

	}

	public static void testGetLoanAccountsForClient() throws MambuApiException {

		LoansService loanService = MambuAPIFactory.getLoanService();

		List<LoanAccount> loanAccounts = loanService.getLoanAccountsForClient(CLIENT_ID);

		System.out.println("Got loan accounts for the client with the " + CLIENT_ID + " id:");
		for (LoanAccount account : loanAccounts) {
			System.out.print(account.getLoanName() + " ");
		}
	}

	public static void testGetLoanAccountsForGroup() throws MambuApiException {

		LoansService loanService = MambuAPIFactory.getLoanService();

		List<LoanAccount> loanAccounts = loanService.getLoanAccountsForGroup(GROUP_ID);

		System.out.println("Got loan accounts for the group with the " + GROUP_ID + " id:");
		for (LoanAccount account : loanAccounts) {
			System.out.print(account.getLoanName() + ", ");
		}
	}

	public static void testApproveLoanAccount() throws MambuApiException {

		LoansService loanService = MambuAPIFactory.getLoanService();

		String response = loanService.approveLoanAccount(LOAN_ACCOUNT_ID, "some notes");

		System.out.println("Approving loan account with the " + LOAN_ACCOUNT_ID + " id returned " + response
				+ " response");
	}
}
