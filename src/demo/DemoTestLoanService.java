package demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.mambu.accounts.shared.model.AccountHolderType;
import com.mambu.accounts.shared.model.AccountState;
import com.mambu.accounts.shared.model.InterestRateSource;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.LoanAccountExpanded;
import com.mambu.apisdk.services.LoansService;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.InterestRateSettings;
import com.mambu.core.shared.model.LoanPenaltyCalculationMethod;
import com.mambu.core.shared.model.Money;
import com.mambu.core.shared.model.RepaymentAllocationElement;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;
import com.mambu.loans.shared.model.AmortizationMethod;
import com.mambu.loans.shared.model.GracePeriodType;
import com.mambu.loans.shared.model.Guaranty;
import com.mambu.loans.shared.model.Guaranty.GuarantyType;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanAccount.RepaymentPeriodUnit;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanTranche;
import com.mambu.loans.shared.model.LoanTransaction;
import com.mambu.loans.shared.model.Repayment;
import com.mambu.loans.shared.model.RepaymentScheduleMethod;
import com.mambu.loans.shared.model.ScheduleDueDatesMethod;

/**
 * Test class to show example usage of the api calls
 * 
 * @author edanilkis
 * 
 */
public class DemoTestLoanService {

	private static String LOAN_ACCOUNT_ID;
	private static String NEW_LOAN_ACCOUNT_ID; // will be assigned after creation in testCreateJsonAccount()

	private static Client demoClient;
	private static Group demoGroup;
	private static User demoUser;
	private static LoanProduct demoProduct;
	private static LoanAccount demoLoanAccount;

	private static LoanAccountExpanded newAccount;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			// Get demo entities needed for testing
			final String testProductId = null; // use specific test ID or null to get random product
			final String testAccountId = null; // use specific test ID or null to get random loan account

			demoClient = DemoUtil.getDemoClient();
			demoGroup = DemoUtil.getDemoGroup();
			demoUser = DemoUtil.getDemoUser();

			demoProduct = DemoUtil.getDemoLoanProduct(testProductId);
			demoLoanAccount = DemoUtil.getDemoLoanAccount(testAccountId);
			LOAN_ACCOUNT_ID = demoLoanAccount.getId();

			testCreateJsonAccount();
			// Test Reject transactions first
			testPatchLoanAccountTerms(); // Available since 3.9.3
			testRejectLoanAccount();
			testDeleteLoanAccount();

			testCreateJsonAccount();

			testApproveLoanAccount();
			testUndoApproveLoanAccount();
			testApproveLoanAccount();
			// Test Disburse and Undo disburse
			testDisburseLoanAccountWithDetails();
			testUndoDisburseLoanAccount(); // Available since 3.9
			testDisburseLoanAccountWithDetails();

			testGetLoanProductSchedule(); // Available since 3.9

			testUpdateLoanAccount();

			testGetLoanAccount();
			testGetLoanAccountDetails();
			testGetLoanAccountsByBranchCentreOfficerState();

			testGetLoanAccountsForClient();
			testGetLoanAccountsForGroup();

			testApplyFeeToLoanAccount();
			testRepayLoanAccount();

			// transactions
			testGetLoanAccountTransactions();

			// Products
			testGetLoanProducts();
			testGetLoanProductById();

			testLockLoanAccount(); // Available since 3.6
			testUnlockLoanAccount(); // Available since 3.6

			testGetDocuments(); // Available since Mambu 3.6

			testUpdateDeleteCustomFields(); // Available since 3.8

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Loan Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testGetLoanAccount() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String accountId = NEW_LOAN_ACCOUNT_ID; // LOAN_ACCOUNT_ID NEW_LOAN_ACCOUNT_ID

		System.out.println("Got loan account  by ID: " + loanService.getLoanAccount(accountId).getName());

	}

	public static void testGetLoanAccountDetails() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccountDetails");

		LoansService loanService = MambuAPIFactory.getLoanService();
		String accountId = NEW_LOAN_ACCOUNT_ID; // LOAN_ACCOUNT_ID NEW_LOAN_ACCOUNT_ID
		System.out.println("Got loan account by ID with details: "
				+ loanService.getLoanAccountDetails(accountId).getName());

	}

	// Create Loan Account
	public static void testCreateJsonAccount() throws MambuApiException {
		System.out.println("\nIn testCreateJsonAccount");

		LoansService loanService = MambuAPIFactory.getLoanService();

		LoanAccount account = makeLoanAccountForDemoProduct();

		// Set Custom fields to null in the account (they are sent in customInformation field)
		account.setCustomFieldValues(null);

		// Use helper to make test custom fields valid for the account's product
		List<CustomFieldValue> clientCustomInformation = DemoUtil.makeForEntityCustomFieldValues(
				CustomField.Type.LOAN_ACCOUNT_INFO, demoProduct.getEncodedKey());

		// Create Account Expanded
		LoanAccountExpanded accountExpanded = new LoanAccountExpanded();

		accountExpanded.setLoanAccount(account);
		accountExpanded.setCustomInformation(clientCustomInformation);

		// Create Account in Mambu
		newAccount = loanService.createLoanAccount(accountExpanded);

		NEW_LOAN_ACCOUNT_ID = newAccount.getLoanAccount().getId();

		System.out.println("Loan Account created OK, ID=" + newAccount.getLoanAccount().getId() + " Name= "
				+ newAccount.getLoanAccount().getLoanName() + " Account Holder Key="
				+ newAccount.getLoanAccount().getAccountHolderKey());

		// Check returned custom fields after create. For LoanAccountExpanded custom information is not part of the
		// LoanAccount but is a member of LoanAccountExoended. So get it from there
		List<CustomFieldValue> updatedCustomFields = newAccount.getCustomInformation();

		if (updatedCustomFields != null) {
			System.out.println("Custom Fields for Account\n");
			for (CustomFieldValue value : updatedCustomFields) {
				System.out.println("CustomFieldKey=" + value.getCustomFieldKey() + "\tValue=" + value.getValue()
						+ "\tName=" + value.getCustomField().getName());

			}
		}
	}

	// Update Loan account
	public static void testUpdateLoanAccount() throws MambuApiException {
		System.out.println("\nIn testUpdateLoanAccount");

		LoansService loanService = MambuAPIFactory.getLoanService();

		// Use the newly created account and update some custom fields
		LoanAccountExpanded updatedAccount = newAccount;
		List<CustomFieldValue> customFields = updatedAccount.getCustomInformation();
		if (customFields != null) {
			for (CustomFieldValue value : customFields) {
				value = DemoUtil.makeNewCustomFieldValue(value);
			}
		}

		// Update account in Mambu
		LoanAccountExpanded updatedAccountResult = loanService.updateLoanAccount(updatedAccount);

		System.out.println("Loan Update OK, ID=" + updatedAccountResult.getLoanAccount().getId() + "\tAccount Name="
				+ updatedAccountResult.getLoanAccount().getName());

		// Get returned custom fields
		List<CustomFieldValue> updatedCustomFields = updatedAccountResult.getCustomInformation();

		if (updatedCustomFields != null) {
			System.out.println("Custom Fields for Loan Account\n");
			for (CustomFieldValue value : updatedCustomFields) {
				System.out.println("CustomFieldKey=" + value.getCustomFieldKey() + "\tValue=" + value.getValue()
						+ "\tName=" + value.getCustomField().getName());
			}
		}

	}

	// Test Patch Loan account terms API.
	public static void testPatchLoanAccountTerms() throws MambuApiException {
		System.out.println("\nIn testPatchLoanAccountTerms");

		LoansService loanService = MambuAPIFactory.getLoanService();

		// Use the newly created account and update some terms fields
		LoanAccount theAccount = newAccount.getLoanAccount();
		// Create new account with only the terms to be patched
		LoanAccount account = new LoanAccount();
		account.setId(theAccount.getId());
		account.setEncodedKey(theAccount.getEncodedKey()); // encoded key is needed for patching
		// Set fields to be updated. Update some account terms
		account.setLoanAmount(theAccount.getLoanAmount()); // loanAmount
		account.setInterestRate(theAccount.getInterestRate()); // interestRate
		// Leave some other updatable terms unchanged
		account.setInterestSpread(theAccount.getInterestSpread()); // interestSpread
		account.setRepaymentInstallments(theAccount.getRepaymentInstallments()); // repaymentInstallments
		account.setRepaymentPeriodCount(theAccount.getRepaymentPeriodCount()); // repaymentPeriodCount
		account.setRepaymentPeriodUnit(theAccount.getRepaymentPeriodUnit()); // repaymentPeriodUnit
		account.setExpectedDisbursementDate(theAccount.getExpectedDisbursementDate()); // expectedDisbursementDate
		account.setFirstRepaymentDate(theAccount.getFirstRepaymentDate()); // firstRepaymentDate
		account.setGracePeriod(theAccount.getGracePeriod()); // gracePeriod
		account.setPrincipalRepaymentInterval(theAccount.getPrincipalRepaymentInterval()); // principalRepaymentInterval
		account.setPenaltyRate(theAccount.getPenaltyRate()); // penaltyRate
		account.setPeriodicPayment(theAccount.getPeriodicPayment()); // periodicPayment

		// Patch loan account terms
		boolean result = loanService.patchLoanAccount(account);
		System.out.println("Loan Terms Update for account. Status=" + result);

	}

	// / Transactions testing
	public static void testDisburseLoanAccountWithDetails() throws MambuApiException {
		System.out.println("\nIn test Disburse LoanAccount with Details");

		LoansService loanService = MambuAPIFactory.getLoanService();

		String amount = "10000.00";
		String accountId = NEW_LOAN_ACCOUNT_ID;
		String disbursalDate = "2014-10-3";
		String firstRepaymentDate = null; // "2012-12-06";
		String notes = "Disbursed loan for testing";

		// Make demo transactionDetails with the valid channel fields
		TransactionDetails transactionDetails = DemoUtil.makeDemoTransactionDetails();

		LoanTransaction transaction = loanService.disburseLoanAccount(accountId, amount, disbursalDate,
				firstRepaymentDate, notes, transactionDetails);

		System.out.println("\nLoan for Disbursement with Details: Transaction Id=" + transaction.getTransactionId()
				+ " amount=" + transaction.getAmount().toString());
	}

	// Test undo disbursement transaction
	public static void testUndoDisburseLoanAccount() throws MambuApiException {
		System.out.println("\nIn testUndoDisburseLoanAccount");

		LoansService loanService = MambuAPIFactory.getLoanService();
		String accountId = NEW_LOAN_ACCOUNT_ID;
		LoanTransaction transaction = loanService.undoDisburseLoanAccount(accountId);
		System.out.println("\nOK Undo Loan Disbursement for account=" + accountId + "\tTransaction Id="
				+ transaction.getTransactionId());

	}

	public static void testGetLoanAccountTransactions() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccount Transactions");
		LoansService loanService = MambuAPIFactory.getLoanService();
		String offest = "0";
		String limit = "8";

		final String accountId = NEW_LOAN_ACCOUNT_ID; // LOAN_ACCOUNT_ID or NEW_LOAN_ACCOUNT_ID
		List<LoanTransaction> transactions = loanService.getLoanAccountTransactions(accountId, offest, limit);

		System.out.println("Got loan accounts transactions, total=" + transactions.size()
				+ " in a range for the Loan with the " + accountId + " id:" + " Range=" + offest + "  " + limit);
		for (LoanTransaction transaction : transactions) {
			System.out.println("Trans ID=" + transaction.getTransactionId() + "  " + transaction.getType() + "  "
					+ transaction.getEntryDate().toString());
		}
	}

	public static void testRepayLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Repay LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();
		Money repaymentAmount = demoLoanAccount.getDueAmount(RepaymentAllocationElement.PRINCIPAL);
		if (repaymentAmount == null || repaymentAmount.isNegativeOrZero()) {
			repaymentAmount = new Money(320);
		}
		String amount = repaymentAmount.toPlainString();
		String date = null; // "2012-11-23";
		String notes = "repayment notes from API";

		String accountId = NEW_LOAN_ACCOUNT_ID;

		// Make demo transactionDetails with the valid channel fields
		TransactionDetails transactionDetails = DemoUtil.makeDemoTransactionDetails();

		LoanTransaction transaction = loanService.makeLoanRepayment(accountId, amount, date, notes, transactionDetails);

		System.out.println("repayed loan account with the " + accountId + " id response="
				+ transaction.getTransactionId() + "   for amount=" + transaction.getAmount());
	}

	public static void testApplyFeeToLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Applying Fee to a Loan Account");

		// API supports applying fee only for products with 'Allow Arbitrary Fees" setting
		if (!demoProduct.getAllowArbitraryFees()) {
			System.out.println("\nWARNING: demo product=" + demoProduct.getName()
					+ " doesn't allow Arbitrary Fees. Use other product to test applyFee API");
			return;
		}
		LoansService loanService = MambuAPIFactory.getLoanService();
		String amount = "10";
		String repaymentNumber = "10";
		String accountId = NEW_LOAN_ACCOUNT_ID;
		String notes = "Notes for applying fee to a loan";

		LoanTransaction transaction = loanService.applyFeeToLoanAccount(accountId, amount, repaymentNumber, notes);

		System.out.println("Loan Fee response= " + transaction.getTransactionId().toString() + "  Trans Amount="
		// + transaction.getAmount().toString() + "   Fees paid=" + transaction.getFeesPaid().toString());
				+ transaction.getAmount().toString() + "   Fees amount=" + transaction.getFeesAmount().toString());

	}

	public static void testGetLoanAccountsByBranchCentreOfficerState() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccountsByBranchCentreOfficerState");

		LoansService loanService = MambuAPIFactory.getLoanService();

		String branchId = demoClient.getAssignedBranchKey();
		String centreId = demoClient.getAssignedCentreKey(); // Centre ID filter is available since 3.7
		String creditOfficerUserName = demoUser.getUsername();
		String accountState = null; // AccountState.ACTIVE.name(); // CLOSED_WITHDRAWN ACTIVE_IN_ARREARS
		String offset = null;
		String limit = null;

		List<LoanAccount> loanAccounts = loanService.getLoanAccountsByBranchCentreOfficerState(branchId, centreId,
				creditOfficerUserName, accountState, offset, limit);

		if (loanAccounts != null)
			System.out.println("Got loan accounts for the branch, centre, officer, state, total loans="
					+ loanAccounts.size());
		for (LoanAccount account : loanAccounts) {
			System.out.println("Account Name=" + account.getId() + "-" + account.getLoanName() + "\tBranchId="
					+ account.getAssignedBranchKey() + "\tCentreId=" + account.getAssignedCentreKey()
					+ "\tCredit Officer=" + account.getAssignedUserKey());
		}
	}

	public static void testGetLoanAccountsForClient() throws MambuApiException {
		System.out.println("\nIn testGetLoan Accounts ForClient");
		LoansService loanService = MambuAPIFactory.getLoanService();
		String clientId = demoClient.getId();
		List<LoanAccount> loanAccounts = loanService.getLoanAccountsForClient(clientId);

		System.out.println("Got loan accounts for the client with the " + clientId + " id, Total="
				+ loanAccounts.size());
		for (LoanAccount account : loanAccounts) {
			System.out.print(account.getLoanName() + " ");
		}
		System.out.println();
	}

	public static void testGetLoanAccountsForGroup() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccounts ForGroup");
		LoansService loanService = MambuAPIFactory.getLoanService();
		String groupId = demoGroup.getId();
		List<LoanAccount> loanAccounts = loanService.getLoanAccountsForGroup(groupId);

		System.out.println("Got " + loanAccounts.size() + " loan accounts for the group with the " + groupId
				+ " id, Total= " + loanAccounts.size());
		int i = 0;
		for (LoanAccount account : loanAccounts) {
			if (i > 0) {
				System.out.print("\n");
			}
			System.out.print(" Account id=" + account.getId() + "\tAccount Name=" + account.getLoanName());
			i++;
		}
		System.out.println();
	}

	public static void testApproveLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Approve LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		LoanAccount account = loanService.approveLoanAccount(NEW_LOAN_ACCOUNT_ID, "some demo notes");

		System.out.println("Approving loan account with the " + NEW_LOAN_ACCOUNT_ID + " Loan name="
				+ account.getLoanName() + "  Account State=" + account.getState().toString());
	}

	public static void testRejectLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Reject LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		LoanAccount account = loanService.rejectLoanAccount(NEW_LOAN_ACCOUNT_ID, "some demo notes ', \" ü = : \n as");

		System.out.println("Rejecting loan account with the " + NEW_LOAN_ACCOUNT_ID + " Loan name"
				+ account.getLoanName() + "  Account State=" + account.getState().toString());
	}

	public static void testUndoApproveLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Undo Approve LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		final String accountId = NEW_LOAN_ACCOUNT_ID;
		LoanAccount account = loanService.undoApproveLoanAccount(accountId, "some undo approve demo notes");

		System.out.println("Undo Approving loan account with the " + accountId + " Loan name " + account.getLoanName()
				+ "  Account State=" + account.getState().toString());
	}

	public static void testLockLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Lock LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String accountId = NEW_LOAN_ACCOUNT_ID;
		LoanTransaction transaction = loanService.lockLoanAccount(accountId, "some lock demo notes");

		System.out.println("Locked account with ID " + accountId + " Transaction  " + transaction.getTransactionId()
				+ " Type=" + transaction.getType() + "  Balance=" + transaction.getBalance());
	}

	public static void testUnlockLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Unlock LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String accountId = NEW_LOAN_ACCOUNT_ID;
		LoanTransaction transaction = loanService.unlockLoanAccount(accountId, "some unlock demo notes");

		System.out.println("UnLocked account with ID " + accountId + " Transaction  " + transaction.getTransactionId()
				+ " Type=" + transaction.getType() + "  Balance=" + transaction.getBalance());
	}

	public static void testDeleteLoanAccount() throws MambuApiException {
		System.out.println("\nIn testDeleteLoanAccount");

		LoansService loanService = MambuAPIFactory.getLoanService();
		String loanAccountId = NEW_LOAN_ACCOUNT_ID;

		boolean status = loanService.deleteLoanAccount(loanAccountId);

		System.out.println("Deletion status=" + status);
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
				System.out.println("Product=" + product.getName() + "  Id=" + product.getId() + "  Key="
						+ product.getEncodedKey() + " Loan Type=" + product.getLoanType().name());
			}
		}

	}

	public static void testGetLoanProductById() throws MambuApiException {
		System.out.println("\nIn testGetLoanProductById");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String productId = demoProduct.getId();

		LoanProduct product = loanService.getLoanProduct(productId);

		System.out.println("Product=" + product.getName() + "  Id=" + product.getId() + " Loan Type="
				+ product.getLoanType().name());

	}

	public static void testGetLoanProductSchedule() throws MambuApiException {
		System.out.println("\nIn testGetLoanProductSchedule");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String productId = demoProduct.getId();

		LoanAccount loanAccount = makeLoanAccountForDemoProduct();

		// Get the repayment schedule for these loan params
		List<Repayment> repayments = loanService.getLoanProductSchedule(productId, loanAccount);

		// Log the results
		int totalRepayments = (repayments == null) ? 0 : repayments.size();
		System.out.println("Total repayments=" + totalRepayments + "\tfor product ID=" + productId);
		if (totalRepayments == 0) {
			return;
		}
		Repayment firstRepayment = repayments.get(0);
		Repayment lastRepayment = repayments.get(totalRepayments - 1);
		System.out.println("First Repayment. Date Date=" + firstRepayment.getDueDate() + "\tTotal Due="
				+ firstRepayment.getTotalDue());
		System.out.println("Last Repayment. Date Date=" + lastRepayment.getDueDate() + "\tTotal Due="
				+ lastRepayment.getTotalDue());
	}

	private static final String apiTestLoanNamePrefix = "API Test Loan ";

	// Create demo loan account with parameters consistent with the demo product
	private static LoanAccount makeLoanAccountForDemoProduct() {
		System.out.println("\nIn makeLoanAccountForDemoProduct for product name=" + demoProduct.getName() + " id="
				+ demoProduct.getId());

		if (!demoProduct.isActivated()) {
			System.out.println("*** WARNING ***: demo product is NOT Active. Product name=" + demoProduct.getName()
					+ " id=" + demoProduct.getId());
		}
		LoanAccount loanAccount = new LoanAccount();

		// Set params to be consistent with the demo product (to be accepted by the GET product schedule API and create
		// loan
		loanAccount.setId(null);
		loanAccount.setLoanName(apiTestLoanNamePrefix + new Date().getTime());
		loanAccount.setProductTypeKey(demoProduct.getEncodedKey());

		boolean isForClient = demoProduct.isForIndividuals();
		String holderKey = (isForClient) ? demoClient.getEncodedKey() : demoGroup.getEncodedKey();
		AccountHolderType holderType = (isForClient) ? AccountHolderType.CLIENT : AccountHolderType.GROUP;
		loanAccount.setAccountHolderKey(holderKey);
		loanAccount.setAccountHolderType(holderType);

		// LoanAmount
		Money amountDef = demoProduct.getDefaultLoanAmount();
		Money amountMin = demoProduct.getMinLoanAmount();
		Money amountMax = demoProduct.getMaxLoanAmount();
		Money amount = amountDef;
		amount = (amount == null && amountMin != null) ? amountMin : amount;
		amount = (amount == null && amountMax != null) ? amountMax : amount;
		if (amount == null) {
			// Is still null, so no limits
			amount = new Money(3000.00f);
		}
		loanAccount.setLoanAmount(amount); // Mandatory

		// Add periodic payment: required and is mandatory for BALLOON_PAYMENTS products
		Money nullMoney = null;
		loanAccount.setPeriodicPayment(nullMoney);
		if (demoProduct.getAmortizationMethod() == AmortizationMethod.BALLOON_PAYMENTS) {
			loanAccount.setPeriodicPayment(amount.multiply(new BigDecimal(0.5)));
		}
		// InterestRate
		loanAccount.setInterestRate(null);
		loanAccount.setInterestRateSource(null);
		if (demoProduct.getRepaymentScheduleMethod() != RepaymentScheduleMethod.NONE) {
			InterestRateSettings intRateSettings = demoProduct.getInterestRateSettings();

			BigDecimal interestRateDef = (intRateSettings == null) ? null : intRateSettings.getDefaultInterestRate();
			BigDecimal interestRateMin = (intRateSettings == null) ? null : intRateSettings.getMinInterestRate();
			BigDecimal interestRateMax = (intRateSettings == null) ? null : intRateSettings.getMaxInterestRate();

			BigDecimal interestRate = interestRateDef;
			interestRate = (interestRate == null && interestRateMin != null) ? interestRateMin : interestRate;
			interestRate = (interestRate == null && interestRateMax != null) ? interestRateMax : interestRate;
			if (interestRate == null) {
				// Is still null, so no limits
				interestRate = new BigDecimal(6.5f);
			}
			loanAccount.setInterestRate(interestRate);
			//
			InterestRateSource intSoure = (intRateSettings == null) ? null : intRateSettings.getInterestRateSource();
			loanAccount.setInterestRateSource(intSoure);

		}

		// DisbursementDate
		// Set dates 3-4 days into the future
		long timeNow = new Date().getTime();
		long aDay = 24 * 60 * 60 * 1000; // 1 day in msecs
		loanAccount.setDisbursementDate(new Date(timeNow + 3 * aDay)); // 3 days from now
		// FirstRepaymentDate

		Date firstRepaymentDate = new Date(timeNow + 4 * aDay);// 4 days form now
		// Check for fixed days product
		ScheduleDueDatesMethod scheduleDueDatesMethod = demoProduct.getScheduleDueDatesMethod();
		if (scheduleDueDatesMethod == ScheduleDueDatesMethod.FIXED_DAYS_OF_MONTH) {
			List<Integer> fixedDays = demoProduct.getFixedDaysOfMonth();
			if (fixedDays != null && fixedDays.size() > 0) {
				Calendar date = Calendar.getInstance();
				int year = date.get(Calendar.YEAR);
				int month = date.get(Calendar.MONTH);
				date.clear();
				date.setTimeZone(TimeZone.getTimeZone("UTC"));
				date.set(year, month + 1, fixedDays.get(fixedDays.size() - 1));
				firstRepaymentDate = date.getTime();
			}
		}
		loanAccount.setFirstRepaymentDate(firstRepaymentDate);

		// Tranches
		Integer maxTranches = demoProduct.getMaxNumberOfDisbursementTranches();
		if (maxTranches != null && maxTranches > 1) {
			LoanTranche tranche = new LoanTranche(loanAccount.getLoanAmount(), loanAccount.getDisbursementDate());
			loanAccount.setDisbursementDate(null);
			ArrayList<LoanTranche> tanches = new ArrayList<LoanTranche>();
			tanches.add(tranche);
			loanAccount.setTranches(tanches);
		}
		// RepaymentPeriodCount
		Integer defRepPeriodCount = demoProduct.getDefaultRepaymentPeriodCount();
		if (defRepPeriodCount == null) {
			defRepPeriodCount = 30;
		}
		loanAccount.setRepaymentPeriodCount(defRepPeriodCount);
		// RepaymentPeriodUnit
		RepaymentPeriodUnit units = demoProduct.getRepaymentPeriodUnit();
		if (units == null) {
			units = RepaymentPeriodUnit.DAYS;
		}
		loanAccount.setRepaymentPeriodUnit(units);
		// RepaymentInstallments
		Integer repaymentInsatllments = demoProduct.getDefaultNumInstallments();
		if (repaymentInsatllments == null) {
			repaymentInsatllments = 10;
		}
		loanAccount.setRepaymentInstallments(repaymentInsatllments);
		// PrincipalRepaymentInterval
		Integer principalRepaymentInterva = demoProduct.getDefaultPrincipalRepaymentInterval();
		if (principalRepaymentInterva != null) {
			loanAccount.setPrincipalRepaymentInterval(principalRepaymentInterva);
		} else {
			// TODO: remove this assignment when model changed to Integer
			loanAccount.setPrincipalRepaymentInterval(1);
		}
		// Penalty Rate
		loanAccount.setPenaltyRate(null);
		if (demoProduct.getLoanPenaltyCalculationMethod() != LoanPenaltyCalculationMethod.NONE) {
			BigDecimal defPenaltyRate = demoProduct.getDefaultPenaltyRate();
			BigDecimal minPenaltyRate = demoProduct.getMinPenaltyRate();
			BigDecimal maxPenaltyRate = demoProduct.getMaxPenaltyRate();
			BigDecimal penaltyRate = defPenaltyRate;
			penaltyRate = (penaltyRate == null && minPenaltyRate != null) ? minPenaltyRate : penaltyRate;
			penaltyRate = (penaltyRate == null && maxPenaltyRate != null) ? maxPenaltyRate : penaltyRate;
			loanAccount.setPenaltyRate(penaltyRate);
		}

		// GracePeriod
		loanAccount.setGracePeriod(null);
		if (demoProduct.getGracePeriodType() != GracePeriodType.NONE) {
			Integer defGrace = demoProduct.getDefaultGracePeriod();
			Integer minGrace = demoProduct.getMinGracePeriod();
			Integer maxGrace = demoProduct.getMaxGracePeriod();
			Integer gracePeriod = defGrace;
			gracePeriod = (gracePeriod == null && minGrace != null) ? minGrace : gracePeriod;
			gracePeriod = (gracePeriod == null && maxGrace != null) ? maxGrace : gracePeriod;
			// TODO: set directly when the new model has gracePeriod as Integer
			if (gracePeriod != null) {
				loanAccount.setGracePeriod(gracePeriod);
			}
		}
		// Set Guarantees. Available for API since 3.9. See MBU-6528
		ArrayList<Guaranty> guarantees = new ArrayList<Guaranty>();
		if (demoProduct.getAllowGuarantors()) {
			// GuarantyType.GUARANTOR
			Guaranty guarantySecurity = new Guaranty(GuarantyType.GUARANTOR);
			guarantySecurity.setAmount(loanAccount.getLoanAmount());
			guarantySecurity.setGuarantorKey(demoClient.getEncodedKey());
			guarantees.add(guarantySecurity);
		}
		if (demoProduct.getAllowCollateral()) {
			// GuarantyType.ASSET
			Guaranty guarantyAsset = new Guaranty(GuarantyType.ASSET);
			guarantyAsset.setAssetName("Asset Name as a collateral");
			guarantyAsset.setAmount(loanAccount.getLoanAmount());
			guarantees.add(guarantyAsset);

		}
		// Add all guarantees to Loan account
		loanAccount.setGuarantees(guarantees);

		loanAccount.setExpectedDisbursementDate(loanAccount.getDisbursementDate());
		loanAccount.setDisbursementDate(null);

		loanAccount.setNotes("Created by DemoTest on " + new Date());
		return loanAccount;

	}

	public static void testGetDocuments() throws MambuApiException {
		System.out.println("\nIn testGetDocuments");

		LoanAccount account = DemoUtil.getDemoLoanAccount();
		String accountId = account.getId();

		LoansService loanService = MambuAPIFactory.getLoanService();

		List<Document> documents = loanService.getLoanAccountDocuments(accountId);

		// Log returned documents using DemoTestDocumentsService helper
		System.out.println("Documents returned for a Loan Account with ID=" + accountId);
		DemoTestDocumentsService.logDocuments(documents);

	}

	// Update Custom Field values for the Loan Account and delete the first available custom field
	public static void testUpdateDeleteCustomFields() throws MambuApiException {
		System.out.println("\nIn testUpdateDeleteCustomFields");

		List<CustomFieldValue> customFieldValues;
		System.out.println("\nUpdating demo Loan Account custom fields...");
		customFieldValues = updateCustomFields();

		System.out.println("\nDeleting first custom field for a demo Loan Account ...");
		deleteCustomField(customFieldValues);

	}

	// Private helper to Update all custom fields for a Loan Account
	private static List<CustomFieldValue> updateCustomFields() throws MambuApiException {

		Class<?> entityClass = LoanAccount.class;
		String entityName = entityClass.getSimpleName();
		String entityId = demoLoanAccount.getId();

		// Get Current custom field values first for a Demo account
		List<CustomFieldValue> customFieldValues = demoLoanAccount.getCustomFieldValues();

		if (customFieldValues == null || customFieldValues.size() == 0) {
			System.out.println("WARNING: No Custom fields defined for demo " + entityName + " with ID=" + entityId
					+ ". Nothing to update");
			return null;
		}
		// Update custom field values
		LoansService loanService = MambuAPIFactory.getLoanService();
		for (CustomFieldValue value : customFieldValues) {

			String fieldId = value.getCustomFieldId(); // return null for Group, Branch, Centre?
			// Create valid new value for a custom field
			String newValue = DemoUtil.makeNewCustomFieldValue(value).getValue();

			// Update Custom Field value
			boolean updateStatus;
			System.out.println("\nUpdating Custom Field with ID=" + fieldId + " for " + entityName + " with ID="
					+ entityId);

			updateStatus = loanService.updateLoanAccountCustomField(entityId, fieldId, newValue);

			String statusMessage = (updateStatus) ? "Success" : "Failure";
			System.out.println(statusMessage + " updating Custom Field, ID=" + fieldId + " for demo " + entityName
					+ " with ID=" + entityId + " New value=" + newValue);

		}

		return customFieldValues;
	}

	// Private helper to Delete the first custom field for a Loan Account
	private static void deleteCustomField(List<CustomFieldValue> customFieldValues) throws MambuApiException {

		Class<?> entityClass = LoanAccount.class;
		String entityName = entityClass.getSimpleName();
		String entityId = demoLoanAccount.getId();

		if (customFieldValues == null || customFieldValues.size() == 0) {
			System.out.println("WARNING: No Custom fields defined for demo " + entityName + " with ID=" + entityId
					+ ". Nothing to delete");
			return;
		}
		// Delete the first field on the list
		String customFieldId = customFieldValues.get(0).getCustomField().getId();

		LoansService loanService = MambuAPIFactory.getLoanService();
		boolean deleteStatus = loanService.deleteLoanAccountCustomField(entityId, customFieldId);

		String statusMessage = (deleteStatus) ? "Success" : "Failure";
		System.out.println(statusMessage + " deleting Custom Field, ID=" + customFieldId + " for demo " + entityName
				+ " with ID=" + entityId);
	}

	// Internal clean up routine. Can be used to delete non-disbursed accounts created by these demo test runs
	public static void deleteTestAPILoanAccounts() throws MambuApiException {
		System.out.println("\nIn deleteTestAPILoanAccounts");
		System.out.println("**  Deleting all Test Loan Accounts for Client =" + demoClient.getFullNameWithId() + " **");
		LoansService loanService = MambuAPIFactory.getLoanService();
		List<LoanAccount> accounts = loanService.getLoanAccountsForClient(demoClient.getId());
		if (accounts == null || accounts.size() == 0) {
			System.out.println("Nothing to delete for client " + demoClient.getFullNameWithId());
			return;
		}
		for (LoanAccount account : accounts) {
			String name = account.getLoanName();
			if (name.contains(apiTestLoanNamePrefix)) {
				AccountState state = account.getAccountState();
				if (state == AccountState.PARTIAL_APPLICATION || state == AccountState.PENDING_APPROVAL
						|| state == AccountState.APPROVED) {
					String id = account.getId();
					System.out.println("Deleting loan account " + name + " ID=" + id);
					try {
						loanService.deleteLoanAccount(id);
					} catch (MambuApiException e) {
						System.out.println("Account " + id + " is NOT deleted. Exception=" + e.getMessage());
						continue;
					}
				}
			}
		}

	}
}
