package demo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mambu.accounting.shared.column.TransactionsDataField;
import com.mambu.accounts.shared.model.AccountState;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraint;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraints;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.apisdk.services.LoansService;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.services.SearchService;
import com.mambu.apisdk.util.DateUtils;
import com.mambu.clients.shared.data.GroupsDataField;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.data.DataFieldType;
import com.mambu.core.shared.data.DataItemType;
import com.mambu.core.shared.data.FilterElement;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.SearchResult;
import com.mambu.core.shared.model.SearchType;
import com.mambu.loans.shared.data.LoansDataField;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanTransaction;
import com.mambu.savings.shared.data.SavingsDataField;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsTransaction;

/**
 * Test class to show example usage of the api calls
 * 
 * @author mdanilkis
 * 
 */
public class DemoTestSearchService {

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testSearchAll();

			testSearchClientsGroups();

			testSearchLoansSavings();

			testSearchUsersBranchesCentres();

			testSearchGlAccounts();
			testTypesCombinations();

			testSearchEntitiesByFilter(); // Available since Mambu 3.12

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Search Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testSearchAll() throws MambuApiException {
		System.out.println("\nIn testSearchAll");

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "m";
		String limit = "5";

		Date d1 = new Date();

		Map<SearchType, List<SearchResult>> results = searchService.search(query, null, limit);

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Search All types with a query=" + query + "\tReturned=" + results.size() + "\tTotal time="
				+ diff);

		logSearchResults(results);

	}

	public static void testSearchClientsGroups() throws MambuApiException {
		System.out.println("\nIn testSearchClientsGroups");
		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "i";
		String limit = "300";
		List<SearchType> searchTypes = Arrays.asList(SearchType.CLIENT, SearchType.GROUP); // or null

		Date d1 = new Date();
		Map<SearchType, List<SearchResult>> results = searchService.search(query, searchTypes, limit);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Search Clients for query=" + query + "\tReturned=" + results.size() + "\tTotal time="
				+ diff);

		logSearchResults(results);

	}

	public static void testSearchLoansSavings() throws MambuApiException {
		System.out.println("\nIn testSearchLoansSavings");

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "fish";
		String limit = "100";

		List<SearchType> searchTypes = Arrays.asList(SearchType.LOAN_ACCOUNT, SearchType.SAVINGS_ACCOUNT); // or null

		Map<SearchType, List<SearchResult>> results = searchService.search(query, searchTypes, limit);

		System.out.println("Search Loans/Savings for query=" + query + "\tReturned=" + results.size());

		logSearchResults(results);

	}

	public static void testSearchUsersBranchesCentres() throws MambuApiException {
		System.out.println("\nIn testSearchUsersBranchesCentres");

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "Map";
		String limit = "100";

		List<SearchType> searchTypes = Arrays.asList(SearchType.USER, SearchType.BRANCH, SearchType.CENTRE); // or null

		Date d1 = new Date();

		Map<SearchType, List<SearchResult>> results = searchService.search(query, searchTypes, limit);

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Search Users/Branches for query=" + query + "\tReturned=" + results.size()
				+ "\tTotal time=" + diff);

		logSearchResults(results);

	}

	public static void testSearchGlAccounts() throws MambuApiException {
		System.out.println("\nIn testSearchGlAccounts");

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "g";
		String limit = "100";

		List<SearchType> searchTypes = Arrays.asList(SearchType.GL_ACCOUNT);
		Date d1 = new Date();

		Map<SearchType, List<SearchResult>> results = searchService.search(query, searchTypes, limit);

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Search GL Accounts for query=" + query + "\tReturned=" + results.size() + "\tTotal time="
				+ diff);

		logSearchResults(results);

	}

	public static void testTypesCombinations() throws MambuApiException {
		System.out.println("\nIn testTypesCombinations");

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "Бо"; // Russian Бо // \u00c1 Spanish A == UTF8 hex = c3 81
		String limit = "100";

		// Use different Search Types combinations as needed
		// List<Type> searchTypes = Arrays.asList(Type.CLIENT, Type.GROUP, Type.LOAN_ACCOUNT, Type.SAVINGS_ACCOUNT,
		// Type.USER, Type.CENTRE); // or null
		List<SearchType> searchTypes = Arrays.asList(SearchType.CLIENT);
		Map<SearchType, List<SearchResult>> results = searchService.search(query, searchTypes, limit);

		if (results != null)
			System.out.println("Searching for query=" + query + "\tTypes Returned=" + results.size());

		logSearchResults(results);

	}

	// Test API to GET entities by On The Fly Filter
	private static void testSearchEntitiesByFilter() throws MambuApiException {
		System.out.println("\nIn testSearchEntitiesByFilter");

		String offset = "0";
		String limit = "5";
		JSONFilterConstraints filterConstraints = new JSONFilterConstraints();
		List<JSONFilterConstraint> constraints = new ArrayList<JSONFilterConstraint>();
		JSONFilterConstraint constraint1 = new JSONFilterConstraint();

		// Clients
		// Test GET Clients by Custom Field value
		ClientsService clientsService = MambuAPIFactory.getClientService();

		Client demoClient = DemoUtil.getDemoClient();
		ClientExpanded clientDetails = DemoUtil.getDemoClientDetails(demoClient.getEncodedKey());
		List<CustomFieldValue> customFields = clientDetails.getCustomFieldValues();
		if (customFields != null && customFields.size() > 0) {
			CustomFieldValue fieldValue = customFields.get(0);

			// Specify Filter to get Clients custom field value
			constraint1.setDataFieldType(DataFieldType.CUSTOM.name());
			constraint1.setFilterSelection(fieldValue.getCustomFieldKey());
			constraint1.setFilterElement(FilterElement.EQUALS.name());
			constraint1.setValue(fieldValue.getValue());
			constraint1.setSecondValue(null);

			constraints.add(constraint1);
			filterConstraints.setFilterConstraints(constraints);

			System.out.println("\nTesting Get Clients by filter:");
			List<Client> clients = clientsService.getClients(filterConstraints, offset, limit);
			System.out.println("Total clients returned=" + clients.size());
		} else {
			System.out.println("Warning: Cannot test filter by custom field. Client " + demoClient.getFullNameWithId()
					+ " has no assigned custom fields");
		}

		// Groups
		// Test Get Groups by Group name
		Group demoGroup = DemoUtil.getDemoGroup();
		constraints = new ArrayList<JSONFilterConstraint>();
		constraint1 = new JSONFilterConstraint();

		// Specify Filter to get Groups by group name
		constraint1.setDataFieldType(DataFieldType.NATIVE.name());
		constraint1.setFilterSelection(GroupsDataField.GROUP_NAME.name());
		constraint1.setFilterElement(FilterElement.EQUALS.name());
		constraint1.setValue(demoGroup.getGroupName());
		constraint1.setSecondValue(null);

		constraints.add(constraint1);
		filterConstraints.setFilterConstraints(constraints);

		System.out.println("\nTesting Get Groups by filter:");
		List<Group> groups = clientsService.getGroups(filterConstraints, offset, limit);
		System.out.println("Total groups returned=" + groups.size());

		// Loan Accounts
		// Test Get Loan Accounts by account ID's first char and account state
		LoanAccount demoLoanAccount = DemoUtil.getDemoLoanAccount();
		LoansService loansService = MambuAPIFactory.getLoanService();
		constraints = new ArrayList<JSONFilterConstraint>();
		constraint1 = new JSONFilterConstraint();

		// Specify Filter to get Loans by account ID's first char
		constraint1.setDataFieldType(DataFieldType.NATIVE.name());
		constraint1.setFilterSelection(LoansDataField.ACCOUNT_ID.name());
		constraint1.setFilterElement(FilterElement.STARTS_WITH.name());
		constraint1.setValue(demoLoanAccount.getId().substring(0, 1));
		constraint1.setSecondValue(null);

		constraints.add(constraint1);

		// Constraint 2: not Active accounts
		JSONFilterConstraint constraint2 = new JSONFilterConstraint();
		constraint2.setDataFieldType(DataFieldType.NATIVE.name());
		constraint2.setFilterSelection(LoansDataField.ACCOUNT_STATE.name());
		constraint2.setFilterElement(FilterElement.DIFFERENT_THAN.name());
		constraint2.setValue(AccountState.ACTIVE.name());
		constraint2.setSecondValue(null);

		constraints.add(constraint2);

		filterConstraints.setFilterConstraints(constraints);

		System.out.println("\nTesting Get Loan Accounts by filter:");
		List<LoanAccount> loans = loansService.getLoanAccounts(filterConstraints, offset, limit);
		System.out.println("Total loans returned=" + loans.size());

		// Loan Transactions
		// Test Get Loan Transactions by parent account id
		if (loans != null && loans.size() > 0) {
			constraints = new ArrayList<JSONFilterConstraint>();
			constraint1 = new JSONFilterConstraint();

			// Specify Filter to get Loan Transactions by parent account ID
			constraint1.setDataFieldType(DataFieldType.NATIVE.name());
			constraint1.setDataItemType(DataItemType.LOAN_TRANSACTION.name());
			constraint1.setFilterSelection(TransactionsDataField.PARENT_ACCOUNT_ID.name());
			constraint1.setFilterElement(FilterElement.EQUALS.name());
			constraint1.setValue(loans.get(0).getId());
			constraint1.setSecondValue(null);

			constraints.add(constraint1);
			filterConstraints.setFilterConstraints(constraints);

			System.out.println("\nTesting Get Loan Transactions by filter:");
			List<LoanTransaction> loanTransactions = loansService.getLoanTransactions(filterConstraints, offset, limit);
			System.out.println("Total loan transactions returned=" + loanTransactions.size());

		} else {
			System.out.println("Warning: Cannot test loan transactions: no loan accounts returned");
		}
		// Savings Accounts
		// Test Get Savings Accounts Created in the last 20 days
		SavingsService savingsService = MambuAPIFactory.getSavingsService();
		constraints = new ArrayList<JSONFilterConstraint>();
		constraint1 = new JSONFilterConstraint();

		// Filter for Account Creation date to be in the last 20 days
		constraint1 = new JSONFilterConstraint();
		constraint1.setDataFieldType(DataFieldType.NATIVE.name());
		constraint1.setDataItemType(DataItemType.SAVINGS.name());
		constraint1.setFilterSelection(SavingsDataField.CREATION_DATE.name());
		constraint1.setFilterElement(FilterElement.BETWEEN.name());
		Date now = new Date();
		final long offsetDays = 20 * 24 * 60 * 60 * 1000; // 20 days
		Date from = new Date(now.getTime() - offsetDays);
		DateFormat df = new SimpleDateFormat(DateUtils.DATE_FORMAT);
		constraint1.setValue(df.format(from));
		constraint1.setSecondValue(df.format(now));

		constraints.add(constraint1);

		filterConstraints.setFilterConstraints(constraints);

		System.out.println("\nTesting Get Savings Accounts by filter:");
		List<SavingsAccount> savings = savingsService.getSavingsAccounts(filterConstraints, offset, limit);
		System.out.println("Total savings returned=" + savings.size());

		// Savings Transactions
		// Test Get Savings Transactions by parent account id
		if (savings != null && savings.size() > 0) {
			constraints = new ArrayList<JSONFilterConstraint>();
			constraint1 = new JSONFilterConstraint();

			// Specify Filter to get Savings Transactions by parent account ID
			constraint1.setDataFieldType(DataFieldType.NATIVE.name());
			constraint1.setDataItemType(DataItemType.SAVINGS_TRANSACTION.name());
			constraint1.setFilterSelection(TransactionsDataField.PARENT_ACCOUNT_ID.name());
			constraint1.setFilterElement(FilterElement.EQUALS.name());
			constraint1.setValue(savings.get(0).getId());
			constraint1.setSecondValue(null);

			constraints.add(constraint1);
			filterConstraints.setFilterConstraints(constraints);

			System.out.println("\nTesting Get Savings Transactions by filter:");
			List<SavingsTransaction> savingsTransactions = savingsService.getSavingsTransactions(filterConstraints,
					offset, limit);
			System.out.println("Total Savings transactions returned=" + savingsTransactions.size());

		} else {
			System.out.println("Warning: Cannot test savings transactions: no savings accounts returned");
		}
	}

	// Helper for printing search results
	private static void logSearchResults(Map<SearchType, List<SearchResult>> results) {

		if (results == null || results.size() == 0) {
			System.out.println("No results found");
			return;

		}
		for (SearchType type : results.keySet()) {
			List<SearchResult> items = results.get(type);
			System.out.println("Returned Search Type=" + type.toString() + "  with " + items.size() + "  items:");

			for (SearchResult result : items) {
				System.out.println("   Type=" + result.getSelectionType() + " \tId=" + result.getResultID()
						+ "\tDisplay String=" + result.getDisplayString() + "\tDisplay Text="
						+ result.getDisplayString() + "\tKey=" + result.getSelectionKey());

			}

		}
	}

}
