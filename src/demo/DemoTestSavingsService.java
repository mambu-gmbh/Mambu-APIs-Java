package demo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.mambu.accounts.shared.model.Account;
import com.mambu.accounts.shared.model.AccountHolderType;
import com.mambu.accounts.shared.model.AccountState;
import com.mambu.accounts.shared.model.InterestRateSource;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.util.APIData;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.InterestRateSettings;
import com.mambu.core.shared.model.Money;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;
import com.mambu.savings.shared.model.SavingsTransaction;
import com.mambu.savings.shared.model.SavingsType;

/**
 * Test class to show example usage of the api calls
 * 
 * @author mdanilkis
 * 
 */
public class DemoTestSavingsService {

	private static String GROUP_ID;
	private static String SAVINGS_ACCOUNT_ID;

	private static Client demoClient;
	private static Group demoGroup;
	private static User demoUser;
	private static SavingsProduct demoSavingsProduct;
	private static SavingsAccount demoSavingsAccount;

	private static JSONSavingsAccount newAccount;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			// Get Demo data
			// Get demo entities needed for testing
			final String testProductId = null; // use specific test product or null to get random product
			final String testAccountId = null; // use specific test account or null to get random loan account

			demoClient = DemoUtil.getDemoClient();
			demoGroup = DemoUtil.getDemoGroup();
			demoUser = DemoUtil.getDemoUser();

			demoSavingsProduct = DemoUtil.getDemoSavingsProduct(testProductId);
			demoSavingsAccount = DemoUtil.getDemoSavingsAccount(testAccountId);
			SAVINGS_ACCOUNT_ID = demoSavingsAccount.getId();

			testCreateSavingsAccount();

			testCloseSavingsAccount(); // Available since 3.4
			testDeleteSavingsAccount(); // Available since 3.4

			testCreateSavingsAccount();

			testUpdateSavingsAccount(); // Available since 3.4

			testApproveSavingsAccount();

			testUndoApproveSavingsAccount(); // Available since 3.5
			testApproveSavingsAccount(); // Available since 3.5

			testGetSavingsAccount();
			testGetSavingsAccountDetails();

			testGetSavingsAccountsByBranchCentreOfficerState();

			testGetSavingsAccountsForClient();

			// Test deposit and reversal transactions
			SavingsTransaction deposiTransaction = testDepositToSavingsAccount();
			testReverseSavingsAccountTransaction(deposiTransaction); // Available since 3.10
			testDepositToSavingsAccount(); // Make another deposit after reversal to continue testing

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

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Savings Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testGetSavingsAccount() throws MambuApiException {
		System.out.println("\nIn testGetSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		SavingsAccount account = savingsService.getSavingsAccount(SAVINGS_ACCOUNT_ID);

		System.out.println("Got Savings account: " + account.getName());

	}

	public static void testGetSavingsAccountDetails() throws MambuApiException {
		System.out.println("\nIn testGetSavingsAccount with Details");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		SavingsAccount account = savingsService.getSavingsAccountDetails(SAVINGS_ACCOUNT_ID);

		System.out.println("Got Savings account: " + account.getName());

	}

	public static void testGetSavingsAccountsForClient() throws MambuApiException {
		System.out.println("\nIn testGetSavingsAccountsFor Client");
		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String clientid = demoClient.getId();
		List<SavingsAccount> savingsAccounts = savingsService.getSavingsAccountsForClient(clientid);

		System.out.println("Got Savings accounts for the client with the " + clientid + " id, Total="
				+ savingsAccounts.size());
		for (SavingsAccount account : savingsAccounts) {
			System.out.print(account.getName() + " ");
		}
		System.out.println();
	}

	public static void testGetSavingsAccountsForGroup() throws MambuApiException {
		System.out.println("\nIn testGetSavingsAccountsFor Group");
		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		List<SavingsAccount> savingsAccounts = savingsService.getSavingsAccountsForGroup(demoGroup.getId());

		System.out.println("Got Savings accounts for the group with the " + GROUP_ID + " id, Total ="
				+ savingsAccounts.size());
		for (SavingsAccount account : savingsAccounts) {
			System.out.print(account.getName() + ", ");
		}
		System.out.println();
	}

	// Get All Transaction
	public static void testGetSavingsAccountTransactions() throws MambuApiException {
		System.out.println("\nIn testGetSavingsAccountTransactions");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		List<SavingsTransaction> transactions = savingsService.getSavingsAccountTransactions(SAVINGS_ACCOUNT_ID, null,
				null);

		System.out.println("Got Savings Transactions for account with the " + SAVINGS_ACCOUNT_ID + " id:");
		for (SavingsTransaction transaction : transactions) {
			System.out.println(transaction.getEntryDate().toString() + " " + transaction.getType());
		}
		System.out.println();
	}

	// Make Withdrawal
	public static SavingsTransaction testWithdrawalFromSavingsAccount() throws MambuApiException {
		System.out.println("\nIn testWithdrawalFromSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		String amount = "93.55";
		String date = null;
		String notes = "Withdrawal notes from API";

		// Make demo transactionDetails with the valid channel fields
		TransactionDetails transactionDetails = DemoUtil.makeDemoTransactionDetails();

		SavingsTransaction transaction = savingsService.makeWithdrawal(SAVINGS_ACCOUNT_ID, amount, date, notes,
				transactionDetails);
		System.out
				.println("Made Withdrawal from Savings for account with the " + SAVINGS_ACCOUNT_ID + " id:"
						+ ". Amount=" + transaction.getAmount().toString() + " Balance ="
						+ transaction.getBalance().toString());

		return transaction;
	}

	// Deposit
	public static SavingsTransaction testDepositToSavingsAccount() throws MambuApiException {
		System.out.println("\nIn testDepositToSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		String amount = "150.00";
		String date = null;
		String notes = "Deposit notes - API";

		// Make demo transactionDetails with the valid channel fields
		TransactionDetails transactionDetails = DemoUtil.makeDemoTransactionDetails();

		SavingsTransaction transaction = savingsService.makeDeposit(SAVINGS_ACCOUNT_ID, amount, date, notes,
				transactionDetails);

		System.out.println("Made Deposit To Savings for account with the " + SAVINGS_ACCOUNT_ID + " id:" + ". Amount="
				+ transaction.getAmount().toString() + " Balance =" + transaction.getBalance().toString());

		return transaction;
	}

	public static SavingsTransaction testTransferFromSavingsAccount() throws MambuApiException {
		System.out.println("\nIn testTransferFromSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String destinationAccountKey = DemoUtil.getDemoLoanAccount().getId();

		String amount = "20.50";
		String notes = "Transfer notes from API";

		Account.Type destinationAccountType = Account.Type.LOAN;
		SavingsTransaction transaction = savingsService.makeTransfer(SAVINGS_ACCOUNT_ID, destinationAccountKey,
				destinationAccountType, amount, notes);

		System.out.println("Transfer From account:" + SAVINGS_ACCOUNT_ID + "   To account id=" + destinationAccountKey
				+ " Amount=" + transaction.getAmount().toString() + " Transac Id=" + transaction.getTransactionId()
				+ "\tBalance=" + transaction.getBalance());

		return transaction;
	}

	// Test Reversing savings transaction. Available since 3.10 for Deposit, Withdrawal and Transfer transactions
	public static void testReverseSavingsAccountTransaction(SavingsTransaction transaction) throws MambuApiException {
		System.out.println("\nIn testReverseSavingsAccountTransaction");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String notes = "Reversed by Demo API";
		SavingsTransaction reversed = savingsService.reverseSavingsTransaction(transaction, notes);

		System.out.println("Reversed Transaction=" + transaction.getType() + "\tReversed Amount="
				+ reversed.getAmount().toString() + "\tBalance =" + reversed.getBalance().toString()
				+ "Transaction Type=" + reversed.getType() + "\tAccount key=" + reversed.getParentAccountKey());
	}

	// Apply Arbitrary Fee. Available since 3.6
	public static void testApplyFeeToSavingsAccount() throws MambuApiException {
		System.out.println("\nIn testApplyFeeToSavingsAccount");

		// API supports applying fee only for products with 'Allow Arbitrary Fees" setting
		if (!demoSavingsProduct.getAllowArbitraryFees()) {
			System.out.println("\nWARNING: demo product=" + demoSavingsProduct.getName()
					+ " doesn't allow Arbitrary Fees. Use other product to test applyFee API");
			return;
		}
		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		String amount = "5.00";
		String notes = "Apply Fee to savings via API notes";

		String accountId = SAVINGS_ACCOUNT_ID;
		System.out.println("Demo Savings account with Id=" + accountId);

		SavingsTransaction transaction = savingsService.applyFeeToSavingsAccount(accountId, amount, notes);

		System.out.println("Apply Fee To Savings for account with  " + accountId + " id:" + ". Amount="
				+ transaction.getAmount().toString() + " Balance =" + transaction.getBalance().toString());

	}

	public static void testGetSavingsAccountsByBranchCentreOfficerState() throws MambuApiException {
		System.out.println("\nIn testGetSavingsAccountsByBranchCentreOfficerState");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String branchId = demoClient.getAssignedBranchKey();
		String centreId = demoClient.getAssignedCentreKey(); // Centre ID filter is available since 3.7
		String creditOfficerUserName = demoUser.getUsername();
		String accountState = AccountState.ACTIVE.name(); // CLOSED_WITHDRAWN ACTIVE APPROVED
		String offset = "0";
		String limit = "2";

		List<SavingsAccount> accounts = savingsService.getSavingsAccountsByBranchCentreOfficerState(branchId, centreId,
				creditOfficerUserName, accountState, offset, limit);

		System.out.println("Got Savings accounts for the branch, centre, officer, state, total Deposits="
				+ accounts.size());
		for (SavingsAccount account : accounts) {
			System.out.println("AccountsID=" + account.getId() + " " + account.getName() + "\tBranchId="
					+ account.getAssignedBranchKey() + "\tCentreId=" + account.getAssignedCentreKey()
					+ "\tCredit Officer=" + account.getAssignedUserKey());
			// Save one of the accounts for subsequent transaction testing
			SAVINGS_ACCOUNT_ID = (SAVINGS_ACCOUNT_ID == null && account.isActive()) ? account.getId()
					: SAVINGS_ACCOUNT_ID;
		}
		System.out.println("Saved savings Account ID=" + SAVINGS_ACCOUNT_ID);
	}

	// Savings Products
	public static void testGetSavingsProducts() throws MambuApiException {
		System.out.println("\nIn testGetSavingsProducts");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String offset = "1";
		String limit = "3";

		List<SavingsProduct> products = savingsService.getSavingsProducts(offset, limit);

		System.out.println("Got Savings products, count=" + products.size());

		if (products.size() > 0) {
			for (SavingsProduct product : products) {
				System.out.println("Product=" + product.getName() + "  Id=" + product.getId() + " Savings Type="
						+ product.getTypeOfProduct().name());
			}
		}

	}

	public static void testGetSavingsProductById() throws MambuApiException {
		System.out.println("\nIn testGetSavingsProductById");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String productId = demoSavingsProduct.getId(); // DSP FDS SP highInterest_001

		SavingsProduct product = savingsService.getSavingsProduct(productId);

		System.out.println("Product=" + product.getName() + "  Id=" + product.getId() + " Loan Type="
				+ product.getTypeOfProduct().name());

	}

	public static void testCreateSavingsAccount() throws MambuApiException {
		System.out.println("\nIn testCreateSavingsAccount");

		SavingsService service = MambuAPIFactory.getSavingsService();

		SavingsAccount savingsAccount = makeSavingsAccountForDemoProduct();

		// Add Custom Fields
		List<CustomFieldValue> clientCustomInformation = DemoUtil.makeForEntityCustomFieldValues(
				CustomField.Type.SAVINGS_ACCOUNT_INFO, demoSavingsProduct.getEncodedKey());
		//

		JSONSavingsAccount jsonSavingsAccount = new JSONSavingsAccount(savingsAccount);
		jsonSavingsAccount.setCustomInformation(clientCustomInformation);

		// Create Account in Mambu
		newAccount = service.createSavingsAccount(jsonSavingsAccount);
		SavingsAccount savingsAccountResult = newAccount.getSavingsAccount();

		SAVINGS_ACCOUNT_ID = savingsAccountResult.getId();

		System.out.println("Savings Account created OK, ID=" + savingsAccountResult.getId() + " Name= "
				+ savingsAccountResult.getName() + " Account Holder Key=" + savingsAccountResult.getAccountHolderKey());

		// Get Custom Information from the JSONSavingsAccount
		List<CustomFieldValue> updatedCustomFields = newAccount.getCustomInformation();

		if (updatedCustomFields != null) {
			System.out.println("Custom Fields for Account\n");
			for (CustomFieldValue value : updatedCustomFields) {
				System.out.println("CustomFieldKey=" + value.getCustomFieldKey() + "\tValue=" + value.getValue());

			}
		}
	}

	// Update Loan account
	public static void testUpdateSavingsAccount() throws MambuApiException {
		System.out.println("\nIn testUpdateSavingsAccount");

		SavingsService service = MambuAPIFactory.getSavingsService();

		// Use the newly created account and update some custom fields
		JSONSavingsAccount updatedAccount = newAccount;
		// Savings API doesn't return CustomField with the CustomFieldValue. Need to refresh account with full details
		SavingsAccount account = updatedAccount.getSavingsAccount();
		account = service.getSavingsAccountDetails(account.getEncodedKey());

		List<CustomFieldValue> customFields = account.getCustomFieldValues();
		if (customFields != null) {
			for (CustomFieldValue value : customFields) {
				value = DemoUtil.makeNewCustomFieldValue(value);
			}
		}
		updatedAccount.setCustomInformation(customFields);

		// Update account in Mambu
		JSONSavingsAccount updatedAccountResult = service.updateSavingsAccount(updatedAccount);

		System.out.println("Savings Update OK, ID=" + updatedAccountResult.getSavingsAccount().getId()
				+ "\tAccount Name=" + updatedAccountResult.getSavingsAccount().getName());

		// Get returned custom fields
		List<CustomFieldValue> updatedCustomFields = updatedAccountResult.getCustomInformation();

		if (updatedCustomFields != null) {
			System.out.println("Custom Fields for Loan Account\n");
			for (CustomFieldValue value : updatedCustomFields) {
				System.out.println("CustomField Key=" + value.getCustomFieldKey() + "\tValue=" + value.getValue());

			}
		}

	}

	public static void testApproveSavingsAccount() throws MambuApiException {
		System.out.println("\nIn test Approve Savings Account");

		SavingsService service = MambuAPIFactory.getSavingsService();

		SavingsAccount account = service
				.approveSavingsAccount(SAVINGS_ACCOUNT_ID, "Approve savings account demo notes");

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
		System.out.println("\nIn test Undo Approve Savings Account");

		SavingsService service = MambuAPIFactory.getSavingsService();
		String accountId = SAVINGS_ACCOUNT_ID;
		SavingsAccount account = service
				.undoApproveSavingsAccount(accountId, "UNDO Approve Savings account demo notes");

		System.out.println("UNDO Approved Savings account with id=" + account.getId() + "\tName=" + account.getName()
				+ "\tAccount State=" + account.getAccountState().toString());

	}

	public static void testDeleteSavingsAccount() throws MambuApiException {
		System.out.println("\nIn test Delete Savings Account");

		SavingsService service = MambuAPIFactory.getSavingsService();

		String accountId = SAVINGS_ACCOUNT_ID;
		boolean accountDeleted = service.deleteSavingsAccount(accountId);

		SAVINGS_ACCOUNT_ID = null;
		System.out.println("Deleted Savings account with id=" + accountId + "\tDeletion status=" + accountDeleted);
	}

	public static void testCloseSavingsAccount() throws MambuApiException {
		System.out.println("\nIn test Close Savings Account");

		SavingsService service = MambuAPIFactory.getSavingsService();

		String accountId = SAVINGS_ACCOUNT_ID;
		APIData.CLOSER_TYPE closerType = APIData.CLOSER_TYPE.WITHDRAW; // APIData.CLOSER_TYPE.WITHDRAW //
																		// APIData.CLOSER_TYPE.REJECT

		String notes = "Account Closed notes";

		SavingsAccount account = service.closeSavingsAccount(accountId, closerType, notes);

		System.out.println("Closed account id:" + account.getId() + "\tState=" + account.getAccountState().name());
	}

	public static void testGetDocuments() throws MambuApiException {
		System.out.println("\nIn testGetDocuments");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		List<Document> documents = savingsService.getSavingsAccountDocuments(demoSavingsAccount.getId());

		// Log returned documents using DemoTestDocumentsService helper
		System.out.println("Documents returned for a Savings Account with ID=" + demoSavingsAccount.getId());
		DemoTestDocumentsService.logDocuments(documents);

	}

	// Update Custom Field values for the Savings Account and delete the first available custom field
	public static void testUpdateDeleteCustomFields() throws MambuApiException {
		System.out.println("\nIn testUpdateDeleteCustomFields");

		List<CustomFieldValue> customFieldValues;
		System.out.println("\nUpdating demo Savings Account custom fields...");
		customFieldValues = updateCustomFields();

		System.out.println("\nDeleting first custom field for a demo Savings Account ...");
		deleteCustomField(customFieldValues);

	}

	// Private helper to Update all custom fields for a Savings Account
	private static List<CustomFieldValue> updateCustomFields() throws MambuApiException {

		Class<?> entityClass = SavingsAccount.class;
		String entityName = entityClass.getSimpleName();
		String entityId = demoSavingsAccount.getId();

		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		demoSavingsAccount = savingsService.getSavingsAccountDetails(entityId);

		// Get Current custom field values first for a Demo account
		List<CustomFieldValue> customFieldValues = demoSavingsAccount.getCustomFieldValues();

		if (customFieldValues == null || customFieldValues.size() == 0) {
			System.out.println("WARNING: No Custom fields defined for demo " + entityName + " with ID=" + entityId
					+ ". Nothing to update");
			return null;
		}

		// Update custom field values
		for (CustomFieldValue value : customFieldValues) {

			String fieldId = value.getCustomFieldId();
			// Create valid new value for a custom field
			String newValue = DemoUtil.makeNewCustomFieldValue(value).getValue();

			// Update Custom Field value
			boolean updateStatus = false;
			System.out.println("\nUpdating Custom Field with ID=" + fieldId + " for " + entityName + " with ID="
					+ entityId);

			updateStatus = savingsService.updateSavingsAccountCustomField(entityId, fieldId, newValue);

			String statusMessage = (updateStatus) ? "Success" : "Failure";
			System.out.println(statusMessage + " updating Custom Field, ID=" + fieldId + " for demo " + entityName
					+ " with ID=" + entityId + " New value=" + newValue);

		}

		return customFieldValues;
	}

	// Private helper to Delete the first custom field for a Savings Account
	private static void deleteCustomField(List<CustomFieldValue> customFieldValues) throws MambuApiException {

		Class<?> entityClass = SavingsAccount.class;
		String entityName = entityClass.getSimpleName();
		String entityId = demoSavingsAccount.getId();

		if (customFieldValues == null || customFieldValues.size() == 0) {
			System.out.println("WARNING: No Custom fields defined for demo " + entityName + " with ID=" + entityId
					+ ". Nothing to delete");
			return;
		}
		// Delete the first field on the list
		String customFieldId = customFieldValues.get(0).getCustomField().getId();

		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		boolean deleteStatus = savingsService.deleteSavingsAccountCustomField(entityId, customFieldId);

		String statusMessage = (deleteStatus) ? "Success" : "Failure";
		System.out.println(statusMessage + " deleting Custom Field, ID=" + customFieldId + " for demo " + entityName
				+ " with ID=" + entityId);
	}

	private static final String apiTestNamePrefix = "API Test Savings ";

	// Create demo savings account with parameters consistent with the demo product
	private static SavingsAccount makeSavingsAccountForDemoProduct() {
		System.out.println("\nIn makeSavingsAccountForDemoProduct for product name=" + demoSavingsProduct.getName()
				+ " id=" + demoSavingsProduct.getId());

		if (!demoSavingsProduct.isActivated()) {
			System.out.println("*** WARNING ***: demo product is NOT Active. Product name="
					+ demoSavingsProduct.getName() + " id=" + demoSavingsProduct.getId());
		}
		SavingsAccount savingsAccount = new SavingsAccount();
		savingsAccount.setId(null);

		savingsAccount.setProductTypeKey(demoSavingsProduct.getEncodedKey());
		SavingsType savingsType = demoSavingsProduct.getProductType();
		savingsAccount.setAccountType(savingsType);

		savingsAccount.setName(apiTestNamePrefix + new Date().getTime());
		savingsAccount.setAccountState(AccountState.PENDING_APPROVAL);

		boolean isForClient = demoSavingsProduct.isForIndividuals();
		String holderKey = (isForClient) ? demoClient.getEncodedKey() : demoGroup.getEncodedKey();
		AccountHolderType holderType = (isForClient) ? AccountHolderType.CLIENT : AccountHolderType.GROUP;
		savingsAccount.setAccountHolderKey(holderKey);
		savingsAccount.setAccountHolderType(holderType);
		// Max Withdrawal
		Money maxWidthdrawlAmount = demoSavingsProduct.getMaxWidthdrawlAmount();
		if (maxWidthdrawlAmount == null) {
			maxWidthdrawlAmount = new Money(300.00);
		}
		// Max deposit
		savingsAccount.setMaxWidthdrawlAmount(maxWidthdrawlAmount);
		Money recommendedDepositAmount = demoSavingsProduct.getRecommendedDepositAmount();
		if (recommendedDepositAmount == null && savingsType != SavingsType.FIXED_DEPOSIT) {
			recommendedDepositAmount = new Money(400.00);
		}
		savingsAccount.setRecommendedDepositAmount(recommendedDepositAmount);

		final long hundredDays = 100 * 24 * 60 * 60 * 1000; // 100 days
		// Overdraft params
		if (demoSavingsProduct.isAllowOverdraft()) {
			// Set Overdraft Amount
			savingsAccount.setAllowOverdraft(true);
			Money maxOverdraftLimit = demoSavingsProduct.getMaxOverdraftLimitMoney();
			maxOverdraftLimit = (maxOverdraftLimit != null) ? maxOverdraftLimit : new Money(120.00);
			savingsAccount.setOverdraftLimitMoney(maxOverdraftLimit);
			// Set Overdraft Interest rate
			InterestRateSettings overdraftRateSettings = demoSavingsProduct.getOverdraftInterestRateSettings();
			if (overdraftRateSettings == null) {
				overdraftRateSettings = new InterestRateSettings();
			}
			InterestRateSource rateSource = overdraftRateSettings.getInterestRateSource();

			BigDecimal defOerdraftInterestRate = overdraftRateSettings.getDefaultInterestRate();
			BigDecimal minOerdraftInterestRate = overdraftRateSettings.getMinInterestRate();
			BigDecimal maxOverdraftInterestRate = overdraftRateSettings.getMaxInterestRate();

			BigDecimal overdraftInterestRate = defOerdraftInterestRate;
			overdraftInterestRate = (overdraftInterestRate == null && minOerdraftInterestRate != null) ? minOerdraftInterestRate
					: overdraftInterestRate;
			overdraftInterestRate = (overdraftInterestRate == null && maxOverdraftInterestRate != null) ? maxOverdraftInterestRate
					: overdraftInterestRate;
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
		System.out.println("\nIn deleteTestAPISavingsAccounts");
		System.out.println("**  Deleting all Test Savings Accounts for Client =" + demoClient.getFullNameWithId()
				+ " **");
		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		List<SavingsAccount> accounts = savingsService.getSavingsAccountsForClient(demoClient.getId());
		if (accounts == null || accounts.size() == 0) {
			System.out.println("Nothing to delete for client " + demoClient.getFullNameWithId());
			return;
		}
		for (SavingsAccount account : accounts) {
			String name = account.getName();
			if (name.contains(apiTestNamePrefix)) {
				AccountState state = account.getAccountState();
				if (state == AccountState.PENDING_APPROVAL || state == AccountState.APPROVED) {
					String id = account.getId();
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
}
