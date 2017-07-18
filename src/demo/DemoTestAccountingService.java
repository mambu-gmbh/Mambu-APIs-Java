package demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mambu.accounting.shared.model.EntryType;
import com.mambu.accounting.shared.model.GLAccount;
import com.mambu.accounting.shared.model.GLAccountType;
import com.mambu.accounting.shared.model.GLJournalEntry;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.ApiGLJournalEntry;
import com.mambu.apisdk.services.AccountingService;
import com.mambu.apisdk.util.DateUtils;
import com.mambu.organization.shared.model.Branch;

/**
 * Test class to show example usage of the api calls for Accounting API
 * 
 * @author mdanilkis
 * 
 */
public class DemoTestAccountingService {

	private static Branch demoBranch;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			demoBranch = DemoUtil.getDemoBranch();

			List<GLAccount> allAccounts = testGetGLAccountsByType(); // Available since Mambu 1.1
			testGetGLAccountByCode(allAccounts); // Available since Mambu 1.1

			testPostGLJournalEntries(allAccounts); // Available since 2.0
			testPostGLJournalEntriesWithTransactionId(allAccounts);
			testGetGLJournalEntries(); // Available since 2.0
			

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Accounting Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	// Test Getting GLAccount by account type
	public static List<GLAccount> testGetGLAccountsByType() throws MambuApiException {

		System.out.println("\nIn testGetGLAccountsByType");

		AccountingService service = MambuAPIFactory.getAccountingService();

		// Return all available accounts for subsequent test cases
		List<GLAccount> allGlAccounts = new ArrayList<>();
		for (GLAccountType accountType : GLAccountType.values()) {
			// Get GL Account
			System.out.println("Getting GLAccount by GLAccountType=" + accountType);
			List<GLAccount> gLAccounts = service.getGLAccounts(accountType);
			if (gLAccounts == null) {
				System.out.println("NULL GLAccounts for " + accountType);
				continue;
			}
			// Store with all accounts
			allGlAccounts.addAll(gLAccounts);
			// Log output
			System.out.println("Total GLAccounts=" + gLAccounts.size() + " of " + accountType + " type");
			for (GLAccount gLAccount : gLAccounts) {
				System.out.println("\tID=" + gLAccount.getId() + "\tName=" + gLAccount.getName() + "\tType="
						+ gLAccount.getType() + "\tBalance" + gLAccount.getBalance());
			}
		}
		System.out.println("Overall Total GLAccounts=" + allGlAccounts.size());
		return allGlAccounts;
	}

	// Test Getting GLAccount by code
	public static void testGetGLAccountByCode(List<GLAccount> allAccounts) throws MambuApiException {

		System.out.println("\nIn testGetGLAccountByCode");

		if (allAccounts == null || allAccounts.size() == 0) {
			System.out.println("No GLAccounts to test Get GLAccountByCode");
			return;
		}
		AccountingService service = MambuAPIFactory.getAccountingService();

		Date toDate = new Date();
		int forNumberOfDays = 60;
		long forMsecInterval = 1000L * 60 * 60 * 24 * forNumberOfDays;
		// From forNumberOfDays ago
		Date fromDate = new Date(new Date().getTime() - forMsecInterval);

		int accountIndex = (int) Math.random() * (allAccounts.size() - 1);
		GLAccount testAccount = allAccounts.get(accountIndex);
		String glCode = testAccount.getId();
		String fromDateStr = DateUtils.format(fromDate);
		String toDateStr = DateUtils.format(toDate);
		// Get GL Account;
		System.out.println("Testing Getting Account =" + glCode);
		GLAccount gLAccount = service.getGLAccount(glCode, fromDateStr, toDateStr);
		System.out.println("GLAccount ID=" + gLAccount.getId() + "\tName=" + gLAccount.getName() + "\tType="
				+ gLAccount.getType() + "\tBalance" + gLAccount.getBalance());

	}

	// Test posting GLJournalEntries. Need at least three(3) test GL Accounts for this test
	public static void testPostGLJournalEntries(List<GLAccount> allAccounts) throws MambuApiException {

		System.out.println("\nIn testPostGLJournalEntries");

		final int needTestGlAccounts = 3;
		if (allAccounts == null || allAccounts.size() < needTestGlAccounts) {
			int totalAccounts = allAccounts == null ? 0 : allAccounts.size();
			System.out.println("WARNING: Not enough GLAccounts. Need " + needTestGlAccounts + " Have " + totalAccounts);
			return;
		}

		AccountingService service = MambuAPIFactory.getAccountingService();
		// Create Debit/Credit transaction entries
		List<ApiGLJournalEntry> entries = new ArrayList<>();
		GLAccount account1 = allAccounts.get(0);
		GLAccount account2 = allAccounts.get(1);
		GLAccount account3 = allAccounts.get(2);

		// Add one debit and one matching credit transaction
		BigDecimal amount = new BigDecimal("500.00");
		ApiGLJournalEntry entry1 = new ApiGLJournalEntry(account1.getGlCode(), EntryType.DEBIT, amount);
		ApiGLJournalEntry entry1a = new ApiGLJournalEntry(account2.getGlCode(), EntryType.CREDIT, amount);
		entries.add(entry1);
		entries.add(entry1a);
		 // Add one debit and two matching credit transactions
		 BigDecimal halfAmount = amount.divide(new BigDecimal(2)); // credit half of debit amount to each of two accounts
		 ApiGLJournalEntry entry2 = new ApiGLJournalEntry(account2.getGlCode(), EntryType.DEBIT, amount);
		 ApiGLJournalEntry entry2a = new ApiGLJournalEntry(account1.getGlCode(), EntryType.CREDIT, halfAmount);
		 ApiGLJournalEntry entry2b = new ApiGLJournalEntry(account3.getGlCode(), EntryType.CREDIT, halfAmount);
		 entries.add(entry2);
		 entries.add(entry2a);
		 entries.add(entry2b);
		 // Add two debit and one matching credit transaction
		 // TODO: re-test this scenario when MBU-14104 issue is fixed: Journal Entries cannot be added via API as long as
		 // 2 GL Accounts used as Debit are equal with 1 GL Account used as Credit
		 ApiGLJournalEntry entry3a = new ApiGLJournalEntry(account3.getGlCode(), EntryType.DEBIT, halfAmount);
		 ApiGLJournalEntry entry3b = new ApiGLJournalEntry(account1.getGlCode(), EntryType.DEBIT, halfAmount);
		 ApiGLJournalEntry entry3 = new ApiGLJournalEntry(account2.getGlCode(), EntryType.CREDIT, amount);
		 entries.add(entry3a);
		 entries.add(entry3b);
		 entries.add(entry3);

		// Specify Date and Branch Id
		String date = DateUtils.format(new Date());
		String branchId = demoBranch.getId();

		// POST entries
		List<GLJournalEntry> gLJournalEntries = service.postGLJournalEntries(entries, branchId, date, "API entry");
		System.out.println("Total GLJournalEntries Created=" + gLJournalEntries.size());

		// Log output
		for (GLJournalEntry entry : gLJournalEntries) {
			System.out.println(
					"\tID=" + entry.getEntryId() + "\tAmount=" + entry.getAmount() + "\tType=" + entry.getType());
		}

	}
	
	public static void testPostGLJournalEntriesWithTransactionId(List<GLAccount> allAccounts)
			throws MambuApiException {

		System.out.println("\nIn testPostGLJournalEntriesWithTransactionId");

		final int needTestGlAccounts = 3;
		if (allAccounts == null || allAccounts.size() < needTestGlAccounts) {
			int totalAccounts = allAccounts == null ? 0 : allAccounts.size();
			System.out.println("WARNING: Not enough GLAccounts. Need " + needTestGlAccounts + " Have " + totalAccounts);
			return;
		}

		AccountingService service = MambuAPIFactory.getAccountingService();
		// Create Debit/Credit transaction entries
		List<ApiGLJournalEntry> entries = new ArrayList<>();
		GLAccount account1 = allAccounts.get(0);
		GLAccount account2 = allAccounts.get(1);
		
		// Add one debit and one matching credit transaction
		BigDecimal amount = new BigDecimal("500.00");
		ApiGLJournalEntry entry1 = new ApiGLJournalEntry(account1.getGlCode(), EntryType.DEBIT, amount);
		ApiGLJournalEntry entry1a = new ApiGLJournalEntry(account2.getGlCode(), EntryType.CREDIT, amount);
		entries.add(entry1);
		entries.add(entry1a);

		// Specify Date and Branch Id
		String date = DateUtils.format(new Date());
		String branchId = demoBranch.getId();

		// POST entries
		List<GLJournalEntry> gLJournalEntries = service.postGLJournalEntries(entries, branchId, date, "API entry",
				"589214");
		System.out.println("Total GLJournalEntries Created=" + gLJournalEntries.size());

		// Log output
		for (GLJournalEntry entry : gLJournalEntries) {
			System.out.println(
					"\tID=" + entry.getEntryId() + "\tAmount=" + entry.getAmount() + "\tType=" + entry.getType());
		}

	}

	// Test getting GLJournalEntries
	public static void testGetGLJournalEntries() throws MambuApiException {

		System.out.println("\nIn testGetGLJournalEntries");

		AccountingService service = MambuAPIFactory.getAccountingService();

		Date toDate = new Date();
		int forNumberOfDays = 60;
		long forMsecInterval = 1000 * 60 * 60 * 24 * forNumberOfDays;
		// From forNumberOfDays ago
		Date fromDate = new Date(new Date().getTime() - forMsecInterval);
		int offset = 0;
		int limit = 20;
		// Get branch id
		String branchId = demoBranch.getId();
		List<GLJournalEntry> gLJournalEntries = service.getGLJournalEntries(branchId, fromDate, toDate, offset, limit);
		System.out.println("Total GLJournalEntry=" + gLJournalEntries.size());
		// Log output
		for (GLJournalEntry entry : gLJournalEntries) {
			System.out.println(
					"\tID=" + entry.getEntryId() + "\tAmount=" + entry.getAmount() + "\tType=" + entry.getType());
		}
	}

}
