package demo;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.SearchService;
import com.mambu.core.shared.model.SearchResult;
import com.mambu.core.shared.model.SearchResult.Type;

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

			testSearchUsersBranches();

			testTypesCombinations();

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

		Map<SearchResult.Type, List<SearchResult>> results = searchService.search(query, null, limit);

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Search All types with a query=" + query + "  Returned=" + results.size() + " Total time="
				+ diff);

		logSearchResults(results);

	}

	public static void testSearchClientsGroups() throws MambuApiException {
		System.out.println("\nIn testSearchClientsGroups");
		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "i";
		String limit = "300";
		List<Type> searchTypes = Arrays.asList(Type.CLIENT, Type.GROUP); // or null

		Date d1 = new Date();
		Map<SearchResult.Type, List<SearchResult>> results = searchService.search(query, searchTypes, limit);
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Search Clients for query=" + query + "Returned=" + results.size() + " Total time=" + diff);

		logSearchResults(results);

	}
	public static void testSearchLoansSavings() throws MambuApiException {
		System.out.println("\nIn testSearchLoansSavings");

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "fish";
		String limit = "100";

		List<Type> searchTypes = Arrays.asList(Type.LOAN_ACCOUNT, Type.SAVINGS_ACCOUNT); // or null

		Map<SearchResult.Type, List<SearchResult>> results = searchService.search(query, searchTypes, limit);

		System.out.println("Search Loans/Savings for query=" + query + "Returned=" + results.size());

		logSearchResults(results);

	}

	public static void testSearchUsersBranches() throws MambuApiException {
		System.out.println("\nIn testSearchUsersBranches");

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "Map";
		String limit = "100";

		List<Type> searchTypes = Arrays.asList(Type.USER, Type.BRANCH); // or null

		Date d1 = new Date();

		Map<SearchResult.Type, List<SearchResult>> results = searchService.search(query, searchTypes, limit);

		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();

		System.out.println("Search Users/Branches for query=" + query + "Returned=" + results.size() + " Total time="
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
		// Type.USER); // or null
		List<Type> searchTypes = Arrays.asList(Type.CLIENT);
		Map<SearchResult.Type, List<SearchResult>> results = searchService.search(query, searchTypes, limit);

		if (results != null)
			System.out.println("Searching for query=" + query + " Types Returned=" + results.size());

		logSearchResults(results);

	}

	// Helper for printing search results
	private static void logSearchResults(Map<SearchResult.Type, List<SearchResult>> results) {

		if (results == null || results.size() == 0) {
			System.out.println("No results found");
			return;

		}
		for (SearchResult.Type type : results.keySet()) {
			List<SearchResult> items = results.get(type);
			System.out.println("Returned Search Type=" + type.toString() + "  with " + items.size() + "  items:");

			for (SearchResult result : items) {
				System.out.println("   Type=" + result.getResultType().toString() + "  Id=" + result.getResultID()
						+ " Display String=" + result.getDisplayString() + "  Display Text=" + result.getDisplayText());
			}

		}
	}

}
