/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraints;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.apisdk.util.ServiceHelper;
import com.mambu.core.shared.model.SearchResult;
import com.mambu.core.shared.model.SearchType;
import com.mambu.notifications.shared.model.NotificationMessage;

/**
 * Service class which handles the API operations available for the Search
 * 
 * @author mdanilkis
 * 
 */

public class SearchService {

	// Param names for QUERY and TYPE
	private String QUERY = APIData.QUERY;
	private String SEARCH_TYPES = APIData.SEARCH_TYPES;

	private static final String LIMIT = APIData.LIMIT;

	private ServiceExecutor serviceExecutor;
	private final static ApiDefinition searchEntitiies = new ApiDefinition(ApiType.GET_LIST, SearchResult.class);

	/***
	 * Create a new search service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public SearchService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}

	/**
	 * Get a Map of search results <SearchResult, List<SearchResult> for a given query and an optional list of search
	 * types
	 * 
	 * @param query
	 *            the string to query
	 * @param searchTypes
	 *            list, in brackets,separated by comma of search types to query. E.g. [CLIENT, GROUP]. Null if searching
	 *            for all types (defined by SearchType). The results of the query shall be limited to the specified
	 *            types
	 * @param limit
	 *            maximum number of results to return. If null, Mambu defaults this to 100.
	 * 
	 * 
	 * @return Map<SearchType, List<SearchResult>> is returned. Empty map and/or Mambu exception if not found
	 * 
	 * @throws MambuApiException
	 */
	public Map<SearchType, List<SearchResult>> search(String query, List<SearchType> searchTypes, String limit)
			throws MambuApiException {

		if (query == null) {
			throw new IllegalArgumentException("Query must not be null");
		}
		// strip possible blank chars
		query = query.trim();

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(QUERY, query);
		paramsMap.addParam(LIMIT, limit);

		// Add search Types, if any
		if (searchTypes != null && searchTypes.size() > 0) {
			String typeParamsString = new String("[");
			for (int i = 0; i < searchTypes.size(); i++) {
				// a comma separated list of Search Types, e.g. GROUP,CLIENT, LOAN_ACCOUNT
				if (i > 0)
					typeParamsString = typeParamsString.concat(",");
				typeParamsString = typeParamsString.concat(searchTypes.get(i).toString());
			}
			typeParamsString = typeParamsString.concat("]");
			paramsMap.addParam(SEARCH_TYPES, typeParamsString);
		}

		return serviceExecutor.execute(searchEntitiies, paramsMap);

	}

	/**
	 * Convenience method to GET Mambu entities by specifying filter constraints. This generic method can be used to
	 * retrieve entities by filter constraints for any supported entity type. API users can also use methods specific to
	 * each entity, for example to get clients by filter constraints use
	 * {@link ClientsService#getClients(JSONFilterConstraints, String, String)}
	 * <p>
	 * Note: This method is deprecated, you may use the {@link SearchService#searchEntitiesWithFullDetails(MambuEntityType, JSONFilterConstraints, String, String)} in order to obtain entities
	 * with full details (custom fields included) or searchEntitiesWithBasicDetails to obtain the entities in basic details level.
	 *
	 * @param searchEntityType  Mambu entity type. Must not be null. Currently searching with filter constrains API supports the
	 *                          following entities: Clients, Groups, Loans, Savings, Loan Transactions, SavingsTransactions and
	 *                          NotificationMessages
	 * @param filterConstraints JSONFilterConstraints object defining an array of applicable filter constraints and an optional sort
	 *                          order. Must not be null
	 * @param offset            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit             pagination limit. If not null it must be an integer greater than zero
	 * @return list of entities of the searchEntityType matching provided filter constraints
	 * @throws MambuApiException in case exception occurs while fetching entities
	 */
	@Deprecated
	public <T> List<T> searchEntities(MambuEntityType searchEntityType, JSONFilterConstraints filterConstraints,
			String offset, String limit) throws MambuApiException {

		return searchEntitiesWithFullDetails(searchEntityType, filterConstraints, offset, limit);

	}

	/**
	 * Convenience method to GET Mambu entities by specifying filter constraints. This generic method can be used to
	 * retrieve entities by filter constraints for any supported entity type. API users can also use methods specific to
	 * each entity, for example to get clients by filter constraints use
	 * {@link ClientsService#getClients(JSONFilterConstraints, String, String)}
	 *
	 * Note: the retrieved entities will not contain custom fields. In case custom fields are needed
	 * {@link SearchService#searchEntitiesWithFullDetails(MambuEntityType, JSONFilterConstraints, String, String)} must be used
	 *
	 * @param searchEntityType
	 *            Mambu entity type. Must not be null. Currently searching with filter constrains API supports the
	 *            following entities: Clients, Groups, Loans, Savings, Loan Transactions, SavingsTransactions and
	 *            NotificationMessages
	 * @param filterConstraints
	 *            JSONFilterConstraints object defining an array of applicable filter constraints and an optional sort
	 *            order. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 *
	 * @return list of entities of the searchEntityType matching provided filter constraints
	 * @throws MambuApiException in case exception occurs while fetching entities
	 */
	public <T> List<T> searchEntitiesWithBasicDetails(MambuEntityType searchEntityType, JSONFilterConstraints filterConstraints,
									  String offset, String limit) throws MambuApiException {

//		 Available since Mambu 3.12. See MBU-8986, MBU-8975
//		 For NotificationMessages available since Mambu 3.14. See MBU-10646
//		 Specifying the sort order is available since Mambu 3.14. See MBU-10444
//		 POST {JSONFilterConstraints} /api/savings/transactions/search?offset=0&limit=5
//		 Example: POST /api/loans/search {
//		 "filterConstraints":[{"filterSelection":"CREATION_DATE", "filterElement":"BETWEEN", "value":"2000-01-01",
//		 "secondValue":"2072-01-01", "dataItemType":"CLIENT" }],
//		 "sortDetails":{"sortingColumn":"ACCOUNT_ID", "sortingOrder":"ASCENDING", "dataItemType":"LOANS"}}

		ApiDefinition apiDefinition = SearchService.makeApiDefinitionForSearchByFilter(searchEntityType);

		// POST Filter JSON with pagination params map
		return serviceExecutor.executeJson(apiDefinition, filterConstraints, null, null,
				ServiceHelper.makePaginationParams(offset, limit));

	}

	/**
	 * Convenience method to GET Mambu entities by specifying filter constraints. This generic method can be used to
	 * retrieve entities by filter constraints for any supported entity type. API users can also use methods specific to
	 * each entity, for example to get clients by filter constraints use
	 * {@link ClientsService#getClients(JSONFilterConstraints, String, String)}
	 *
	 * @param searchEntityType
	 *            Mambu entity type. Must not be null. Currently searching with filter constrains API supports the
	 *            following entities: Clients, Groups, Loans, Savings, Loan Transactions, SavingsTransactions and
	 *            NotificationMessages
	 * @param filterConstraints
	 *            JSONFilterConstraints object defining an array of applicable filter constraints and an optional sort
	 *            order. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 *
	 * @return list of entities of the searchEntityType matching provided filter constraints
	 * @throws MambuApiException in case exception occurs while fetching entities
	 */
	public <T> List<T> searchEntitiesWithFullDetails(MambuEntityType searchEntityType, JSONFilterConstraints filterConstraints,
										  String offset, String limit) throws MambuApiException {

//		Available since Mambu 3.12. See MBU-8986, MBU-8975
//		For NotificationMessages available since Mambu 3.14. See MBU-10646
//		Specifying the sort order is available since Mambu 3.14. See MBU-10444
//		POST {JSONFilterConstraints} /api/savings/transactions/search?fullDetails=true&offset=0&limit=5
//		Example: POST /api/loans/search?fullDetails=true {
//		"filterConstraints":[{"filterSelection":"CREATION_DATE", "filterElement":"BETWEEN", "value":"2000-01-01",
//		"secondValue":"2072-01-01", "dataItemType":"CLIENT" }],
//		"sortDetails":{"sortingColumn":"ACCOUNT_ID", "sortingOrder":"ASCENDING", "dataItemType":"LOANS"}}

		ApiDefinition apiDefinition = makeSearchEntitiesWithFullApiDefinition(searchEntityType);

		// POST Filter JSON with pagination params map
		return serviceExecutor.executeJson(apiDefinition, filterConstraints, null, null,
				ServiceHelper.makePaginationParams(offset, limit));

	}

	/**
	 * Helper to create ApiDefintion for searching entities matching filter criteria
	 * 
	 * @param searchEntityType
	 *            entity type for searching with filter constraints. Must not be null. Currently API supports the
	 *            following entities: Clients, Groups, Loans, Savings, Loan Transactions, SavingsTransactions, and
	 *            NotificationMessages
	 * 
	 *            See MBU-8986, MBU-10646, MBU-11120 for more details
	 * 
	 * @return api definition for searching entities using filter constraints
	 */
	public static ApiDefinition makeApiDefinitionForSearchByFilter(MambuEntityType searchEntityType) {

		// See MBU-8986, MBU-8975, MBU-8987, MBU-8988, MBU-8989, MBU-11120
		// POST Example for searching clients. See MBU-8975.
		// POST {"filterConstraints":[
		// {"filterSelection":"BIRTH_DATE",
		// "filterElement":"BETWEEN",
		// "value":"2000-01-01",
		// "secondValue":"2002-01-01"
		// },
		// { "filterSelection":"40288a134700f486014700f6074200e6",
		// "dataFieldType":"CUSTOM",
		// "filterElement":"EQUALS",
		// "value":"ABC123"
		// } ] } /api/clients/search

		// Crate search URL. Example: /api/clients/search?offset=0&limit=5
		String searchUrl = makeUrlForSearchWithFilter(searchEntityType);

		// Specify Api definition for searching with filter constraints.
		Class<?> returnEntityClass = searchEntityType.getEntityClass();
		return  new ApiDefinition(searchUrl, ContentType.JSON, Method.POST, returnEntityClass,
				ApiReturnFormat.COLLECTION);
	}

	/**
	 * Create url for searching entities using filter constraints
	 * 
	 * @param searchEntityType
	 *            entity type for searching with filter constraints.
	 * @return search url
	 */
	private static String makeUrlForSearchWithFilter(MambuEntityType searchEntityType) {

		if (searchEntityType == null) {
			throw new IllegalArgumentException("Search Entity must not be NULL");
		}

		String apiDelimiter = "/";
		String entityUrl;
		switch (searchEntityType) {
		case CLIENT:
			entityUrl = APIData.CLIENTS;
			break;
		case GROUP:
			entityUrl = APIData.GROUPS;
			break;
		case LOAN_ACCOUNT:
			entityUrl = APIData.LOANS;
			break;
		case SAVINGS_ACCOUNT:
			entityUrl = APIData.SAVINGS;
			break;
		case LOAN_TRANSACTION:
			entityUrl = APIData.LOANS + apiDelimiter + APIData.TRANSACTIONS; // loans/transactions"
			break;
		case SAVINGS_TRANSACTION:
			entityUrl = APIData.SAVINGS + apiDelimiter + APIData.TRANSACTIONS; // savings/transactions"
			break;
		case NOTIFICATION_MESSAGE:
			// Example: /api/notifications/messages/search. See MBU-10646
			entityUrl = APIData.NOTIFICATIONS + apiDelimiter + APIData.MESSAGES;
			break;
		case GL_JOURNAL_ENTRY:
			entityUrl = APIData.GLJOURNALENTRIES;
			break;

		default:
			throw new IllegalArgumentException("Search for Entity " + searchEntityType.name() + " is not supported");
		}

		// Add "search" to the URL, e.g. "clients/search"
		entityUrl = entityUrl + apiDelimiter + APIData.SEARCH;
		return entityUrl;

	}

	/**
	 * Get a history of notification messages by specifying filter constraints
	 * 
	 * @param filterConstraints
	 *            filter constraints. Must not be null. See MBU-10646 for a list of supported constraints
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return list of notification messages matching filter constraints
	 * @throws MambuApiException in case something wrong happens while getting notification messages
	 */
	public List<NotificationMessage> getNotificationMessages(JSONFilterConstraints filterConstraints, String offset,
			String limit) throws MambuApiException {
		// Available since Mambu 3.14. See MBU-10646 for more details
		// POST {JSONFilterConstraints} /api/notifications/messages/search?offset=0&limit=5

		ApiDefinition apiDefinition = SearchService
				.makeApiDefinitionForSearchByFilter(MambuEntityType.NOTIFICATION_MESSAGE);

		// POST Filter JSON with pagination params map
		return serviceExecutor.executeJson(apiDefinition, filterConstraints, null, null,
				ServiceHelper.makePaginationParams(offset, limit));

	}


	private ApiDefinition makeSearchEntitiesWithFullApiDefinition(MambuEntityType searchEntityType) {
		ApiDefinition apiDefinition = SearchService.makeApiDefinitionForSearchByFilter(searchEntityType);
		apiDefinition.setWithFullDetails(true);
		return apiDefinition;
	}

}
