package com.mambu.apisdk.util;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.mambu.accounting.shared.model.GLAccount;
import com.mambu.accounting.shared.model.GLJournalEntry;
import com.mambu.accounts.shared.model.Account;
import com.mambu.accounts.shared.model.DocumentTemplate;
import com.mambu.accounts.shared.model.TransactionChannel;
import com.mambu.admin.shared.model.ExchangeRate;
import com.mambu.api.server.handler.activityfeed.model.JSONActivity;
import com.mambu.api.server.handler.loan.model.JSONLoanAccount;
import com.mambu.api.server.handler.loan.model.JSONTransactionRequest;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.exception.MambuApiResponseMessage;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.clients.shared.model.GroupRoleName;
import com.mambu.clients.shared.model.IdentificationDocumentTemplate;
import com.mambu.core.shared.model.ClientRole;
import com.mambu.core.shared.model.Comment;
import com.mambu.core.shared.model.Currency;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldSet;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.CustomView;
import com.mambu.core.shared.model.ObjectLabel;
import com.mambu.core.shared.model.Role;
import com.mambu.core.shared.model.SearchResult;
import com.mambu.core.shared.model.SearchType;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;
import com.mambu.intelligence.shared.model.Intelligence.Indicator;
import com.mambu.linesofcredit.shared.model.LineOfCredit;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanTransaction;
import com.mambu.loans.shared.model.Repayment;
import com.mambu.notifications.shared.model.NotificationMessage;
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
	@Inject
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

		// Process API Response. Get the return format from the apiDefintion
		ApiReturnFormat returnFormat = apiDefinition.getApiReturnFormat();

		ByteArrayOutputStream byteArrayOutputStreamResult = null;
		String jsonResponse = null;

		// Use mambuAPIService to execute request
		switch (returnFormat) {
		case ZIP_ARCHIVE:
			byteArrayOutputStreamResult = mambuAPIService.executeRequest(apiUrlPath, paramsMap, apiDefinition);
			break;
		default:
			jsonResponse = mambuAPIService.executeRequest(apiUrlPath, paramsMap, method, contentType);
			break;
		}

		R result = null;
		switch (returnFormat) {
		case OBJECT:
			// Get Single Object from the response
			result = getObject(jsonResponse, apiDefinition);
			break;
		case COLLECTION:
			// Get result as a collection
			result = getCollection(jsonResponse, apiDefinition);
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
		case ZIP_ARCHIVE:
			result = (R) byteArrayOutputStreamResult;
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
	 * @param apiDefinition
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
	 * @param paramsMap
	 *            params map with additional parameters. could be null. Currently used to provided optional pagination
	 *            params
	 * @return object a result object, which will be an API specific object
	 * 
	 * @throws MambuApiException
	 */
	public <R, T> R executeJson(ApiDefinition apiDefinition, T object, String objectId, String relatedEntityId,
			ParamsMap paramsMap) throws MambuApiException {

		if (object == null) {
			throw new IllegalArgumentException("JSON object must not be NULL");
		}

		// Make API JSON string based on its ApiDefinition
		final String jsonData = ServiceHelper.makeApiJson(object, apiDefinition);
		// Add JSON string as JSON_OBJECT to the ParamsMap
		if (paramsMap == null) {
			paramsMap = new ParamsMap();
		}
		paramsMap.put(APIData.JSON_OBJECT, jsonData);

		// Execute this request with apiDefintion, objectId, relatedEntityId and paramsMap
		return execute(apiDefinition, objectId, relatedEntityId, paramsMap);

	}

	/****
	 * Convenience method for executing API JSON Post Request using its ApiDefinition, supplied object and object ID.
	 * Can be used for JSON requests which do not require related entity id
	 * 
	 * @param apiDefinition
	 *            API definition for the request
	 * @param object
	 *            the Mambu object to be created.
	 * @param objectId
	 *            object's id (optional, could be null if not used, for example for JSON create requests)
	 * 
	 * @return object a result object, which will be an API specific object
	 * @throws MambuApiException
	 */
	public <R, T> R executeJson(ApiDefinition apiDefinition, T object, String objectId) throws MambuApiException {

		String relatedEntityId = null;
		ParamsMap paramsMap = null;
		return executeJson(apiDefinition, object, objectId, relatedEntityId, paramsMap);
	}

	/****
	 * Convenience method for executing API JSON Post Request using its ApiDefinition and supplied object. Can be used
	 * for JSON requests which do not require objectId and related entity id parameters
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
		String relatedEntityId = null;
		ParamsMap paramsMap = null;
		return executeJson(apiDefinition, object, objectId, relatedEntityId, paramsMap);
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
		String urlPath = apiDefinition.getUrlPath();
		if (urlPath != null) {
			// We have URL path provided
			return mambuAPIService.createUrl(urlPath);
		}
		// Make URL path from the Api Definition
		urlPath = apiDefinition.getEndPoint();

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
		if (relatedEntity != null && !relatedEntity.isEmpty()) {
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
	private <R> R getObject(String jsonResponse, ApiDefinition apiDefinition) {

		// Create Gson with optional deserializers as per ApiDefinition
		Gson gson = GsonUtils.createDeserializerGson(apiDefinition);
		// Get return class from ApiDefinition
		Class<?> returnClass = apiDefinition.getReturnClass();
		// Get object from jsonResponse
		return (R) gson.fromJson(jsonResponse, returnClass);
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
	private <R> R getCollection(String jsonResponse, ApiDefinition apiDefinition) {

		// Create Gson with optional deserializers as per ApiDefinition
		Gson gson = GsonUtils.createDeserializerGson(apiDefinition);
		Class<?> returnClass = apiDefinition.getReturnClass();
		// Get return class from ApiDefinition and make a collection type for it
		Type collectionType = getCollectionType(returnClass);
		// Get collection of objects from jsonResponse
		return gson.fromJson(jsonResponse, collectionType);
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
		collectionTypesMap.put(Client.class, new TypeToken<List<Client>>() {}.getType());
		// ClientExpanded
		collectionTypesMap.put(ClientExpanded.class, new TypeToken<List<ClientExpanded>>() {}.getType());
		// Group
		collectionTypesMap.put(Group.class, new TypeToken<List<Group>>() {}.getType());
		// GroupExpanded
		collectionTypesMap.put(GroupExpanded.class, new TypeToken<List<GroupExpanded>>() {}.getType());
		// LoanAccount
		collectionTypesMap.put(LoanAccount.class, new TypeToken<List<LoanAccount>>() {}.getType());
		// JSONLoanAccount
		collectionTypesMap.put(JSONLoanAccount.class, new TypeToken<List<JSONLoanAccount>>() {}.getType());
		// LoanTransaction
		collectionTypesMap.put(LoanTransaction.class, new TypeToken<List<LoanTransaction>>() {}.getType());
		// SavingsAccount
		collectionTypesMap.put(SavingsAccount.class, new TypeToken<List<SavingsAccount>>() {}.getType());
		// JSONSavingsAccount
		collectionTypesMap.put(JSONSavingsAccount.class, new TypeToken<List<JSONSavingsAccount>>() {}.getType());
		// SavingsTransaction
		collectionTypesMap.put(SavingsTransaction.class, new TypeToken<List<SavingsTransaction>>() {}.getType());
		// Repayment
		collectionTypesMap.put(Repayment.class, new TypeToken<List<Repayment>>() {}.getType());
		// LoanProduct
		collectionTypesMap.put(LoanProduct.class, new TypeToken<List<LoanProduct>>() {}.getType());
		// SavingsProduct
		collectionTypesMap.put(SavingsProduct.class, new TypeToken<List<SavingsProduct>>() {}.getType());
		// Branch
		collectionTypesMap.put(Branch.class, new TypeToken<List<Branch>>() {}.getType());
		// Centre
		collectionTypesMap.put(Centre.class, new TypeToken<List<Centre>>() {}.getType());
		// User
		collectionTypesMap.put(User.class, new TypeToken<List<User>>() {}.getType());
		// Currency
		collectionTypesMap.put(Currency.class, new TypeToken<List<Currency>>() {}.getType());
		// CustomFieldSet
		collectionTypesMap.put(CustomFieldSet.class, new TypeToken<List<CustomFieldSet>>() {}.getType());
		// CustomField
		collectionTypesMap.put(CustomField.class, new TypeToken<List<CustomField>>() {}.getType());
		// Task
		collectionTypesMap.put(Task.class, new TypeToken<List<Task>>() {}.getType());
		// CustomView
		collectionTypesMap.put(CustomView.class, new TypeToken<List<CustomView>>() {}.getType());
		// JSONActivity
		collectionTypesMap.put(JSONActivity.class, new TypeToken<List<JSONActivity>>() {}.getType());
		// Document
		collectionTypesMap.put(Document.class, new TypeToken<List<Document>>() {}.getType());
		// TransactionChannel
		collectionTypesMap.put(TransactionChannel.class, new TypeToken<List<TransactionChannel>>() {}.getType());
		// SearchResult. Note Search API returns Map<SearchType, List<SearchResult>>
		collectionTypesMap.put(SearchResult.class, new TypeToken<Map<SearchType, List<SearchResult>>>() {}.getType());
		// Indicator. Note Indicator API returns HashMap<String, String>
		collectionTypesMap.put(Indicator.class, new TypeToken<HashMap<String, String>>() {}.getType());
		// ClientRole
		collectionTypesMap.put(ClientRole.class, new TypeToken<List<ClientRole>>() {}.getType());
		// Group Role
		collectionTypesMap.put(GroupRoleName.class, new TypeToken<List<GroupRoleName>>() {}.getType());
		// Comment
		collectionTypesMap.put(Comment.class, new TypeToken<List<Comment>>() {}.getType());
		// IdentificationDocumentTemplate
		collectionTypesMap.put(IdentificationDocumentTemplate.class,
				new TypeToken<List<IdentificationDocumentTemplate>>() {}.getType());
		// DocuementTemplate
		collectionTypesMap.put(DocumentTemplate.class, new TypeToken<List<DocumentTemplate>>() {}.getType());
		// Object Labels
		collectionTypesMap.put(ObjectLabel.class, new TypeToken<List<ObjectLabel>>() {}.getType());
		// Lines of Credit
		collectionTypesMap.put(LineOfCredit.class, new TypeToken<List<LineOfCredit>>() {}.getType());
		// GLJournalEntry
		collectionTypesMap.put(GLJournalEntry.class, new TypeToken<List<GLJournalEntry>>() {}.getType());
		// GLAccount
		collectionTypesMap.put(GLAccount.class, new TypeToken<List<GLAccount>>() {}.getType());
		// Role
		collectionTypesMap.put(Role.class, new TypeToken<List<Role>>() {}.getType());
		// NotificationMessage
		collectionTypesMap.put(NotificationMessage.class, new TypeToken<List<NotificationMessage>>() {}.getType());
		// Exchange Rates. See MBU-12628
		collectionTypesMap.put(ExchangeRate.class, new TypeToken<List<ExchangeRate>>() {}.getType());
		//
		collectionTypesMap.put(CustomFieldValue.class, new TypeToken<List<CustomFieldValue>>() {}.getType());
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

	// A set of convenience helper methods to perform standard API requests by specifying Mambu entities
	// Note: These methods throw exception if a requested API operation is not supported by Mambu. Users of these these
	// methods must ensure they are invoked for the supported API operations only

	/**
	 * Get a list of entities owned by a parent entity. For example GET all documents for a client or for a loan account
	 * 
	 * @param parentEntity
	 *            parent's MambuEntityType. Example MambuEntityType.CLIENT
	 * @param parentId
	 *            encoded key or id of the parent entity
	 * @param ownedEntity
	 *            Mambu owned entity. Example, MambuEntityType.COMMENT
	 * @param relatedEntityId
	 *            related entity id. Can be null if not required
	 * @param params
	 *            params map with filtering parameters
	 * @param requiresFullDetails
	 *            boolean flag indicates whether the full or object is wanted
	 * @return list of owned entities
	 * @throws MambuApiException
	 */
	public <R> R getOwnedEntities(MambuEntityType parentEntity, String parentId, MambuEntityType ownedEntity,
			String relatedEntityId, ParamsMap params, boolean requiresFullDetails) throws MambuApiException {

		if (parentEntity == null || ownedEntity == null) {
			throw new IllegalArgumentException("Parent Entity and Owned Entity cannot be null");
		}
		Class<?> parentClass = parentEntity.getEntityClass();
		Class<?> ownedClass = ownedEntity.getEntityClass();

		ApiDefinition  apiDefinition = createApiDefinitionForGetOwnedEntities(requiresFullDetails, parentClass, ownedClass);
		
		return execute(apiDefinition, parentId, relatedEntityId, params);
	}

	/**
	 * Helper method, creates the ApiDefinition used in obtaining the owned entities
	 * 
	 * @param requiresFullDetails
	 *            boolean flag indicates whether the full or basic object version is wanted
	 * @param parentClass
	 *            the parent class
	 * @param ownedClass
	 *            the owned class
	 * @return newly created ApiDefinition
	 */
	private ApiDefinition createApiDefinitionForGetOwnedEntities(boolean requiresFullDetails, Class<?> parentClass,
			Class<?> ownedClass) {

		ApiDefinition apiDefinition;
		
		if(requiresFullDetails){
			apiDefinition = new ApiDefinition(ApiType.GET_OWNED_ENTITIES_WITH_DETAILS, parentClass, ownedClass);
		}else{
			apiDefinition = new ApiDefinition(ApiType.GET_OWNED_ENTITIES, parentClass, ownedClass);
		}
		
		return apiDefinition;
	}

	/**
	 * Convenience method to Get a list of entities owned by a parent entity by specifying only pagination parameters
	 * offset and limit. For example GET all comments for a client or for a loan account with offset and limit
	 * 
	 * @param parentEntity
	 *            parent's MambuEntityType. Example MambuEntityType.CLIENT
	 * @param parentId
	 *            encoded key or id of the parent entity
	 * @param ownedEntity
	 *            Mambu owned entity. Example, MambuEntityType.COMMENT
	 * @param offset
	 *            pagination offset
	 * @param limit
	 *            pagination limit
	 * @param requiresFullDetails boolean flag indicating whether a full or basic object is wanted
	 * @return list of owned entities
	 * @throws MambuApiException
	 */
	public <R> R getOwnedEntities(MambuEntityType parentEntity, String parentId, MambuEntityType ownedEntity,
			Integer offset, Integer limit, boolean requiresFullDetails) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		if (offset != null) {
			params.addParam(APIData.OFFSET, String.valueOf(offset));
		}
		if (limit != null) {
			params.addParam(APIData.LIMIT, String.valueOf(limit));
		}

		return getOwnedEntities(parentEntity, parentId, ownedEntity, null, params, requiresFullDetails);

	}
	
	
	/**
	 * Convenience method to Get a list of entities owned by a parent entity by specifying only pagination parameters
	 * offset and limit. For example GET all comments for a client or for a loan account with offset and limit
	 * 
	 * @param parentEntity
	 *            parent's MambuEntityType. Example MambuEntityType.CLIENT
	 * @param parentId
	 *            encoded key or id of the parent entity
	 * @param ownedEntity
	 *            Mambu owned entity. Example, MambuEntityType.COMMENT
	 * @param offset
	 *            pagination offset
	 * @param limit
	 *            pagination limit
	 * @return list of owned entities
	 * @throws MambuApiException
	 */
	public <R> R getOwnedEntities(MambuEntityType parentEntity, String parentId, MambuEntityType ownedEntity,
			Integer offset, Integer limit) throws MambuApiException {

		return getOwnedEntities(parentEntity, parentId, ownedEntity, offset, limit, false);

	}

	/**
	 * Get owned entity. For example GET all documents for a client or for a loan account
	 * 
	 * @param parentEntity
	 *            parent's MambuEntityType. Example MambuEntityType.CLIENT
	 * @param parentId
	 *            encoded key or id of the parent entity
	 * @param ownedEntity
	 *            Mambu owned entity. Example, MambuEntityType.COMMENT
	 * @param ownedEntityId
	 *            encoded key or id of the owned entity. Optional. Can be null. Example GET
	 *            /api/loanproducts/{ID}/schedule
	 * @param params
	 *            params map with filtering parameters. Optional. Can be null.
	 * 
	 * @return owned entity
	 * @throws MambuApiException
	 */
	public <R> R getOwnedEntity(MambuEntityType parentEntity, String parentId, MambuEntityType ownedEntity,
			String ownedEntityId, ParamsMap params) throws MambuApiException {

		if (parentEntity == null) {
			throw new IllegalArgumentException("Parent Entity cannot be null");
		}
		Class<?> parentClass = parentEntity.getEntityClass();
		Class<?> ownedClass = ownedEntity.getEntityClass();

		ApiDefinition apiDefinition = new ApiDefinition(ApiType.GET_OWNED_ENTITY, parentClass, ownedClass);
		return execute(apiDefinition, parentId, ownedEntityId, params);
	}

	/**
	 * Create new entity owned by a parent entity. For example. Create new document for a client or for a loan account.
	 * This method is used for API returning objects of the same class as posted entity
	 * 
	 * @param parentEntity
	 *            parent entity
	 * @param parentId
	 *            encoded key or id of the parent entity
	 * @param postEntity
	 *            posted entity
	 * @return updated posted entity
	 * @throws MambuApiException
	 */
	public <T> T createOwnedEntity(MambuEntityType parentEntity, String parentId, T postEntity)
			throws MambuApiException {

		if (parentEntity == null || postEntity == null) {
			throw new IllegalArgumentException("Parent Class and Owned Entity cannot be null");
		}

		Class<?> parentClass = parentEntity.getEntityClass();
		Class<?> ownedEntityClass = postEntity.getClass();
		ApiDefinition postDefinition = new ApiDefinition(ApiType.POST_OWNED_ENTITY, parentClass, ownedEntityClass);
		postDefinition.setContentType(ContentType.JSON);

		return executeJson(postDefinition, postEntity, parentId);
	}

	/**
	 * Create new entity owned by a parent entity. For example. Create new document for a client or for a loan account.
	 * This method is used for API returning objects of a different class than the posted entity's class. The class of
	 * returned object must be specified in the call
	 * 
	 * @param parentEntity
	 *            parent entity
	 * @param parentId
	 *            encoded key or id of the parent entity
	 * @param postEntity
	 *            posted entity
	 * @param resultClass
	 *            the class of the Mambu model returned in response.
	 * @return updated posted entity
	 * @throws MambuApiException
	 */
	public <R, T> R createOwnedEntity(MambuEntityType parentEntity, String parentId, T postEntity, Class<?> resultClass)
			throws MambuApiException {

		if (parentEntity == null || postEntity == null) {
			throw new IllegalArgumentException("Parent Class and Owned Entity cannot be null");
		}

		Class<?> parentClass = parentEntity.getEntityClass();
		Class<?> ownedEntityClass = postEntity.getClass();
		ApiDefinition postDefinition = new ApiDefinition(ApiType.POST_OWNED_ENTITY, parentClass, ownedEntityClass,
				resultClass);
		postDefinition.setContentType(ContentType.JSON);

		return executeJson(postDefinition, postEntity, parentId);
	}

	/**
	 * Update existent owned entity. For example. Update document for a client or for a loan account
	 * 
	 * @param parentEntity
	 *            parent entity
	 * @param parentId
	 *            encoded key or id of the parent entity
	 * @param ownedEntityId
	 *            the encoded key or id of the entity being patched
	 * @return updated owned entity
	 * @throws MambuApiException
	 */
	public <R, T> R updateOwnedEntity(MambuEntityType parentEntity, String parentId, T ownedEntity,
			String ownedEntityId) throws MambuApiException {

		if (parentEntity == null || ownedEntity == null) {
			throw new IllegalArgumentException("Parent Class and Owned Entity cannot be null");
		}

		Class<?> parentClass = parentEntity.getEntityClass();
		Class<?> ownedEntityClass = ownedEntity.getClass();

		ApiDefinition patchEntity = new ApiDefinition(ApiType.PATCH_OWNED_ENTITY, parentClass, ownedEntityClass);
		ParamsMap paramsMap = null;
		return executeJson(patchEntity, ownedEntity, parentId, ownedEntityId, paramsMap);
	}

	/**
	 * Delete owned entity. For example. DELETE document for a client or for a loan account
	 * 
	 * @param parentEntity
	 *            parent entity
	 * @param parentId
	 *            encoded key or id of the parent entity
	 * @param ownedEntityId
	 *            the encoded key or id of the entity being patched
	 * @return true if successful
	 * @throws MambuApiException
	 */
	public <R> Boolean deleteOwnedEntity(MambuEntityType parentEntity, String parentId, MambuEntityType ownedEntity,
			String ownedEntityId) throws MambuApiException {

		Class<?> parentClass = parentEntity.getEntityClass();
		Class<?> ownedClass = ownedEntity.getEntityClass();
		// Create ApiDefinition for DELETE_OWNED_ENTITY
		ApiDefinition apiDefinition = new ApiDefinition(ApiType.DELETE_OWNED_ENTITY, parentClass, ownedClass);
		return execute(apiDefinition, parentId, ownedEntityId, null);
	}

	// Other helpers. Not used yet currently
	/**
	 * Get Mambu entity. Example: GET api/clients/{entityId}
	 * 
	 * @param mambuEntity
	 *            Mambu entity
	 * @param entityId
	 *            entity id or encoded key
	 * @return Mambu object
	 * @throws MambuApiException
	 */
	public <R> R getEntity(MambuEntityType mambuEntity, String entityId) throws MambuApiException {

		Class<?> clazz = mambuEntity.getEntityClass();
		ApiDefinition apiDefinition = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, clazz);
		return execute(apiDefinition, entityId);
	}

	/**
	 * Get a list of Mambu entities. Example; GET /api/groups (by branch and centre and pagination params)
	 * 
	 * @param mambuEntity
	 *            Mambu entity
	 * @param params
	 *            params map for getting a list of entities. The params map can contain all parameters specific to the
	 *            API, for example, pagination parameters, filter setting, branch and credit officer is, etc.
	 * @return list of entities
	 * @throws MambuApiException
	 */
	public <R> List<R> getList(MambuEntityType mambuEntity, ParamsMap params) throws MambuApiException {

		Class<?> clazz = mambuEntity.getEntityClass();
		ApiDefinition apiDefinition = new ApiDefinition(ApiType.GET_LIST, clazz);
		return execute(apiDefinition, params);
	}

	/**
	 * Convenience method to GET a paginated list of Mambu entities. Example; GET /api/clients?fullDetails=true with offset and limit
	 * 
	 * @param mambuEntity
	 *            Mambu entity
	 * @param offset
	 *            offset
	 * @param limit
	 *            limit
	 * @param requiresFullDetails
	 *            flag indicating if is a full call if true then 'fullDetails=true' parameter will be added to the call
	 * @return list of entities for the requested page
	 * @throws MambuApiException
	 */
	public <R> List<R> getPaginatedList(MambuEntityType mambuEntity, Integer offset, Integer limit,
			boolean requiresFullDetails) throws MambuApiException {

		Class<?> clazz = mambuEntity.getEntityClass();

		ParamsMap params = new ParamsMap();
		if (offset != null) {
			params.addParam(APIData.OFFSET, String.valueOf(offset));
		}
		if (limit != null) {
			params.addParam(APIData.LIMIT, String.valueOf(limit));
		}

		return execute(getApiDefinitionForPaginatedList(requiresFullDetails, clazz), params);
	}
	
	/**
	 * Convenience method to GET a paginated list of Mambu entities. Example; GET /api/clients with offset and limit
	 * 
	 * @param mambuEntity
	 *            Mambu entity
	 * @param offset
	 *            offset
	 * @param limit
	 *            limit
	 * @return list of entities for the requested page
	 * @throws MambuApiException
	 */
	public <R> List<R> getPaginatedList(MambuEntityType mambuEntity, Integer offset, Integer limit) throws MambuApiException {

		return getPaginatedList(mambuEntity, offset, limit, false);
	}

	/**
	 * Creates an ApiDefinition in order to get a list with basic or full details based on requiresFullDetails flag
	 * 
	 * @param fullDetails
	 *  flag indicating the call return type, a collection with full details or a collection with basic details 
	 * @param clazz
	 *            the class used to create the ApiDefinition
	 * @return newly created ApiDefinition
	 */
	private ApiDefinition getApiDefinitionForPaginatedList(boolean requiresFullDetails, Class<?> clazz) {

		ApiDefinition apiDefinition;

		if (requiresFullDetails) {
			apiDefinition = new ApiDefinition(ApiType.GET_LIST_WITH_DETAILS, clazz);
		} else {
			apiDefinition = new ApiDefinition(ApiType.GET_LIST, clazz);
		}
		return apiDefinition;
	}

	/**
	 * Create new entity
	 * 
	 * @param entity
	 *            entity to create
	 * @return created entity
	 * @throws MambuApiException
	 */
	public <R> R createEntity(Class<R> entity) throws MambuApiException {

		ApiDefinition apiDefinition = new ApiDefinition(ApiType.CREATE_JSON_ENTITY, entity.getClass());
		return executeJson(apiDefinition, entity);
	}

	/**
	 * Update entity
	 * 
	 * @param entity
	 *            entity to update
	 * @param entityId
	 *            entity id or encoded key
	 * @return updated entity
	 * @throws MambuApiException
	 */
	public <R> R updateEntity(Class<R> entity, String entityId) throws MambuApiException {

		ApiDefinition apiDefinition = new ApiDefinition(ApiType.POST_ENTITY, entity.getClass());
		return executeJson(apiDefinition, entity, entityId);
	}

	/**
	 * Delete entity
	 * 
	 * @param mambuEntity
	 *            mambu entity to delete
	 * @param entityId
	 *            id or encoded key of the entity to be deleted
	 * @return true if successfully deleted
	 * @throws MambuApiException
	 */
	public <R> Boolean deleteEntity(MambuEntityType mambuEntity, String entityId) throws MambuApiException {

		Class<?> clazz = mambuEntity.getEntityClass();
		ApiDefinition apiDefinition = new ApiDefinition(ApiType.DELETE_ENTITY, clazz);
		return executeJson(apiDefinition, entityId);
	}

	/**
	 * POST JSON Transaction Request
	 * 
	 * @param accountId
	 *            account id or encoded key. Must not be null
	 * @param request
	 *            JSON Transaction Request
	 * @param accountType
	 *            account type, LOAN or SAVINGS
	 * @param transactionTypeName
	 *            transaction type name. E.g. FEE, DISBURSEMENT, DEPOSIT
	 * @return loan transaction or savings transaction depending on accountType
	 * @throws MambuApiException
	 */
	public <R> R executeJSONTransactionRequest(String accountId, JSONTransactionRequest request,
			Account.Type accountType, String transactionTypeName) throws MambuApiException {

		if (request == null || transactionTypeName == null || accountType == null || accountId == null) {
			throw new IllegalArgumentException("All input parameters must not be null");
		}
		// Create Params Map containing JSON for the transaction request
		ParamsMap paramsMap = ServiceHelper.makeParamsForTransactionRequest(transactionTypeName, request);

		// Create API Definition specifying entity class and expected result class
		Class<?> entityClass = accountType == Account.Type.LOAN ? LoanAccount.class : SavingsAccount.class;
		Class<?> transactionClass = accountType == Account.Type.LOAN ? LoanTransaction.class : SavingsTransaction.class;

		// Make ApiDefinition to POST_OWNED_ENTITY using JSON format
		ApiDefinition postJsonAccountTransaction = new ApiDefinition(ApiType.POST_OWNED_ENTITY, entityClass,
				transactionClass);
		postJsonAccountTransaction.setContentType(ContentType.JSON);

		// Execute API request with ParamsMap containing JSON
		// Returns LoanTransaction or SavingsTransaction (depending on accountType),
		return execute(postJsonAccountTransaction, accountId, paramsMap);
	}
}
