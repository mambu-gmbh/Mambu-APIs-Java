package demo;

import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.util.APIData;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;
import com.mambu.savings.shared.model.SavingsTransaction;

/**
 * Test class to show example usage of the api calls
 * 
 * @author manilkis
 * 
 */
public class DemoTestSavingsService {

	private static String CLIENT_ID = "548919675"; // 282600987 046360136

	private static String GROUP_ID = "118035060"; // 842485684 414659806
	private static String SAVINGS_ACCOUNT_ID = "XTTO550"; // ILRP761 SHAS854 UBUV290
	private static String TO_LOAN_ACCOUNT_ID = "ZKII792"; // XXBE600 TPXA949 QBDP300 SEFY187

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testGetSavingsAccount();
			testGetSavingsAccountDetails();

			testGetSavingsAccountsByBranchOfficerState();

			testGetSavingsAccountsForClient();

			testDepositToSavingsAccount();
			testWithdrawalFromSavingsAccount();

			testTransferFromSavingsAccount();

			testGetSavingsAccountTransactions();

			testGetSavingsAccountsForGroup();

			testGetSavingsProducts();
			testGetSavingsProductById();

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

		List<SavingsAccount> savingsAccounts = savingsService.getSavingsAccountsForClient(CLIENT_ID);

		System.out.println("Got Savings accounts for the client with the " + CLIENT_ID + " id, Total="
				+ savingsAccounts.size());
		for (SavingsAccount account : savingsAccounts) {
			System.out.print(account.getName() + " ");
		}
		System.out.println();
	}

	public static void testGetSavingsAccountsForGroup() throws MambuApiException {
		System.out.println("\nIn testGetSavingsAccountsFor Group");
		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		List<SavingsAccount> savingsAccounts = savingsService.getSavingsAccountsForGroup(GROUP_ID);

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
		String notes = "Withdrawal notes - API";

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

		// String destinationAccountKey = "8ad661123b36cfaf013b43b1ab5f3e77"; // Loan OAMT736 Irina Chernaya;
		String destinationAccountKey = TO_LOAN_ACCOUNT_ID;

		String amount = "20.50";
		String notes = "Transfer notes from API";

		APIData.ACCOUNT_TYPE accountType = APIData.ACCOUNT_TYPE.LOAN;

		SavingsTransaction transaction = savingsService.makeTransfer(SAVINGS_ACCOUNT_ID, destinationAccountKey,
				accountType, amount, notes);

		System.out.println("Transfer From account:" + SAVINGS_ACCOUNT_ID + "   To account id=" + destinationAccountKey
				+ "Amount=" + transaction.getAmount().toString() + " Transac Id=" + transaction.getTransactionId());

	}
	public static void testGetSavingsAccountsByBranchOfficerState() throws MambuApiException {
		System.out.println("\nIn testGetSavingsAccountsByBranchOfficerState");

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		String branchId = "GBK 001"; // "RICHMOND_001"; // Berlin_001 RICHMOND_001 GBK 001
		String creditOfficerUserName = null;// "demo";
		String accountState = null; // "ACTIVE"; // CLOSED_WITHDRAWN ACTIVE APPROVED
		String offset = "1";
		String limit = "2";

		List<SavingsAccount> accounts = savingsService.getSavingsAccountsByBranchOfficerState(branchId,
				creditOfficerUserName, accountState, offset, limit);

		System.out.println("Got Savings accounts for the branch, officer, state, total Deposits=" + accounts.size());
		for (SavingsAccount account : accounts) {
			System.out.println("AccountsID=" + account.getId() + " " + account.getName() + "  BranchId="
					+ account.getAssignedBranchKey() + "   Credit Officer=" + account.getAssignedUserKey());
		}
		System.out.println();
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

		String productId = "DSP"; // DSP FDS SP highInterest_001

		SavingsProduct product = savingsService.getSavingsProduct(productId);

		System.out.println("Product=" + product.getName() + "  Id=" + product.getId() + " Loan Type="
				+ product.getTypeOfProduct().name());

	}
}
