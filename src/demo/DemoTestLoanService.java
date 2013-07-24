package demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.mambu.accounts.shared.model.AccountHolderType;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.LoanAccountExpanded;
import com.mambu.apisdk.services.LoansService;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.Money;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanAccount.RepaymentPeriodUnit;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanTransaction;

/**
 * Test class to show example usage of the api calls
 * 
 * @author edanilkis
 * 
 */
public class DemoTestLoanService {

	private static String CLIENT_ID = "729859576"; // 548919675 282600987 729859576

	private static String GROUP_ID = "118035060"; // 118035060 588752540
	private static String LOAN_ACCOUNT_ID = "RNTV156"; // DTQK377 WETZ340 RRSF961 PRTO161 DLWY699
	private static String BRANCH_ID = "NE008"; // GBK 001

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testCreateJsonAccount();
			testGetLoanAccountDetails();

			testGetLoanProducts();

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

			// Show latest transactions now
			testGetLoanAccountTransactions();

			// Products
			testGetLoanProducts();
			testGetLoanProductById();

			testCreateJsonAccount();

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

	// Create Loan Account
	public static void testCreateJsonAccount() throws MambuApiException {
		System.out.println("\nIn testCreateJsonAccount");

		LoansService loanService = MambuAPIFactory.getLoanService();

		LoanAccount account = new LoanAccount();
		account.setId(null);
		account.setAccountHolderKey("8ad661123b36cfaf013b42c2e0f46dca"); // CLIENT_ID "8ad661123b36cfaf013b42c2e0f46dca"
		account.setAccountHolderType(AccountHolderType.CLIENT);
		account.setProductTypeKey("8ad661123b36cfaf013b42cbcf2c6dd3");// "8ad661123b36cfaf013b42cbcf2c6dd3"
		account.setLoanAmount(new Money(7500.00));
		account.setInterestRate(new BigDecimal("3.2"));
		account.setRepaymentInstallments(20);
		// From Product
		account.setRepaymentPeriodUnit(RepaymentPeriodUnit.DAYS);
		account.setRepaymentPeriodCount(1);
		// Set Custom fields to null in the account
		account.setCustomFieldValues(null);

		// ADd Custom Fields

		List<CustomFieldValue> clientCustomInformation = new ArrayList<CustomFieldValue>();

		CustomFieldValue custField1 = new CustomFieldValue();
		String customFieldId = "Loan_Purpose_Loan_Accounts";
		String customFieldValue = "My Loan Purpose 5";

		custField1.setCustomFieldId(customFieldId);
		custField1.setValue(customFieldValue);
		// Add new field to the list
		clientCustomInformation.add(custField1);
		// Field #2
		// Loan_Originator_Loan_Accounts
		CustomFieldValue custField2 = new CustomFieldValue();
		customFieldId = "Loan_Originator_Loan_Accounts";
		customFieldValue = "Trust";

		custField2.setCustomFieldId(customFieldId);
		custField2.setValue(customFieldValue);
		// Add new field to the list
		clientCustomInformation.add(custField2);

		// Add All custom fields
		// account.setCustomFieldValues(clientCustomInformation);

		// Create Account Expanded
		LoanAccountExpanded accountExpanded = new LoanAccountExpanded();
		accountExpanded.setLoanAccount(account);
		accountExpanded.setCustomInformation(clientCustomInformation);

		// Create Account in Mambu
		LoanAccountExpanded newAccount = loanService.createAccount(accountExpanded);

		// accented E

		System.out.println("Loan Account created OK, ID=" + newAccount.getLoanAccount().getId() + " Name= "
				+ newAccount.getLoanAccount().getLoanName() + " Account Holder Key="
				+ newAccount.getLoanAccount().getAccountHolderKey());

	}
	// / Transactions testing

	public static void testDisburseLoanAccountWithDetails() throws MambuApiException {
		System.out.println("\nIn test Disburse LoanAccount with Deatils");

		LoansService loanService = MambuAPIFactory.getLoanService();

		String accountId = LOAN_ACCOUNT_ID;
		String amount = "10000.00";
		String disbursalDate = "2013-4-3";
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

		System.out.println("\nLoan for Disbursement with Details: Transaction Id=" + transaction.getTransactionId()
				+ " amount=" + transaction.getAmount().toString());
	}

	public static void testGetLoanAccountTransactions() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccount Transactions");
		LoansService loanService = MambuAPIFactory.getLoanService();
		String offest = "4";
		String limit = "8";

		List<LoanTransaction> transactions = loanService.getLoanAccountTransactions(LOAN_ACCOUNT_ID, offest, limit);

		System.out.println("Got loan accounts transactions, total=" + transactions.size()
				+ " in a range for the Loan with the " + LOAN_ACCOUNT_ID + " id:" + " Range=" + offest + "  " + limit);
		for (LoanTransaction transaction : transactions) {
			System.out.println("Trans ID=" + transaction.getTransactionId() + "  " + transaction.getType() + "  "
					+ transaction.getEntryDate().toString());
		}
	}
	public static void testRepayLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Repay LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();
		String amount = "93.55";
		String date = null; // "2012-11-23";
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
		String repaymentNumber = "100";
		String notes = "Notes for applying fee to a loan";

		LoanTransaction transaction = loanService.applyFeeToLoanAccount(accountId, amount, repaymentNumber, notes);

		System.out.println("Loan Fee response= " + transaction.getTransactionId().toString() + "  Trans Amount="
		// + transaction.getAmount().toString() + "   Fees paid=" + transaction.getFeesPaid().toString());
				+ transaction.getAmount().toString() + "   Fees amount=" + transaction.getFeesAmount().toString());

	}

	public static void testGetLoanAccountsByBranchOfficerState() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccounts ByBranch Officer State");

		LoansService loanService = MambuAPIFactory.getLoanService();

		String branchId = BRANCH_ID; // BRANCH_ID Berlin_001 RICHMOND_001 GBK 001
		String creditOfficerUserName = null; // "demo"; // MichaelD
		String accountState = "ACTIVE"; // CLOSED_WITHDRAWN ACTIVE_IN_ARREARS
		String offset = null;
		String limit = null;

		List<LoanAccount> loanAccounts = loanService.getLoanAccountsByBranchOfficerState(branchId,
				creditOfficerUserName, accountState, offset, limit);

		if (loanAccounts != null)
			System.out.println("Got loan accounts for the branch, officer, state, total loans=" + loanAccounts.size());
		for (LoanAccount account : loanAccounts) {
			System.out.println("Account Name=" + account.getId() + "-" + account.getLoanName() + "  BranchId="
					+ account.getAssignedBranchKey() + "   Credit Officer=" + account.getAssignedUserKey());
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

		String offset = "0";
		String limit = "100";

		List<LoanProduct> products = loanService.getLoanProducts(offset, limit);

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
