package com.mambu.apisdk.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.ExclusionStrategy;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.mambu.accounting.shared.model.GLAccount;
import com.mambu.accounting.shared.model.GLJournalEntry;
import com.mambu.accounts.shared.model.DocumentTemplate;
import com.mambu.accounts.shared.model.TransactionChannel;
import com.mambu.accountsecurity.shared.model.Guaranty;
import com.mambu.accountsecurity.shared.model.InvestorFund;
import com.mambu.admin.shared.model.ExchangeRate;
import com.mambu.api.server.handler.activityfeed.model.JSONActivity;
import com.mambu.api.server.handler.coments.model.JSONComment;
import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.api.server.handler.linesofcredit.model.JSONLineOfCredit;
import com.mambu.api.server.handler.loan.model.JSONLoanAccount;
import com.mambu.api.server.handler.loan.model.JSONLoanRepayments;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.api.server.handler.tasks.model.JSONTask;
import com.mambu.api.server.handler.users.model.JSONUser;
import com.mambu.apisdk.model.DatabaseBackup;
import com.mambu.apisdk.model.DatabaseBackupRequest;
import com.mambu.apisdk.model.NotificationsToBeResent;
import com.mambu.apisdk.model.SettlementAccount;
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
import com.mambu.core.shared.model.GeneralSettings;
import com.mambu.core.shared.model.Image;
import com.mambu.core.shared.model.IndexRate;
import com.mambu.core.shared.model.IndexRateSource;
import com.mambu.core.shared.model.ObjectLabel;
import com.mambu.core.shared.model.Organization;
import com.mambu.core.shared.model.Role;
import com.mambu.core.shared.model.SearchResult;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;
import com.mambu.intelligence.shared.model.Intelligence.Indicator;
import com.mambu.linesofcredit.shared.model.AccountsFromLineOfCredit;
import com.mambu.linesofcredit.shared.model.LineOfCredit;
import com.mambu.linesofcredit.shared.model.LineOfCreditExpanded;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanTranche;
import com.mambu.loans.shared.model.LoanTransaction;
import com.mambu.loans.shared.model.Repayment;
import com.mambu.organization.shared.model.Branch;
import com.mambu.organization.shared.model.Centre;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;
import com.mambu.savings.shared.model.SavingsTransaction;
import com.mambu.tasks.shared.model.Task;

/**
 * ApiDefinition is a helper class which allows service classes to provide a specification for a Mambu API request and
 * then use ServiceHelper class to actually execute the API request with this ApiDefinition and with provided input
 * parameters. This class allows users to define API format parameters required by a specific API request, including the
 * URL path structure, the HTTP method and content type, and the specification for the expected Mambu response.
 * ApiDefinition also allows optionally specifying custom JsonSerializers and JsonDeserializers to be used when
 * generating API requests or processing Mambu responses as well as serialisation exclusion strategies
 * 
 * For the URL path part of the specification, the API definition assumes the URL path to be build in the following
 * format: endpoint[/objectId][/relatedEntity][/[relatedEntityID]], with all parts , except the endpoint, being
 * optional. Examples: /loans, /savings/1234, clients/456/loans, /loans/4556/repayments, /groups/998878,
 * /loans/9876/transactions, /clients/645/custominformation/field_id_777
 * 
 * 
 * For the HTTP part, the ApiDefinition allows users to specify such HTTP parameters as method (GET, POST, DELETE) and
 * the content type
 * 
 * For the expected response, the API definition allows users to specify what is returned by Mambu: the object's class
 * and is it a single object or a collection returned by Mambu for this API request.
 * 
 * ApiDefinition class provides a number of convenience constructors to be used to set the required parameters.
 * 
 * ApiDefinition class also defines a number of typical Mambu API request types (see ApiType). These include standard
 * Mambu API requests, such as Get Entity Details, Get a List of Entities, Create JSON Entity, Get Account Transactions,
 * etc..). Constructor with an ApiType parameter derives the URL format and HTTP parameters from the ApiType, the user
 * typically only needs to additionally specify the Mambu class to be used with this request.
 * 
 * Examples: to define a specification for getting LoanDetails the user can specify a constructor in the following way:
 * new ApiDefinition(ApiType.GetEntityDetails, LoanAccount.class); To provide a specification for Creating New client
 * the user can create definition with new ApiDefinition(ApiType.Create, LoanAccountExpanded.class);
 * 
 * Note, that the Api definition doesn't include any of the input parameters which can be supplied with an API request,
 * this is to be provided as input params map in the service class' calls. The only exception is the ability to specify
 * that the 'fullDetails' parameter is required, in this case the caller doesn't need to provide it as input. For
 * example, when specifying the ApiType.GetEntityDetails type, the fullDetails parameter will be added automatically for
 * this definition.
 * 
 * 
 * @author mdanilkis
 * 
 */
public class ApiDefinition {

	/**
	 * ApiType defines all typical types of the Mambu API requests, such as getting entity details, getting lists and
	 * others.
	 * 
	 */
	// These symbolic names are used in ApiType enum constructors just for constrcutor's readability
	private final static boolean withObjectId = true;
	private final static boolean noObjectId = false;
	private final static boolean hasRelatedEntityPart = true;
	private final static boolean noRelatedEntityPart = false;
	private final static boolean fullDetails = true;
	private final static boolean noFullDetails = false;

	public enum ApiType {

		// Get Entity without Details. Example GET clients/3444
		GET_ENTITY(Method.GET, ContentType.WWW_FORM, withObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// Get Entity wit full Details. Example: GET loan/5566?fullDetails=true
		GET_ENTITY_DETAILS(Method.GET, ContentType.WWW_FORM, withObjectId, fullDetails, noRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// Get a List of Entities. Example: GET savings/
		GET_LIST(Method.GET, ContentType.WWW_FORM, noObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.COLLECTION),
		// Get a List of Entities with all the details. Example: GET savings?fullDetails=true
		GET_LIST_WITH_DETAILS(Method.GET, ContentType.WWW_FORM, noObjectId, fullDetails, noRelatedEntityPart,
				ApiReturnFormat.COLLECTION),
		// Get Entities owned by another entity with all its details. Example: GET clients/1233/loans or GET
		// loans/233/transactions?fullDetails=true
		GET_OWNED_ENTITIES(Method.GET, ContentType.WWW_FORM, withObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.COLLECTION),
		// Get Entities owned by another entity, Example: GET clients/1233/loans or GET loans/233/transactions
		GET_OWNED_ENTITIES_WITH_DETAILS(Method.GET, ContentType.WWW_FORM, withObjectId, fullDetails, hasRelatedEntityPart,
						ApiReturnFormat.COLLECTION),
		// Get an Entity owned by another entity, Example: /api/loanproducts/<ID>/schedule
		GET_OWNED_ENTITY(Method.GET, ContentType.WWW_FORM, withObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// Get Entities related to another entity, For example get transactions of loan type. Example: GET
		// loans/transactions or GET savings/transactions
		GET_RELATED_ENTITIES(Method.GET, ContentType.WWW_FORM, noObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.COLLECTION),
		// Update an entity owned by another entity. Example, update custom field value for a client or group:
		// PATCH clients/client_id/custominformation/custom_field_id
		PATCH_OWNED_ENTITY(Method.PATCH, ContentType.JSON, withObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.BOOLEAN),
		// Update a list of entities owned by another entity with a JSON request and returning a list of updated
		// entities. Example, update repayments for a loan account and get updated list back
		// PATCH loans/loan_id/repayments/
		PATCH_OWNED_ENTITIES(Method.PATCH, ContentType.JSON, withObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.COLLECTION),
		// Delete and an entity owned by another entity. Example, delete custom field for a client:
		// DELETE clients/client_id/custominformation/custom_field_id
		DELETE_OWNED_ENTITY(Method.DELETE, ContentType.WWW_FORM, withObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.BOOLEAN),
		// Create Entity JSON request. Example: POST client/ (contentType=JSON)
		CREATE_JSON_ENTITY(Method.POST, ContentType.JSON, noObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// POST Entity using ContentType.WWW_FORM with params map. Used for older APIs versions not using JSON
		CREATE_FORM_ENTITY(Method.POST, ContentType.WWW_FORM, noObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// Update Entity JSON request. Example: POST loans/88666 (contentType=JSON) to update custom fields
		POST_ENTITY(Method.POST, ContentType.JSON, withObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// Patch Entity JSON request. Example: PATCH loans/88666 (contentType=JSON) to update loan term
		PATCH_ENTITY(Method.PATCH, ContentType.JSON, withObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.BOOLEAN),
		// Delete Entity Example: DELETE client/976
		DELETE_ENTITY(Method.DELETE, ContentType.WWW_FORM, withObjectId, noFullDetails, noRelatedEntityPart,
				ApiReturnFormat.BOOLEAN),
		// Post Owned Entity. Example: POST loans/822/transactions?type=REPAYMENT; returns Owned Entity (e.g returns
		// LoanTransaction)
		POST_OWNED_ENTITY(Method.POST, ContentType.WWW_FORM, withObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.OBJECT),
		// Post Entity Change. Example: POST loans/822/transactions?type=APPROVE. Returns Entity object (e.g.
		// LoanAccount)
		POST_ENTITY_ACTION(Method.POST, ContentType.WWW_FORM, withObjectId, noFullDetails, hasRelatedEntityPart,
				ApiReturnFormat.OBJECT);

		/**
		 * Initialise ApiType enum specifying API parameters to be used by this enum value
		 * 
		 * @param method
		 *            HTTP Method to be used by the API request (e.g. Method.GET, Method.POST)
		 * @param contentType
		 *            contentType to be used by the API request (e.g. ContentType.WWW_FORM, ContentType.JSON)
		 * @param requiresObjectId
		 *            a boolean specifying if the request must add object ID to the API request
		 * @param withFullDetails
		 *            a boolean specifying if the request must specify fullDetails parameter
		 * @param requiresRelatedEntity
		 *            a boolean specifying if the request must add the 'relatedEntity' component in the URL path,
		 *            formatted as /endpoint[/objectId][/relatedEntity][/relatedEntityID]
		 * @param relatedEntity
		 *            a string to be used as a 'relatedEntity' part in the URL path
		 * @param returnFormat
		 *            the return type expected for the API request
		 */
		private ApiType(Method method, ContentType contentType, boolean requiresObjectId, boolean withFullDetails,
				boolean requiresRelatedEntity, ApiReturnFormat returnFormat) {
			this.method = method;
			this.contentType = contentType;
			this.requiresObjectId = requiresObjectId;
			this.withFullDetails = withFullDetails;
			this.requiresRelatedEntity = requiresRelatedEntity;
			this.returnFormat = returnFormat;
		}

		private Method method;
		private ContentType contentType;
		private boolean requiresObjectId;
		private boolean withFullDetails;
		private boolean requiresRelatedEntity;
		private ApiReturnFormat returnFormat;

		// Getters
		public Method getMethod() {

			return method;
		}

		public ContentType getContentType() {

			return contentType;
		}

		public boolean isObjectIdNeeded() {

			return requiresObjectId;
		}

		public boolean isWithFullDetails() {

			return withFullDetails;
		}

		public boolean isWithRelatedEntity() {

			return requiresRelatedEntity;
		}

		public ApiReturnFormat getApiReturnFormat() {

			return returnFormat;
		}
	}

	/**
	 * ApiReturnFormat specifies if Mambu's returned JSON string represents a single object, a collection of objects or
	 * just a success/failure response
	 */
	public enum ApiReturnFormat {
		OBJECT, COLLECTION, BOOLEAN, RESPONSE_STRING, ZIP_ARCHIVE
	}

	private ApiType apiType;

	private Method method;
	private ContentType contentType;

	// URL path can be specified directly or created in the format: endPoint/objectID/relatedEntity
	// URL path if specified directly
	private String urlPath;
	// API's end point
	private String endPoint;
	private boolean requiresObjectId;
	// The 'relatedEntity' part of the URL path
	private String relatedEntity;
	// API return format. Specified in the ApiType but can be modified
	private ApiReturnFormat returnFormat;
	// Is fill details param required
	private boolean isWithFullDetails;

	// The class of the object returned by Mambu
	private Class<?> returnClass;
	// Date time format for the output JSON strings. Mambu supports ISO-8601 "yyyy-MM-dd'T'HH:mm:ssZ". This is the
	// default. ApiDefinition allows optionally setting this format for a specific API definition. For example, to use a
	// shorter date only format, like "yyyy-MM-dd"
	private String jsonDateTimeFormat = GsonUtils.defaultDateTimeFormat;

	// support specifying optional exclusion strategies
	List<ExclusionStrategy> serializationExclusionStrategies = null;

	// support optional API request JsonSerializers
	private HashMap<Class<?>, JsonSerializer<?>> jsonSerializers = null;
	// support optional API response JsonDeserializers
	private HashMap<Class<?>, JsonDeserializer<?>> jsonDeserializers = null;

	/**
	 * Constructor used with ApiType requests for which only one entity class needs to be specified, Example GET
	 * loans/123.
	 * 
	 * @param entityClass
	 *            determines API's endpoint (e.g. LoanAccount for loans/)
	 */
	public ApiDefinition(ApiType apiType, Class<?> entityClass) {
		this.relatedEntity = null; // no related entity
		initDefintion(apiType, entityClass, null);
	}

	/**
	 * Constructor used with ApiType requests for which two entity classes need to be specified, Example GET
	 * clients/123/loans.
	 * 
	 * @param entityClass
	 *            determines API's endpoint (e.g. LoanAccount for loans/)
	 * @param resultClass
	 *            determines the entity to be retrieved. E.g. loans in the API calls GET clients/333/loans
	 */

	public ApiDefinition(ApiType apiType, Class<?> entityClass, Class<?> resultClass) {
		this.relatedEntity = null; // the related entity name to be determined by the specified resultClass
		initDefintion(apiType, entityClass, resultClass);
	}

	/**
	 * Constructor used with ApiType requests for which two api endpoints need to be specified and the api returns
	 * related entity associated with the second endpoint. Example GET api/settings/iddocumenttemplates, GET
	 * api/loans/transactions
	 * 
	 * This constructor accepts the first api endpoint as a string but otherwise is identical to the
	 * @See #ApiDefinition(ApiType apiType, Class<?> entityClass, Class<?> resultClass). This constructor can be used
	 * in cased when there is no Mambu class to map to the api endpoint. Example GET settings/iddocumenttemplates.
	 * 
	 * @param apiType
	 *            the API type
	 * @param apiEndPoint
	 *            determines API's endpoint string directly . E.g "settings" as in /api/settings
	 * @param resultClass
	 *            determines the entity to be retrieved. E.g. Organization in the API calls GET api/setting/organization
	 */

	public ApiDefinition(ApiType apiType, String apiEndPoint, Class<?> resultClass) {
		this.relatedEntity = null; // the related entity name to be determined by the specified resultClass
		this.endPoint = apiEndPoint; // api endpoint string
		initDefintion(apiType, null, resultClass);
	}

	/**
	 * Make ApiDefintion by explicitly specifying all required HTTPS request parameters.
	 * 
	 * @param urlPath
	 *            full URL path (without the https prefix). Example settings/branding/log
	 * @param contentType
	 *            content type
	 * @param method
	 *            method
	 * @param retrunClass
	 *            returned object's class
	 * @param returnFormt
	 *            returned format type
	 */
	public ApiDefinition(String urlPath, ContentType contentType, Method method, Class<?> retrunClass,
			ApiReturnFormat returnFormt) {

		this.urlPath = urlPath;
		this.method = method;
		this.returnClass = retrunClass;
		this.contentType = contentType;
		this.returnFormat = returnFormt;
		this.requiresObjectId = false;
		// Not specified
		this.apiType = null;
		this.isWithFullDetails = false;
		this.relatedEntity = null;
		this.endPoint = null;

	}

	/**
	 * Constructor which can be used with ApiType requests for which the result class must be specified independently of
	 * the related entity class. For example, when getting client's profile picture which returns a string object ( GET
	 * api/clients/\{ID\}/documents/PROFILE_PICTURE) or posting a profile picture, which returns a Boolean (POST
	 * api/clients/\{ID\}/documents/PROFILE_PICTURE)
	 * 
	 * @param entityClass
	 *            determines API's endpoint (e.g. LoanAccount for loans/)
	 * @param relatedEntity
	 *            related entity name class
	 * @param resultClass
	 *            determines the entity to be retrieved.
	 */

	public ApiDefinition(ApiType apiType, Class<?> entityClass, Class<?> relatedEntity, Class<?> resultClass) {
		this.relatedEntity = getApiEndPoint(relatedEntity);
		initDefintion(apiType, entityClass, resultClass);
	}

	/**
	 * Initialise all API definition parameters for the specified ApiType
	 * 
	 * @param apiType
	 *            API type
	 * @param entityClass
	 *            entity class which identifies the api's end point
	 * @param resultClass
	 *            the class for the objects returned by the api. Needed for ApiType.GetOwnedEntities and is optional for
	 *            CREATE and UPDATE ApiTypes. For all other API types entity class determines also the result class
	 * 
	 */

	private void initDefintion(ApiType apiType, Class<?> entityClass, Class<?> resultClass) {

		if (apiType == null) {
			throw new IllegalArgumentException("apiType must not be null");
		}

		this.apiType = apiType;
		this.contentType = apiType.getContentType();
		this.method = apiType.getMethod();
		this.requiresObjectId = apiType.isObjectIdNeeded();
		this.isWithFullDetails = apiType.isWithFullDetails();

		// Get defaults from the ApiType
		returnFormat = apiType.getApiReturnFormat();

		// Get the end point. It can be specified directly or derived from an entityClass
		this.urlPath = null;
		if (endPoint == null) {
			this.endPoint = getApiEndPoint(entityClass);
		}

		switch (apiType) {
		case GET_LIST_WITH_DETAILS:
		case GET_ENTITY:
		case GET_ENTITY_DETAILS:
		case GET_LIST:
		case CREATE_FORM_ENTITY:
			if (entityClass == null) {
				throw new IllegalArgumentException("entityClass must not be null for " + apiType.name());
			}
			returnClass = resultClass == null ? entityClass : resultClass;
			break;
		case CREATE_JSON_ENTITY:
		case POST_ENTITY:
		case PATCH_ENTITY:
			// If the result class was provided - use it. Otherwise assuming the return class is the same as the
			// entityClass. For example, when creating loans, LoanAccountExpanded is used as input and also as output.
			// But when creating a Document, JSONDocument is the input but the result class must be specified as
			// Document
			returnClass = (resultClass != null) ? resultClass : entityClass;
			if (returnClass == null) {
				throw new IllegalArgumentException(
						"Either entityClass or Result class must not be null for " + apiType.name());
			}
			break;
		case GET_OWNED_ENTITY:
		case GET_OWNED_ENTITIES:
		case GET_OWNED_ENTITIES_WITH_DETAILS:
		case GET_RELATED_ENTITIES:
		case POST_OWNED_ENTITY:
		case PATCH_OWNED_ENTITY:
		case PATCH_OWNED_ENTITIES:
		case DELETE_OWNED_ENTITY:
			// For these API types the resultClass defines the 'relatedEntity' part. E.g. LOANS part in
			// /clients/1233/LOANS or transactions part: /loans/123/transactions. These types return the result class
			if (resultClass == null) {
				throw new IllegalArgumentException("resultClass must be not null for " + apiType.name());
			}
			// Get relatedEntity based on the specified resultClass, unless relatedEntity was specified explicitly
			if (relatedEntity == null) {
				relatedEntity = getApiEndPoint(resultClass);
			}
			// These API types return object (or collection) of the resultClass (for OBJECT and COLLECTION return
			// formats)
			switch (returnFormat) {
			case OBJECT:
			case ZIP_ARCHIVE:
			case COLLECTION:
				returnClass = resultClass;
				// Change return format for our special cases (to avoid Gson parsing to Object for these special cases)
				if (returnClass.equals(String.class)) {
					returnFormat = ApiReturnFormat.RESPONSE_STRING;
				} else if (returnClass.equals(Boolean.class)) {
					returnFormat = ApiReturnFormat.BOOLEAN;
				}
				break;
			case BOOLEAN:
				returnClass = Boolean.class;
				break;
			case RESPONSE_STRING:
				returnClass = String.class;
				break;
			}
			break;
		case DELETE_ENTITY:
			returnClass = Boolean.class;
			break;
		case POST_ENTITY_ACTION:
			// This type returns the entityClass class
			// For this API type the returned class is the same as the entityClass, E.g. LOANS part in
			// /clients/1233/LOANS or transactions part: /loans/123/transactions.
			if (resultClass == null) {
				throw new IllegalArgumentException("resultClass must be not null for " + apiType.name());
			}
			// Get relatedEntity based on the specified resultClass, unless relatedEntity was specified explicitly
			if (relatedEntity == null) {
				relatedEntity = getApiEndPoint(resultClass);
			}
			returnClass = entityClass;
			break;
		}
	}

	// apiEndPointsMap maps Mambu classes to the corresponding Mambu API URL path endpoints.
	private final static Map<Class<?>, String> apiEndPointsMap;
	static {
		apiEndPointsMap = new HashMap<Class<?>, String>();

		apiEndPointsMap.put(Client.class, APIData.CLIENTS);
		apiEndPointsMap.put(ClientExpanded.class, APIData.CLIENTS);
		apiEndPointsMap.put(Group.class, APIData.GROUPS);
		apiEndPointsMap.put(GroupExpanded.class, APIData.GROUPS);

		apiEndPointsMap.put(ClientRole.class, APIData.CLIENT_TYPES);
		apiEndPointsMap.put(GroupRoleName.class, APIData.GROUP_ROLE_NAMES);

		apiEndPointsMap.put(LoanAccount.class, APIData.LOANS);
		apiEndPointsMap.put(JSONLoanAccount.class, APIData.LOANS);

		apiEndPointsMap.put(LoanTransaction.class, APIData.TRANSACTIONS);
		apiEndPointsMap.put(Repayment.class, APIData.REPAYMENTS);

		apiEndPointsMap.put(SavingsAccount.class, APIData.SAVINGS);
		apiEndPointsMap.put(JSONSavingsAccount.class, APIData.SAVINGS);
		apiEndPointsMap.put(SavingsTransaction.class, APIData.TRANSACTIONS);

		apiEndPointsMap.put(Branch.class, APIData.BRANCHES);
		
		apiEndPointsMap.put(User.class, APIData.USERS);
		apiEndPointsMap.put(JSONUser.class, APIData.USERS);
		
		apiEndPointsMap.put(Centre.class, APIData.CENTRES);
		apiEndPointsMap.put(Currency.class, APIData.CURRENCIES);
		apiEndPointsMap.put(TransactionChannel.class, APIData.TRANSACTION_CHANNELS);

		apiEndPointsMap.put(Task.class, APIData.TASKS);
		apiEndPointsMap.put(JSONTask.class, APIData.TASKS);

		apiEndPointsMap.put(LoanProduct.class, APIData.LOANPRODUCTS);
		apiEndPointsMap.put(SavingsProduct.class, APIData.SAVINGSRODUCTS);

		apiEndPointsMap.put(JSONLoanRepayments.class, APIData.SCHEDULE);

		apiEndPointsMap.put(Document.class, APIData.DOCUMENTS);
		apiEndPointsMap.put(JSONDocument.class, APIData.DOCUMENTS);

		apiEndPointsMap.put(CustomFieldSet.class, APIData.CUSTOM_FIELD_SETS);
		apiEndPointsMap.put(CustomField.class, APIData.CUSTOM_FIELDS);
		apiEndPointsMap.put(CustomFieldValue.class, APIData.CUSTOM_INFORMATION);

		apiEndPointsMap.put(GLAccount.class, APIData.GLACCOUNTS);
		apiEndPointsMap.put(GLJournalEntry.class, APIData.GLJOURNALENTRIES);
		apiEndPointsMap.put(Indicator.class, APIData.INDICATORS);

		apiEndPointsMap.put(CustomView.class, APIData.VIEWS);
		apiEndPointsMap.put(JSONActivity.class, APIData.ACTIVITIES);

		apiEndPointsMap.put(Image.class, APIData.IMAGES);

		apiEndPointsMap.put(SearchResult.class, APIData.SEARCH);
		// Index interest sources
		apiEndPointsMap.put(IndexRateSource.class, APIData.INDEXRATESOURCES);
		apiEndPointsMap.put(IndexRate.class, APIData.INDEXRATES);
		// Comments
		apiEndPointsMap.put(Comment.class, APIData.COMMENTS);
		apiEndPointsMap.put(JSONComment.class, APIData.COMMENTS);
		// Identification Document Template
		apiEndPointsMap.put(IdentificationDocumentTemplate.class, APIData.ID_DOCUMENT_TEMPLATES);
		// Document Template
		apiEndPointsMap.put(DocumentTemplate.class, APIData.TEMPLATES);
		// Organization
		apiEndPointsMap.put(Organization.class, APIData.ORGANIZATION);
		apiEndPointsMap.put(GeneralSettings.class, APIData.GENERAL);
		apiEndPointsMap.put(ObjectLabel.class, APIData.LABELS);
		apiEndPointsMap.put(ExchangeRate.class, APIData.RATES); // "rates" api endpoint. Available since 4.2. For more
																// details see MBU-12629

		// Lines Of Credit
		apiEndPointsMap.put(LineOfCredit.class, APIData.LINES_OF_CREDIT);
		apiEndPointsMap.put(JSONLineOfCredit.class, APIData.LINES_OF_CREDIT); // Available since 4.2. See MBU-13767
		apiEndPointsMap.put(LineOfCreditExpanded.class, APIData.LINES_OF_CREDIT);
		apiEndPointsMap.put(AccountsFromLineOfCredit.class, APIData.ACCOUNTS);
		apiEndPointsMap.put(LoanTranche.class, APIData.TRANCHES);
		apiEndPointsMap.put(InvestorFund.class, APIData.FUNDS); // "funds" api end point
		apiEndPointsMap.put(Guaranty.class, APIData.GUARANTEES); // "guarantees" api end point
		apiEndPointsMap.put(Role.class, APIData.USER_ROLES); // "userroles" api end point

		// DB
		apiEndPointsMap.put(DatabaseBackupRequest.class, APIData.DATABASE); // "database" api end point
		apiEndPointsMap.put(DatabaseBackup.class, APIData.DATABASE);

		// SettlementAccount endPoint, a workaround because there is no SettlementAccount class
		apiEndPointsMap.put(SettlementAccount.class, APIData.SETTLEMENT_ACCOUNTS);
		
		//Notifications endpoint, a workaround for resending failed messages
		apiEndPointsMap.put(NotificationsToBeResent.class, APIData.NOTIFICATIONS);
		
	}

	// Get an Api endpoint for a Mambu class
	public static String getApiEndPoint(Class<?> entityClass) {

		if (entityClass == null) {
			throw new IllegalArgumentException("Entity Class cannot be NULL");
		}

		if (!apiEndPointsMap.containsKey(entityClass)) {
			throw new IllegalArgumentException("No Api end point is defined for class" + entityClass.getName());
		}
		return apiEndPointsMap.get(entityClass);
	}

	// Getters ////////////////
	public ApiType getApiType() {

		return apiType;
	}

	public String getEndPoint() {

		return endPoint;
	}

	public boolean isObjectIdNeeded() {

		return requiresObjectId;
	}

	public Method getMethod() {

		return method;
	}

	public ContentType getContentType() {

		return contentType;
	}

	public String getRelatedEntity() {

		return relatedEntity;
	}

	public ApiReturnFormat getApiReturnFormat() {

		return returnFormat;
	}

	public boolean getWithFullDetails() {

		return isWithFullDetails;
	}

	public Class<?> getReturnClass() {

		return returnClass;
	}

	// Setters for params which can be modified
	public void setApiType(ApiType apiType) {

		this.apiType = apiType;
	}

	public void setEndPoint(String endPoint) {

		this.endPoint = endPoint;
	}

	public void setApiReturnFormat(ApiReturnFormat returnFormat) {

		this.returnFormat = returnFormat;
	}

	public void setContentType(ContentType contentType) {

		this.contentType = contentType;
	}

	public void setMethod(Method method) {

		this.method = method;
	}

	public void setJsonDateTimeFormat(String dateTimeFormat) {

		this.jsonDateTimeFormat = dateTimeFormat;
	}

	public String getJsonDateTimeFormat() {

		return jsonDateTimeFormat;
	}

	public void setRequiresObjectId(boolean requires) {

		this.requiresObjectId = requires;
	}

	public String getUrlPath() {

		return urlPath;
	}

	public void setUrlPath(String urlPath) {

		this.urlPath = urlPath;
	}

	/**
	 * Add serialization ExclusionStrategy to the API definition
	 * 
	 * @param exclusionStrategy
	 *            exclusion strategy
	 */
	public void addSerializationExclusionStrategy(ExclusionStrategy exclusionStrategy) {

		if (serializationExclusionStrategies == null) {
			serializationExclusionStrategies = new ArrayList<>();
		}
		serializationExclusionStrategies.add(exclusionStrategy);
	}

	/**
	 * Get serialization ExclusionStrategy specified in the API definition
	 * 
	 * @return exclusion strategy
	 */
	public List<ExclusionStrategy> getSerializationExclusionStrategies() {

		return serializationExclusionStrategies;
	}

	/**
	 * Add JsonSerializer for a specific class to the API definition
	 * 
	 * @param clazz
	 *            class
	 * @param serializer
	 *            JsonSerializer
	 */
	public void addJsonSerializer(Class<?> clazz, JsonSerializer<?> serializer) {

		if (jsonSerializers == null) {
			jsonSerializers = new HashMap<>();
		}
		jsonSerializers.put(clazz, serializer);
	}

	/**
	 * Get JsonSerializers specified in the API definition
	 * 
	 * @return map of classes to JsonSerializer for these classes
	 */
	public HashMap<Class<?>, JsonSerializer<?>> getJsonSerializers() {

		return jsonSerializers;
	}

	/**
	 * Add JsonDeserializer for a specific class to the API definition
	 * 
	 * @param clazz
	 *            class
	 * @param deserializer
	 *            Json Deserializer
	 */
	public void addJsonDeserializer(Class<?> clazz, JsonDeserializer<?> deserializer) {

		if (jsonDeserializers == null) {
			jsonDeserializers = new HashMap<>();
		}
		jsonDeserializers.put(clazz, deserializer);
	}

	/**
	 * Get JsonDeserializers specified in the API definition
	 * 
	 * @return map of classes to JsonDeserializers for these classes
	 */
	public HashMap<Class<?>, JsonDeserializer<?>> getJsonDeserializers() {

		return jsonDeserializers;
	}

}
