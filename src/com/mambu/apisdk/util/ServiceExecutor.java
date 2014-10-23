package com.mambu.apisdk.util;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.mambu.accounts.shared.model.TransactionChannel;
import com.mambu.api.server.handler.activityfeed.model.JSONActivity;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.exception.MambuApiResponseMessage;
import com.mambu.apisdk.model.LoanAccountExpanded;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.core.shared.model.Currency;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.core.shared.model.CustomView;
import com.mambu.core.shared.model.SearchResult;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;
import com.mambu.intelligence.shared.model.Intelligence.Indicator;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanTransaction;
import com.mambu.loans.shared.model.Repayment;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;
import com.mambu.savings.shared.model.SavingsTransaction;
import com.mambu.tasks.shared.model.Task;

/**
 * ServiceExecutor class provides services to build and execute API requests for other API Service classes. It uses
 * supplied ApiDefintion for a particular request to build required URL, execute https request (with the specified
 * method and the content type), to parse Mambu response into the requested type and to return the result to the caller.
 * 
 * Other services can use ServiceExecutor methods to execute their API requests. A typical pattern for using
 * ServiceExecutor could be: a) define the ApiDefintion for an API request b) call serviceExecutor.execute(apiDefintion,
 * parameters...)
 * 
 * Note that all ServiceExecutor execute...(...) methods are generic methods returning a generic type R. Invoking these
 * methods can be done using either a parameterised type or using Java's target typing to infer the return type
 * parameter of a generic method invocation. Examples below are using target typing, which infer the return R type from
 * the statement. For example, in getLoanAccount() wrapper method specifies LoanAccount as a returned type then when
 * using a statement: return serviceExecutor.execute(getAccount, params) within the getLoanAccount() Java infers type R
 * to be LoanAccount and is equivalent to the following invocation: return serviceExecutor.<LoanAccount>
 * execute(getAccount, accountId);. Similarly, from the assignment statement Client client =
 * serviceExecutor.execute(...) Java infers the return type R for this execute() statement to be a Client class and is
 * equivalent to Client client= serviceExecutor.<Client>execute(...)
 * 
 * Usage Example:
 * 
 * 1. If LoanService needs to execute GET Loan Account details request then:
 * 
 * serviceExecutor = new ServiceExecutor(mambuAPIService)
 * 
 * ApiDefinition getAccount = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, LoanAccount.class);
 * 
 * return serviceExecutor.execute(getAccount, accountId);
 * 
 * 
 * 2. If Client Service needs to execute GET a list of Clients request then:
 * 
 * serviceExecutor = new ServiceExecutor(mambuAPIService)
 * 
 * ApiDefinition getClientsList = new ApiDefinition(ApiType.GET_LIST, Client.class);
 * 
 * return serviceExecutor.execute(getClientsList, params);
 * 
 * 
 * @author mdanilkis
 * 
 */
public class ServiceExecutor {

	private MambuAPIService mambuAPIService;

	/***
	 * Create a new ServiceExecutor
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	public ServiceExecutor(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/****
	 * Execute API Request using its ApiDefinition and supplied input data
	 * 
	 * @param apiDefinition
	 *            API definition for the request
	 * @param objectId
	 *            api's object id (optional, must be null if not used)
	 * @param relatedEntityId
	 *            an id of the relatedEntity (optional, must be null if not used)
	 * @param paramsMap
	 *            map with API parameters
	 * 
	 * @return object result object, which will be an API specific object or a list of objects
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public <R> R execute(ApiDefinition apiDefinition, String objectId, String relatedEntityId, ParamsMap paramsMap)
			throws MambuApiException {

		if (apiDefinition == null) {
			throw new IllegalArgumentException("ApiDefinition cannot be NULL");

		}

		// Create URL for this API request using specification in its apiDefintion and input IDs
		String apiUrlPath = getApiPath(apiDefinition, objectId, relatedEntityId);

		// Add full details parameter if required by apiDefintion specification
		if (apiDefinition.getWithFullDetails()) {
			if (paramsMap == null) {
				paramsMap = new ParamsMap();
			}
			paramsMap.put(APIData.FULL_DETAILS, "true");
		}

		// Execute Request. Get Method and ContentType from the apiDefintion
		Method method = apiDefinition.getMethod();
		ContentType contentType = apiDefinition.getContentType();

		// Use mambuAPIService to execute request
		String jsonResponse = mambuAPIService.executeRequest(apiUrlPath, paramsMap, method, contentType);

		// Process API Response. Get the return format and returnClass from the apiDefintion
		Class<?> returnClass = apiDefinition.getReturnClass();
		ApiReturnFormat returnFormat = apiDefinition.getApiReturnFormat();

		R result = null;
		switch (returnFormat) {
		case OBJECT:
			// Get Single Object from the response
			result = getObject(jsonResponse, returnClass);
			break;
		case COLLECTION:
			// Get a list of Objects from the response
			Type collectionType = getCollectionType(returnClass);
			// Get result as a collection
			result = getCollection(jsonResponse, collectionType);
			break;
		case BOOLEAN:
			// Get result as a boolean
			result = (R) getBoolean(jsonResponse);
			break;
		case RESPONSE_STRING:
			// Return the response string as is, with no additional processing.
			// This can be used for the services to perform any subsequent processing or for such APIs as getDocument()
			result = (R) jsonResponse;
			break;
		}

		return result;
	}

	/****
	 * Convenience method to Execute API Request without relatedEntityId parameter
	 * 
	 * @param apiDefinition
	 *            API definition for the request
	 * @param objectId
	 *            api's object id (optional, must be null if not used)
	 * @param paramsMap
	 *            map with API parameters
	 * 
	 * @return object result object, which will be an API specific object or a list of objects
	 * 
	 * @throws MambuApiException
	 */

	public <R> R execute(ApiDefinition apiDefinition, String objectId, ParamsMap paramsMap) throws MambuApiException {
		String relatedEntityId = null;
		return execute(apiDefinition, objectId, relatedEntityId, paramsMap);
	}

	/****
	 * Convenience method to execute API Request using its ApiDefinition and object ID. This version of the execute
	 * method can be used when the API request doesn't use ParamsMap, for example get entity details API request
	 * 
	 * @param apiDefinition
	 *            API definition for the request
	 * @param objectId
	 *            api's object id (optional, must be null if not used)
	 * 
	 * @return object result object, which will be an API specific object or a list of objects
	 * 
	 * @throws MambuApiException
	 */
	public <R> R execute(ApiDefinition apiDefinition, String objectId) throws MambuApiException {
		ParamsMap paramsMap = null;
		return execute(apiDefinition, objectId, paramsMap);
	}

	/****
	 * Convenience method to execute API Request using its ApiDefinition and params map. This version of the execute
	 * method can be used when the API request doesn't use object ID (for example in get list requests)
	 * 
	 * @param api
	 *            API definition for the request
	 * @param paramsMap
	 *            map with API parameters
	 * 
	 * @return object result object, which will be an API specific object or a list of objects
	 * 
	 * @throws MambuApiException
	 */
	public <R> R execute(ApiDefinition apiDefinition, ParamsMap paramsMap) throws MambuApiException {
		String objectId = null;
		return execute(apiDefinition, objectId, paramsMap);
	}

	/****
	 * Convenience method to execute API request using its ApiDefinition. This version of the execute method can be used
	 * when the API request doesn't use ParamsMap and doesn't need an object Id, for example GET list of currencies
	 * 
	 * @param apiDefinition
	 *            API definition for the request
	 * @param objectId
	 *            api's object id (optional, must be null if not used)
	 * 
	 * @return object result object, which will be an API specific object or a list of objects
	 * 
	 * @throws MambuApiException
	 */
	public <R> R execute(ApiDefinition apiDefinition) throws MambuApiException {
		ParamsMap paramsMap = null;
		String objectId = null;
		return execute(apiDefinition, objectId, paramsMap);
	}

	/****
	 * Execute API JSON Post Request using its ApiDefinition and supplied input data. Used for JSON create and update
	 * requests.
	 * 
	 * @param apiDefinition
	 *            API definition for the request
	 * @param object
	 *            the Mambu object to be created. Currently (as of Mambu 3.7) only Client, LoanAccount, SavingsAccount
	 *            and Document objects can be created using JSON API requests
	 * @param objectId
	 *            object's id (optional, could be null if not used, for example for JSON create requests)
	 * 
	 * @return object a result object, which will be an API specific object
	 * 
	 * @throws MambuApiException
	 */
	public <R, T> R executeJson(ApiDefinition apiDefinition, T object, String objectId) throws MambuApiException {

		if (object == null) {
			throw new IllegalArgumentException("JSON object must not be NULL");
		}

		// Parse input object into a JSON string
		final String dateTimeFormat = APIData.yyyyMmddFormat;
		final String jsonData = GsonUtils.createGson(dateTimeFormat).toJson(object, object.getClass());

		// Add JSON string as JSON_OBJECT to the ParamsMap
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(APIData.JSON_OBJECT, jsonData);

		// Execute this request with apiDefintion, objectId and paramsMap
		return execute(apiDefinition, objectId, paramsMap);

	}

	/****
	 * Convenience method for executing API JSON Post Request using its ApiDefinition and supplied object. Can be used
	 * for JSON requests which do not require objectId parameter
	 * 
	 * @param apiDefinition
	 *            API definition for the request
	 * @param object
	 *            the Mambu object to be created.
	 * @return object a result object, which will be an API specific object
	 * 
	 * @throws MambuApiException
	 */
	public <R, T> R executeJson(ApiDefinition apiDefinition, T object) throws MambuApiException {
		String objectId = null;
		return executeJson(apiDefinition, object, objectId);
	}

	// // Private Helper methods ////
	/****
	 * Get URL path for the API request based on the request's ApiDefinition. The URL path is created to comply with the
	 * following URL path pattern: endPoint/objectId/relatedEntity. ApiDefinition for the request determines which URL
	 * path parts are required for this request.
	 * 
	 * @param apiDefinition
	 *            Api Definition for the API request
	 * @param objectId
	 *            object ID for the API request. It's optional and can be null, if allowed by request's apiDefinition
	 * @param relatedEntityId
	 *            an id of the relatedEntity (optional, must be null if not used)
	 */
	private String getApiPath(ApiDefinition apiDefinition, String objectId, String relatedEntityId) {
		if (apiDefinition == null) {
			throw new IllegalArgumentException("Api definition cannot be null");
		}
		// Get the API's end point
		String urlPath = apiDefinition.getEndPoint();

		// Build URL path as per definition pattern
		// For APIs requiring an object ID, add object id's value after the api's end point
		if (apiDefinition.isObjectIdNeeded()) {
			if (objectId == null || objectId.trim().isEmpty()) {
				throw new IllegalArgumentException("Object ID cannot be null or empty");
			}
			// Add object id
			urlPath = urlPath + "/" + objectId;
		}
		// If a 'relatedEntity' part of the request was provided - add it too.
		// For example, adding 'transaction' to make "/loans/12233/transaction"
		String relatedEntity = apiDefinition.getRelatedEntity();
		if (relatedEntity != null && relatedEntity.length() > 0) {
			urlPath = urlPath + "/" + relatedEntity;
			// Add related entity ID, if provided
			if (relatedEntityId != null && relatedEntityId.length() > 0) {
				// Add Related Entity Id
				urlPath = urlPath + "/" + relatedEntityId;
			}
		}

		// Use URL helper to return the final URL path string
		return mambuAPIService.createUrl(urlPath);

	}

	/****
	 * Get Object represented by Mambu's JSON response string
	 * 
	 * @param jsonResponse
	 *            JSON response string
	 * @param objectClass
	 *            class representing this object
	 * @return object the returned object can be cast to the objectClass by the calling methods
	 */
	@SuppressWarnings("unchecked")
	private <R> R getObject(String jsonResponse, Class<?> objectClass) {
		return (R) GsonUtils.createGson().fromJson(jsonResponse, objectClass);
	}

	/****
	 * Get a list of Objects represented by Mambu's JSON response string
	 * 
	 * @param jsonResponse
	 *            JSON response string
	 * @param collectionType
	 *            collection type representing the list of objects
	 * 
	 * @return object this object represents a list of entities and must be case to the object's list type
	 */
	private <R> R getCollection(String jsonResponse, Type collectionType) {
		return GsonUtils.createGson().fromJson(jsonResponse, collectionType);
	}

	/****
	 * Get a boolean value represented by Mambu's response string
	 * 
	 * @param jsonResponse
	 *            JSON response string
	 * 
	 */
	private Boolean getBoolean(String jsonResponse) {

		MambuApiResponseMessage response = new MambuApiResponseMessage(jsonResponse);
		if (response.getReturnCode() == 0) {
			return true;
		}
		return false;
	}

	// Collection Types Map: it maps Mambu class T to its List<T> type (TypeToken<List<T>>(){}.getType())
	// New entries shall be added to this map when creating wrappers returning lists for classes not present in this map
	private final static Map<Class<?>, Type> collectionTypesMap;
	static {
		collectionTypesMap = new HashMap<Class<?>, Type>();
		// Client
		collectionTypesMap.put(Client.class, new TypeToken<List<Client>>() {
		}.getType());
		// ClientExpanded
		collectionTypesMap.put(ClientExpanded.class, new TypeToken<List<ClientExpanded>>() {
		}.getType());
		// Group
		collectionTypesMap.put(Group.class, new TypeToken<List<Group>>() {
		}.getType());
		// GroupExpanded
		collectionTypesMap.put(GroupExpanded.class, new TypeToken<List<GroupExpanded>>() {
		}.getType());
		// LoanAccount
		collectionTypesMap.put(LoanAccount.class, new TypeToken<List<LoanAccount>>() {
		}.getType());
		// LoanAccountExpanded
		collectionTypesMap.put(LoanAccountExpanded.class, new TypeToken<List<LoanAccountExpanded>>() {
		}.getType());
		// LoanTransaction
		collectionTypesMap.put(LoanTransaction.class, new TypeToken<List<LoanTransaction>>() {
		}.getType());
		// SavingsAccount
		collectionTypesMap.put(SavingsAccount.class, new TypeToken<List<SavingsAccount>>() {
		}.getType());
		// JSONSavingsAccount
		collectionTypesMap.put(JSONSavingsAccount.class, new TypeToken<List<JSONSavingsAccount>>() {
		}.getType());
		// SavingsTransaction
		collectionTypesMap.put(SavingsTransaction.class, new TypeToken<List<SavingsTransaction>>() {
		}.getType());
		// Repayment
		collectionTypesMap.put(Repayment.class, new TypeToken<List<Repayment>>() {
		}.getType());
		// LoanProduct
		collectionTypesMap.put(LoanProduct.class, new TypeToken<List<LoanProduct>>() {
		}.getType());
		// SavingsProduct
		collectionTypesMap.put(SavingsProduct.class, new TypeToken<List<SavingsProduct>>() {
		}.getType());
		// Branch
		collectionTypesMap.put(Branch.class, new TypeToken<List<Branch>>() {
		}.getType());
		// Centre
		collectionTypesMap.put(Centre.class, new TypeToken<List<Centre>>() {
		}.getType());
		// User
		collectionTypesMap.put(User.class, new TypeToken<List<User>>() {
		}.getType());
		// Currency
		collectionTypesMap.put(Currency.class, new TypeToken<List<Currency>>() {
		}.getType());
		// CustomFieldSet
		collectionTypesMap.put(CustomFieldSet.class, new TypeToken<List<CustomFieldSet>>() {
		}.getType());
		// CustomField
		collectionTypesMap.put(CustomField.class, new TypeToken<List<CustomField>>() {
		}.getType());
		// Task
		collectionTypesMap.put(Task.class, new TypeToken<List<Task>>() {
		}.getType());
		// CustomView
		collectionTypesMap.put(CustomView.class, new TypeToken<List<CustomView>>() {
		}.getType());
		// JSONActivity
		collectionTypesMap.put(JSONActivity.class, new TypeToken<List<JSONActivity>>() {
		}.getType());
		// Document
		collectionTypesMap.put(Document.class, new TypeToken<List<Document>>() {
		}.getType());
		// TransactionChannel
		collectionTypesMap.put(TransactionChannel.class, new TypeToken<List<TransactionChannel>>() {
		}.getType());
		// SearchResult. Note Search API returns Map<SearchResult.Type, List<SearchResult>>
		collectionTypesMap.put(SearchResult.class, new TypeToken<Map<SearchResult.Type, List<SearchResult>>>() {
		}.getType());
		// Indicator. Note Indicator API returns HashMap<String, String>
		collectionTypesMap.put(Indicator.class, new TypeToken<HashMap<String, String>>() {
		}.getType());

	}

	//
	/****
	 * Get Type for the collection of objects returned by API for the specified class (e.g. List<Class<?>> )
	 * 
	 * @param clazz
	 *            class name for the object associated with the collection
	 * 
	 */
	public static Type getCollectionType(Class<?> clazz) {

		if (clazz == null) {
			throw new IllegalArgumentException("Class Name cannot be null");
		}

		if (!collectionTypesMap.containsKey(clazz)) {
			throw new IllegalArgumentException("Class Name " + clazz + " is not handled by getCollectionType");
		}
		return collectionTypesMap.get(clazz);

	}

}
