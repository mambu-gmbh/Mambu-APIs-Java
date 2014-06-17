package com.mambu.apisdk.util;

import com.mambu.accounting.shared.model.GLAccount;
import com.mambu.accounting.shared.model.GLJournalEntry;
import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.model.LoanAccountExpanded;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.core.shared.model.Currency;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldSet;
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

/**
 * ApiDefinition is a helper class which allows service classes to provide a specification for a Mambu API request and
 * then use ServiceHelper class to actually execute the API request with this Api Definition and with provided input
 * parameters. This class allows users to defined API format parameters required by a specific API request, including
 * the url structure, the https method and content type, and the specification for the expected Mambu response (response
 * type).
 * 
 * For the URL part of the specification, the api definition assumes the url to be build in the following format:
 * endpoint[/objectId][/operation], with all parts , except the endpoint, being optional. Examples: /loans,
 * /savings/1234, clients/456/loans, loans/4556/repayments, groups/998878. loans/9876/transactions
 * 
 * For the HTTP part, the Api Definition allows user to specify such HTTP parameters as Method (GET, POST, DELETE) and
 * the ContentType
 * 
 * For the expected response, the API definition allows user to specify what is returned by Mambu: the object's class
 * and is it a single object or a collection returned by Mambu for this api request.
 * 
 * Api Definition class provides a number of convenience constructors to be used to set the required parameters
 * 
 * API Definition class also defines a number of typical Mambu API request Types (see ApiType). These include standard
 * Mambu API requests, such as Get Entity Details, Get a List of Entities, Create Json Entity, Get Account Transactions,
 * etc..). Constructor with an ApiType parameter derive url format, HTTPS parameters from the type, the user typically
 * only needs to also specify the Mmabu class to be used with this request.
 * 
 * Examples: to define a specification for getting LoanDetails the user can specify a constructor in the following way:
 * new ApiDefinition(ApiType.GetEntityDetails, LoanAccount.class); To provide a specification for Creating New client
 * the user can create definition with new ApiDefinition(ApiType.Create, LoanAccountExpanded.class);
 * 
 * Note, that the Api definition doesn't include any input parameters which can be supplied with an API request, this is
 * to be provided as input params map in the service class' calls. The only exception, is the ability to specify that
 * fullDetails parameter is required, in this case the caller doesn't need to provide it as input. For example, when
 * specifying the ApiType.GetEntityDetails type, the fullDetails parameter will be added automatically for this
 * definition.
 * 
 * 
 * @author mdanilkis
 * 
 */
public class ApiDefinition {

	/**
	 * ApiType defines all typical types of Mambu API requests, such as getting entity details, getting lists and
	 * others. Using a constructor with the ApiType defaults many other api request parameters as expected by Mambu for
	 * such requests.
	 * 
	 */
	public enum ApiType {
		GetEntity /* Get Entity without Details. Example: GET clients/3444 */,

		GetEntityDetails /* Get Entity wit full Details. Example: GET loan/5566?fullDetails=true */,

		GetList /* Get List of Entities. Example: GET savings/ */,

		Create /* Create Entity Json request. Example: POST client/ (contentType=JSON) */,

		Update/* Update Entity Json request. Example: POST loans/88666 (contentType=JSON) */,

		Delete /* Delete Entity Example: DELETE client/976 */,

		GetOwnedEntities /* Get Entities owned by another entity, Example: GET clients/1233/loans */,

		// Account Specific APIs types
		GetAccountTransactions /* Get Account Transactions. Example: GET loans/233/transactions */,

		PostAccountTransaction /* Post Account Transactions Example: POST loans/822/transactions. Returns Transactions */,

		PostAccountStatusChange /* Post Account State Change Example: POST loans/822/transactions. Returns Account */
	}

	/**
	 * ApiReturnFormat specifies if Mambu's returned json string represents a single object, a collection of objects or
	 * just a success/failure response
	 */
	public enum ApiReturnFormat {
		OBJECT, COLLECTION, BOOLEAN, RESPONSE_STRING
	}

	// Url details in the format : endPoint/objectID/operation

	private String endPoint;
	private boolean needObjectId;
	private String operation;

	private ApiType apiType;

	// HTTPS Method and ContentType for the request
	private Method method;
	private ContentType contentType;

	// Flag indicating if api should specify full details parameter
	private boolean withFullDetails;

	// Return values specification: Type and return class
	private ApiReturnFormat returnFormat;
	private Class<?> returnClass;

	/**
	 * Constructor used with ApiType requests for which only one entity class needs to be specified, Example GET
	 * loans/123. T
	 * 
	 * @param entityClass
	 *            determines API's endpoint (e.g. LoanAccount for loans/)
	 */
	public ApiDefinition(ApiType apiType, Class<?> entityClass) {
		initDefintion(apiType, entityClass, null);
	}

	/**
	 * Constructor used with ApiType requests for which only two entity class need to be specified, Example GET
	 * clients/123/loans. Currently this is used only with ApiType.GetOwnedEntities
	 * 
	 * @param entityClass
	 *            determines API's endpoint (e.g. LoanAccount for loans/)
	 * @param resultClass
	 *            determines the entity to be retrieved. E.g. loans in the api calls GET clients/333/loans
	 */

	public ApiDefinition(ApiType apiType, Class<?> entityClass, Class<?> resultClass) {
		initDefintion(apiType, entityClass, resultClass);
	}

	/**
	 * Initialise all API definition parameters for the specified ApiType
	 * 
	 * @param apiType
	 *            api type
	 * @param entityClass
	 *            entity class which identifies the api's end point
	 * @param resultClass
	 *            the class for the objects returned by the api. Needed only for ApiType.GetOwnedEntities. For all other
	 *            api types entity class determines the result class
	 * 
	 */

	private void initDefintion(ApiType apiType, Class<?> entityClass, Class<?> resultClass) {

		this.apiType = apiType;
		endPoint = apiEndPoint(entityClass);
		// Default to no details and ContentType.WWW_FORM
		withFullDetails = false;
		contentType = ContentType.WWW_FORM;

		switch (apiType) {
		case GetEntity:
			method = Method.GET;
			operation = null;
			needObjectId = true;
			returnFormat = ApiReturnFormat.OBJECT;
			returnClass = entityClass;
			break;
		case GetEntityDetails:
			method = Method.GET;
			withFullDetails = true;
			operation = null;
			needObjectId = true;
			returnFormat = ApiReturnFormat.OBJECT;
			returnClass = entityClass;
			break;
		case GetList:
			method = Method.GET;
			operation = null;
			needObjectId = false;
			returnFormat = ApiReturnFormat.COLLECTION;
			returnClass = entityClass;
			break;
		case GetOwnedEntities:
			// E.g. "Get Loans for a Client" or "Get Savings for a Group", etc.
			// Used for any url of the full URL format: endPoint/objectID/operation
			method = Method.GET;
			// For this type: the resultClass defines the 'Operation' part. E.g. LOANS part in clients/1233/LOANS
			operation = apiEndPoint(resultClass);
			needObjectId = true;
			returnFormat = ApiReturnFormat.COLLECTION;
			returnClass = resultClass;
			break;

		case Create:
			// Json Create. Returns an object type specified by the resultClass
			method = Method.POST;
			contentType = ContentType.JSON;
			needObjectId = false;
			operation = null;
			returnFormat = ApiReturnFormat.OBJECT;
			returnClass = entityClass;
			break;
		case Update:
			// Json Update.
			method = Method.POST;
			contentType = ContentType.JSON;
			needObjectId = true; // needed for Update
			operation = null;
			returnFormat = ApiReturnFormat.OBJECT;
			returnClass = entityClass;
			break;
		case Delete:
			// Post Delete entity.
			method = Method.DELETE;
			needObjectId = true;
			operation = null;
			// Mambu returns success or failure for DELETE transactions
			returnFormat = ApiReturnFormat.BOOLEAN;
			returnClass = Boolean.class;
			break;
		case GetAccountTransactions:
			// Accounts API specific type. Get Transactions returns a list of Transactions
			boolean isLoan;
			if (entityClass.equals(LoanAccount.class)) {
				isLoan = true;
			} else if (entityClass.equals(SavingsAccount.class)) {
				isLoan = false;
			} else {
				throw new IllegalArgumentException("Account Transactions class be LoanAccount or SavingsTransaction");
			}
			method = Method.GET;
			operation = APIData.TRANSACTIONS;
			needObjectId = true;
			returnFormat = ApiReturnFormat.COLLECTION;
			returnClass = (isLoan) ? LoanTransaction.class : SavingsTransaction.class;
			break;
		case PostAccountTransaction:
			// Accounts API specific type. Post Transactions returns a Transaction object
			if (entityClass.equals(LoanAccount.class)) {
				isLoan = true;
			} else if (entityClass.equals(SavingsAccount.class)) {
				isLoan = false;
			} else {
				throw new IllegalArgumentException("Account Transactions class be LoanAccount or SavingsTransaction");
			}
			method = Method.POST;
			operation = APIData.TRANSACTIONS;
			needObjectId = true;
			returnFormat = ApiReturnFormat.OBJECT;
			returnClass = (isLoan) ? LoanTransaction.class : SavingsTransaction.class;
			break;
		case PostAccountStatusChange:
			// Accounts API specific type. Post Account change returns an Account object
			if (!entityClass.equals(LoanAccount.class) && !entityClass.equals(SavingsAccount.class)) {
				throw new IllegalArgumentException(
						"Account Status change entity class be LoanAccount or SavingsTransaction");
			}
			method = Method.POST;
			operation = APIData.TRANSACTIONS;
			needObjectId = true;
			returnFormat = ApiReturnFormat.OBJECT;
			returnClass = entityClass;
			break;
		}
		// System.out.println("Return class=" + returnClass);
	}

	// Generic constructor to supply all required Api definition parameters individually
	// It's not used currently but can be used in cases where current ApiTypes are not providing required flexibility
	public ApiDefinition(String endPoint, boolean needsObjectId, String operation, Method method,
			ContentType contentType, ApiReturnFormat returnFormat, Class<?> returnClass) {
		this.apiType = null;
		this.endPoint = endPoint;
		this.needObjectId = needsObjectId;
		this.operation = operation;
		this.method = method;
		this.contentType = contentType;
		this.returnFormat = returnFormat;
		this.returnClass = returnClass;
		this.withFullDetails = false;
	}

	private String apiEndPoint(Class<?> entityClass) {

		if (entityClass == null) {
			throw new IllegalArgumentException("Entity Class cannot be NULL");
		}

		if (entityClass.equals(Client.class)) {
			return APIData.CLIENTS;
		}
		if (entityClass.equals(ClientExpanded.class)) {
			return APIData.CLIENTS;
		}
		if (entityClass.equals(Group.class)) {
			return APIData.GROUPS;
		}
		if (entityClass.equals(GroupExpanded.class)) {
			return APIData.GROUPS;
		}
		if (entityClass.equals(LoanAccount.class)) {
			return APIData.LOANS;
		}
		if (entityClass.equals(LoanAccountExpanded.class)) {
			return APIData.LOANS;
		}
		if (entityClass.equals(SavingsAccount.class)) {
			return APIData.SAVINGS;
		}
		if (entityClass.equals(JSONSavingsAccount.class)) {
			return APIData.SAVINGS;
		}
		if (entityClass.equals(Branch.class)) {
			return APIData.BRANCHES;
		}
		if (entityClass.equals(User.class)) {
			return APIData.USERS;
		}
		if (entityClass.equals(Centre.class)) {
			return APIData.CENTRES;
		}
		if (entityClass.equals(LoanProduct.class)) {
			return APIData.LOANPRODUCTS;
		}
		if (entityClass.equals(SavingsProduct.class)) {
			return APIData.SAVINGSRODUCTS;
		}
		if (entityClass.equals(Currency.class)) {
			return APIData.CURRENCIES;
		}
		// Repayment
		if (entityClass.equals(Repayment.class)) {
			return APIData.REPAYMENTS;
		}
		// Documents
		if (entityClass.equals(Document.class)) {
			return APIData.DOCUMENTS;
		}
		if (entityClass.equals(JSONDocument.class)) {
			return APIData.DOCUMENTS;
		}
		// Custom Field sets
		if (entityClass.equals(CustomFieldSet.class)) {
			return APIData.CUSTOM_FIELD_SETS;
		}
		//
		if (entityClass.equals(CustomField.class)) {
			return APIData.CUSTOM_FIELDS;
		}
		// Accounting
		if (entityClass.equals(GLAccount.class)) {
			return APIData.GLACCOUNTS;
		}
		if (entityClass.equals(GLJournalEntry.class)) {
			return APIData.GLJOURNALENTRIES;
		}
		if (entityClass.equals(Indicator.class)) {
			return APIData.INDICATORS;
		}
		throw new IllegalArgumentException("NO Api end point defined for class" + entityClass.getName());
	}

	// Getters ////////////////

	public ApiType getApiType() {
		return apiType;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public boolean getNeedObjectId() {
		return needObjectId;
	}

	public Method getMethod() {
		return method;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public String getOperation() {
		return operation;
	}

	public ApiReturnFormat getApiReturnFormat() {
		return returnFormat;
	}

	public boolean getWithFullDetails() {
		return withFullDetails;
	}

	public Class<?> getReturnClass() {
		return returnClass;
	}

	// Setters ////////////////

	public void setApiType(ApiType apiType) {
		this.apiType = apiType;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public void setNeedObjectId(boolean needObjectId) {
		this.needObjectId = needObjectId;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setApiReturnFormat(ApiReturnFormat returnFormat) {
		this.returnFormat = returnFormat;
	}

	public void setWithFullDetails(boolean withFullDeatils) {
		this.withFullDetails = withFullDeatils;
	}

	public void setReturnClass(Class<?> returnClass) {
		this.returnClass = returnClass;
	}

}
