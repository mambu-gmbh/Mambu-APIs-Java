package demo;

import java.util.Date;
import java.util.List;

import com.mambu.accounts.shared.model.Account.Type;
import com.mambu.accounts.shared.model.DocumentTemplate;
import com.mambu.accounts.shared.model.DocumentTemplateType;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.DocumentTemplatesService;
import com.mambu.apisdk.services.LoansService;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.util.DateUtils;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanTransaction;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;
import com.mambu.savings.shared.model.SavingsTransaction;

/**
 * Test class to show example usage for Document Templates API
 * 
 * @author mdanilkis
 * 
 */
public class DemoTestDocumentTemplatesService {

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			// Test getting account and transaction templates for Loans
			testGetLoanAccountTemplates(); // Available since 3.14

			// Test getting account and transaction templates for Savings
			testGetSavingsAccountTemplates(); // Available since 3.14

		} catch (MambuApiException e) {
			System.out.println("Exception caught in DemoTestDocumentTemplatesService");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	// Test getting populated document templates for Loan account
	public static void testGetLoanAccountTemplates() throws MambuApiException {
		System.out.println("\nIn testGetLoanAccountTemplates");
		// Get Demo data needed for test: accounts, product templates and transaction to test
		// Get loan account for a loan product with document templates
		String testAccountId = DemoUtil.demoLaonAccountId;
		LoanAccount loanAccount = DemoUtil.getDemoLoanAccount(testAccountId);
		if (loanAccount == null) {
			System.out.println("WARNING: no demo loan account to test, ID=" + testAccountId);
			return;
		}
		String accountId = loanAccount.getId();
		System.out.println("Testing Loan with Id=" + accountId);
		// Get product for the account. Test Product should have some templates defined
		LoanProduct loanProduct = DemoUtil.getDemoLoanProduct(loanAccount.getProductTypeKey());

		// Get transaction to test
		LoansService loanService = MambuAPIFactory.getLoanService();
		List<LoanTransaction> loanTransactions = loanService.getLoanAccountTransactions(loanAccount.getId(), "0", "5");
		String transactionId = null;
		if (loanTransactions != null && loanTransactions.size() > 0) {
			transactionId = loanTransactions.get(0).getEncodedKey();
		}

		// Test Loan templates with the demo data
		List<DocumentTemplate> loanTemplates = loanProduct.getTemplates();
		testAccountDocumentTemplates(Type.LOAN, accountId, loanTemplates, transactionId);

	}

	// Test getting populated document templates for Savings account
	public static void testGetSavingsAccountTemplates() throws MambuApiException {
		System.out.println("\nIn testGetSavingsAccountTemplates");
		// Get Demo data needed for test: accounts, product templates and transaction to test
		// Get savings account for a savings product with document templates
		String testAccountId = DemoUtil.demoSavingsAccountId;
		SavingsAccount savingsAccount = DemoUtil.getDemoSavingsAccount(testAccountId);

		if (savingsAccount == null) {
			System.out.println("WARNING: no demo savings account to test, ID=" + testAccountId);
			return;
		}
		String accountId = savingsAccount.getId();
		System.out.println("Testing Savings with Id=" + accountId);
		// Get product for the account. Test Product should have some templates defined
		SavingsProduct savingsProduct = DemoUtil.getDemoSavingsProduct(savingsAccount.getProductTypeKey());
		// Get transaction to test
		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		List<SavingsTransaction> savingsTransactions = savingsService.getSavingsAccountTransactions(
				savingsAccount.getId(), "0", "5");
		String transactionId = null;
		if (savingsTransactions != null && savingsTransactions.size() > 0) {
			transactionId = savingsTransactions.get(0).getEncodedKey();
		}
		// Test Savings templates with the demo data
		List<DocumentTemplate> savingsTemplates = savingsProduct.getTemplates();
		testAccountDocumentTemplates(Type.SAVINGS, accountId, savingsTemplates, transactionId); // Available since 3.14

	}

	/**
	 * Private helper to test getting populated document templates for accounts and transactions
	 * 
	 * @param accountType
	 *            account type
	 * @param accountId
	 *            account id
	 * @param templates
	 *            document templates available for the account's product
	 * @param transactionId
	 *            transaction ID to test API for a transaction
	 * @throws MambuApiException
	 */
	private static void testAccountDocumentTemplates(Type accountType, String accountId,
			List<DocumentTemplate> templates, String transactionId) throws MambuApiException {
		if (templates == null || templates.size() == 0) {
			System.out.println("WARNING: no templates exists for " + accountType + "\tAccount Id=" + accountId);
			return;
		}
		System.out.println("Testing " + accountType + "\tId=" + accountId + "\tTransaction Id=" + transactionId);
		DocumentTemplatesService service = MambuAPIFactory.getDocumentTemplatesService();

		// Test getting documents for the available templates
		for (DocumentTemplate template : templates) {
			String templateKey = template.getEncodedKey();
			DocumentTemplateType type = template.getType();
			String templateName = template.getName();
			System.out.println("\nTemplate Type=" + type + "\tKey=" + templateKey + "\tName=" + templateName);
			String htmlResponse = null;

			// Set "startDate" and "endDate" parameters: they are applicable only to
			// DocumentTemplateType.ACCOUNT_WITH_TRANSACTIONS type
			final long thirtyDays = 30 * 86400000L; // 30 days
			Date now = new Date();
			String endDate = type == DocumentTemplateType.ACCOUNT_WITH_TRANSACTIONS ? DateUtils.format(now) : null;
			String startDate = endDate != null ? DateUtils.format(new Date(now.getTime() - thirtyDays)) : null;

			// Execute API calls. Log and continue testing if there is an exception
			try {
				switch (type) {
				case ACCOUNT:
				case ACCOUNT_WITH_TRANSACTIONS:
					// Get account template
					switch (accountType) {
					case LOAN:
						System.out.println("Getting Loan Template=" + templateKey + "\tAccount Id=" + accountId);
						htmlResponse = service.getPopulatedLoanTemplate(accountId, templateKey, startDate, endDate);
						break;
					case SAVINGS:
						System.out.println("Getting Savings Template=" + templateKey + "\tAccount Id=" + accountId);
						htmlResponse = service.getPopulatedSavingsTemplate(accountId, templateKey, startDate, endDate);
						break;
					}
					System.out.println("Populated Account Template:\n" + htmlResponse);
					break;
				case TRANSACTION:
					if (transactionId == null) {
						System.out.println("WARNING: no  Transactions to test for account " + accountId);
						break;
					}
					switch (accountType) {
					case LOAN:
						System.out.println("Getting Loan Transaction Template=" + templateKey + "\tAccount Id="
								+ accountId + "\tTransactionId=" + transactionId);
						htmlResponse = service.getPopulatedLoanTransactionTemplate(accountId, templateKey,
								transactionId);
						break;
					case SAVINGS:
						System.out.println("Getting Savings Transaction Template=" + templateKey + "\tAccount Id="
								+ accountId + "\tTransactionId=" + transactionId);
						htmlResponse = service.getPopulatedSavingsTransactionTemplate(accountId, templateKey,
								transactionId);
						break;
					}
					System.out.println("Populated Transaction Template:\n" + htmlResponse);
					break;
				}
			} catch (MambuApiException e) {
				System.out.println("Exception:" + e.getMessage() + " for " + type + " Account Id=" + accountId);
				continue;
			}
		}
	}
}
