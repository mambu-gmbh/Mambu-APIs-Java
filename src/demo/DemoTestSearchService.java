package demo;

import java.util.Arrays;
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

		// Test APIs
		try {

			// testSearchAll();

			// testSearchClientsGroups();

			// testSearchLoansSavings();

			// testSearchUsersBranches();

			testTypesCombinations();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Search Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testSearchAll() throws MambuApiException {
		System.out.println("In testSearchAll");

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "tom";

		Map<SearchResult.Type, List<SearchResult>> results = searchService.search(query, null);

		System.out.println("Search All for query=" + query + "Returned=" + results.size());

		logSearchResults(results);

	}

	public static void testSearchClientsGroups() throws MambuApiException {
		System.out.println("In testSearchClientsGroups");
		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "inrina";
		List<Type> searchTypes = Arrays.asList(Type.CLIENT, Type.GROUP); // or null

		Map<SearchResult.Type, List<SearchResult>> results = searchService.search(query, searchTypes);

		System.out.println("Search Clients for query=" + query + "Returned=" + results.size());

		logSearchResults(results);

	}
	public static void testSearchLoansSavings() throws MambuApiException {
		System.out.println("In testSearchLoansSavings");

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "tom";

		List<Type> searchTypes = Arrays.asList(Type.LOAN_ACCOUNT, Type.SAVINGS_ACCOUNT); // or null

		Map<SearchResult.Type, List<SearchResult>> results = searchService.search(query, searchTypes);

		System.out.println("Search Loans/Savings for query=" + query + "Returned=" + results.size());

		logSearchResults(results);

	}

	public static void testSearchUsersBranches() throws MambuApiException {
		System.out.println("In testSearchUsersBranches");

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "Michael";

		List<Type> searchTypes = Arrays.asList(Type.USER, Type.BRANCH); // or null

		Map<SearchResult.Type, List<SearchResult>> results = searchService.search(query, searchTypes);

		System.out.println("Search Users/Branches for query=" + query + "Returned=" + results.size());

		logSearchResults(results);

	}

	public static void testTypesCombinations() throws MambuApiException {
		System.out.println("In testTypesCombinations");

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "irina";

		// Modify for different search Types combinations as needed
		List<Type> searchTypes = Arrays.asList(Type.BRANCH); // or null

		Map<SearchResult.Type, List<SearchResult>> results = searchService.search(query, null);

		System.out.println("Search for query=" + query + "Returned=" + results.size());

		logSearchResults(results);

	}
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
