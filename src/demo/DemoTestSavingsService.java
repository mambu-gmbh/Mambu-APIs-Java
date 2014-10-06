package demo;

import java.util.ArrayList;
import java.util.List;

import com.mambu.accounts.shared.model.AccountState;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.util.APIData;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldValue;
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
			demoClient = DemoUtil.getDemoClient();
			demoGroup = DemoUtil.getDemoGroup();
			demoUser = DemoUtil.getDemoUser();
			demoSavingsProduct = DemoUtil.getDemoSavingsProduct();
			demoSavingsAccount = DemoUtil.getDemoSavingsAccount();
			SAVINGS_ACCOUNT_ID = demoSavingsAccount.getId();

			testCreateSavingsAccount();

			// Available since 3.4
			testUpdateSavingsAccount();

			testApproveSavingsAccount();

			testGetSavingsAccount();
			testGetSavingsAccountDetails();

			// Available since 3.5
			testUndoApproveSavingsAccount();

			// Available since 3.4
			testCloseSavingsAccount();
			testDeleteSavingsAccount();

			testGetSavingsAccountsByBranchCentreOfficerState();

			testGetSavingsAccountsForClient();

			testDepositToSavingsAccount();
			testWithdrawalFromSavingsAccount();

			testTransferFromSavingsAccount();

			// Available since 3.6
			testApplyFeeToSavingsAccount();

			testGetSavingsAccountTransactions();

			testGetSavingsAccountsForGroup();

			testGetSavingsProducts();
			testGetSavingsProductById();

			// Available since Mambu 3.6
			testGetDocuments();

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
	public static void testWithdrawalFromSavingsAccount() throws MambuApiException {
		System.out.println("\nIn testWithdrawalFromSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		String amount = "93.55";
		String date = null;
		String notes = "Withdrawal notes from API";
		String paymentMethod = "CASH";// CHECK,
		String receiptNumber = "REC_NUMBER_1123";
		String bankNumber = "BANK_NUMBER_345";
		String checkNumber = "CHECK_NUMBER_9900";
		String bankAccountNumber = "BANK_ACCT_NUMB_4567";
		String bankRoutingNumber = "BNK_ROUT_NUMBER_2344";

		SavingsTransaction transaction = savingsService.makeWithdrawal(SAVINGS_ACCOUNT_ID, amount, date, notes,
				paymentMethod, receiptNumber, bankNumber, checkNumber, bankAccountNumber, bankRoutingNumber);

		System.out
				.println("Made Withdrawal from Savings for account with the " + SAVINGS_ACCOUNT_ID + " id:"
						+ ". Amount=" + transaction.getAmount().toString() + " Balance ="
						+ transaction.getBalance().toString());

	}

	// Deposit
	public static void testDepositToSavingsAccount() throws MambuApiException {
		System.out.println("\nIn testDepositToSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		String amount = "150.00";
		String date = null;
		String notes = "Deposit notes - API";

		String paymentMethod = "CASH";// CHECK,
		String receiptNumber = null; // "REC_NUMBER_1123";
		String bankNumber = null; // "BANK_NUMBER_345";
		String checkNumber = null; // CHECK_NUMBER_9900";
		String bankAccountNumber = null;// "BANK_ACCT_NUMB_4567";
		String bankRoutingNumber = null;// "BNK_ROUT_NUMBER_2344";

		SavingsTransaction transaction = savingsService.makeDeposit(SAVINGS_ACCOUNT_ID, amount, date, notes,
				paymentMethod, receiptNumber, bankNumber, checkNumber, bankAccountNumber, bankRoutingNumber);

		System.out.println("Made Deposit To Savings for account with the " + SAVINGS_ACCOUNT_ID + " id:" + ". Amount="
				+ transaction.getAmount().toString() + " Balance =" + transaction.getBalance().toString());

	}

	public static void testTransferFromSavingsAccount() throws MambuApiException {
		System.out.println("\nIn testTransferFromSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String destinationAccountKey = DemoUtil.getDemoLoanAccount().getId();

		String amount = "20.50";
		String notes = "Transfer notes from API";

		APIData.ACCOUNT_TYPE accountType = APIData.ACCOUNT_TYPE.LOAN;

		SavingsTransaction transaction = savingsService.makeTransfer(SAVINGS_ACCOUNT_ID, destinationAccountKey,
				accountType, amount, notes);

		System.out.println("Transfer From account:" + SAVINGS_ACCOUNT_ID + "   To account id=" + destinationAccountKey
				+ "Amount=" + transaction.getAmount().toString() + " Transac Id=" + transaction.getTransactionId());

	}

	// Apply Arbitrary Fee. Available since 3.6
	public static void testApplyFeeToSavingsAccount() throws MambuApiException {
		System.out.println("\nIn testApplyFeeToSavingsAccount");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		String amount = "45.00";
		String notes = "Apply Fee to savings via API notes";

		System.out.println("Demo Savings account " + demoSavingsAccount.getName() + " with Id="
				+ demoSavingsAccount.getId());
		String accountId = demoSavingsAccount.getId();
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

		SavingsAccount savingsAccount = new SavingsAccount();
		savingsAccount.setProductTypeKey(demoSavingsProduct.getEncodedKey());
		savingsAccount.setId(null);
		savingsAccount.setAccountState(AccountState.PENDING_APPROVAL);
		savingsAccount.setName("My Savings Account");
		savingsAccount.setAccountType(SavingsType.SAVINGS_PLAN);
		savingsAccount.setClientAccountHolderKey(demoClient.getEncodedKey());

		// Add Custom Fields
		List<CustomFieldValue> clientCustomInformation = new ArrayList<CustomFieldValue>();

		CustomFieldValue custField1 = new CustomFieldValue();
		custField1.setCustomFieldId("Target_Deposit_Accounts");
		custField1.setValue("Target Deposit Accounts Value");
		clientCustomInformation.add(custField1);

		CustomFieldValue custField2 = new CustomFieldValue();
		custField2.setCustomFieldId("Required_Deposit_Accounts_2");
		custField2.setValue("Required_Deposit_Accounts_2 Value");
		clientCustomInformation.add(custField2);

		CustomFieldValue custField3 = new CustomFieldValue();
		custField3.setCustomFieldId("Required_Deposit_Accounts");
		custField3.setValue("Required_Deposit_Accounts Value");
		clientCustomInformation.add(custField3);
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
				System.out.println("CustomFieldKey" + value.getCustomFieldKey() + "\tValue" + value.getValue()
						+ "\tName" + value.getCustomField().getName());

			}
		}
	}

	// Update Loan account
	public static void testUpdateSavingsAccount() throws MambuApiException {
		System.out.println("\nIn testUpdateSavingsAccount");

		SavingsService service = MambuAPIFactory.getSavingsService();

		// Use the newly created account and update some custom fields
		JSONSavingsAccount updatedAccount = newAccount;
		List<CustomFieldValue> customFields = updatedAccount.getCustomInformation();
		String customFieldIdToModifyValue = "Target_Deposit_Accounts";
		if (customFields != null) {

			for (CustomFieldValue value : customFields) {
				CustomField field = value.getCustomField();
				String fieldId = field.getId();

				if (fieldId.equals(customFieldIdToModifyValue)) {
					// Update the value for this field
					value.setValue("Value updated by testUpdateSavingsAccount");
				}
			}
		}

		// Update account in Mambu
		JSONSavingsAccount updatedAccountResult = service.updateSavingsAccount(updatedAccount);

		System.out.println("Savings Update OK, ID=" + updatedAccountResult.getSavingsAccount().getId()
				+ "\tAccount Name=" + updatedAccountResult.getSavingsAccount().getName());

		// Get returned custom fields
		List<CustomFieldValue> updatedCustomFields = updatedAccountResult.getCustomInformation();

		if (updatedCustomFields != null) {
			System.out.println("Custom Fields for Loan Account\n");
			for (CustomFieldValue value : updatedCustomFields) {
				System.out.println("CustomFieldKey" + value.getCustomFieldKey() + "\tValue" + value.getValue()
						+ "\tName" + value.getCustomField().getName());

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
				System.out.println("CustomFieldKey" + value.getCustomFieldKey() + "\tValue" + value.getValue()
						+ "\tName" + value.getCustomField().getName());

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
}
