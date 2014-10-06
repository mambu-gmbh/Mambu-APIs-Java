package demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.mambu.accounts.shared.model.AccountHolderType;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.LoanAccountExpanded;
import com.mambu.apisdk.services.LoansService;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.Money;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;
import com.mambu.loans.shared.model.Guaranty;
import com.mambu.loans.shared.model.Guaranty.GuarantyType;
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

			demoClient = DemoUtil.getDemoClient();
			demoGroup = DemoUtil.getDemoGroup();
			demoProduct = DemoUtil.getDemoLoanProduct();
			demoUser = DemoUtil.getDemoUser();
			demoLoanAccount = DemoUtil.getDemoLoanAccount();
			LOAN_ACCOUNT_ID = demoLoanAccount.getId();

			testCreateJsonAccount();

			testApproveLoanAccount();
			testUndoApproveLoanAccount();
			testGetLoanAccountDetails();
			testUpdateLoanAccount();

			testGetLoanProducts();

			testGetLoanAccount();
			testGetLoanAccountDetails();

			testGetLoanAccountsForClient();
			testGetLoanAccountsForGroup();

			testGetLoanAccountsByBranchCentreOfficerState();
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

			testRejectLoanAccount();

			// Available since 3.5
			testUndoApproveLoanAccount();

			// Available since 3.6
			testLockLoanAccount();
			testUnlockLoanAccount();

			testDeleteLoanAccount();

			// Available since Mambu 3.6
			testGetDocuments();

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
		String accountId = LOAN_ACCOUNT_ID; // LOAN_ACCOUNT_ID NEW_LOAN_ACCOUNT_ID
		System.out.println("Got loan account by ID with details: "
				+ loanService.getLoanAccountDetails(accountId).getName());

	}

	// Create Loan Account
	public static void testCreateJsonAccount() throws MambuApiException {
		System.out.println("\nIn testCreateJsonAccount");

		LoansService loanService = MambuAPIFactory.getLoanService();

		LoanAccount account = new LoanAccount();
		account.setId(null);
		account.setAccountHolderKey(demoClient.getEncodedKey()); // CLIENT_ID
		account.setAccountHolderType(AccountHolderType.CLIENT);

		account.setProductTypeKey(demoProduct.getEncodedKey()); //

		// The required fields below depend on the selected product type
		account.setLoanAmount(new Money(5500.00));
		account.setInterestRate(new BigDecimal("3.2"));
		account.setRepaymentInstallments(20);
		account.setGracePeriod(1);
		// From Product
		account.setRepaymentPeriodUnit(RepaymentPeriodUnit.DAYS);
		account.setRepaymentPeriodCount(1);

		// Set Custom fields to null in the account
		account.setCustomFieldValues(null);
		// ADd Custom Fields

		List<CustomFieldValue> clientCustomInformation = new ArrayList<CustomFieldValue>();

		CustomFieldValue custField1 = new CustomFieldValue();
		String customFieldId = "Loan_Purpose_Loan_Accounts";
		String customFieldValue = "My Loan_Purpose";

		custField1.setCustomFieldId(customFieldId);
		custField1.setValue(customFieldValue);
		// Add new field to the list
		clientCustomInformation.add(custField1);
		// Field #2
		// Loan_Originator_Loan_Accounts
		CustomFieldValue custField2 = new CustomFieldValue();
		customFieldId = "Loan_Originator_Loan_Accounts";
		customFieldValue = "Bank";

		custField2.setCustomFieldId(customFieldId);
		custField2.setValue(customFieldValue);
		// Add new field to the list
		clientCustomInformation.add(custField2);

		// Set Guarantees
		ArrayList<Guaranty> guarantees = new ArrayList<Guaranty>();
		// GuarantyType.GUARANTOR
		Guaranty guarantySecurity = new Guaranty(GuarantyType.GUARANTOR);
		guarantySecurity.setAmount(new Money((double) 450.0));
		guarantySecurity.setGuarantorKey("8ad661123b36cfaf013b42c9545abd23");
		guarantySecurity.setSavingsAccountKey("8ad661123b36cfaf013b42c954566dd1");
		guarantees.add(guarantySecurity);

		// GuarantyType.ASSET
		Guaranty guarantyAsset = new Guaranty(GuarantyType.ASSET);
		guarantyAsset.setAssetName("Asset Name as a collateral");
		guarantyAsset.setAmount(new Money((double) 180.0));
		guarantees.add(guarantyAsset);

		// Add all guarantees to Loan account
		account.setGuarantees(guarantees);

		// Create Account Expanded
		LoanAccountExpanded accountExpanded = new LoanAccountExpanded();
		accountExpanded.setLoanAccount(account);
		accountExpanded.setCustomInformation(clientCustomInformation);

		// Create Account in Mambu
		newAccount = loanService.createLoanAccount(accountExpanded);

		// accented E

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
				System.out.println("CustomFieldKey" + value.getCustomFieldKey() + "\tValue" + value.getValue()
						+ "\tName" + value.getCustomField().getName());

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
		String customFieldIdToModifyValue = "Loan_Purpose_Loan_Accounts";
		if (customFields != null) {

			for (CustomFieldValue value : customFields) {
				CustomField field = value.getCustomField();
				String fieldId = field.getId();

				if (fieldId.equals(customFieldIdToModifyValue)) {
					// Update the value for this field
					value.setValue("Value updated by testUpdateLoanAccount");
				}
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
				System.out.println("CustomFieldKey" + value.getCustomFieldKey() + "\tValue" + value.getValue()
						+ "\tName" + value.getCustomField().getName());

			}
		}

	}

	// / Transactions testing
	public static void testDisburseLoanAccountWithDetails() throws MambuApiException {
		System.out.println("\nIn test Disburse LoanAccount with Deatils");

		LoansService loanService = MambuAPIFactory.getLoanService();

		String amount = "10000.00";
		String accountId = NEW_LOAN_ACCOUNT_ID;
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
		String offest = "0";
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

		String accountId = LOAN_ACCOUNT_ID;
		LoanTransaction transaction = loanService.makeLoanRepayment(accountId, amount, date, notes, paymentMethod,
				receiptNumber, bankNumber, checkNumber, bankAccountNumber, bankRoutingNumber);

		System.out.println("repayed loan account with the " + accountId + " id response="
				+ transaction.getTransactionId() + "   for amount=" + transaction.getAmount());
	}

	public static void testApplyFeeToLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Applying Fee to a Loan Account");

		LoansService loanService = MambuAPIFactory.getLoanService();
		String amount = "10";
		String repaymentNumber = "100";
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

		LoanAccount account = loanService.approveLoanAccount(LOAN_ACCOUNT_ID, "some demo notes");

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

		final String accountId = LOAN_ACCOUNT_ID;
		LoanAccount account = loanService.undoApproveLoanAccount(accountId, "some undo approve demo notes");

		System.out.println("Undo Approving loan account with the " + accountId + " Loan name " + account.getLoanName()
				+ "  Account State=" + account.getState().toString());
	}

	public static void testLockLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Lock LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String accountId = LOAN_ACCOUNT_ID;
		LoanTransaction transaction = loanService.lockLoanAccount(accountId, "some lock demo notes");

		System.out.println("Locked account with ID " + accountId + " Transaction  " + transaction.getTransactionId()
				+ " Type=" + transaction.getType() + "  Balance=" + transaction.getBalance());
	}

	public static void testUnlockLoanAccount() throws MambuApiException {
		System.out.println("\nIn test Unlock LoanAccount");
		LoansService loanService = MambuAPIFactory.getLoanService();

		String accountId = LOAN_ACCOUNT_ID;
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
}
