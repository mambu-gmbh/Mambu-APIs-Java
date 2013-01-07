package demo;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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
		// Get LOGGER
		try {

			FileInputStream loggingFile = new FileInputStream("logging.properties");

			LogManager.getLogManager().readConfiguration(loggingFile);

		} catch (IOException e) {
			System.out.println("  Exception reading property file in Demo Test Search");
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
		}

		// Test APIs
		try {
			MambuAPIFactory.setUp("demo.mambucloud.com", "api", "api");

			testSearch();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Search Service");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}

	}

	public static void testSearch() throws MambuApiException {

		SearchService searchService = MambuAPIFactory.getSearchService();

		String query = "tom";

		List<Type> searchTypes = Arrays.asList(Type.CLIENT, Type.GROUP); // or null

		List<SearchResult> results = searchService.search(query, searchTypes);

		System.out.println("Total Search results found=" + results.size());
		int i = 0;
		for (SearchResult result : results) {

			System.out.println("Search Result #" + i + ".  Type=" + result.getResultType().toString() + "  Id="
					+ result.getResultID() + " Display String=" + result.getDisplayString() + "  Display Text="
					+ result.getDisplayText());
			i++;
		}

	}

}
