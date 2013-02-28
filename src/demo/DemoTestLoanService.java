package demo;

import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.LoansService;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanTransaction;

/**
 * Test class to show example usage of the api calls
 * 
 * @author edanilkis
 * 
 */
public class DemoTestLoanService {

	private static String CLIENT_ID = "250213653"; // 046360136 282600987

	private static String GROUP_ID = "588752540"; //
	private static String LOAN_ACCOUNT_ID = "ZKII792"; // ZKII792
	private static String BRANCH_ID = "Richmond01";

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testGetLoanAccount();
			testGetLoanAccountDetails();

			testGetLoanAccountsForClient();
			testGetLoanAccountsForGroup();

			testGetLoanAccountsByBranchOfficerState();
			// transactions
			testGetLoanAccountTransactions();

			testApproveLoanAccount();

			testDisburseLoanAccountWithDetails();

			testRepayLoanAccount();
			testApplyFeeToLoanAccount();

			testGetLoanAccountTransactions();

			// Products
			testGetLoanProducts();
			testGetLoanProductById();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Loan Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testGetLoanAccount() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		System.out.println("Got loan account  by ID: " + loanService.getLoanAccount(LOAN_ACCOUNT_ID).getName());

	}

	public static void testGetLoanAccountDetails() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccountDetails");
		LoansService loanService = MambuAPIFactory.getLoanService();

		System.out.println("Got loan account by ID with details: "
				+ loanService.getLoanAccountDetails(LOAN_ACCOUNT_ID).getName());

	}

	// / Transactions testing

	public static void testDisburseLoanAccountWithDetails() throws MambuApiException {
		System.out.println("\nIn test Disburse LoanAccount with Deatils");

		LoansService loanService = MambuAPIFactory.getLoanService();

		String accountId = LOAN_ACCOUNT_ID;
		String amount = "10000.00";
		String disbursalDate = null;// "2013-1-22";
		String firstRepaymentDate = null; // "2012-12-06";
		String paymentMethod = "CASH";// CASH CHECK RECEIPT BANK_TRANSFER
		String receiptNumber = "D_REC1123";
		String bankNumber = "D_BAN_KNUMBER345";
		String checkNumber = "D_CHECK9900";
		String bankAccountNumber = "D_BANK_ACCT4567";
		String bankRoutingNumber = "D_BNK_ROUT_2344";
		String notes = "Disbursed loan for testing";

		LoanTransaction transaction = loanService.disburseLoanAccount(accountId, amount, disbursalDate,
				firstRepaymentDate, paymentMethod, bankNumber, receiptNumber, checkNumber, bankAccountNumber,
				bankRoutingNumber, notes);

		System.out.println("\nLoan for Disbursment with Details: Trans Id=" + transaction.getTransactionId()
				+ " amount=" + transaction.getAmount().toString());
	}

	public static void testGetLoanAccountTransactions() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccount Transactions");
		LoansService loanService = MambuAPIFactory.getLoanService();
		String offest = "0";
		String limit = "10";

		List<LoanTransaction> transactions = loanService.getLoanAccountTransactions(LOAN_ACCOUNT_ID, offest, limit);

		System.out.println("Got loan accounts transactions in a range for the Loan with the " + LOAN_ACCOUNT_ID
				+ " id:" + " Range=" + offest + "  " + limit);
		for (LoanTransaction transaction : transactions) {
			System.out.println("Trans Date, type=" + transaction.getEntryDate().toString() + "  "
					+ transaction.getType());
		}
	}

	public static void testRepayLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Repay LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();
		String amount = "93.55";
		String date = "2012-11-23";
		String notes = "repayment notes from API";
		String paymentMethod = "CASH";// CHECK,
		String receiptNumber = "REC1123";
		String bankNumber = "BAN_KNUMBER345";
		String checkNumber = "CHECK9900";
		String bankAccountNumber = "BANK_ACCT4567";
		String bankRoutingNumber = "BNK_ROUT_2344";

		LoanTransaction transaction = loanService.makeLoanRepayment(LOAN_ACCOUNT_ID, amount, date, notes,
				paymentMethod, receiptNumber, bankNumber, checkNumber, bankAccountNumber, bankRoutingNumber);

		System.out.println("repayed loan account with the " + LOAN_ACCOUNT_ID + " id response=" + "   for amount="
				+ transaction.getAmount());
	}

	public static void testApplyFeeToLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Applying Fee to a Loan Account");

		LoansService loanService = MambuAPIFactory.getLoanService();
		String accountId = LOAN_ACCOUNT_ID;
		String amount = "10";
		String repaymentNumber = "2";
		String notes = "Notes for applying fee to a loan";

		LoanTransaction transaction = loanService.applyFeeToLoanAccount(accountId, amount, repaymentNumber, notes);

		System.out.println("Loan Fee response= " + transaction.getTransactionId().toString() + "  Trans Amount="
				+ transaction.getAmount().toString() + "   Fees paid=" + transaction.getFeesPaid().toString());

	}

	public static void testGetLoanAccountsByBranchOfficerState() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccounts ByBranch Officer State");

		LoansService loanService = MambuAPIFactory.getLoanService();

		String branchId = "Richmond01"; // Berlin_001 RICHMOND_001
		String creditOfficerUserName = "MichaelD";
		String accountState = "ACTIVE"; // CLOSED_WITHDRAWN ACTIVE_IN_ARREARS

		List<LoanAccount> loanAccounts = loanService.getLoanAccountsByBranchOfficerState(branchId,
				creditOfficerUserName, accountState);

		if (loanAccounts != null)
			System.out.println("Got loan accounts for the branch, officer, state, total loans=" + loanAccounts.size());
		for (LoanAccount account : loanAccounts) {
			System.out.println("Account Name=" + account.getLoanName() + "  BranchId=" + account.getAssignedBranchKey()
					+ "   Credit Officer=" + account.getAssignedUserKey());
		}
	}
	public static void testGetLoanAccountsForClient() throws MambuApiException {
		System.out.println("\nIn testGetLoan Accounts ForClient");
		LoansService loanService = MambuAPIFactory.getLoanService();

		List<LoanAccount> loanAccounts = loanService.getLoanAccountsForClient(CLIENT_ID);

		System.out.println("Got loan accounts for the client with the " + CLIENT_ID + " id, Total="
				+ loanAccounts.size());
		for (LoanAccount account : loanAccounts) {
			System.out.print(account.getLoanName() + " ");
		}
		System.out.println();
	}

	public static void testGetLoanAccountsForGroup() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccounts ForGroup");
		LoansService loanService = MambuAPIFactory.getLoanService();

		List<LoanAccount> loanAccounts = loanService.getLoanAccountsForGroup(GROUP_ID);

		System.out.println("Got " + loanAccounts.size() + " loan accounts for the group with the " + GROUP_ID
				+ " id, Total= " + loanAccounts.size());
		int i = 0;
		for (LoanAccount account : loanAccounts) {
			if (i > 0)
				System.out.print(", ");
			System.out.print(account.getLoanName());
		}
		System.out.println();
	}

	public static void testApproveLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Approve LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		LoanAccount account = loanService.approveLoanAccount(LOAN_ACCOUNT_ID, "some demo notes");

		System.out.println("Approving loan account with the " + LOAN_ACCOUNT_ID + " Loan name" + account.getLoanName()
				+ "  Account State=" + account.getState().toString());
	}

	// Loan Products
	public static void testGetLoanProducts() throws MambuApiException {
		System.out.println("\nIn testGetLoanProducts");
		LoansService loanService = MambuAPIFactory.getLoanService();

		List<LoanProduct> products = loanService.getLoanProducts();

		System.out.println("Got loan products, count=" + products.size());

		if (products.size() > 0) {
			for (LoanProduct product : products) {
				System.out.println("Product=" + product.getName() + "  Id=" + product.getId() + " Loan Type="
						+ product.getLoanType().name());
			}
		}

	}

	public static void testGetLoanProductById() throws MambuApiException {
		System.out.println("\nIn testGetLoanProductById");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String productId = "AGL"; //

		LoanProduct product = loanService.getLoanProduct(productId);

		System.out.println("Product=" + product.getName() + "  Id=" + product.getId() + " Loan Type="
				+ product.getLoanType().name());

	}
}
