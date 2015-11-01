package demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.mambu.accounting.shared.model.GLAccountingRule;
import com.mambu.accounts.shared.model.AccountHolderType;
import com.mambu.accounts.shared.model.AccountState;
import com.mambu.accounts.shared.model.InterestRateSource;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.accountsecurity.shared.model.Guaranty;
import com.mambu.accountsecurity.shared.model.Guaranty.GuarantyType;
import com.mambu.accountsecurity.shared.model.InvestorFund;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.LoanAccountExpanded;
import com.mambu.apisdk.services.LoansService;
import com.mambu.apisdk.util.APIData.CLOSER_TYPE;
import com.mambu.apisdk.util.DateUtils;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.CustomFieldType;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.InterestRateSettings;
import com.mambu.core.shared.model.LoanPenaltyCalculationMethod;
import com.mambu.core.shared.model.Money;
import com.mambu.core.shared.model.RepaymentAllocationElement;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;
import com.mambu.loans.shared.model.AmortizationMethod;
import com.mambu.loans.shared.model.GracePeriodType;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanAccount.RepaymentPeriodUnit;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanProductType;
import com.mambu.loans.shared.model.LoanTranche;
import com.mambu.loans.shared.model.LoanTransaction;
import com.mambu.loans.shared.model.LoanTransactionType;
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

	private static String NEW_LOAN_ACCOUNT_ID; // will be assigned after creation in testCreateJsonAccount()

	private static Client demoClient;
	private static Group demoGroup;
	private static User demoUser;
	private static LoanProduct demoProduct;
	private static LoanAccount demoLoanAccount;

	private static LoanAccountExpanded newAccount;

	private static String methodName = null; // print method name on exception

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			// Get demo entities needed for testing
			// Use specific product ID or null to get random product. If set to "ALL" then test for all product types
			final String testProductId = DemoUtil.demoLaonProductId;
			final String testAccountId = null; // use specific test ID or null to get random loan account

			demoClient = DemoUtil.getDemoClient(null);
			demoGroup = DemoUtil.getDemoGroup(null);
			demoUser = DemoUtil.getDemoUser();

			demoLoanAccount = DemoUtil.getDemoLoanAccount(testAccountId);

			LoanProductType productTypes[];
			boolean productTypesTesting;
			// If demoLaonProductId configuration set to ALL then test all available product types
			if (testProductId != null && testProductId.equals(DemoUtil.allProductTypes)) {
				productTypesTesting = true;
				// Run tests for all products types, selecting random active product ID of each type once
				productTypes = LoanProductType.values();
			} else {
				// Use demoLaonProductId configuration: a specific product ID (if not null) or a random product (if
				// null)
				productTypesTesting = false;
				LoanProduct testProduct = DemoUtil.getDemoLoanProduct(testProductId);
				// Set test product types to the demoLaonProductId's type only
				productTypes = new LoanProductType[] { testProduct.getLoanProductType() };
			}

			// Run tests for all required product types
			for (LoanProductType productType : productTypes) {
				System.out.println("\n*** Product Type=" + productType + " ***");

				// Get random product of a specific type or a product for a specific product id
				demoProduct = (productTypesTesting) ? DemoUtil.getDemoLoanProduct(productType) : DemoUtil
						.getDemoLoanProduct(testProductId);

				if (demoProduct == null) {
					continue;
				}
				System.out.println("Product Id=" + demoProduct.getId() + " Name=" + demoProduct.getName() + " ***");

				try {
					testCreateJsonAccount();
					testUpdateLoanAccount();

					// Test Reject transactions first
					testPatchLoanAccountTerms(); // Available since 3.9.3
					testCloseLoanAccount(); // Available since 3.3
					testDeleteLoanAccount();

					testCreateJsonAccount();

					testRequestApprovalLoanAccount(); // Available since 3.13
					testApproveLoanAccount();
					testUndoApproveLoanAccount();
					testUpdatingAccountTranches(); // Available since 3.12.3
					testUpdatingAccountFunds();// Available since 3.13
					testApproveLoanAccount();

					// Test Disburse and Undo disburse
					testDisburseLoanAccount();
					testLockLoanAccount(); // Available since 3.6
					testUnlockLoanAccount(); // Available since 3.6
					testUndoDisburseLoanAccount(); // Available since 3.9
					testDisburseLoanAccount();

					testGetLoanProductSchedule(); // Available since 3.9

					testGetLoanAccount();
					testGetLoanAccountDetails();
					testGetLoanAccountsByBranchCentreOfficerState();

					testGetLoanAccountsForClient();
					testGetLoanAccountsForGroup();

					testApplyFeeToLoanAccount();
					testApplyInterestToLoanAccount(); // Available since 3.1
					testRepayLoanAccount();
					testWriteOffLoanAccount(); // Available since 3.14

					// transactions
					List<LoanTransaction> transactions = testGetLoanAccountTransactions();
					testReverseLoanAccountTransactions(transactions); // Available since Mambu 3.13 for PENALTY_APPLIED
																		// reversal

					// Products
					testGetLoanProducts();
					testGetLoanProductById();

					testGetDocuments(); // Available since Mambu 3.6

					testUpdateDeleteCustomFields(); // Available since 3.8

				} catch (MambuApiException e) {
					System.out.println("*** Exception *** " + methodName + " " + e.getMessage());
					System.out.println("Product Type=" + demoProduct.getLoanProductType() + "\tID="
							+ demoProduct.getId() + "\tName=" + demoProduct.getName());
				}
			}

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Loan Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testGetLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetLoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String accountId = NEW_LOAN_ACCOUNT_ID; // LOAN_ACCOUNT_ID NEW_LOAN_ACCOUNT_ID

		System.out.println("Got loan account  by ID: " + loanService.getLoanAccount(accountId).getName());

	}

	public static void testGetLoanAccountDetails() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetLoanAccountDetails");

		LoansService loanService = MambuAPIFactory.getLoanService();
		String accountId = demoLoanAccount.getId();

		LoanAccount loanDeatils = loanService.getLoanAccountDetails(accountId);
		System.out.println("Got loan account by ID with details: " + loanDeatils.getName() + "\tId="
				+ loanDeatils.getId());

		// If account has Securities, log their custom info to test MBU-7684
		// See MBU-7684 As a Developer, I need to work with guarantees with custom fields
		List<Guaranty> guarantees = loanDeatils.getGuarantees();
		if (guarantees == null) {
			System.out.println("Account has no guarantees defined");
			return;
		}

		System.out.println("Logging Guarantees:");
		for (Guaranty guaranty : guarantees) {
			System.out.println("Guarantor type=" + guaranty.getType() + "\tAssetName=" + guaranty.getAssetName()
					+ "\tGurantor Key=" + guaranty.getGuarantorKey() + "\tSavinsg Key="
					+ guaranty.getSavingsAccountKey() + "\tAmount=" + guaranty.getAmount());

			List<CustomFieldValue> guarantyCustomValues = guaranty.getCustomFieldValues();
			DemoUtil.logCustomFieldValues(guarantyCustomValues, "Guarantor", guaranty.getEncodedKey());
		}
		// Log Investor Funds. Available since Mambu 3.13. See MBU-9887
		List<InvestorFund> funds = loanDeatils.getFunds();
		if (funds == null) {
			System.out.println("Account has no fund defined");
			return;
		}
		System.out.println("Logging Investor Funds:");
		for (InvestorFund fund : funds) {
			System.out.println("Guarantor type=" + fund.getType() + "\tGurantor Key=" + fund.getGuarantorKey()
					+ "\tSavinsg Key=" + fund.getSavingsAccountKey() + "\tAmount=" + fund.getAmount());

			List<CustomFieldValue> fundCustomValues = fund.getCustomFieldValues();
			DemoUtil.logCustomFieldValues(fundCustomValues, "Fund", fund.getEncodedKey());
		}
	}

	// Create Loan Account
	public static void testCreateJsonAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn testCreateJsonAccount");

		LoansService loanService = MambuAPIFactory.getLoanService();

		LoanAccount account = makeLoanAccountForDemoProduct();

		// Set Custom fields to null in the account (they are sent in customInformation field)
		account.setCustomFieldValues(null);

		// Use helper to make test custom fields valid for the account's product
		List<CustomFieldValue> clientCustomInformation = DemoUtil.makeForEntityCustomFieldValues(
				CustomFieldType.LOAN_ACCOUNT_INFO, demoProduct.getEncodedKey());

		// Create Account Expanded
		LoanAccountExpanded accountExpanded = new LoanAccountExpanded();

		accountExpanded.setLoanAccount(account);
		accountExpanded.setCustomInformation(clientCustomInformation);

		// Create Account in Mambu
		newAccount = loanService.createLoanAccount(accountExpanded);
		LoanAccount createdAccount = newAccount.getLoanAccount();

		NEW_LOAN_ACCOUNT_ID = createdAccount.getId();

		System.out.println("Loan Account created OK, ID=" + createdAccount.getId() + " Name= "
				+ createdAccount.getLoanName() + " Account Holder Key=" + createdAccount.getAccountHolderKey());

		// Check returned custom fields after create. For LoanAccountExpanded custom information is not part of the
		// LoanAccount but is a member of LoanAccountExoended. So get it from there
		List<CustomFieldValue> customFieldValues = newAccount.getCustomInformation();
		// Log Custom Field Values

		String accountName = createdAccount.getLoanName();
		DemoUtil.logCustomFieldValues(customFieldValues, accountName, createdAccount.getId());

	}

	// Update Loan account
	public static void testUpdateLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn testUpdateLoanAccount");

		LoansService loanService = MambuAPIFactory.getLoanService();

		// Use the newly created account and update some custom fields
		LoanAccountExpanded updatedAccount = newAccount;
		List<CustomFieldValue> customFields = updatedAccount.getCustomInformation();
		List<CustomFieldValue> updatedFields = new ArrayList<CustomFieldValue>();
		if (customFields != null) {
			for (CustomFieldValue value : customFields) {
				value = DemoUtil.makeNewCustomFieldValue(value);
				updatedFields.add(value);
			}
		}
		updatedAccount.setCustomInformation(updatedFields);

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
		System.out.println(methodName = "\nIn testPatchLoanAccountTerms");

		LoansService loanService = MambuAPIFactory.getLoanService();

		// Use the newly created account and update some terms fields
		LoanAccount theAccount = newAccount.getLoanAccount();
		// Create new account with only the terms to be patched
		LoanAccount account = new LoanAccount();
		account.setId(theAccount.getId());
		account.setEncodedKey(theAccount.getEncodedKey()); // encoded key is needed for patching
		// Set fields to be updated. Update some account terms
		account.setLoanAmount(theAccount.getLoanAmount()); // loanAmount
		if (demoProduct.getInterestRateSettings().getInterestRateSource() == InterestRateSource.FIXED_INTEREST_RATE) {
			account.setInterestRate(theAccount.getInterestRate()); // interestRate
		} else {
			account.setInterestSpread(theAccount.getInterestSpread());
		}
		// Leave some other updatable terms unchanged
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

	// Test Updating Loan Account Tranches API: modify, add, delete
	public static void testUpdatingAccountTranches() throws MambuApiException {
		System.out.println(methodName = "\nIn testUpdatingAccountTranches");

		// Use demo loan account and update tranche details
		LoanAccount theAccount = demoLoanAccount;
		String accountId = theAccount.getId();

		// Get Loan Account Tranches
		List<LoanTranche> allTranches = theAccount.getTranches();
		if (allTranches == null || allTranches.size() == 0) {
			System.out.println("WARNING: cannot test update tranches: loan account " + theAccount.getId()
					+ " doesn't have tranches");
			return;
		}
		// See if we have any non-disbursed tranches
		List<LoanTranche> nonDisbursedTranches = theAccount.getNonDisbursedTranches();
		if (nonDisbursedTranches == null || nonDisbursedTranches.size() == 0) {
			System.out.println("WARNING: cannot test update tranches: loan account " + theAccount.getId()
					+ " doesn't have any non-disbursed tranches");
			return;
		}
		// Test updating tranches first
		Date trancheDate = new Date();
		long fiveDays = 5 * 24 * 60 * 60 * 1000; // 5 days in msecs
		for (LoanTranche tranche : nonDisbursedTranches) {
			trancheDate = new Date(trancheDate.getTime() + fiveDays); // make tranche dates to be 5 days apart
			tranche.setExpectedDisbursementDate(trancheDate);

		}
		System.out.println("\nUpdating existent tranches");
		LoansService loanService = MambuAPIFactory.getLoanService();
		LoanAccount result = loanService.updateLoanAccountTranches(accountId, nonDisbursedTranches);

		System.out.println("Loan Tranches updated for account " + accountId + " Total New Tranches="
				+ result.getNonDisbursedTranches().size());

		// Test deleting and then adding tranches now. Setting tranche's encoded key to null should result in all
		// existent tranches being deleted and the new ones (with the same data) created
		for (LoanTranche tranche : nonDisbursedTranches) {
			// Set all encoded keys to null. This would treat these tranches as new ones
			// The original versions will be deleted
			tranche.setEncodedKey(null);
		}
		System.out.println("\nDeleting and re-creating the same tranches");
		LoanAccount result2 = loanService.updateLoanAccountTranches(accountId, nonDisbursedTranches);
		System.out.println("Loan Tranches deleted and added for account " + accountId + " Total New Tranches="
				+ result2.getNonDisbursedTranches().size());
	}

	// Test Updating Loan Account Funds API: modify, add, delete
	public static void testUpdatingAccountFunds() throws MambuApiException {
		System.out.println(methodName = "\nIn testUpdatingAccountFunds");

		// Use demo loan account and update tranche details
		LoanAccount theAccount = demoLoanAccount;
		String accountId = theAccount.getId();

		// Get Loan Account Funds
		List<InvestorFund> funds = theAccount.getFunds();
		if (funds == null || funds.size() == 0) {
			System.out.println("WARNING: cannot test update funds: loan account " + theAccount.getId()
					+ " doesn't have funds");
			return;
		}

		// Test updating existent funds first
		for (InvestorFund fund : funds) {
			fund.setAmount(fund.getAmount().add(new BigDecimal(50.0)));

		}
		System.out.println("\nUpdating existent funds");
		LoansService loanService = MambuAPIFactory.getLoanService();
		LoanAccount result = loanService.updateLoanAccountFunds(accountId, funds);

		System.out.println("Loan Funds updated for account " + accountId + " Total New Funds="
				+ result.getFunds().size());

		// Test deleting and then adding funds now. Setting fund's encoded keys to null should result in these
		// existent funds being deleted and the new ones (with the same data) created
		List<InvestorFund> resultFunds = result.getFunds();
		List<InvestorFund> updatedFunds = new ArrayList<>();
		for (InvestorFund fund : resultFunds) {
			// Create new funds as copies of the original (but without the encoded key). This would treat these funds as
			// new ones. The original versions will be deleted
			InvestorFund newFund = new InvestorFund();
			// Make a copy
			newFund.setAmount(fund.getAmount());
			newFund.setGuarantorKey(fund.getGuarantorKey());
			newFund.setSavingsAccountKey(fund.getSavingsAccountKey());
			newFund.setCustomFieldValues(fund.getCustomFieldValues());
			updatedFunds.add(newFund);

		}
		System.out.println("\nDeleting and re-creating the same funds");
		LoanAccount result2 = loanService.updateLoanAccountFunds(accountId, updatedFunds);
		System.out.println("Loan Funds deleted and added for account " + accountId + " Total New Funds="
				+ result2.getFunds().size());
	}

	// / Transactions testing
	public static void testDisburseLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn test Disburse LoanAccount");

		LoansService loanService = MambuAPIFactory.getLoanService();
		if (newAccount == null || newAccount.getLoanAccount() == null) {
			System.out.println("\nThere is no account to disburse");
			return;
		}

		String amount = newAccount.getLoanAccount().getLoanAmount().toPlainString();
		String accountId = NEW_LOAN_ACCOUNT_ID;
		String disbursalDate = DateUtils.format(new Date());
		String firstRepaymentDate = DateUtils.format(new Date());

		LoanAccount account = newAccount.getLoanAccount();
		// Support for disbursing loan account with tranches is available since Mambu 3.13. See MBU-10045
		if (account.hasTranches()) {
			// For loan account with tranches in Mambu 3.13):
			// a) Amount must be null.
			// b) Only the first tranche can have firstRepaymentDate
			// c) the backdate is also not needed for disbursing with tranches
			amount = null;
			disbursalDate = null;
			// If not the first tranche - set the firstRepaymentDate to null
			if (account.getDisbursedTranches() != null && account.getDisbursedTranches().size() > 0) {
				firstRepaymentDate = null;
			}

		}
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
		System.out.println(methodName = "\nIn testUndoDisburseLoanAccount");

		LoansService loanService = MambuAPIFactory.getLoanService();
		String accountId = NEW_LOAN_ACCOUNT_ID;
		String notes = "Undo disbursement via Demo API";
		LoanTransaction transaction = loanService.undoDisburseLoanAccount(accountId, notes);
		System.out.println("\nOK Undo Loan Disbursement for account=" + accountId + "\tTransaction Id="
				+ transaction.getTransactionId());

	}

	// Test writing off loan account
	public static void testWriteOffLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn testWriteOffLoanAccount");

		LoansService loanService = MambuAPIFactory.getLoanService();
		String accountId = NEW_LOAN_ACCOUNT_ID;
		String notes = "Write off account via Demo API";
		LoanTransaction transaction = loanService.writeOffLoanAccount(accountId, notes);
		System.out.println("\nOK Write Off for account=" + accountId + "\tTransaction Id="
				+ transaction.getTransactionId());

	}

	public static List<LoanTransaction> testGetLoanAccountTransactions() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetLoanAccount Transactions");
		LoansService loanService = MambuAPIFactory.getLoanService();
		String offest = "0";
		String limit = "8";

		final String accountId = demoLoanAccount.getId();
		List<LoanTransaction> transactions = loanService.getLoanAccountTransactions(accountId, offest, limit);

		System.out.println("Got loan accounts transactions, total=" + transactions.size()
				+ " in a range for the Loan with the " + accountId + " id:" + " Range=" + offest + "  " + limit);
		for (LoanTransaction transaction : transactions) {
			System.out.println("Trans ID=" + transaction.getTransactionId() + "  " + transaction.getType() + "  "
					+ transaction.getEntryDate().toString());
		}
		return transactions;
	}

	// Test Reversing loan transactions. Available since 3.13 for PENALTY_APPLIED transaction. See MBU-9998
	public static void testReverseLoanAccountTransactions(List<LoanTransaction> transactions) throws MambuApiException {
		System.out.println(methodName = "\nIn testReverseLoanAccountTransactions");

		if (transactions == null || transactions.size() == 0) {
			System.out.println("WARNING: no transactions available to test transactions reversal");
		}
		LoansService loanService = MambuAPIFactory.getLoanService();

		// Try reversing any of the supported types. Some calls may throw validation exceptions (if not allowed for
		// reversal)
		boolean reversalTested = false;
		for (LoanTransaction transaction : transactions) {
			LoanTransactionType originalTransactionType = transaction.getType();
			// as of Mambu 3.13 only PENALTY_APPLIED transaction can be reversed
			switch (originalTransactionType) {
			case PENALTY_APPLIED:
				reversalTested = true;
				// Try reversing supported transaction type
				// Catch exceptions: For example, if there were later transactions logged after this one then Mambu
				// would return an exception
				try {
					String reversalNotes = "Reversed " + originalTransactionType + " by Demo API";
					String originalTransactionId = String.valueOf(transaction.getTransactionId());
					LoanTransaction reversed = loanService.reverseLoanTransaction(demoLoanAccount.getId(),
							originalTransactionType, originalTransactionId, reversalNotes);

					System.out.println("Reversed Transaction " + transaction.getType() + "\tReversed Amount="
							+ reversed.getAmount().toString() + "\tBalance =" + reversed.getBalance().toString()
							+ "Transaction Type=" + reversed.getType() + "\tAccount key="
							+ reversed.getParentAccountKey());
				} catch (MambuApiException e) {
					System.out.println("Reversing Transaction " + transaction.getType() + " returned exception"
							+ e.getMessage());
					continue;
				}
				break;
			default:
				break;
			}
		}
		if (!reversalTested) {
			System.out
					.println("WARNING: no transaction types supporting reversal is available to test transaction reversal");
		}
	}

	public static void testRepayLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn test Repay LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();
		Money repaymentAmount = demoLoanAccount.getDueAmount(RepaymentAllocationElement.PRINCIPAL);
		if (repaymentAmount == null || repaymentAmount.isNegativeOrZero()) {
			repaymentAmount = new Money(320);
		}
		String amount = repaymentAmount.toPlainString();
		String date = null; // "2012-11-23";
		String notes = "repayment notes from API";

		String accountId = demoLoanAccount.getId();

		// Make demo transactionDetails with the valid channel fields
		TransactionDetails transactionDetails = DemoUtil.makeDemoTransactionDetails();

		LoanTransaction transaction = loanService.makeLoanRepayment(accountId, amount, date, notes, transactionDetails);

		System.out.println("repayed loan account with the " + accountId + " id response="
				+ transaction.getTransactionId() + "   for amount=" + transaction.getAmount());
	}

	public static void testApplyFeeToLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn test Applying Fee to a Loan Account");

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

	public static void testApplyInterestToLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn test Applying Interest to a Loan Account");

		LoansService loanService = MambuAPIFactory.getLoanService();

		String accountId = demoLoanAccount.getId();
		Date date = new Date();
		String notes = "Notes for applying interest to a loan";

		LoanTransaction transaction = loanService.applyInterestToLoanAccount(accountId, date, notes);

		System.out.println("Loan Interest response= " + transaction.getTransactionId().toString()
				+ "\tTransaction Amount=" + transaction.getAmount().toString());
	}

	public static void testGetLoanAccountsByBranchCentreOfficerState() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetLoanAccountsByBranchCentreOfficerState");

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
		System.out.println(methodName = "\nIn testGetLoan Accounts ForClient");
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
		System.out.println(methodName = "\nIn testGetLoanAccounts ForGroup");
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

	// Test request loan account approval - this changes account state from Partial Application to Pending Approval
	public static void testRequestApprovalLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn testRequestApprovalLoanAccount");

		// Check if the account is in PARTIAL_APPLICATION state
		if (newAccount == null || newAccount.getLoanAccount() == null
				|| newAccount.getLoanAccount().getAccountState() != AccountState.PARTIAL_APPLICATION) {
			System.out
					.println("WARNING: Need to create loan account in PARTIAL_APPLICATION state to test Request Approval");
			return;
		}
		LoansService loanService = MambuAPIFactory.getLoanService();

		String accountId = newAccount.getLoanAccount().getId();
		String requestNotes = "Requested Approval by Demo API";
		LoanAccount account = loanService.requestApprovalLoanAccount(accountId, requestNotes);

		System.out.println("Requested Approval for loan account with the " + accountId + " Loan name="
				+ account.getLoanName() + "  Account State=" + account.getState());
	}

	public static void testApproveLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn test Approve LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		LoanAccount account = loanService.approveLoanAccount(NEW_LOAN_ACCOUNT_ID, "some demo notes");

		System.out.println("Approving loan account with the " + NEW_LOAN_ACCOUNT_ID + " Loan name="
				+ account.getLoanName() + "  Account State=" + account.getState().toString());
	}

	public static void testCloseLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn test Close LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		// CLose as REJECT or WITHDRAW
		CLOSER_TYPE closerType = CLOSER_TYPE.WITHDRAW; // or CLOSER_TYPE.REJECT
		LoanAccount account = loanService.closeLoanAccount(NEW_LOAN_ACCOUNT_ID, closerType,
				"some demo notes ', \" ü = : \n as");

		System.out.println("Rejecting loan account with the " + NEW_LOAN_ACCOUNT_ID + " Loan name"
				+ account.getLoanName() + "  Account State=" + account.getState().toString());
	}

	public static void testUndoApproveLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn test Undo Approve LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		final String accountId = NEW_LOAN_ACCOUNT_ID;
		LoanAccount account = loanService.undoApproveLoanAccount(accountId, "some undo approve demo notes");

		System.out.println("Undo Approving loan account with the " + accountId + " Loan name " + account.getLoanName()
				+ "  Account State=" + account.getState().toString());
	}

	public static void testLockLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn test Lock LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String accountId = NEW_LOAN_ACCOUNT_ID;
		// Updated to return a list. See MBU-8370
		List<LoanTransaction> transactions = loanService.lockLoanAccount(accountId, "some lock demo notes");

		if (transactions == null || transactions.size() == 0) {
			System.out.println("No Transactions returned in response");
			return;
		}
		for (LoanTransaction transaction : transactions) {
			System.out.println("Locked account with ID " + accountId + " Transaction  "
					+ transaction.getTransactionId() + " Type=" + transaction.getType() + "  Balance="
					+ transaction.getBalance());

		}
	}

	public static void testUnlockLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn test Unlock LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String accountId = NEW_LOAN_ACCOUNT_ID;
		// Updated to return a list. See MBU-8370
		List<LoanTransaction> transactions = loanService.unlockLoanAccount(accountId, "some unlock demo notes");
		if (transactions == null || transactions.size() == 0) {
			System.out.println("No Transactions returned in response");
			return;
		}
		for (LoanTransaction transaction : transactions) {
			System.out.println("UnLocked account with ID " + accountId + " Transaction  "
					+ transaction.getTransactionId() + " Type=" + transaction.getType() + "  Balance="
					+ transaction.getBalance());
		}

	}

	public static void testDeleteLoanAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn testDeleteLoanAccount");

		LoansService loanService = MambuAPIFactory.getLoanService();
		String loanAccountId = NEW_LOAN_ACCOUNT_ID;

		boolean status = loanService.deleteLoanAccount(loanAccountId);

		System.out.println("Deletion status=" + status);
	}

	// Loan Products
	public static List<LoanProduct> testGetLoanProducts() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetLoanProducts");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String offset = "0";
		String limit = "100";

		List<LoanProduct> products = loanService.getLoanProducts(offset, limit);

		System.out.println("Got loan products, count=" + products.size());

		if (products.size() > 0) {
			for (LoanProduct product : products) {
				List<GLAccountingRule> accountingRules = product.getGlProductRules();
				int totalAccountingRules = accountingRules == null ? 0 : accountingRules.size();
				System.out.println("Product=" + product.getName() + "\tId=" + product.getId() + "\tKey="
						+ product.getEncodedKey() + "\tProduct Type=" + product.getLoanProductType() + "\tRules="
						+ totalAccountingRules);
			}
		}

		return products;

	}

	public static void testGetLoanProductById() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetLoanProductById");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String productId = demoProduct.getId();

		LoanProduct product = loanService.getLoanProduct(productId);
		List<GLAccountingRule> accountingRules = product.getGlProductRules();
		// Log also the count for accounting rules. Available since 3.14, See MBU-10422
		int totalAccountingRules = accountingRules == null ? 0 : accountingRules.size();
		System.out.println("Product=" + product.getName() + "\tId=" + product.getId() + "\tProduct Type="
				+ product.getLoanProductType() + "\tAccountingRules=" + totalAccountingRules);

	}

	public static void testGetLoanProductSchedule() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetLoanProductSchedule");
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
		LoanProductType productType = demoProduct.getLoanProductType();
		// Set params to be consistent with the demo product (to be accepted by the GET product schedule API and create
		// loan
		final long time = new Date().getTime();
		loanAccount.setId("API-" + time); // specifying ID is supported in 3.14
		loanAccount.setLoanName(demoProduct.getName());
		loanAccount.setProductTypeKey(demoProduct.getEncodedKey());
		// Set PrincipalPaymentSettings from product: needed for Revolving Credit products since 3.14
		loanAccount.setPrincipalPaymentSettings(demoProduct.getPrincipalPaymentSettings());

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
			InterestRateSource rateSource = (intRateSettings == null) ? null : intRateSettings.getInterestRateSource();

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
			if (rateSource == InterestRateSource.INDEX_INTEREST_RATE) {
				loanAccount.setInterestSpread(interestRate); // set the spread
			} else {
				loanAccount.setInterestRate(interestRate); // set the rate
			}
		}

		// DisbursementDate
		// Set dates 3-4 days into the future
		long timeNow = new Date().getTime();
		long aDay = 24 * 60 * 60 * 1000; // 1 day in msecs
		loanAccount.setDisbursementDate(new Date(timeNow + 3 * aDay)); // 3 days from now

		// Tranches;
		if (productType == LoanProductType.TRANCHED_LOAN) {
			LoanTranche tranche = new LoanTranche(loanAccount.getLoanAmount(), loanAccount.getDisbursementDate());
			loanAccount.setDisbursementDate(null);
			ArrayList<LoanTranche> tanches = new ArrayList<LoanTranche>();
			tranche.setIndex(null); // index must by null. Default is zero
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
		loanAccount.setPrincipalRepaymentInterval(1);
		Integer principalRepaymentInterva = demoProduct.getDefaultPrincipalRepaymentInterval();
		if (principalRepaymentInterva != null) {
			loanAccount.setPrincipalRepaymentInterval(principalRepaymentInterva);
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
			loanAccount.setGracePeriod(gracePeriod);

		}
		// Set Guarantees. Available for API since 3.9. See MBU-6528
		ArrayList<Guaranty> guarantees = new ArrayList<Guaranty>();
		if (demoProduct.isGuarantorsEnabled()) {
			// GuarantyType.GUARANTOR
			Guaranty guarantySecurity = new Guaranty(GuarantyType.GUARANTOR);
			guarantySecurity.setAmount(loanAccount.getLoanAmount());
			guarantySecurity.setGuarantorKey(demoClient.getEncodedKey());
			guarantees.add(guarantySecurity);
		}
		if (demoProduct.isCollateralEnabled()) {
			// GuarantyType.ASSET
			Guaranty guarantyAsset = new Guaranty(GuarantyType.ASSET);
			guarantyAsset.setAssetName("Asset Name as a collateral");
			guarantyAsset.setAmount(loanAccount.getLoanAmount());
			guarantees.add(guarantyAsset);

		}
		// Add all guarantees to Loan account
		loanAccount.setGuarantees(guarantees);

		loanAccount.setExpectedDisbursementDate(loanAccount.getDisbursementDate());
		if (loanAccount.getExpectedDisbursementDate() == null) {
			loanAccount.setExpectedDisbursementDate(new Date());
		}
		loanAccount.setDisbursementDate(null);

		// Set first repayment date
		Date firstRepaymentDate = makeFirstRepaymentDate(loanAccount, demoProduct);
		loanAccount.setFirstRepaymentDate(firstRepaymentDate);

		loanAccount.setNotes("Created by DemoTest on " + new Date());
		return loanAccount;

	}

	public static void testGetDocuments() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetDocuments");

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
		System.out.println(methodName = "\nIn testUpdateDeleteCustomFields");

		// Delegate tests to new since 3.11 DemoTestCustomFiledValueService
		DemoTestCustomFiledValueService.testUpdateDeleteCustomFields(MambuEntityType.LOAN_ACCOUNT);

	}

	// Internal clean up routine. Can be used to delete non-disbursed accounts created by these demo test runs
	public static void deleteTestAPILoanAccounts() throws MambuApiException {
		System.out.println(methodName = "\nIn deleteTestAPILoanAccounts");
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

	// Create first repayment date considering its ScheduleDueDatesMethod and product restrictions, like a definition
	// for a minimum allowed first repayments date offset or the fixed days repayments
	// See MBU-9730 -As a Credit Officer, I want to have an offset applied to the first repayment date
	private static Date makeFirstRepaymentDate(LoanAccount account, LoanProduct product) {
		if (account == null || product == null) {
			return null;
		}
		Date disbDate = account.getExpectedDisbursementDate();
		if (disbDate == null) {
			return new Date();
		}
		long aDay = 24 * 60 * 60 * 1000; // 1 day in msecs
		Date firstRepaymentDate = new Date(disbDate.getTime() + 4 * aDay); // default to 4 days from disbursement date

		// Set the first repayment date depending on product's ScheduleDueDatesMethod
		ScheduleDueDatesMethod scheduleDueDatesMethod = product.getScheduleDueDatesMethod();
		if (scheduleDueDatesMethod == null) {
			return firstRepaymentDate;
		}
		switch (scheduleDueDatesMethod) {
		case FIXED_DAYS_OF_MONTH:
			// For fixed days product set to one of the allowed days
			System.out.println("Fixed day product:");
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
			return firstRepaymentDate;
		case INTERVAL:
			// For INTERVAL due dates product check for the allowed minimum offset time
			Integer repaymentPeriodCount = account.getRepaymentPeriodCount();
			RepaymentPeriodUnit unit = account.getRepaymentPeriodUnit();
			if (unit == null || repaymentPeriodCount == null) {
				return null;
			}

			// get minimum offset
			Integer minOffsetDays = product.getMinFirstRepaymentDueDateOffset();
			System.out.println("INTERVAL schedule due dates product. Min offset=" + minOffsetDays
					+ " RepaymentPeriodUnit=" + unit + " repaymentPeriodCount=" + repaymentPeriodCount);

			if (minOffsetDays == null) {
				// if no offset to 4 days in a future
				return firstRepaymentDate;
			}

			// Create UTC disbursement date with day, month year only
			Calendar disbDateCal = Calendar.getInstance();
			disbDateCal.setTime(disbDate);
			int day = disbDateCal.get(Calendar.DAY_OF_MONTH);
			int year = disbDateCal.get(Calendar.YEAR);
			int month = disbDateCal.get(Calendar.MONTH);
			disbDateCal.clear();
			disbDateCal.setTimeZone(TimeZone.getTimeZone("UTC"));
			disbDateCal.set(year, month, day);

			// Create calendar for firstRepaymDate and set it to be equal to the disbursement date
			Calendar firstRepaymDateCal = Calendar.getInstance();
			firstRepaymDateCal.clear();
			firstRepaymDateCal.setTimeZone(TimeZone.getTimeZone("UTC"));
			firstRepaymDateCal.setTime(disbDateCal.getTime());

			// Calculate the first expected repayment date without the offset
			switch (unit) {
			case DAYS:
				firstRepaymDateCal.add(Calendar.DAY_OF_MONTH, repaymentPeriodCount);
				break;
			case MONTHS:
				firstRepaymDateCal.add(Calendar.MONTH, repaymentPeriodCount);
				break;
			case WEEKS:
				firstRepaymDateCal.add(Calendar.DAY_OF_MONTH, (repaymentPeriodCount * 7));
				break;
			case YEARS:
				firstRepaymDateCal.add(Calendar.YEAR, repaymentPeriodCount);
				break;
			}
			// Add minimum offset in days
			firstRepaymDateCal.add(Calendar.DAY_OF_MONTH, 1 + minOffsetDays);
			// Get firstRepaymentDate
			firstRepaymentDate = firstRepaymDateCal.getTime();
			System.out.println("ExpDisb=" + disbDate + "\tMin Offset=" + minOffsetDays + "\trepaymentPeriodCount="
					+ repaymentPeriodCount + "\tunit=" + unit + "\tfirstRepaymentDate=" + firstRepaymentDate);
			return firstRepaymentDate;
		}

		return firstRepaymentDate;

	}
}
