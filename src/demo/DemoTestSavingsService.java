package demo;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.mambu.apisdk.services.DocumentsService;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.CustomFieldType;
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

			demoSavingsAccount = DemoUtil.getDemoSavingsAccount(testAccountId);
			SAVINGS_ACCOUNT_ID = demoSavingsAccount.getId();

			SavingsType productTypes[];
			boolean productTypesTesting;
			// If demoSavingsProductId configuration set to ALL then test all available product types
			if (testProductId != null && testProductId.equals(DemoUtil.allProductTypes)) {
				productTypesTesting = true;
				// Run tests for all products types, selecting random active product ID of each type once
				productTypes = SavingsType.values();
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
				demoSavingsProduct = (productTypesTesting) ? DemoUtil.getDemoSavingsProduct(productType) : DemoUtil
						.getDemoSavingsProduct(testProductId);

				if (demoSavingsProduct == null) {
					continue;
				}
				System.out.println("Product Id=" + demoSavingsProduct.getId() + " Name=" + demoSavingsProduct.getName()
						+ " ***");

				try {

					testCreateSavingsAccount();
					testPatchSavingsAccountTerms(); // Available since 3.12.2
					testCloseSavingsAccount(); // Available since 3.4
					testDeleteSavingsAccount(); // Available since 3.4

					testCreateSavingsAccount();

					testUpdateSavingsAccount(); // Available since 3.4
					testPatchSavingsAccountTerms(); // Available since 3.12.2

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
					System.out.println("*** Exception *** " + methodName + " " + e.getMessage());
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

	public static void testGetSavingsAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		SavingsAccount account = savingsService.getSavingsAccount(SAVINGS_ACCOUNT_ID);

		System.out.println("Got Savings account: " + account.getName());

	}

	public static void testGetSavingsAccountDetails() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetSavingsAccount with Details");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		SavingsAccount account = savingsService.getSavingsAccountDetails(SAVINGS_ACCOUNT_ID);

		System.out.println("Got Savings account: " + account.getName());

	}

	public static void testGetSavingsAccountsForClient() throws MambuApiException {
		System.out.println(methodName = "\nIn testGetSavingsAccountsFor Client");
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
		System.out.println(methodName = "\nIn testGetSavingsAccountsFor Group");
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
		System.out.println(methodName = "\nIn testGetSavingsAccountTransactions");

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
		System.out.println(methodName = "\nIn testWithdrawalFromSavingsAccount");

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
		System.out.println(methodName = "\nIn testDepositToSavingsAccount");

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
		System.out.println(methodName = "\nIn testTransferFromSavingsAccount");

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
		System.out.println(methodName = "\nIn testReverseSavingsAccountTransaction");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String notes = "Reversed by Demo API";
		SavingsTransaction reversed = savingsService.reverseSavingsTransaction(transaction, notes);

		System.out.println("Reversed Transaction=" + transaction.getType() + "\tReversed Amount="
				+ reversed.getAmount().toString() + "\tBalance =" + reversed.getBalance().toString()
				+ "Transaction Type=" + reversed.getType() + "\tAccount key=" + reversed.getParentAccountKey());
	}

	// Apply Arbitrary Fee. Available since 3.6
	public static void testApplyFeeToSavingsAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn testApplyFeeToSavingsAccount");

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
		System.out.println(methodName = "\nIn testGetSavingsProducts");

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
		System.out.println(methodName = "\nIn testGetSavingsProductById");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String productId = demoSavingsProduct.getId(); // DSP FDS SP highInterest_001

		SavingsProduct product = savingsService.getSavingsProduct(productId);

		System.out.println("Product=" + product.getName() + "  Id=" + product.getId() + " Loan Type="
				+ product.getTypeOfProduct().name());

	}

	public static void testCreateSavingsAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn testCreateSavingsAccount");

		SavingsService service = MambuAPIFactory.getSavingsService();

		SavingsAccount savingsAccount = makeSavingsAccountForDemoProduct();

		// Add Custom Fields
		List<CustomFieldValue> clientCustomInformation = DemoUtil.makeForEntityCustomFieldValues(
				CustomFieldType.SAVINGS_ACCOUNT_INFO, demoSavingsProduct.getEncodedKey());
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
		System.out.println(methodName = "\nIn testUpdateSavingsAccount");

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

	// Test Patch Savings account terms API
	public static void testPatchSavingsAccountTerms() throws MambuApiException {
		System.out.println(methodName = "\nIn testPatchSavingsAccountTerms");

		// See MBU-10447 for a list of fields that can be updated (as of Mambu 3.14)
		SavingsAccount savingsAccount = demoSavingsAccount;
		String productKey = savingsAccount.getProductTypeKey();
		SavingsProduct product = DemoUtil.getDemoSavingsProduct(productKey);
		SavingsType productType = product.getProductType();
		System.out.println("\tProduct=" + product.getName() + "\tType=" + productType + "\tId=" + product.getId());

		// Update account
		InterestRateSettings overdraftRateSettings = product.getOverdraftInterestRateSettings();
		InterestRateSource overdraftRateSource = (overdraftRateSettings == null) ? null : overdraftRateSettings
				.getInterestRateSource();

		// Update overdraft fields
		if (product.isAllowOverdraft() && overdraftRateSource != null) {
			// get MaxInterestRat to limit our test changes
			BigDecimal maxOverdraftRate = overdraftRateSettings.getMaxInterestRate();
			final BigDecimal rateIncrease = new BigDecimal(0.5f);
			final BigDecimal limitIncrease = new BigDecimal(400.00f);

			switch (overdraftRateSource) {
			case FIXED_INTEREST_RATE:
				// Set new Overdraft Limit
				BigDecimal overdraftLimit = savingsAccount.getOverdraftLimit() != null ? savingsAccount
						.getOverdraftLimit() : BigDecimal.ZERO;
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
				BigDecimal overdraftInterestRate = savingsAccount.getOverdraftInterestRate() != null ? savingsAccount
						.getOverdraftInterestRate() : BigDecimal.ZERO;
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
				BigDecimal rateSpread = savingsAccount.getOverdraftInterestSpread() != null ? savingsAccount
						.getOverdraftInterestSpread() : BigDecimal.ZERO;
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
			// Set overdraftLimit to null. Mmabu's default is "0" and this causes PATCH API to fail
			savingsAccount.setOverdraftLimit(null);
		}

		// interestRate. If the product has Interest Paid into Account checked
		if (product.isInterestPaidIntoAccount() && product.getInterestRateSettings() != null) {
			InterestRateSettings rateSettings = product.getInterestRateSettings();
			BigDecimal rate = savingsAccount.getInterestRate() != null ? savingsAccount.getInterestRate()
					: BigDecimal.ZERO;
			// Increase by the test amount and limit to the max allowed
			BigDecimal rateIncrease = new BigDecimal(0.55);
			rate = rate.add(rateIncrease).setScale(2, RoundingMode.DOWN);
			BigDecimal maxRate = rateSettings.getMaxInterestRate();
			if (maxRate != null) {
				rate = rate.min(maxRate);
			}
			// Set new Interest Rate
			System.out.println("New Interest rate=" + rate);
			savingsAccount.setInterestRate(rate);

		}
		// Modify MaxWidthdrawalAmount
		final BigDecimal increaseMaxWithdrawl = new BigDecimal(50.00f);
		Money currentMaxWidthdrawlAmount = savingsAccount.getMaxWidthdrawlAmount() != null ? savingsAccount
				.getMaxWidthdrawlAmount() : Money.zero();
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
		SavingsService service = MambuAPIFactory.getSavingsService();
		boolean status = service.patchSavingsAccount(savingsAccount);
		System.out.println("Patched savings account status=" + status);
	}

	public static void testApproveSavingsAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn test Approve Savings Account");

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
		System.out.println(methodName = "\nIn test Undo Approve Savings Account");

		SavingsService service = MambuAPIFactory.getSavingsService();
		String accountId = SAVINGS_ACCOUNT_ID;
		SavingsAccount account = service
				.undoApproveSavingsAccount(accountId, "UNDO Approve Savings account demo notes");

		System.out.println("UNDO Approved Savings account with id=" + account.getId() + "\tName=" + account.getName()
				+ "\tAccount State=" + account.getAccountState().toString());

	}

	public static void testDeleteSavingsAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn test Delete Savings Account");

		SavingsService service = MambuAPIFactory.getSavingsService();

		String accountId = SAVINGS_ACCOUNT_ID;
		boolean accountDeleted = service.deleteSavingsAccount(accountId);

		SAVINGS_ACCOUNT_ID = null;
		System.out.println("Deleted Savings account with id=" + accountId + "\tDeletion status=" + accountDeleted);
	}

	public static void testCloseSavingsAccount() throws MambuApiException {
		System.out.println(methodName = "\nIn test Close Savings Account");

		SavingsService service = MambuAPIFactory.getSavingsService();

		String accountId = SAVINGS_ACCOUNT_ID;
		APIData.CLOSER_TYPE closerType = APIData.CLOSER_TYPE.WITHDRAW; // APIData.CLOSER_TYPE.WITHDRAW //
																		// APIData.CLOSER_TYPE.REJECT

		String notes = "Account Closed notes";

		SavingsAccount account = service.closeSavingsAccount(accountId, closerType, notes);

		System.out.println("Closed account id:" + account.getId() + "\tState=" + account.getAccountState().name());
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
		DemoTestCustomFiledValueService.testUpdateDeleteCustomFields(MambuEntityType.SAVINGS_ACCOUNT);

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
		final long time = new Date().getTime();
		savingsAccount.setId("API-" + time);
		savingsAccount.setName(demoSavingsProduct.getName());
		savingsAccount.setProductTypeKey(demoSavingsProduct.getEncodedKey());

		SavingsType savingsType = demoSavingsProduct.getProductType();
		savingsAccount.setAccountType(savingsType);
		savingsAccount.setAccountState(AccountState.PENDING_APPROVAL);

		boolean isForClient = demoSavingsProduct.isForIndividuals();
		String holderKey = (isForClient) ? demoClient.getEncodedKey() : demoGroup.getEncodedKey();
		AccountHolderType holderType = (isForClient) ? AccountHolderType.CLIENT : AccountHolderType.GROUP;
		savingsAccount.setAccountHolderKey(holderKey);
		savingsAccount.setAccountHolderType(holderType);

		// Set Interest rate. required since Mambu 3.13. See MBU-9806
		InterestRateSettings rateSettings = demoSavingsProduct.getInterestRateSettings();
		if (rateSettings == null) {
			rateSettings = new InterestRateSettings();
		}
		BigDecimal interestRate = null;
		// Set interest rate only if it is paid into the account
		if (demoSavingsProduct.isInterestPaidIntoAccount()) {
			BigDecimal defInterestRate = rateSettings.getDefaultInterestRate();
			BigDecimal minInterestRate = rateSettings.getMinInterestRate();
			BigDecimal maxInterestRate = rateSettings.getMaxInterestRate();
			interestRate = defInterestRate;
			interestRate = (interestRate != null) ? interestRate : minInterestRate;
			interestRate = (interestRate != null) ? interestRate : maxInterestRate;
			if (interestRate == null) {
				interestRate = new BigDecimal(3.5f);
			}
		}
		savingsAccount.setInterestRate(interestRate);

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

			BigDecimal defOverdraftInterestRate = overdraftRateSettings.getDefaultInterestRate();
			BigDecimal minOverdraftInterestRate = overdraftRateSettings.getMinInterestRate();
			BigDecimal maxOverdraftInterestRate = overdraftRateSettings.getMaxInterestRate();

			BigDecimal overdraftInterestRate = defOverdraftInterestRate;
			overdraftInterestRate = (overdraftInterestRate == null && minOverdraftInterestRate != null) ? minOverdraftInterestRate
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
		System.out.println(methodName = "\nIn deleteTestAPISavingsAccounts");
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
