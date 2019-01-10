
package demo;

import static com.mambu.accounting.shared.column.TransactionsDataField.AMOUNT;
import static com.mambu.accounting.shared.column.TransactionsDataField.PARENT_ACCOUNT_KEY;
import static com.mambu.core.shared.data.DataFieldType.NATIVE;
import static com.mambu.core.shared.data.FilterElement.EQUALS;
import static com.mambu.core.shared.data.FilterElement.MORE_THAN;
import static demo.DemoTestSearchService.createConstraint;
import static demo.DemoTestSearchService.createSingleFilterConstraints;
import static demo.DemoUtil.logCustomFieldValues;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.mambu.accounts.shared.model.Account;
import com.mambu.accounts.shared.model.AccountHolderType;
import com.mambu.accounts.shared.model.AccountState;
import com.mambu.accounts.shared.model.InterestAccountSettings;
import com.mambu.accounts.shared.model.InterestChargeFrequencyMethod;
import com.mambu.accounts.shared.model.InterestRateSource;
import com.mambu.accounts.shared.model.InterestRateTerms;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.admin.shared.model.InterestProductSettings;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraint;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraints;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.CustomFieldValueService;
import com.mambu.apisdk.services.DocumentsService;
import com.mambu.apisdk.services.OrganizationService;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.services.SearchService;
import com.mambu.apisdk.util.APIData.CLOSER_TYPE;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.data.DataItemType;
import com.mambu.core.shared.model.Currency;
import com.mambu.core.shared.model.CustomFieldType;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.Money;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;
import com.mambu.loans.shared.model.CustomPredefinedFee;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;
import com.mambu.savings.shared.model.SavingsTransaction;
import com.mambu.savings.shared.model.SavingsType;

import demo.DemoUtil.FeeCategory;

/**
 * Test class to show example usage of the api calls
 * 
 * @author mdanilkis
 * 
 */
public class DemoTestSavingsService {

	private static String GROUP_ID;

	private static Client demoClient;
	private static Group demoGroup;
	private static User demoUser;
	private static SavingsProduct demoSavingsProduct;
	private static SavingsAccount demoSavingsAccount;

	private static SavingsAccount newAccount;
	private static String NEW_ACCOUNT_ID;
	private static String methodName = null; // print method name on exception

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			// Get Demo data
			// Get demo entities needed for testing
			// Use specific product ID or null to get random product. If set to "ALL" then test for all product types
			final String testProductId = DemoUtil.demoSavingsProductId;
			final String testAccountId = null; // use specific test account or null to get random loan account

			demoClient = DemoUtil.getDemoClient(null);
			demoGroup = DemoUtil.getDemoGroup(null);
			demoUser = DemoUtil.getDemoUser();

			SavingsType productTypes[];
			boolean productTypesTesting;
			// If demoSavingsProductId configuration set to ALL then test all available product types
			if (testProductId != null && testProductId.equals(DemoUtil.allProductTypes)) {
				productTypesTesting = true;
				// Run tests for all products types, selecting random active product ID of each type once
				productTypes = SavingsType.values();
				System.out.println("Testing all Product Types =" + productTypes.length);
			} else {
				// Use demoSavingsProductId configuration: a specific product ID (if not null) or a random product (if
				// null)
				productTypesTesting = false;
				SavingsProduct testProduct = DemoUtil.getDemoSavingsProduct(testProductId);
				// Set test product types to the demoSavingsProductId type only
				productTypes = new SavingsType[] { testProduct.getProductType() };
			}

			// Run tests for all required product types
			for (SavingsType productType : productTypes) {

				System.out.println("\n*** Product Type=" + productType + " ***");

				// Get random product of a specific type or a product for a specific product id
				demoSavingsProduct = (productTypesTesting) ? DemoUtil.getDemoSavingsProduct(productType)
						: DemoUtil.getDemoSavingsProduct(testProductId);

				if (demoSavingsProduct == null) {
					continue;
				}
				// Log product services
				List<Currency> currencies = demoSavingsProduct.getCurrencies();
				String currencyCode = currencies != null && currencies.size() > 0 ? currencies.get(0).getCode() : null;
				System.out.println("Product Id=" + demoSavingsProduct.getId() + "\tName=" + demoSavingsProduct.getName()
						+ "\tCurrency=" + currencyCode + " ***");

				// Get demo savings account
				demoSavingsAccount = DemoUtil.getDemoSavingsAccount(testAccountId);

				try {

					// Test savings operations. Create new account for these tests
					testCreateSavingsAccount();
					testPatchSavingsAccountTerms(); // Available since 3.12.2
					testUpdateSavingsAccount(); // Available since 3.4
					testApproveSavingsAccount(); // Available since 3.5
					testUndoApproveSavingsAccount(); // Available since 3.5
					testApproveSavingsAccount(); // Available since 3.5

					testGetFundedLoanAccounts(); // Available since 3.14

					testGetSavingsAccount();
					testGetSavingsAccountDetails();

					testGetSavingsAccountsByBranchCentreOfficerState();

					testGetSavingsAccountsForClient();

					// Test deposit and reversal transactions
					SavingsTransaction depositTransaction = testDepositToSavingsAccount();
					
					testSearchSavingsTransactionsWithCustomFields(depositTransaction);
					testSearchSavingsTransactionsWithoutCustomFields(depositTransaction);

					testSearchSavingsTransactionEntitiesWithoutCustomFields(depositTransaction);
					testSearchSavingsTransactionEntitiesWithCustomFields(depositTransaction);

					testReverseSavingsAccountTransaction(depositTransaction); // Available since 3.10

					testDepositToSavingsAccount(); // Make another deposit after reversal to continue testing
					testStartMaturityForSavingAccount(); // Available since 4.4
					// Test withdrawal and reversal transactions
					SavingsTransaction withdrawalTransaction = testWithdrawalFromSavingsAccount();
					testReverseSavingsAccountTransaction(withdrawalTransaction); // Available since 3.10

					// Test transfer and reversal transactions
					SavingsTransaction transferTransaction = testTransferFromSavingsAccount();
					testReverseSavingsAccountTransaction(transferTransaction);

					testApplyFeeToSavingsAccount(); // Available since 3.6

					testGetSavingsAccountTransactions();

					testGetSavingsAccountsForGroup();

					testGetSavingsProducts();
					testGetSavingsProductById();

					testGetDocuments();// Available since 3.6

					testUpdateDeleteCustomFields(); // Available since 3.8

					// Test account deletion now
					testCreateSavingsAccount();
					testDeleteSavingsAccount(); // Available since 3.4

					testBulkReverseOnSavingTransactions(); // Available since 4.2

					// Test rejecting an account now
					CLOSER_TYPE testTypes[] = { CLOSER_TYPE.WITHDRAW, CLOSER_TYPE.REJECT };
					for (CLOSER_TYPE closerType : testTypes) {
						testCreateSavingsAccount();
						// Test REJECT and REJECT transactions first
						// Test Close and UNDO Close as REJECT and REJECT first (we cannot CLOSE pending accounts)
						SavingsAccount closedAccount = testCloseSavingsAccount(closerType); // Available since 3.4
						testUndoCloseSavingsAccount(closedAccount); // Available since 4.2
						// Test Closing accounts with obligations met and UNDO CLOSE
						closedAccount = testCloseSavingsAccount(CLOSER_TYPE.CLOSE); // Available since 4.0
						testUndoCloseSavingsAccount(closedAccount); // Available since 4.2
					}

				} catch (MambuApiException e) {
					DemoUtil.logException(methodName, e);
					System.out.println("Product Type=" + demoSavingsProduct.getProductType() + "\tID="
							+ demoSavingsProduct.getId() + "\tName=" + demoSavingsProduct.getName());
				}
			}

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Savings Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	/* Tests starting maturity on a saving account */
	private static void testStartMaturityForSavingAccount() throws MambuApiException {

		methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		String accountId = NEW_ACCOUNT_ID;

		SavingsType savingAccountType = demoSavingsProduct.getProductType();
		SavingsService savingService = MambuAPIFactory.getSavingsService();
		AccountState accountState = savingService.getSavingsAccount(accountId).getAccountState();

		if ((SavingsType.FIXED_DEPOSIT.equals(savingAccountType) || (SavingsType.SAVINGS_PLAN.equals(savingAccountType)))
				&& (AccountState.ACTIVE.equals(accountState) || AccountState.DORMANT.equals(accountState))) {

			Calendar now = Calendar.getInstance();
			now.add(Calendar.DAY_OF_WEEK, 1);
			String notes = "Notes created through API=" + System.currentTimeMillis();
			SavingsAccount updatedAccount = savingService.startMaturity(accountId, now.getTime(), notes);
			
			// log account details
			System.out.println("Started maturity for saving account, ID=" + updatedAccount.getId() + "\tName= " + updatedAccount.getName()
			+ "\tCurrency=" + updatedAccount.getCurrencyCode() + "\tHolder Key=" + updatedAccount.getAccountHolderKey());
			
		} else {
			System.out.println("WARNING: The test " +  methodName +  " wasn`t run due to account conditions.");
		}

	}

	// tests bulk reverse on saving transactions
	private static void testBulkReverseOnSavingTransactions() throws MambuApiException {

		// create saving account
		System.out.println(methodName = "\nIn testBulkReverseOnSavingTransactions");

		SavingsService service = MambuAPIFactory.getSavingsService();

		SavingsAccount savingsAccount = makeSavingsAccountForDemoProduct();
		// Create Account in Mambu
		SavingsAccount newAccount = service.createSavingsAccount(savingsAccount);

		if (newAccount == null) {
			System.out.println("Saving account couldn`t be created");
			return;
		}
		// approve it
		newAccount = service.approveSavingsAccount(newAccount.getId(), "Approve savings account demo notes");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		// create 3 transactions
		List<SavingsTransaction> transactions = createThreeSavingTransactionsForBulkReversalTest(newAccount);

		if (transactions != null && (transactions.size() == 3)) {
			// reverse second transaction
			String notes = "Reversed by Demo API";
			SavingsTransaction Transaction = savingsService.reverseSavingsTransaction(transactions.get(1), notes);

			System.out.println("Reversed Transaction=" + Transaction.getType() + "\tReversed Amount="
					+ Transaction.getAmount().toString() + "\tBalance =" + Transaction.getBalance().toString()
					+ "Transaction Type=" + Transaction.getType() + "\tAccount key="
					+ Transaction.getParentAccountKey());
		} else {
			System.out.println(
					"WARNING: Unable to create the saving transactions required by testBulkReverseOnSavingTransactions() test method");
		}

	}

	/**
	 * Helper method, creates three transactions for the account received as parameter to this method
	 * 
	 * @param newAccount
	 *            The saving account
	 * @return a list of transactions for bulk reversal test
	 * @throws MambuApiException if creation of transactions fails
	 */
	private static List<SavingsTransaction> createThreeSavingTransactionsForBulkReversalTest(SavingsAccount newAccount)
			throws MambuApiException {

		List<SavingsTransaction> transactions = new ArrayList<>();

		if (newAccount != null) {

			SavingsService savingsService = MambuAPIFactory.getSavingsService();

			// create 3 saving transactions
			for (int i = 1; i <= 3; i++) {
				Money amount = new Money(150.00 * i);
				Date date = null;
				String notes = "Deposit notes - API - Transaction No" + i;

				// Make demo transactionDetails with the valid channel fields
				TransactionDetails transactionDetails = DemoUtil.makeDemoTransactionDetails();

				SavingsTransaction transaction = savingsService.makeDeposit(newAccount.getId(), amount, date,
						transactionDetails, null, notes);

				System.out.println("Made Deposit To Savings for account with the " + newAccount.getId() + " id:"
						+ ". Amount=" + transaction.getAmount() + " Balance =" + transaction.getBalance());
				transactions.add(transaction);
			}
		}

		return transactions;
	}

	private static void testGetSavingsAccount() throws MambuApiException {

		System.out.println(methodName = "\nIn testGetSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		SavingsAccount account = savingsService.getSavingsAccount(NEW_ACCOUNT_ID);

		System.out.println("Got Savings account: " + account.getName() + "\tCurrency=" + account.getCurrencyCode());

	}

	public static void testGetSavingsAccountDetails() throws MambuApiException {

		System.out.println(methodName = "\nIn testGetSavingsAccountDetails");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		SavingsAccount account = savingsService.getSavingsAccountDetails(NEW_ACCOUNT_ID);

		// Log some account details
		System.out.println("Account name: " + account.getName() + "\tID=" + account.getId() + "\tCurrency="
				+ account.getCurrencyCode() + "\tInterest=" + account.getInterestRate() + "\tFrequency="
				+ account.getInterestChargeFrequency() + "\tFrequency Count="
				+ account.getInterestChargeFrequencyCount());

	}

	public static void testGetSavingsAccountsForClient() throws MambuApiException {

		System.out.println(methodName = "\nIn testGetSavingsAccountsForClient");
		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String clientid = demoClient.getId();
		List<SavingsAccount> savingsAccounts = savingsService.getSavingsAccountsForClient(clientid);

		System.out.println(
				"Got Savings accounts for the client with the " + clientid + " id, Total=" + savingsAccounts.size());
		for (SavingsAccount account : savingsAccounts) {
			System.out.print(account.getName() + " (" + account.getCurrencyCode() + ")" + " ");
		}
		System.out.println();
	}
	// Test getting all loan accounts funded by a deposit investor account
	public static void testGetFundedLoanAccounts() throws MambuApiException {

		System.out.println(methodName = "\nIn testGetFundedLoanAccounts");
		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String savingsId = newAccount.getId();

		// This API call can succeed only if the test account is of SavingsType.INVESTOR_ACCOUNT type
		if (demoSavingsProduct.getProductType() == SavingsType.INVESTOR_ACCOUNT) {
			try {
				List<LoanAccount> fundedAccounts = savingsService.getFundedLoanAccounts(savingsId);
				System.out.println("Total Funded accounts=" + fundedAccounts.size() + " for Savings ID=" + savingsId);
				for (LoanAccount account : fundedAccounts) {
					System.out.print("\tFunded Loan Account: " + account.getName() + " " + account.getId());
				}
				System.out.println();
			} catch (MambuApiException e) {
				DemoUtil.logException(methodName, e);

			}
		} else {
			System.out.println(
					"WARNING: cannot test GET Funded Accounts for product type " + demoSavingsProduct.getProductType());
		}
	}

	public static void testGetSavingsAccountsForGroup() throws MambuApiException {

		System.out.println(methodName = "\nIn testGetSavingsAccountsForGroup");
		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		List<SavingsAccount> savingsAccounts = savingsService.getSavingsAccountsForGroup(demoGroup.getId());

		System.out.println(
				"Got Savings accounts for the group with the " + GROUP_ID + " id, Total =" + savingsAccounts.size());
		for (SavingsAccount account : savingsAccounts) {
			System.out.print(account.getName() + ", ");
		}
		System.out.println();
	}

	// Get All Transaction
	public static void testGetSavingsAccountTransactions() throws MambuApiException {

		System.out.println(methodName = "\nIn testGetSavingsAccountTransactions");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String offset = "1";
		String limit = "2";
		List<SavingsTransaction> transactions = savingsService.getSavingsAccountTransactions(NEW_ACCOUNT_ID, offset,
				limit);

		System.out.println("Got Savings Transactions " + transactions.size() + "  for account " + NEW_ACCOUNT_ID
				+ " Offset=" + offset + " limit=" + limit);
		for (SavingsTransaction transaction : transactions) {
			System.out.println("\tID=" + transaction.getTransactionId() + "\tDate="
					+ transaction.getEntryDate().toString() + "\tType=" + transaction.getType());
		}
		System.out.println();
	}

	// Make Withdrawal
	public static SavingsTransaction testWithdrawalFromSavingsAccount() throws MambuApiException {

		System.out.println(methodName = "\nIn testWithdrawalFromSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		Money amount = new Money(10.00);
		Date date = null;
		String notes = "Withdrawal notes from API";

		// Make demo transactionDetails with the valid channel fields
		TransactionDetails transactionDetails = DemoUtil.makeDemoTransactionDetails();
		// make test transaction fields
		List<CustomFieldValue> transactionFields = DemoUtil.makeForEntityCustomFieldValues(
				CustomFieldType.TRANSACTION_CHANNEL_INFO, transactionDetails.getTransactionChannelKey(), false);

		SavingsTransaction transaction = savingsService.makeWithdrawal(NEW_ACCOUNT_ID, amount, date, transactionDetails,
				transactionFields, notes);

		System.out.println("Made Withdrawal from Savings for account with the " + NEW_ACCOUNT_ID + " id:" + ". Amount="
				+ transaction.getAmount() + " Balance =" + transaction.getBalance());

		// tests getting custom fields from saving transaction
		testGetCustomFieldForSavingTransaction(transaction);

		return transaction;
	}

	/**
	 * Gets the custom field values from the saving transaction passed as argument to this method, and then iterates
	 * over them and call Mambu to get the details and logs them to the console.
	 * 
	 * @param transaction
	 *            The transaction (saving transaction) holding the custom field details
	 * @throws MambuApiException
	 */
	private static void testGetCustomFieldForSavingTransaction(SavingsTransaction transaction)
			throws MambuApiException {

		// Available since 4.2. More details on MBU-13211
		System.out.println(methodName = "\nIn testGetCustomFieldForTransaction");

		if (transaction == null) {
			System.out.println(
					"Warning! Transaction was found null, testGetCustomFieldForSavingTransaction() method couldn`t run.");
			return;
		}

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		SavingsAccount savingAccount = savingsService.getSavingsAccount(NEW_ACCOUNT_ID);

		if (savingAccount == null) {
			System.out.println(
					"Warning!! Account was found null, testGetCustomFieldForSavingTransaction() method couldn`t run!");
			return;
		}

		// get the service for custom fields
		CustomFieldValueService customFieldValueService = MambuAPIFactory.getCustomFieldValueService();

		for (CustomFieldValue customFieldValue : transaction.getCustomFieldValues()) {
			List<CustomFieldValue> retrievedCustomFieldValues = customFieldValueService.getCustomFieldValue(
					MambuEntityType.SAVINGS_ACCOUNT, savingAccount.getId(), MambuEntityType.SAVINGS_TRANSACTION,
					transaction.getEncodedKey(), customFieldValue.getCustomFieldId());
			// logs the details to the console
			logCustomFieldValues(retrievedCustomFieldValues, "SavingTransaction", savingAccount.getId());
		}
	}

	// Deposit
	public static SavingsTransaction testDepositToSavingsAccount() throws MambuApiException {

		System.out.println(methodName = "\nIn testDepositToSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		Money amount = new Money(150.00);
		Date date = null;
		String notes = "Deposit notes - API";

		// Make demo transactionDetails with the valid channel fields
		TransactionDetails transactionDetails = DemoUtil.makeDemoTransactionDetails();
		// Make Transaction Fields
		List<CustomFieldValue> transactionFields = DemoUtil.makeForEntityCustomFieldValues(
				CustomFieldType.TRANSACTION_CHANNEL_INFO, transactionDetails.getTransactionChannelKey(), false);

		SavingsTransaction transaction = savingsService.makeDeposit(NEW_ACCOUNT_ID, amount, date, transactionDetails,
				transactionFields, notes);

		System.out.println("Made Deposit To Savings for account with the " + NEW_ACCOUNT_ID + " id:" + ". Amount="
				+ transaction.getAmount() + " Balance =" + transaction.getBalance());

		return transaction;
	}

	private static void testSearchSavingsTransactionsWithCustomFields(SavingsTransaction savingsTransaction) throws MambuApiException {

		String methodName = new Object() { }.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		JSONFilterConstraints filterConstraints = getJsonFilterConstraintsForParentTransactionGreaterThanOne(savingsTransaction);

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		List<SavingsTransaction> transactions = savingsService.getSavingsTransactionsWithFullDetails(filterConstraints, "0", "100");

		for (SavingsTransaction transaction : transactions) {

			List<CustomFieldValue> customFieldValues = transaction.getCustomFieldValues();
			logCustomFieldValues(customFieldValues, "SavingsAccount", transaction.getParentAccountKey());
		}
	}

	private static void testSearchSavingsTransactionEntitiesWithCustomFields(SavingsTransaction savingsTransaction) throws MambuApiException {

		String methodName = new Object() { }.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		JSONFilterConstraints filterConstraints = getJsonFilterConstraintsForParentTransactionGreaterThanOne(savingsTransaction);

		SearchService searchService = MambuAPIFactory.getSearchService();

		List<SavingsTransaction> transactions = searchService.searchEntitiesWithFullDetails(MambuEntityType.SAVINGS_TRANSACTION, filterConstraints, "0", "100");

		for (SavingsTransaction transaction : transactions) {

			List<CustomFieldValue> customFieldValues = transaction.getCustomFieldValues();
			logCustomFieldValues(customFieldValues, "SavingsAccount", transaction.getParentAccountKey());
		}
	}

	
	private static void testSearchSavingsTransactionsWithoutCustomFields(SavingsTransaction savingsTransaction) throws MambuApiException {

		String methodName = new Object() { }.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		JSONFilterConstraints filterConstraints = getJsonFilterConstraintsForParentTransactionGreaterThanOne(savingsTransaction);
		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		List<SavingsTransaction> transactions = savingsService.getSavingsTransactionsWithBasicDetails(filterConstraints, "0", "100");

		checkCustomFieldValueExistenceOnTransactions(transactions);
	}

	private static void testSearchSavingsTransactionEntitiesWithoutCustomFields(SavingsTransaction savingsTransaction) throws MambuApiException {

		String methodName = new Object() { }.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		JSONFilterConstraints filterConstraints = getJsonFilterConstraintsForParentTransactionGreaterThanOne(savingsTransaction);
		SearchService searchService = MambuAPIFactory.getSearchService();

		List<SavingsTransaction> transactions = searchService.searchEntitiesWithBasicDetails(MambuEntityType.SAVINGS_TRANSACTION,  filterConstraints, "0", "100");

		checkCustomFieldValueExistenceOnTransactions(transactions);
	}

	private static void checkCustomFieldValueExistenceOnTransactions(List<SavingsTransaction> transactions) {
		for (SavingsTransaction transaction : transactions) {

			List<CustomFieldValue> customFieldValues = transaction.getCustomFieldValues();

			if (customFieldValues == null) {
				System.out.println("No custom fields were found for transaction:" + transaction.getId());
			} else {
				System.out.println("WARN: custom fields were returned for transaction");
			}
		}
	}

	public static SavingsTransaction testTransferFromSavingsAccount() throws MambuApiException {

		System.out.println(methodName = "\nIn testTransferFromSavingsAccount");
		SavingsTransaction transaction = null;

		// use try and catch to continue: valid API test transfer transactions often fail on business validation rules
		try {
			SavingsService savingsService = MambuAPIFactory.getSavingsService();
			OrganizationService organizationService = MambuAPIFactory.getOrganizationService();
			String accountId = NEW_ACCOUNT_ID;
			SavingsAccount savings = savingsService.getSavingsAccount(accountId);
			// Since 4.2 Mambu API supports transfers only between accounts in the same currency. See MBU-12619

			String currencyCode = savings.getCurrencyCode();
			List<Currency> allCurrencies = organizationService.getAllCurrencies();

			Currency savingsCurrency = null;

			for (Currency cur : allCurrencies) {
				if (cur.getCode().equals(currencyCode)) {
					savingsCurrency = cur;
				}
			}

			// Check the balance
			Money balance = savings.getBalance();
			if (balance == null || balance.isLessOrEqual(Money.zero())) {
				System.out.println("WARNING: Cannot Transfer to a loan from zero balance account");
				return null;
			}
			// Set transfer amount to be less than remaining balance
			double transferAmount = Math.min(balance.getAmount().doubleValue(), 5.50f);
			String amount = String.valueOf(transferAmount);

			// Test Transferring to a Loan Account first
			// Loan accounts can only be in base currency for now. Check if our savings is in the base currency
			if (!savingsCurrency.isBaseCurrency()) {
				System.out
						.println("WARNING: Cannot test transfer into a Loan: Savings account is in a non-base currency "
								+ savingsCurrency.getCode());
			} else {
				LoanAccount loanAccount = DemoUtil.getDemoLoanAccount();
				String destinationAccountKey = loanAccount.getId();

				String notes = "Transfer notes from API";

				Account.Type destinationAccountType = Account.Type.LOAN;
				transaction = savingsService.makeTransfer(accountId, destinationAccountKey, destinationAccountType,
						amount, notes);

				System.out.println("Transfer From account:" + accountId + "   To Loan Account id="
						+ destinationAccountKey + " Amount=" + transaction.getAmount().toString() + " Transac Id="
						+ transaction.getTransactionId() + "\tBalance=" + transaction.getBalance());
			}

			// Test transferring into a Savings account now
			balance = balance.subtract(new Money(transferAmount));
			if (balance.isLessOrEqual(Money.zero())) {
				System.out.println("WARNING: Cannot Transfer to savings from zero balance account");
				return transaction;
			}
			// Get savings in the same currency
			List<SavingsAccount> savingAccounts = savingsService.getSavingsAccountsForClient(demoClient.getId());
			SavingsAccount transferAccount = null;
			for (SavingsAccount account : savingAccounts) {
				// Compare currency symbols
				if (account.getCurrencyCode().equals(savingsCurrency.getCode())) {
					transferAccount = account;
					break;
				}
			}
			if (transferAccount == null) {
				System.out.println("WARNING: Cannot find Savings to transfer to with the same currency code="
						+ savingsCurrency.getCode());
				return transaction;
			}
			String destinationAccountKey = transferAccount.getId();
			String notes = "Transfer notes to Savings from API";

			Account.Type destinationAccountType = Account.Type.SAVINGS;
			transaction = savingsService.makeTransfer(accountId, destinationAccountKey, destinationAccountType, amount,
					notes);

			System.out.println("Transfer From account:" + accountId + "   To Saving Account id=" + destinationAccountKey
					+ " Amount=" + transaction.getAmount().toString() + " Transac Id=" + transaction.getTransactionId()
					+ "\tBalance=" + transaction.getBalance());

		} catch (MambuApiException e) {
			DemoUtil.logException(methodName, e);
		}
		return transaction;
	}
	// Test Reversing savings transaction. Available since 3.10 for Deposit, Withdrawal and Transfer transactions
	public static void testReverseSavingsAccountTransaction(SavingsTransaction transaction) throws MambuApiException {

		System.out.println(methodName = "\nIn testReverseSavingsAccountTransaction");

		if (transaction == null) {
			System.out.println("WARNING: cannot reverse null transaction");
			return;
		}
		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String notes = "Reversed by Demo API";
		SavingsTransaction reversed = savingsService.reverseSavingsTransaction(transaction, notes);

		System.out.println("Reversed Transaction=" + transaction.getType() + "\tReversed Amount="
				+ reversed.getAmount().toString() + "\tBalance =" + reversed.getBalance().toString()
				+ "Transaction Type=" + reversed.getType() + "\tAccount key=" + reversed.getParentAccountKey());
	}

	// Test Apply Fee. Available since 3.6.
	// Applying Manual Predefined fees is available since Mambu 4.1
	public static void testApplyFeeToSavingsAccount() throws MambuApiException {

		System.out.println(methodName = "\nIn testApplyFeeToSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		String notes = "Fee notes";

		String accountId = NEW_ACCOUNT_ID;
		System.out.println("Demo Savings account with Id=" + accountId + "\tProduct=" + demoSavingsProduct.getId());

		// Create demo fees to apply. Get Manual fees only
		List<CustomPredefinedFee> productFees = DemoUtil.makeDemoPredefinedFees(demoSavingsProduct,
				new HashSet<>(Collections.singletonList(FeeCategory.MANUAL)));
		if (productFees.size() > 0) {
			// Submit random predefined fee from the list of fees available for this product
			int randomIndex = (int) Math.random() * (productFees.size() - 1);
			CustomPredefinedFee predefinedFee = productFees.get(randomIndex);
			System.out.println("Applying Predefined Fee =" + predefinedFee.getPredefinedFeeEncodedKey() + " Amount="
					+ predefinedFee.getAmount());
			List<CustomPredefinedFee> customFees = new ArrayList<>();
			customFees.add(predefinedFee);
			// Submit API request
			SavingsTransaction transaction = savingsService.applyFeeToSavingsAccount(accountId, customFees, notes);

			System.out.println("Predefined Fee. TransactionID=" + transaction.getTransactionId() + "\tAmount="
					+ transaction.getAmount() + "\tFees Amount=" + transaction.getFeesAmount());

			// Test reversing Transaction
			testReverseSavingsAccountTransaction(transaction); // Available since 4.2 for Fees
		} else {
			System.out.println("WARNING: No Predefined Fees defined for product " + demoSavingsProduct.getId());
		}

		// Test Arbitrary Fee
		if (demoSavingsProduct.getAllowArbitraryFees()) {
			BigDecimal amount = new BigDecimal(15);
			// Use Arbitrary fee API
			System.out.println("Applying Arbitrary Fee. Amount=" + amount);
			// Submit API request
			SavingsTransaction transaction = savingsService.applyFeeToSavingsAccount(accountId, amount.toPlainString(),
					notes);
			System.out.println("Arbitrary Fee. TransactionID=" + transaction.getTransactionId() + "\tAmount="
					+ transaction.getAmount().toString() + "\tFees Amount=" + transaction.getFeesAmount());

			// Test reversing Transaction
			testReverseSavingsAccountTransaction(transaction); // Available since 4.2 for Fees

		} else {
			System.out.println("WARNING: Arbitrary Fees no allowed for product " + demoSavingsProduct.getId());
		}
	}

	public static void testGetSavingsAccountsByBranchCentreOfficerState() throws MambuApiException {

		System.out.println(methodName = "\nIn testGetSavingsAccountsByBranchCentreOfficerState");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String branchId = demoClient.getAssignedBranchKey();
		String centreId = demoClient.getAssignedCentreKey(); // Centre ID filter is available since 3.7
		String creditOfficerUserName = demoUser.getUsername();
		String accountState = AccountState.ACTIVE.name(); // CLOSED_WITHDRAWN ACTIVE APPROVED
		String offset = "0";
		String limit = "2";

		List<SavingsAccount> accounts = savingsService.getSavingsAccountsByBranchCentreOfficerState(branchId, centreId,
				creditOfficerUserName, accountState, offset, limit);

		System.out.println(
				"Got Savings accounts for the branch, centre, officer, state, total Deposits=" + accounts.size());
		for (SavingsAccount account : accounts) {
			System.out.println("AccountsID=" + account.getId() + " " + account.getName() + "\tBranchId="
					+ account.getAssignedBranchKey() + "\tCentreId=" + account.getAssignedCentreKey()
					+ "\tCredit Officer=" + account.getAssignedUserKey());

		}

	}

	// Savings Products
	public static void testGetSavingsProducts() throws MambuApiException {

		System.out.println(methodName = "\nIn testGetSavingsProducts");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String offset = "0";
		String limit = "50";

		List<SavingsProduct> products = savingsService.getSavingsProducts(offset, limit);

		System.out.println("Got Savings products, count=" + products.size());

		if (products.size() > 0) {
			for (SavingsProduct product : products) {
				// Log product details, including currency
				List<Currency> productCurerncies = product.getCurrencies();
				String currencyCode = productCurerncies != null && productCurerncies.size() > 0
						? productCurerncies.get(0).getCode() : null;
				System.out.println("Product=" + product.getName() + "\tId=" + product.getId() + "\tProduct Type="
						+ product.getProductType() + "\tCurrency=" + currencyCode);
			}
		}

	}
	public static void testGetSavingsProductById() throws MambuApiException {

		System.out.println(methodName = "\nIn testGetSavingsProductById");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String productId = demoSavingsProduct.getId();

		SavingsProduct product = savingsService.getSavingsProduct(productId);

		// Log also Interest Rate frequency. Available since Mambu 4.0. See MBU-11447 and MBU-11449
		InterestChargeFrequencyMethod frequencyMethod = demoSavingsProduct.getInterestChargeFrequency();
		InterestChargeFrequencyMethod overdraftFrequencyMethod = demoSavingsProduct
				.getOverdraftInterestChargeFrequency();

		// Log Savings Product Currency. Available since 4.2. See MBU-12619
		List<Currency> currencies = product.getCurrencies();
		String currencyCode = currencies != null && currencies.size() > 0 ? currencies.get(0).getCode() : null;
		System.out.println("Product=" + product.getName() + "\tId=" + product.getId() + "\tSavings Type="
				+ product.getTypeOfProduct().name() + "\tCurrency=" + currencyCode + "\tInterestChargeFrequency="
				+ frequencyMethod + "\tOverdraftInterestChargeFrequency=" + overdraftFrequencyMethod);

	}

	public static void testCreateSavingsAccount() throws MambuApiException {

		System.out.println(methodName = "\nIn testCreateSavingsAccount");

		SavingsService service = MambuAPIFactory.getSavingsService();

		SavingsAccount savingsAccount = makeSavingsAccountForDemoProduct();

		// Add Custom Fields
		List<CustomFieldValue> clientCustomInformation = DemoUtil.makeForEntityCustomFieldValues(
				CustomFieldType.SAVINGS_ACCOUNT_INFO, demoSavingsProduct.getEncodedKey());
		savingsAccount.setCustomFieldValues(clientCustomInformation);

		// Create Account in Mambu
		newAccount = service.createSavingsAccount(savingsAccount);

		NEW_ACCOUNT_ID = newAccount.getId();

		System.out.println("Created Savings Account OK, ID=" + newAccount.getId() + "\tName= " + newAccount.getName()
				+ "\tCurrency=" + newAccount.getCurrencyCode() + "\tHolder Key=" + newAccount.getAccountHolderKey());

		// Get Custom Information from the JSONSavingsAccount
		List<CustomFieldValue> updatedCustomFields = newAccount.getCustomFieldValues();

		if (updatedCustomFields != null) {
			System.out.println("Custom Fields for Account\n");
			for (CustomFieldValue value : updatedCustomFields) {
				System.out.println("CustomFieldKey=" + value.getCustomFieldKey() + "\tValue=" + value.getValue());

			}
		}
	}

	// Update Savings account
	public static void testUpdateSavingsAccount() throws MambuApiException {

		System.out.println(methodName = "\nIn testUpdateSavingsAccount");

		SavingsService service = MambuAPIFactory.getSavingsService();

		// Use the newly created account and update some custom fields
		SavingsAccount updatedAccount = newAccount;
		// Note: Savings API doesn't return CustomField with the CustomFieldValue in create response.
		// Need to refresh account with full details to custom field IDs
		updatedAccount = service.getSavingsAccountDetails(updatedAccount.getEncodedKey());

		List<CustomFieldValue> customFields = updatedAccount.getCustomFieldValues();
		List<CustomFieldValue> toUpdateCustomFields = new ArrayList<CustomFieldValue>();

		if (customFields != null) {
			for (CustomFieldValue value : customFields) {
				value = DemoUtil.makeNewCustomFieldValue(value);
				toUpdateCustomFields.add(value);
			}
		}
		updatedAccount.setCustomFieldValues(toUpdateCustomFields);

		// Update account in Mambu
		SavingsAccount updatedAccountResult = service.updateSavingsAccount(updatedAccount);

		System.out.println("Savings Update OK, ID=" + updatedAccountResult.getId() + "\tAccount Name="
				+ updatedAccountResult.getName());

		// Get returned custom fields
		List<CustomFieldValue> updatedCustomFields = updatedAccountResult.getCustomFieldValues();

		if (updatedCustomFields != null) {
			System.out.println("Custom Fields for Savings Account\n");
			for (CustomFieldValue value : updatedCustomFields) {
				System.out.println("CustomField Key=" + value.getCustomFieldKey() + "\tValue=" + value.getValue());

			}
		}

	}

	// Test Patch Savings account terms API
	public static void testPatchSavingsAccountTerms() throws MambuApiException {

		System.out.println(methodName = "\nIn testPatchSavingsAccountTerms");

		SavingsService service = MambuAPIFactory.getSavingsService();
		// See MBU-10447 for a list of fields that can be updated (as of Mambu 3.14)
		SavingsAccount savingsAccount = newAccount;
		String productKey = savingsAccount.getProductTypeKey();
		SavingsProduct product = DemoUtil.getDemoSavingsProduct(productKey);
		SavingsType productType = product.getProductType();
		System.out.println("\tProduct=" + product.getName() + "\tType=" + productType + "\tId=" + product.getId());

		// Update account
		InterestProductSettings overdraftRateSettings = product.getOverdraftInterestRateSettings();
		InterestRateSource overdraftRateSource = (overdraftRateSettings == null) ? null
				: overdraftRateSettings.getInterestRateSource();

		// Update overdraft fields
		// Since 4.1 we can have TIRED overdraft rates, which cannot be updated. See MBU-11948
		if (product.isAllowOverdraft() && overdraftRateSource != null
				&& overdraftRateSettings.getInterestRateTerms() != InterestRateTerms.TIERED) {
			// get MaxInterestRat to limit our test changes
			BigDecimal maxOverdraftRate = overdraftRateSettings.getMaxInterestRate();
			final BigDecimal rateIncrease = new BigDecimal(0.5f);
			final BigDecimal limitIncrease = new BigDecimal(400.00f);

			switch (overdraftRateSource) {
			case FIXED_INTEREST_RATE:
				// Set new Overdraft Limit
				BigDecimal overdraftLimit = savingsAccount.getOverdraftLimit() != null
						? savingsAccount.getOverdraftLimit() : BigDecimal.ZERO;
				// Increase by the test amount and limit to the max allowed
				BigDecimal updatedOverdraftLimit = overdraftLimit.add(limitIncrease).setScale(2, RoundingMode.DOWN);
				BigDecimal maxOverdraftLimit = demoSavingsProduct.getMaxOverdraftLimit();
				if (maxOverdraftLimit != null) {
					updatedOverdraftLimit = updatedOverdraftLimit.min(maxOverdraftLimit);
				}
				// Set new Overdraft Limit
				System.out.println("New Overdraft Limit=" + updatedOverdraftLimit);
				savingsAccount.setOverdraftLimit(updatedOverdraftLimit);
				// Modify Interest Rate
				BigDecimal overdraftInterestRate = savingsAccount.getOverdraftInterestRate() != null
						? savingsAccount.getOverdraftInterestRate() : BigDecimal.ZERO;
				// Increase by the test amount and limit to the max allowed
				overdraftInterestRate = overdraftInterestRate.add(rateIncrease).setScale(2, RoundingMode.DOWN);
				if (maxOverdraftRate != null) {
					overdraftInterestRate = overdraftInterestRate.min(maxOverdraftRate);
				}
				// Set new Overdraft Rate
				System.out.println("New Overdraft Rate=" + overdraftInterestRate);
				savingsAccount.setOverdraftInterestRate(overdraftInterestRate);
				break;
			case INDEX_INTEREST_RATE:
				// OverdraftInterestSpread
				BigDecimal rateSpread = savingsAccount.getOverdraftInterestSpread() != null
						? savingsAccount.getOverdraftInterestSpread() : BigDecimal.ZERO;
				// Increase by the test amount and limit to the max allowed
				rateSpread = rateSpread.add(rateIncrease).setScale(2, RoundingMode.DOWN);
				if (maxOverdraftRate != null) {
					rateSpread = rateSpread.min(maxOverdraftRate);
				}
				// Set new Overdraft Interest Spread
				System.out.println("New Overdraft Interest Spread=" + rateSpread);
				savingsAccount.setOverdraftInterestSpread(rateSpread);
				break;
			}
			// Modify also Overdraft ExpiryDate
			savingsAccount.setOverdraftExpiryDate(new Date());
		} else {
			// Set also "overdraftLimit" to null. Mambu account would contain it as "0" but PATCH would not accept "0"
			// if overdraft is not allowed
			savingsAccount.setOverdraftInterestSettings(null);
			savingsAccount.setOverdraftLimit(null);
		}

		// Set Interest Rate
		// Since 4.1 we have TIERED products, which do not support updating interest rates. See MBU-11266
		if (product.isInterestPaidIntoAccount() && product.getInterestRateSettings() != null
				&& !product.hasTieredInterestRateTerms()) {
			System.out.println("Updating Interest Rate");
			InterestProductSettings productSettings = product.getInterestRateSettings();
			BigDecimal rate = savingsAccount.getInterestRate() != null ? savingsAccount.getInterestRate()
					: BigDecimal.ZERO;
			// Increase by the test amount and limit to the max allowed
			BigDecimal rateIncrease = new BigDecimal(0.55);
			rate = rate.add(rateIncrease).setScale(2, RoundingMode.DOWN);
			BigDecimal maxRate = productSettings.getMaxInterestRate();
			if (maxRate != null) {
				rate = rate.min(maxRate);
			}
			// Set new Interest Rate
			System.out.println("New Interest rate=" + rate);
			savingsAccount.setInterestRate(rate);

		} else {
			// Do not update interest rates
			savingsAccount.setInterestRate(null);

		}
		// Modify MaxWidthdrawalAmount
		final BigDecimal increaseMaxWithdrawl = new BigDecimal(50.00f);
		Money currentMaxWidthdrawlAmount = savingsAccount.getMaxWidthdrawlAmount() != null
				? savingsAccount.getMaxWidthdrawlAmount() : Money.zero();
		// Increase by the test amount and limit to the max allowed
		currentMaxWidthdrawlAmount = currentMaxWidthdrawlAmount.add(increaseMaxWithdrawl);
		Money maxProductWidthdrawlAmount = product.getMaxWidthdrawlAmount();
		if (maxProductWidthdrawlAmount != null) {
			currentMaxWidthdrawlAmount = currentMaxWidthdrawlAmount.min(maxProductWidthdrawlAmount);

		}
		// Set new MaxWidthdrawalAmount
		System.out.println("New  MaxWidthdrawalAmount=" + currentMaxWidthdrawlAmount);
		savingsAccount.setMaxWidthdrawlAmount(currentMaxWidthdrawlAmount);

		// Modify RecommendedDepositAmount. Can be set only for Current Accounts, Savings Accounts and Savings Plans
		if (productType == SavingsType.CURRENT_ACCOUNT || productType == SavingsType.SAVINGS_PLAN) {
			// Update recommendedDepositAmount
			Money recAmount = savingsAccount.getRecommendedDepositAmount();
			final Money addRecommened = new Money(200.00f);
			if (recAmount == null) {
				recAmount = addRecommened;
			} else {
				recAmount = recAmount.add(addRecommened).setScale(2, RoundingMode.DOWN);
			}
			System.out.println("New  RecommendedDepositAmount=" + recAmount);
			savingsAccount.setRecommendedDepositAmount(recAmount);

		} else {
			// May need to clear Recommended Deposit Amount: the createAccount() API currently allows it to be specified
			// even when not applicable for INVESTOR_ACCOUNT product type
			Money nullAmount = null;
			savingsAccount.setRecommendedDepositAmount(nullAmount);
		}
		// Update targetAmount
		if (productType == SavingsType.SAVINGS_PLAN) {
			final Money targetAmountIncrease = new Money(1000.00f);
			Money targetAmount = savingsAccount.getTargetAmount();
			if (targetAmount == null) {
				targetAmount = targetAmountIncrease;
			} else {
				targetAmount = targetAmount.add(targetAmountIncrease).setScale(2, RoundingMode.DOWN);
			}
			System.out.println("New Target Amount=" + targetAmount);
			savingsAccount.setTargetAmount(targetAmount);

		}
		// Submit updated account to Mambu

		boolean status = service.patchSavingsAccount(savingsAccount);
		System.out.println("Patched savings account status=" + status);

	}

	public static void testApproveSavingsAccount() throws MambuApiException {

		System.out.println(methodName = "\nIn testApproveSavingsAccount");

		SavingsService service = MambuAPIFactory.getSavingsService();

		SavingsAccount account = service.approveSavingsAccount(NEW_ACCOUNT_ID, "Approve savings account demo notes");

		System.out.println("Approved Savings account with id=" + account.getId() + "\tName=" + account.getName()
				+ "\tAccount State=" + account.getAccountState().toString());

		// Get returned custom fields
		List<CustomFieldValue> updatedCustomFields = account.getCustomFieldValues();

		if (updatedCustomFields != null) {
			System.out.println("Custom Fields for this Account\n");
			for (CustomFieldValue value : updatedCustomFields) {
				// Savings API doesn't return CustomField with the CustomFieldValue
				System.out.println("CustomFieldKey=" + value.getCustomFieldKey() + "\tValue=" + value.getValue());
			}
		} else {
			System.out.println("No Custom Fields for this Account\n");
		}
	}

	public static void testUndoApproveSavingsAccount() throws MambuApiException {

		System.out.println(methodName = "\nIn testUndoApproveSavingsAccount");

		SavingsService service = MambuAPIFactory.getSavingsService();
		String accountId = NEW_ACCOUNT_ID;
		SavingsAccount account = service.undoApproveSavingsAccount(accountId,
				"UNDO Approve Savings account demo notes");

		System.out.println("UNDO Approved Savings account with id=" + account.getId() + "\tName=" + account.getName()
				+ "\tAccount State=" + account.getAccountState().toString());

	}

	public static void testDeleteSavingsAccount() throws MambuApiException {

		System.out.println(methodName = "\nIn testDeleteSavingsAccount");

		SavingsService service = MambuAPIFactory.getSavingsService();

		String accountId = NEW_ACCOUNT_ID;
		boolean accountDeleted = service.deleteSavingsAccount(accountId);

		NEW_ACCOUNT_ID = null;
		System.out.println("Deleted Savings account with id=" + accountId + "\tDeletion status=" + accountDeleted);
	}

	/**
	 * Test Closing Savings account
	 * 
	 * @param closerType
	 *            closer type. Must not be null. Supported closer types are: REJECT, WITHDRAW and CLOSE
	 * @return closed account
	 * @throws MambuApiException
	 */
	public static SavingsAccount testCloseSavingsAccount(CLOSER_TYPE closerType) throws MambuApiException {

		System.out.println(methodName = "\nIn testCloseSavingsAccount" + " with " + closerType + " CLOSER_TYPE");

		if (closerType == null) {
			throw new IllegalArgumentException("Closer type must not be null");
		}
		SavingsService service = MambuAPIFactory.getSavingsService();

		// Get current account to check its balance
		SavingsAccount account = DemoUtil.getDemoSavingsAccount(NEW_ACCOUNT_ID);
		String accountId = account.getId();

		// For CLOSER_TYPE.CLOSE - withdraw any remaining balance first to test Close transaction
		Money balance = account.getBalance();
		if (closerType == CLOSER_TYPE.CLOSE && balance != null && balance.isPositive()) {
			System.out.println("Withdrawing remaining Balance to test Close transaction");
			service.makeWithdrawal(accountId, balance, null, null, null, "Withdraw to test Close Transaction");
		}
		// Closer type: WITHDRAW, REJECT or CLOSE;
		String notes = "Closed by Demo Test";
		System.out.println("CloserType==" + closerType + "\tID=" + accountId + "\tState=" + account.getAccountState());
		SavingsAccount resultAaccount = service.closeSavingsAccount(accountId, closerType, notes);

		System.out.println("Closed account id:" + resultAaccount.getId() + "\tNew State="
				+ resultAaccount.getAccountState().name());

		return resultAaccount;
	}

	/**
	 * Test Undo Closing Savings account
	 * 
	 * @param closedAccount
	 *            closed savings account. Must not be null. Account must be closed with API supported closer types are:
	 *            REJECT, WITHDRAW and CLOSE
	 * @return updated account
	 * @throws MambuApiException
	 */
	public static SavingsAccount testUndoCloseSavingsAccount(SavingsAccount closedAccount) throws MambuApiException {

		System.out.println(methodName = "\nIn testUndoCloseSavingsAccount");
		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		if (closedAccount == null || closedAccount.getId() == null) {
			System.out.println("Account must be not null for testing undo closer");
			return null;
		}

		String notes = "UNDO Closer notes";

		System.out.println("UNDO Closing account with Id=" + closedAccount.getId() + "\tState="
				+ closedAccount.getAccountState() + "\tSubState=" + closedAccount.getAccountSubState());
		SavingsAccount resultAaccount = savingsService.undoCloseSavingsAccount(closedAccount, notes);

		System.out.println("OK UNDO Closed account id:" + resultAaccount.getId() + "\tNew State="
				+ resultAaccount.getAccountState().name() + "\tSubState=" + resultAaccount.getAccountSubState());

		return resultAaccount;
	}

	public static void testGetDocuments() throws MambuApiException {

		System.out.println(methodName = "\nIn testGetDocuments");

		Integer offset = 0;
		Integer limit = 5;
		DocumentsService documentsService = MambuAPIFactory.getDocumentsService();
		List<Document> documents = documentsService.getDocuments(MambuEntityType.SAVINGS_ACCOUNT,
				demoSavingsAccount.getId(), offset, limit);

		// Log returned documents using DemoTestDocumentsService helper
		System.out.println("Documents returned for a Savings Account with ID=" + demoSavingsAccount.getId());
		DemoTestDocumentsService.logDocuments(documents);

	}

	// Update Custom Field values for the Savings Account and delete the first available custom field
	public static void testUpdateDeleteCustomFields() throws MambuApiException {

		System.out.println(methodName = "\nIn testUpdateDeleteCustomFields");

		// Delegate tests to new since 3.11 DemoTestCustomFiledValueService
		DemoTestCustomFieldValueService.testUpdateDeleteEntityCustomFields(MambuEntityType.SAVINGS_ACCOUNT);

	}

	private static final String apiTestIdPrefix = "API-";

	// Create demo savings account with parameters consistent with the demo product
	private static SavingsAccount makeSavingsAccountForDemoProduct() {

		System.out.println("\nIn makeSavingsAccountForDemoProduct for product name=" + demoSavingsProduct.getName()
				+ " id=" + demoSavingsProduct.getId());

		if (!demoSavingsProduct.isActivated()) {
			System.out.println("*** WARNING ***: demo product is NOT Active. Product name="
					+ demoSavingsProduct.getName() + " id=" + demoSavingsProduct.getId());
		}
		SavingsAccount savingsAccount = new SavingsAccount();
		// Since 4.1 need also to initialise InterestAccountSettings, otherwise there will be a null exception on
		// setInterestRate()
		savingsAccount.setInterestSettings(new InterestAccountSettings());
		// savingsAccount.setOverdraftInterestSettings(new InterestAccountSettings());

		final long time = new Date().getTime();
		savingsAccount.setId(apiTestIdPrefix + time); // Can set ID field since 3.13.1. See MBU-10574
		savingsAccount.setName(demoSavingsProduct.getName());
		savingsAccount.setProductTypeKey(demoSavingsProduct.getEncodedKey());

		SavingsType savingsType = demoSavingsProduct.getProductType();
		savingsAccount.setAccountType(savingsType);
		savingsAccount.setAccountState(AccountState.PENDING_APPROVAL); // AccountState must be set explicitly since 3.14

		boolean isForClient = demoSavingsProduct.isForIndividuals();
		String holderKey = isForClient ? demoClient.getEncodedKey() : demoGroup.getEncodedKey();
		AccountHolderType holderType = isForClient ? AccountHolderType.CLIENT : AccountHolderType.GROUP;
		savingsAccount.setAccountHolderKey(holderKey);
		savingsAccount.setAccountHolderType(holderType);

		// Set the currency in the account. Available since Mambu 4.2. See MBU-12619
		// NOTE: Setting Currency is optional, it is done here for testing only: Mambu will set the currency based on
		// the product if it is not specified in the account create message
		// Get Currency from the account's product
		List<Currency> currencies = demoSavingsProduct.getCurrencies();
		Currency currency = currencies != null && currencies.size() > 0 ? currencies.get(0) : null;

		savingsAccount.setCurrencyCode(currency.getCode());

		// Set Interest rate. required since Mambu 3.13. See MBU-9806
		InterestProductSettings productRateSettings = demoSavingsProduct.getInterestRateSettings();
		if (productRateSettings == null) {
			productRateSettings = new InterestProductSettings();
		}

		BigDecimal interestRate = null;
		// Set interest rate only if it is paid into the account
		// Do not set interest rate for Tiered products. Available since 4.1. See MBU-11266
		InterestRateTerms interestRateTerms = productRateSettings.getInterestRateTerms();
		if (demoSavingsProduct.isInterestPaidIntoAccount() && interestRateTerms != InterestRateTerms.TIERED) {
			BigDecimal defInterestRate = productRateSettings.getDefaultInterestRate();
			BigDecimal minInterestRate = productRateSettings.getMinInterestRate();
			BigDecimal maxInterestRate = productRateSettings.getMaxInterestRate();
			interestRate = defInterestRate;
			interestRate = interestRate != null ? interestRate : minInterestRate;
			interestRate = interestRate != null ? interestRate : maxInterestRate;
			if (interestRate == null) {
				interestRate = new BigDecimal(3.5f);
			}
		}
		// Starting from 4.1 need to initialise InterestAccountSettings first
		if (interestRate != null) {
			InterestAccountSettings accountInterestSettings = new InterestAccountSettings();
			accountInterestSettings.setInterestRateSource(productRateSettings.getInterestRateSource());
			accountInterestSettings.setInterestRateTerms(productRateSettings.getInterestRateTerms());
			savingsAccount.setInterestSettings(accountInterestSettings);
			// set the rate
			savingsAccount.setInterestRate(interestRate);
		}

		// Max Withdrawal
		Money maxWidthdrawlAmount = demoSavingsProduct.getMaxWidthdrawlAmount();
		if (maxWidthdrawlAmount == null) {
			maxWidthdrawlAmount = new Money(300.00);
		}
		savingsAccount.setMaxWidthdrawlAmount(maxWidthdrawlAmount);
		// Recommended Deposit. Not available for FIXED_DEPOSIT and INVESTOR_ACCOUNT products
		Money recommendedDepositAmount = demoSavingsProduct.getRecommendedDepositAmount();
		if (recommendedDepositAmount == null && savingsType != SavingsType.FIXED_DEPOSIT
				&& savingsType != SavingsType.INVESTOR_ACCOUNT) {
			recommendedDepositAmount = new Money(400.00);
		}
		savingsAccount.setRecommendedDepositAmount(recommendedDepositAmount);

		final long hundredDays = 100 * 24 * 60 * 60 * 1000; // 100 days
		// Overdraft params
		if (demoSavingsProduct.isAllowOverdraft()) {
			// Set Overdraft Amount
			// Set Overdraft Interest rate
			InterestProductSettings overdraftRateSettings = demoSavingsProduct.getOverdraftInterestRateSettings();
			if (overdraftRateSettings == null) {
				overdraftRateSettings = new InterestProductSettings();
			}
			// Starting from 4.1 need to initialise overdraft InterestAccountSettings first
			InterestAccountSettings overdraftInteresrSettings = new InterestAccountSettings();
			overdraftInteresrSettings.setInterestRateSource(overdraftRateSettings.getInterestRateSource());
			overdraftInteresrSettings.setInterestRateTerms(overdraftRateSettings.getInterestRateTerms());
			savingsAccount.setOverdraftInterestSettings(overdraftInteresrSettings);

			savingsAccount.setAllowOverdraft(true);
			Money maxOverdraftLimit = demoSavingsProduct.getMaxOverdraftLimitMoney();
			maxOverdraftLimit = (maxOverdraftLimit != null) ? maxOverdraftLimit : new Money(120.00);
			savingsAccount.setOverdraftLimitMoney(maxOverdraftLimit);

			InterestRateSource rateSource = overdraftRateSettings.getInterestRateSource();

			BigDecimal defOverdraftInterestRate = overdraftRateSettings.getDefaultInterestRate();
			BigDecimal minOverdraftInterestRate = overdraftRateSettings.getMinInterestRate();
			BigDecimal maxOverdraftInterestRate = overdraftRateSettings.getMaxInterestRate();

			BigDecimal overdraftInterestRate = defOverdraftInterestRate;
			overdraftInterestRate = (overdraftInterestRate == null && minOverdraftInterestRate != null)
					? minOverdraftInterestRate : overdraftInterestRate;
			overdraftInterestRate = (overdraftInterestRate == null && maxOverdraftInterestRate != null)
					? maxOverdraftInterestRate : overdraftInterestRate;
			if (rateSource == InterestRateSource.INDEX_INTEREST_RATE) {
				savingsAccount.setOverdraftInterestSpread(overdraftInterestRate);
			} else {
				savingsAccount.setOverdraftInterestRate(overdraftInterestRate);
			}
			if (overdraftInterestRate != null) {
				savingsAccount.setOverdraftExpiryDate(new Date(new Date().getTime() + hundredDays));
			}
			savingsAccount.setOverdraftInterestRateSource(rateSource);
			savingsAccount.setOverdraftInterestRateReviewCount(overdraftRateSettings.getInterestRateReviewCount());
			savingsAccount.setOverdraftInterestRateReviewUnit(overdraftRateSettings.getInterestRateReviewUnit());
		}

		if (savingsType == SavingsType.SAVINGS_PLAN) {
			savingsAccount.setTargetAmount(new BigDecimal(1000000.00));
		}
		// Provide Tax Source. Available since 3.10. See MBU-8070- "As a Developer, I need to post savings accounts
		// linked to a tax source"
		if (demoSavingsProduct.hasWithholdingTaxEnabled()) {
			// Need to obtain withholdingTaxSourceKey from Mambu. For example, create an account with a tax source in
			// Mambu and get its full details to obtain withholdingTaxSourceKey
			String withholdingTaxSourceKey = "8a8497464c2e0b01014c2e2337290030";
			savingsAccount.setWithholdingTaxSourceKey(withholdingTaxSourceKey);
		}

		savingsAccount.setNotes("Created by API on " + new Date());
		return savingsAccount;
	}

	// Internal clean up routine. Can be used to delete non-active accounts created by these demo test runs
	public static void deleteTestAPISavingsAccounts() throws MambuApiException {

		System.out.println(methodName = "\nIn deleteTestAPISavingsAccounts");
		System.out.println(
				"**  Deleting all Test Savings Accounts for Client =" + demoClient.getFullNameWithId() + " **");
		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		List<SavingsAccount> accounts = savingsService.getSavingsAccountsForClient(demoClient.getId());
		List<SavingsAccount> groupAccounts = savingsService.getSavingsAccountsForGroup(demoGroup.getId());
		accounts.addAll(groupAccounts);

		if (accounts != null && accounts.size() == 0) {
			System.out.println("Nothing to delete for demo client or group");
			return;
		}
		for (SavingsAccount account : accounts) {
			String name = account.getName();
			String id = account.getId();
			if (id.startsWith(apiTestIdPrefix)) {
				AccountState state = account.getAccountState();
				if (state == AccountState.PENDING_APPROVAL || state == AccountState.APPROVED) {
					System.out.println("Deleting savings account " + name + " ID=" + id);
					try {
						savingsService.deleteSavingsAccount(id);
					} catch (MambuApiException e) {
						System.out.println("Account " + id + " is NOT deleted. Exception=" + e.getMessage());
						continue;
					}
				}
			}
		}
	}


	private static JSONFilterConstraints getJsonFilterConstraintsForParentTransactionGreaterThanOne(SavingsTransaction savingsTransaction) {

		JSONFilterConstraints filterConstraints = createSingleFilterConstraints(
				NATIVE,
				AMOUNT.name(),
				MORE_THAN,
				DataItemType.SAVINGS_TRANSACTION,
				"1",
				null);

		JSONFilterConstraint accountConstraint = createConstraint(
				NATIVE,
				PARENT_ACCOUNT_KEY.name(),
				EQUALS,
				DataItemType.SAVINGS_TRANSACTION,
				savingsTransaction.getParentAccountKey(),
				null);
		filterConstraints.getFilterConstraints().add(accountConstraint);

		return filterConstraints;
	}
	
}
