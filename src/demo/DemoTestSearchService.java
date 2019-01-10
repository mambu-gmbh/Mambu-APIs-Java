package demo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.mambu.accounting.shared.column.TransactionsDataField;
import com.mambu.accounting.shared.model.GLJournalEntry;
import com.mambu.accounts.shared.model.AccountState;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraint;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraints;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONSortDetails;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.AccountingService;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.apisdk.services.LoansService;
import com.mambu.apisdk.services.SavingsService;
import com.mambu.apisdk.services.SearchService;
import com.mambu.apisdk.util.DateUtils;
import com.mambu.clients.shared.data.ClientsDataField;
import com.mambu.clients.shared.data.GroupsDataField;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.data.DataFieldType;
import com.mambu.core.shared.data.DataItemType;
import com.mambu.core.shared.data.FilterElement;
import com.mambu.core.shared.data.SortingOrder;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.SearchResult;
import com.mambu.core.shared.model.SearchType;
import com.mambu.loans.shared.data.DisbursementDetailsDataField;
import com.mambu.loans.shared.data.LoansDataField;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanTransaction;
import com.mambu.notifications.shared.model.MessageTemplateEvent;
import com.mambu.notifications.shared.model.NotificationMessage;
import com.mambu.notifications.shared.model.NotificationMessageDataField;
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

	private static final String ZERO_OFFSET = "0";
	private static final String FIVE_LIMIT = "5";

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testSearchAll();

			testSearchClientsGroups();

			testSearchLoansSavings();

			testSearchUsersBranchesCentres();

			testSearchGlAccounts();
			testSearchLinesOfCredit(); // Available since Mambu 3.14
			testTypesCombinations();

			testSearchEntitiesByFilter(); // Available since Mambu 3.12

			testSearchNotificationMessages(); // Available since Mambu 3.14

			testSearchByDisbursementDetails(); // Available since Mambu 4.3

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Search Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testSearchAll() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "m";
		String limit = "5";

		Date d1 = new Date();

		Map<SearchType, List<SearchResult>> results = searchService.search(query, null, limit);

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println(
				"Search All types with a query=" + query + "\tReturned=" + results.size() + "\tTotal time=" + diff);

		logSearchResults(results);

	}

	public static void testSearchClientsGroups() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);
		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "i";
		String limit = "300";
		List<SearchType> searchTypes = Arrays.asList(SearchType.CLIENT, SearchType.GROUP); // or null

		Date d1 = new Date();
		Map<SearchType, List<SearchResult>> results = searchService.search(query, searchTypes, limit);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out
				.println("Search Clients for query=" + query + "\tReturned=" + results.size() + "\tTotal time=" + diff);

		logSearchResults(results);

	}

	public static void testSearchLoansSavings() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "fish";
		String limit = "100";

		List<SearchType> searchTypes = Arrays.asList(SearchType.LOAN_ACCOUNT, SearchType.SAVINGS_ACCOUNT); // or null

		Map<SearchType, List<SearchResult>> results = searchService.search(query, searchTypes, limit);

		System.out.println("Search Loans/Savings for query=" + query + "\tReturned=" + results.size());

		logSearchResults(results);

	}

	public static void testSearchUsersBranchesCentres() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "Map";
		String limit = "100";

		List<SearchType> searchTypes = Arrays.asList(SearchType.USER, SearchType.BRANCH, SearchType.CENTRE); // or null

		Date d1 = new Date();

		Map<SearchType, List<SearchResult>> results = searchService.search(query, searchTypes, limit);

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Search Users/Branches/Centres for query=" + query + "\tReturned=" + results.size()
				+ "\tTotal time=" + diff);

		logSearchResults(results);

	}

	public static void testSearchGlAccounts() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "g";
		String limit = "100";

		List<SearchType> searchTypes = Arrays.asList(SearchType.GL_ACCOUNT);
		Date d1 = new Date();

		Map<SearchType, List<SearchResult>> results = searchService.search(query, searchTypes, limit);

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println(
				"Search GL Accounts for query=" + query + "\tReturned=" + results.size() + "\tTotal time=" + diff);

		logSearchResults(results);

	}

	// Test Search for Lines of Credit. Lines of Credit can be searched by ID. Available since 3.14. See MBU-10579
	public static void testSearchLinesOfCredit() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);
		SearchService searchService = MambuAPIFactory.getSearchService();

		// Lines of Credit can be searched by ID
		String query = "M";
		String limit = "5";
		// Test with SearchType.LINE_OF_CREDIT
		List<SearchType> searchTypes = Arrays.asList(SearchType.LINE_OF_CREDIT);
		Map<SearchType, List<SearchResult>> results = searchService.search(query, searchTypes, limit);

		System.out.println("Search Lines of Credit for query=" + query + "\tReturned=" + results.size());

		// Log detailed results
		logSearchResults(results);

	}

	public static void testTypesCombinations() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "A"; // Russian Бо // \u00c1 Spanish A == UTF8 hex = c3 81
		String limit = "100";

		// Test all possible Search Types combinations
		List<SearchType> searchTypes = Arrays.asList(SearchType.values()); // or null
		Map<SearchType, List<SearchResult>> results = searchService.search(query, searchTypes, limit);

		if (results != null)
			System.out.println("Searching for query=" + query + "\tTypes Returned=" + results.size());

		logSearchResults(results);

	}

	// Test API to GET entities by On The Fly Filter
	private static void testSearchEntitiesByFilter() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		List<CustomFieldValue> customFields = searchClientsByCustomField();

		searchCustomFieldsUsingEqualsCaseSensitive(customFields);

		searchGroupsByName();

		List<LoanAccount> loans = searchNotActiveLoanAccountsById();

		searchLoanTransactions(loans);

		List<SavingsAccount> savings = searchSavingsAccounts();

		searchSavingsTransactions(savings);

		searchGlJournalEntries();
	}

	private static List<CustomFieldValue> searchClientsByCustomField() throws MambuApiException {

		Client demoClient = DemoUtil.getDemoClient();
		ClientExpanded clientDetails = DemoUtil.getDemoClientDetails(demoClient.getEncodedKey());
		// Clients
		// Test GET Clients by Custom Field value
		ClientsService clientsService = MambuAPIFactory.getClientService();

		List<CustomFieldValue> customFields = clientDetails.getCustomFieldValues();

		if (CollectionUtils.isNotEmpty(customFields)) {
			CustomFieldValue fieldValue = customFields.get(0);

			// Specify Filter to get Clients custom field value
			JSONFilterConstraints filterConstraints = createSingleFilterConstraints(DataFieldType.CUSTOM,
					fieldValue.getCustomFieldKey(), FilterElement.EQUALS, null, fieldValue.getValue(), null);

			// Add sorting order. See MBU-10444. Available since 3.14
			// "sortDetails":{"sortingColumn":"BIRTHDATE", "sortingOrder":"DESCENDING"}
			JSONSortDetails sortDetails = new JSONSortDetails();
			sortDetails.setSortingColumn(ClientsDataField.BIRTHDATE.name());
			sortDetails.setSortingOrder(SortingOrder.DESCENDING.name());
			filterConstraints.setSortDetails(sortDetails);

			System.out.println("\nTesting Get Clients by filter:");
			List<Client> clients = clientsService.getClients(filterConstraints, ZERO_OFFSET, FIVE_LIMIT);
			System.out.println("Total clients returned=" + clients.size());
		} else {
			System.out.println("Warning: Cannot test filter by custom field. Client " + demoClient.getFullNameWithId()
					+ " has no assigned custom fields");
		}

		return customFields;
	}

	private static void searchGlJournalEntries() throws MambuApiException {

		List<JSONFilterConstraint> constraints = new ArrayList<>();

		// GL Journal Entries
		// Filter for amount greater than 1000
		constraints.add(createConstraint(DataFieldType.NATIVE, TransactionsDataField.AMOUNT.name(),
				FilterElement.EQUALS, DataItemType.SAVINGS_TRANSACTION, "1000", null));

		Date now = new Date();
		long offsetDays = 10 * 24 * 60 * 60 * 1000; // 10 days
		Date from = new Date(now.getTime() - offsetDays);
		DateFormat df = new SimpleDateFormat(DateUtils.DATE_FORMAT);
		// Filter for creation date to be in the last 10 days
		constraints.add(createConstraint(DataFieldType.NATIVE, SavingsDataField.CREATION_DATE.name(),
				FilterElement.BETWEEN, DataItemType.SAVINGS_TRANSACTION, df.format(from), df.format(now)));

		JSONFilterConstraints filterConstraints;
		filterConstraints = new JSONFilterConstraints();
		filterConstraints.setFilterConstraints(constraints);

		AccountingService accountingService = MambuAPIFactory.getAccountingService();
		System.out.println("\nTesting Get GL Journal Entries by filters:");
		List<GLJournalEntry> journalEntries = accountingService.getGLJournalEntries(filterConstraints, ZERO_OFFSET,
				FIVE_LIMIT);
		System.out.println("Total journal entries returned = " + journalEntries.size());
	}

	private static void searchSavingsTransactions(List<SavingsAccount> savings) throws MambuApiException {

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		// Savings Transactions
		// Test Get Savings Transactions by parent account id
		if (CollectionUtils.isNotEmpty(savings)) {

			// Specify Filter to get Savings Transactions by parent account ID
			JSONFilterConstraints filterConstraints = createSingleFilterConstraints(DataFieldType.NATIVE,
					TransactionsDataField.PARENT_ACCOUNT_ID.name(), FilterElement.EQUALS,
					DataItemType.SAVINGS_TRANSACTION, savings.get(0).getId(), null);

			System.out.println("\nTesting Get Savings Transactions by filter:");
			List<SavingsTransaction> savingsTransactions = savingsService.getSavingsTransactions(filterConstraints,
					ZERO_OFFSET, FIVE_LIMIT);
			System.out.println("Total Savings transactions returned=" + savingsTransactions.size());

		} else {
			System.out.println("Warning: Cannot test savings transactions: no savings accounts returned");
		}
	}

	private static List<SavingsAccount> searchSavingsAccounts() throws MambuApiException {

		SavingsService savingsService = MambuAPIFactory.getSavingsService();

		// Savings Accounts
		// Test Get Savings Accounts Created in the last 20 days
		// Filter for Account Creation date to be in the last 20 days
		Date now = new Date();
		long offsetDays = 20 * 24 * 60 * 60 * 1000; // 20 days
		Date from = new Date(now.getTime() - offsetDays);
		DateFormat df = new SimpleDateFormat(DateUtils.DATE_FORMAT);

		JSONFilterConstraints savingsFilterConstraints = createSingleFilterConstraints(DataFieldType.NATIVE,
				SavingsDataField.CREATION_DATE.name(), FilterElement.BETWEEN, DataItemType.SAVINGS, df.format(from),
				df.format(now));

		System.out.println("\nTesting Get Savings Accounts by filter:");
		List<SavingsAccount> savings = savingsService.getSavingsAccounts(savingsFilterConstraints, ZERO_OFFSET,
				FIVE_LIMIT);
		System.out.println("Total savings returned=" + savings.size());

		return savings;
	}

	private static void searchLoanTransactions(List<LoanAccount> loans) throws MambuApiException {

		Client demoClient = DemoUtil.getDemoClient();
		ClientExpanded clientDetails = DemoUtil.getDemoClientDetails(demoClient.getEncodedKey());
		LoansService loansService = MambuAPIFactory.getLoanService();

		// Loan Transactions
		// Test Get Loan Transactions by parent account id
		if (CollectionUtils.isNotEmpty(loans)) {

			// Specify Filter to get Loan Transactions by parent account ID
			JSONFilterConstraints filterConstraints = createSingleFilterConstraints(DataFieldType.NATIVE,
					TransactionsDataField.PARENT_ACCOUNT_ID.name(), FilterElement.STARTS_WITH,
					DataItemType.LOAN_TRANSACTION, loans.get(0).getId(), null);

			// Test specifying filter entities based on Another Entity criteria. See MBU-8985. Available since 3.12
			// Add Filter for Loan Transactions to filter by Client ID
			// Example: filterSelection":"ID","filterElement":"EQUALS","dataItemType":"CLIENT","value":"197495342"
			JSONFilterConstraint byClientIdConstraint = createConstraint(null, ClientsDataField.ID.name(),
					FilterElement.EQUALS, DataItemType.CLIENT, clientDetails.getId(), null);

			filterConstraints.getFilterConstraints().add(byClientIdConstraint);

			// Add sorting order. See MBU-10444. Available since 3.14
			// "sortDetails":{"sortingColumn":"AMOUNT", "sortingOrder":"ASCENDING"}
			JSONSortDetails sortDetails = new JSONSortDetails();
			sortDetails.setSortingColumn(TransactionsDataField.AMOUNT.name());
			sortDetails.setSortingOrder(SortingOrder.ASCENDING.name());
			filterConstraints.setSortDetails(sortDetails);

			System.out.println("\nTesting Get Loan Transactions by filter:");
			List<LoanTransaction> loanTransactions = loansService.getLoanTransactions(filterConstraints, ZERO_OFFSET,
					FIVE_LIMIT);
			System.out.println("Total loan transactions returned=" + loanTransactions.size());

		} else {
			System.out.println("Warning: Cannot test loan transactions: no loan accounts returned");
		}
	}

	private static List<LoanAccount> searchNotActiveLoanAccountsById() throws MambuApiException {

		// Loan Accounts
		// Test Get Loan Accounts by account ID's first char and account state
		LoanAccount demoLoanAccount = DemoUtil.getDemoLoanAccount();

		LoansService loansService = MambuAPIFactory.getLoanService();

		// Specify Filter to get Loans by account ID's first char
		JSONFilterConstraints filterConstraints = createSingleFilterConstraints(DataFieldType.NATIVE,
				LoansDataField.ACCOUNT_ID.name(), FilterElement.STARTS_WITH, null,
				demoLoanAccount.getId().substring(0, 1), null);

		// Constraint 2: not Active accounts
		JSONFilterConstraint activeAccountConstraint = createConstraint(DataFieldType.NATIVE,
				LoansDataField.ACCOUNT_STATE.name(), FilterElement.EQUALS, null, AccountState.ACTIVE.name(), null);

		filterConstraints.getFilterConstraints().add(activeAccountConstraint);

		System.out.println("\nTesting Get Loan Accounts by filter:");
		List<LoanAccount> loans = loansService.getLoanAccounts(filterConstraints, ZERO_OFFSET, FIVE_LIMIT);
		System.out.println("Total loans returned=" + loans.size());

		return loans;
	}

	private static void searchGroupsByName() throws MambuApiException {

		ClientsService clientsService = MambuAPIFactory.getClientService();

		// Groups
		// Test Get Groups by Group name
		Group demoGroup = DemoUtil.getDemoGroup();
		JSONFilterConstraints filterConstraints2 = createSingleFilterConstraints(DataFieldType.NATIVE,
				GroupsDataField.GROUP_NAME.name(), FilterElement.EQUALS, null, demoGroup.getName(), null);

		System.out.println("\nTesting Get Groups by filter:");
		List<Group> groups = clientsService.getGroups(filterConstraints2, ZERO_OFFSET, FIVE_LIMIT);

		System.out.println("Total groups returned=" + groups.size());
	}

	static JSONFilterConstraint createConstraint(DataFieldType dataFieldType, String filterSelection,
			FilterElement filterElement, DataItemType dataItemType, String value, String secondValue) {

		JSONFilterConstraint constraint = new JSONFilterConstraint();

		constraint.setDataFieldType(dataFieldType!= null ? dataFieldType.name(): null);
		constraint.setFilterSelection(filterSelection);
		constraint.setFilterElement(filterElement!=null ? filterElement.name():null);
		constraint.setDataItemType(dataItemType != null ? dataItemType.name(): null);
		constraint.setValue(value);
		constraint.setSecondValue(secondValue);

		return constraint;
	}

	private static void searchCustomFieldsUsingEqualsCaseSensitive(List<CustomFieldValue> customFields)
			throws MambuApiException {

		Client demoClient = DemoUtil.getDemoClient();
		ClientsService clientsService = MambuAPIFactory.getClientService();

		// Test GET Clients by Custom Field value using EQUALS_CASE_SENSITIVE
		if (CollectionUtils.isNotEmpty(customFields)) {

			CustomFieldValue fieldValue = customFields.get(0);

			// Specify Filter to get Clients custom field value
			JSONFilterConstraints filterConstraints = createSingleFilterConstraints(DataFieldType.CUSTOM,
					fieldValue.getCustomFieldKey(), FilterElement.EQUALS_CASE_SENSITIVE, null, fieldValue.getValue(),
					null);

			System.out.println("\nTesting Get Clients by filter using EQUALS_CASE_SENSITIVE filter:");
			List<Client> clients = clientsService.getClients(filterConstraints, ZERO_OFFSET, FIVE_LIMIT);
			System.out.println("Total clients returned=" + clients.size());

		} else {
			System.out.println("Warning: Cannot test filter by custom field. Client " + demoClient.getFullNameWithId()
					+ " has no assigned custom fields");
		}
	}

	public static JSONFilterConstraints createSingleFilterConstraints(DataFieldType dataFieldType,
			String filterSelection, FilterElement filterElement, DataItemType dataItemType, String value,
			String secondValue) {

		JSONFilterConstraint constraint = createConstraint(dataFieldType, filterSelection, filterElement, dataItemType,
				value, secondValue);

		JSONFilterConstraints filterConstraints = new JSONFilterConstraints();

		List<JSONFilterConstraint> constraints = new ArrayList<>();
		constraints.add(constraint);

		filterConstraints.setFilterConstraints(constraints);

		return filterConstraints;
	}

	// Test search loans by disbursement details using on the fly filter API
	public static void testSearchByDisbursementDetails() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		// Create Filter Constraints
		ArrayList<JSONFilterConstraint> constraints = new ArrayList<>();
		JSONFilterConstraint constraint = new JSONFilterConstraint();

		// Specify Filter with disbursement details. See MBU-14097
		constraint.setFilterSelection(DisbursementDetailsDataField.DISBURSEMENT_DATE.name());
		constraint.setFilterElement(FilterElement.BETWEEN.name());
		constraint.setDataItemType(DataItemType.DISBURSEMENT_DETAILS.name());

		// from date, 2 months ago
		Calendar from = Calendar.getInstance();
		from.add(Calendar.MONTH, -6);
		// until date, 1 month ago
		Calendar until = Calendar.getInstance();
		until.roll(Calendar.MONTH, -1);

		DateFormat df = new SimpleDateFormat(DateUtils.DATE_FORMAT);

		constraint.setValue(df.format(from.getTime()));
		constraint.setSecondValue(df.format(until.getTime()));

		constraints.add(constraint);

		String offset = "0";
		String limit = "5";

		JSONFilterConstraints filterConstraints = new JSONFilterConstraints();
		filterConstraints.setFilterConstraints(constraints);

		LoansService loansService = MambuAPIFactory.getLoanService();

		System.out.println("\nTesting Get Loans by disbursement details filter:");
		List<LoanAccount> loanAccounts = loansService.getLoanAccounts(filterConstraints, offset, limit);

		if (loanAccounts.isEmpty()) {
			System.out.println("Warning: No loan accounts matching filtering constraints were found");
		} else {
			System.out.println("Total loans returned by search = " + loanAccounts.size());
		}

	}

	// Test Search Notification Messages by on the Fly filter API
	private static void testSearchNotificationMessages() throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);

		// Create Filter Constraints
		ArrayList<JSONFilterConstraint> constraints = new ArrayList<>();
		JSONFilterConstraint constraint1 = new JSONFilterConstraint();
		JSONFilterConstraint constraint2 = new JSONFilterConstraint();

		// Specify Filter to get Notification messages. See MBU-10646 for details on available filters
		constraint1.setDataFieldType(DataFieldType.NATIVE.name());
		constraint1.setFilterSelection(NotificationMessageDataField.EVENT.name());
		constraint1.setFilterElement(FilterElement.EQUALS.name());
		constraint1.setValue(MessageTemplateEvent.LOAN_CREATED.name());

		constraints.add(constraint1);

		// Filter for retrieving the communications based on user recipients. See MBU-12991
		constraint2.setDataFieldType(DataFieldType.NATIVE.name());
		constraint2.setFilterSelection(NotificationMessageDataField.RECIPIENT_USER_KEY.name());
		constraint2.setFilterElement(FilterElement.EMPTY.name());

		constraints.add(constraint2);

		// Create JSONFilterConstraints with these constraints
		JSONFilterConstraints filterConstraints = new JSONFilterConstraints();
		filterConstraints.setFilterConstraints(constraints);

		SearchService searchService = MambuAPIFactory.getSearchService();

		String offset = "0";
		String limit = "5";
		// Execute API
		List<NotificationMessage> notificationMessages = searchService.getNotificationMessages(filterConstraints,
				offset, limit);

		System.out.println("Total Notification messages=" + notificationMessages.size());
		for (NotificationMessage message : notificationMessages) {
			System.out.println("\tID=" + message.getId() + "\tType=" + message.getType() + "\tDestination="
					+ message.getDestination());
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
