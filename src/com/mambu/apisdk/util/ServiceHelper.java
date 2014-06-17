package com.mambu.apisdk.util;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
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
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.core.shared.model.User;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanTransaction;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;
import com.mambu.savings.shared.model.SavingsTransaction;
import com.mambu.tasks.shared.model.Task;

/**
 * Services Helper class provides common services to all other API Service classes. It uses supplied ApiDefintion for a
 * particular request to build required url, execute https request, with the specified methods and and content type, and
 * then parse Mambu response into the requested type and return the result to the caller.
 * 
 * Other services can user ServiceHelpewr method to execute their API requests. A typical pattern for using
 * ServiceHelper could be: a) define the ApiDefintion for an API request b) call serviceHelper.execute(apiDefintion,
 * parameters...)
 * 
 * Usage Example:
 * 
 * . If LoanService needs to execute get Loan Account details request, then:
 * 
 * 1.a Create serviceHelper = new ServiceHelper(mambuAPIService)
 * 
 * 1.b Create new ApiDefinition(ApiType.GetEntityDetails, LoanAccount.class);
 * 
 * 1.c serviceHelper.execute(ApiDefinition, accountId);
 * 
 * 2. If Client Service needs to execute get Client Details request, then:
 * 
 * 2.a Create serviceHelper = new ServiceHelper(mambuAPIService)
 * 
 * 2.b Create new ApiDefinition(ApiType.GetEntityDetails, Client.class);
 * 
 * 2.c serviceHelper.execute(ApiDefinition, clientId);
 * 
 * 
 * @author mdanilkis
 * 
 */
public class ServiceHelper {

	private MambuAPIService mambuAPIService;

	/***
	 * Create a new ServiceHelper
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	public ServiceHelper(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/****
	 * Execute API Json Post Request using its ApiDefinition and supplied input data. Used for Json create and update
	 * requests.
	 * 
	 * @param apiDefinition
	 *            api definition for the request
	 * @param object
	 *            object to be created
	 * @param objectId
	 *            object's id (optional, could be null if not used, for example for json create requests)
	 * 
	 * @return object a result object, which will be an api specific object or a list of objects
	 * 
	 * @throws MambuApiException
	 */
	public Object executeJson(ApiDefinition apiDefinition, Object object, String objectId) throws MambuApiException {

		if (object == null) {
			throw new IllegalArgumentException("Json object must not be NULL");
		}

		// Parse input object into a Josn string
		final String dateTimeFormat = APIData.yyyyMmddFormat;
		final String jsonData = GsonUtils.createGson(dateTimeFormat).toJson(object, object.getClass());

		// Add json string as JSON_OBJECT to the ParamsMap
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(APIData.JSON_OBJECT, jsonData);

		// Execute this request with apiDefintion, objectId and paramsMap
		return execute(apiDefinition, objectId, paramsMap);

	}

	/****
	 * Execute API Request using its ApiDefinition and supplied input data.
	 * 
	 * @param apiDefinition
	 *            api definition for the request
	 * @param objectId
	 *            api's object id (optional, must be null if not used)
	 * @param paramsMap
	 *            map with api parameters
	 * 
	 * @return object result object, which will be an api specific object or a list of objects
	 * 
	 * @throws MambuApiException
	 */
	public Object execute(ApiDefinition apiDefinition, String objectId, ParamsMap paramsMap) throws MambuApiException {

		if (apiDefinition == null) {
			throw new IllegalArgumentException("ApiDefinition cannot be NULL");

		}
		// System.out.println("executeRequest: API type" + apiDefinition.getApiType());

		// Create URL for this API request using specification in its apiDefintion
		String urlString = makeUrl(apiDefinition, objectId);

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
		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, method, contentType);

		// Process API Response. Get the return format and returnClass from the apiDefintion
		Class<?> returnClass = apiDefinition.getReturnClass();
		ApiReturnFormat returnFormat = apiDefinition.getApiReturnFormat();

		Object result = null;
		switch (returnFormat) {
		case OBJECT:
			// Get Single Object from the response
			result = getObject(jsonResponse, returnClass);
			break;
		case COLLECTION:
			// Get a list of Objects from the response
			Type collectionType = getCollectionType(returnClass);
			// System.out.println("executeRequest: collection type=" + collectionType);
			// Get result as a collection
			result = getCollection(jsonResponse, collectionType);
			break;
		case BOOLEAN:
			// Get result as boolean
			result = getBoolean(jsonResponse);
			break;
		case RESPONSE_STRING:
			// Return the response string as is, with no additional processing.
			// This can be used for the service to perform any subsequent processing or for such APIs as getDocument()
			result = jsonResponse;
			break;
		}

		return result;
	}

	// Convenience method for api requests without Params map parameter
	/****
	 * Execute API Request using its ApiDefinition and supplied input data. This version of the execute method can be
	 * used when the API request doesn't use ParamsMap, for example get entity details api request
	 * 
	 * @param apiDefinition
	 *            api definition for the request
	 * @param objectId
	 *            api's object id (optional, must be null if not used)
	 * 
	 * @return object result object, which will be an api specific object or a list of objects
	 * 
	 * @throws MambuApiException
	 */
	public Object execute(ApiDefinition apiDefinition, String objectId) throws MambuApiException {
		ParamsMap paramsMap = null;
		return execute(apiDefinition, objectId, paramsMap);
	}

	// Convenience method for api requests without object ID parameter
	/****
	 * Execute API Request using its ApiDefinition and supplied input data. This version of the execute method can be
	 * used when the API request doesn't use object ID (for example in get list requests)
	 * 
	 * @param api
	 *            api definition for the request
	 * @param paramsMap
	 *            map with api parameters
	 * 
	 * @return object result object, which will be an api specific object or a list of objects
	 * 
	 * @throws MambuApiException
	 */
	public Object execute(ApiDefinition apiDefinition, ParamsMap paramsMap) throws MambuApiException {
		String objectId = null;
		return execute(apiDefinition, objectId, paramsMap);
	}

	// // Private Helper methods ////
	/****
	 * Make url string for the api request based on the request's ApiDefinition. The Url is made to comply with the
	 * following url pattern: endPoint/objectId/operation. ApiDefinition for the request determined which URL parts are
	 * required for this request.
	 * 
	 * @param apiDefinition
	 *            Api Definition for the API request
	 * @param objectId
	 *            object ID for the API request. It's optional and can be null, if allowed by request's apiDefinition
	 * 
	 */
	private String makeUrl(ApiDefinition apiDefinition, String objectId) {
		if (apiDefinition == null) {
			throw new IllegalArgumentException("Api definition cannot be null");
		}
		// Get the API's end point
		String url = apiDefinition.getEndPoint();

		// Build URL as per definition pattern
		// System.out.println("makeUrl: end point=" + url + " Object Id=" + objectId + " (need ObjectId="
		// + apiDefinition.getNeedObjectId() + ") Operation=" + apiDefinition.getOperation());

		// For API requiring object ID add object id's value after the api's end point
		if (apiDefinition.getNeedObjectId()) {
			if (objectId == null || objectId.trim().isEmpty()) {
				throw new IllegalArgumentException("Object ID cannot be null or empty");
			}
			// Add object id
			url = url + "/" + objectId;
		}
		// If an 'operation' part of the request was provided - add it too.
		// For example, adding "transaction to make "loans/12233/transaction"
		String operation = apiDefinition.getOperation();
		if (operation != null && operation.length() > 0) {
			url = url + "/" + operation;
		}

		// User URL helper to build final URL string
		String urlString = new String(mambuAPIService.createUrl(url));

		return urlString;

	}

	/****
	 * Get Object represented by Mambu's json response string
	 * 
	 * @param jsonResponse
	 *            Json response string
	 * @param objectClass
	 *            class representing this object
	 * @return object the returned object can be cast to the objectClass by the calling methiods
	 */
	private Object getObject(String jsonResponse, Class<?> objectClass) {

		// return GsonUtils.createGson().fromJson(jsonResponse, objectClass);
		return GsonUtils.createGson().fromJson(jsonResponse, objectClass);

	}

	/****
	 * Get a list of Objects represented by Mambu's json response string
	 * 
	 * @param jsonResponse
	 *            Json response string
	 * @param collectionType
	 *            collection type representing the list of objects
	 * 
	 * @return object this object represents a list of entities and must be case to the object's list type
	 */
	private Object getCollection(String jsonResponse, Type collectionType) {

		return GsonUtils.createGson().fromJson(jsonResponse, collectionType);
	}

	/****
	 * Get a boolean value represented by Mambu's response string
	 * 
	 * @param jsonResponse
	 *            Json response string
	 * 
	 */
	private Boolean getBoolean(String jsonResponse) {

		MambuApiResponseMessage response = new MambuApiResponseMessage(jsonResponse);
		if (response.getReturnCode() == 0) {
			return true;
		}
		return false;
	}

	//
	//
	/****
	 * Get Type for the List collection of objects of the specified class (i.e. List<className> )
	 * 
	 * Note: As the list of handled class names is predefined here, this method may need to be updated if any new
	 * entities are to be stored as collections in Mambu APIs
	 * 
	 * @param clazz
	 *            class name for the object on the list collection
	 * 
	 */
	private Type getCollectionType(Class<?> clazz) {

		if (clazz == null) {
			throw new IllegalArgumentException("Class Name cannot be null");
		}

		Type collectionType = null;

		// Accounts
		// LoanAccount
		if (clazz.equals(LoanAccount.class)) {
			collectionType = new TypeToken<List<LoanAccount>>() {
			}.getType();
			return collectionType;
		}
		if (clazz.equals(LoanAccountExpanded.class)) {
			collectionType = new TypeToken<List<LoanAccountExpanded>>() {
			}.getType();
			return collectionType;
		}
		if (clazz.equals(LoanTransaction.class)) {
			collectionType = new TypeToken<List<LoanTransaction>>() {
			}.getType();
			return collectionType;
		}
		// SavingsAccount
		if (clazz.equals(SavingsAccount.class)) {
			collectionType = new TypeToken<List<SavingsAccount>>() {
			}.getType();
			return collectionType;
		}

		// SavingsAccount
		if (clazz.equals(JSONSavingsAccount.class)) {
			collectionType = new TypeToken<List<JSONSavingsAccount>>() {
			}.getType();
			return collectionType;
		}
		if (clazz.equals(SavingsTransaction.class)) {
			collectionType = new TypeToken<List<SavingsTransaction>>() {
			}.getType();
			return collectionType;
		}
		// Client
		if (clazz.equals(Client.class)) {
			collectionType = new TypeToken<List<Client>>() {
			}.getType();
			return collectionType;
		}

		// ClientExpanded
		if (clazz.equals(ClientExpanded.class)) {
			collectionType = new TypeToken<List<ClientExpanded>>() {
			}.getType();
			return collectionType;
		}

		// Group
		if (clazz.equals(Group.class)) {
			collectionType = new TypeToken<List<Group>>() {
			}.getType();
			return collectionType;
		}
		// GroupExpanded
		if (clazz.equals(GroupExpanded.class)) {
			collectionType = new TypeToken<List<GroupExpanded>>() {
			}.getType();
			return collectionType;
		}

		// Organizational entities
		// Branch
		if (clazz.equals(Branch.class)) {
			collectionType = new TypeToken<List<Branch>>() {
			}.getType();
			return collectionType;
		}
		// Centre
		if (clazz.equals(Centre.class)) {
			collectionType = new TypeToken<List<Centre>>() {
			}.getType();
			return collectionType;
		}
		// User
		if (clazz.equals(User.class)) {
			collectionType = new TypeToken<List<User>>() {
			}.getType();
			return collectionType;
		}
		// Currency
		if (clazz.equals(Currency.class)) {
			collectionType = new TypeToken<List<Currency>>() {
			}.getType();
			return collectionType;
		}

		// Custom Field Sets
		if (clazz.equals(CustomFieldSet.class)) {
			collectionType = new TypeToken<List<CustomFieldSet>>() {
			}.getType();
			return collectionType;
		}

		// Products
		// LoanProduct
		if (clazz.equals(LoanProduct.class)) {
			collectionType = new TypeToken<List<LoanProduct>>() {
			}.getType();
			return collectionType;
		}
		// SavingsProduct
		if (clazz.equals(SavingsProduct.class)) {
			collectionType = new TypeToken<List<SavingsProduct>>() {
			}.getType();
			return collectionType;
		}

		// Task
		if (clazz.equals(Task.class)) {
			collectionType = new TypeToken<List<Task>>() {
			}.getType();
			return collectionType;
		}

		throw new IllegalArgumentException("Class Name " + clazz + " is not handled by getCollectionType");

	}
}
