/**
 * 
 */
package com.mambu.apisdk.services;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.exception.MambuApiResponseMessage;
import com.mambu.apisdk.model.LoanAccountExpanded;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.docs.shared.model.Document;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanTransaction;

/**
 * Service class which handles API operations like retrieval, creation or changing state of loan accounts. See full
 * Mambu Loan API documentation at: http://api.mambu.com/customer/portal/articles/1162283-loans-api?b_id=874
 * 
 * @author ipenciuc
 * 
 */
@Singleton
public class LoansService {

	private static final String LOANS = APIData.LOANS;
	private static final String CLIENTS = APIData.CLIENTS;
	private static final String GROUPS = APIData.GROUPS;
	private static final String FULL_DETAILS = APIData.FULL_DETAILS;

	private static final String FIRST_REPAYMENT_DATE = APIData.FIRST_REPAYMENT_DATE;

	private static final String TYPE = APIData.TYPE;
	private static final String BANK_NUMBER = APIData.BANK_NUMBER;
	private static final String RECEIPT_NUMBER = APIData.RECEIPT_NUMBER;
	private static final String CHECK_NUMBER = APIData.CHECK_NUMBER;
	private static final String BANK_ACCOUNT_NUMBER = APIData.BANK_ACCOUNT_NUMBER;
	private static final String BANK_ROUTING_NUMBER = APIData.BANK_ROUTING_NUMBER;
	private static final String NOTES = APIData.NOTES;
	//
	private static final String TRANSACTIONS = APIData.TRANSACTIONS;
	private static final String TYPE_REPAYMENT = APIData.TYPE_REPAYMENT;
	private static final String TYPE_DISBURSMENT = APIData.TYPE_DISBURSMENT;
	private static final String TYPE_APPROVAL = APIData.TYPE_APPROVAL;
	private static final String TYPE_UNDO_APPROVAL = APIData.TYPE_UNDO_APPROVAL;
	private static final String TYPE_REJECT = APIData.TYPE_REJECT;
	private static final String TYPE_FEE = APIData.TYPE_FEE;
	private static final String TYPE_LOCK = APIData.TYPE_LOCK;
	private static final String TYPE_UNLOCK = APIData.TYPE_UNLOCK;

	private static final String AMOUNT = APIData.AMOUNT;
	private static final String DATE = APIData.DATE;
	private static final String PAYMENT_METHOD = APIData.PAYMENT_METHOD;

	private static final String REPAYMENT_NUMBER = APIData.REPAYMENT_NUMBER;
	// Loan filters
	private static final String BRANCH_ID = APIData.BRANCH_ID;
	private static final String CREDIT_OFFICER_USER_NAME = APIData.CREDIT_OFFICER_USER_NAME;
	private static final String ACCOUNT_STATE = APIData.ACCOUNT_STATE;

	// Loan products
	private static final String LOANPRODUCTS = APIData.LOANPRODUCTS;

	private MambuAPIService mambuAPIService;

	/***
	 * Create a new loan service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public LoansService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/***
	 * Get a loan account by its id
	 * 
	 * @param accountId
	 *            the id of the account
	 * 
	 * @return the loan account
	 * 
	 * @throws MambuApiException
	 */
	public LoanAccount getLoanAccount(String accountId) throws MambuApiException {

		if (accountId == null || accountId.trim().isEmpty()) {
			throw new IllegalArgumentException("Account ID must not  be null or empty");
		}

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId));
		String jsonResposne = mambuAPIService.executeRequest(urlString, Method.GET);

		LoanAccount account = GsonUtils.createGson().fromJson(jsonResposne, LoanAccount.class);
		return account;
	}

	/***
	 * Get all the loan accounts for a given client
	 * 
	 * @param clientId
	 *            the id of the client
	 * 
	 * @return the client's list of loan accounts
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<LoanAccount> getLoanAccountsForClient(String clientId) throws MambuApiException {

		if (clientId == null || clientId.trim().isEmpty()) {
			throw new IllegalArgumentException("Client ID must not  be null or empty");
		}
		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/" + clientId + "/" + LOANS));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		Type collectionType = new TypeToken<List<LoanAccount>>() {
		}.getType();

		List<LoanAccount> accounts = (List<LoanAccount>) GsonUtils.createGson().fromJson(jsonResponse, collectionType);
		return accounts;
	}

	/***
	 * Get all the loan accounts for a given group
	 * 
	 * @param groupId
	 *            the id of the group
	 * 
	 * @return the group's list of loan accounts
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<LoanAccount> getLoanAccountsForGroup(String groupId) throws MambuApiException {

		if (groupId == null || groupId.trim().isEmpty()) {
			throw new IllegalArgumentException("Group ID must not  be null or empty");
		}

		String urlString = new String(mambuAPIService.createUrl(GROUPS + "/" + groupId + "/" + LOANS));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		Type collectionType = new TypeToken<List<LoanAccount>>() {
		}.getType();

		List<LoanAccount> accounts = (List<LoanAccount>) GsonUtils.createGson().fromJson(jsonResponse, collectionType);

		return accounts;
	}

	/****
	 * Approve a loan account if the user has permission to approve loans, the maximum exposure is not exceeded for the
	 * client, the account was in Pending Approval state and if the number of loans is not exceeded
	 * 
	 * @param accountId
	 *            the id of the account
	 * 
	 * @return loanAccount
	 * 
	 *         Note: The account object in the response doesn't contain custom fields
	 * 
	 * @throws MambuApiException
	 */
	public LoanAccount approveLoanAccount(String accountId, String notes) throws MambuApiException {
		// E.g. format: POST "type=APPROVAL" /api/loans/KHGJ593/transactions

		if (accountId == null || accountId.trim().isEmpty()) {
			throw new IllegalArgumentException("Account ID must not  be null or empty");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_APPROVAL);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		LoanAccount laonAccount = GsonUtils.createGson().fromJson(jsonResponse, LoanAccount.class);

		return laonAccount;
	}

	/****
	 * Undo Approve for a loan account
	 * 
	 * @param accountId
	 *            the id of the account
	 * 
	 * @return loanAccount
	 * 
	 *         Note: The account object in the response doesn't contain custom fields
	 * 
	 * @throws MambuApiException
	 */
	public LoanAccount undoApproveLoanAccount(String accountId, String notes) throws MambuApiException {
		// E.g. format: POST "type=UNDO_APPROVAL" /api/loans/{id}/transactions

		if (accountId == null || accountId.trim().isEmpty()) {
			throw new IllegalArgumentException("Account ID must not  be null or empty");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_UNDO_APPROVAL);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		LoanAccount laonAccount = GsonUtils.createGson().fromJson(jsonResponse, LoanAccount.class);

		return laonAccount;
	}

	/****
	 * Lock loan account
	 * 
	 * @param accountId
	 *            the id of the account
	 * 
	 * @return loan transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction lockLoanAccount(String accountId, String notes) throws MambuApiException {

		if (accountId == null || accountId.trim().isEmpty()) {
			throw new IllegalArgumentException("Account ID must not  be null or empty");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_LOCK);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		LoanTransaction transaction = GsonUtils.createGson().fromJson(jsonResponse, LoanTransaction.class);

		return transaction;
	}

	/****
	 * Unlock loan account
	 * 
	 * @param accountId
	 *            the id of the account
	 * 
	 * @return loan transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction unlockLoanAccount(String accountId, String notes) throws MambuApiException {

		if (accountId == null || accountId.trim().isEmpty()) {
			throw new IllegalArgumentException("Account ID must not  be null or empty");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_UNLOCK);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		LoanTransaction transaction = GsonUtils.createGson().fromJson(jsonResponse, LoanTransaction.class);

		return transaction;
	}

	/***
	 * Delete Loan Account by its Id
	 * 
	 * @param loanAccountId
	 * 
	 * @return status
	 * 
	 * @throws MambuApiException
	 */
	public boolean deleteLoanAccount(String loanAccountId) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + loanAccountId));

		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.DELETE);

		// On success the response is: {"returnCode":0,"returnStatus":"SUCCESS"}
		// An exception can be thrown: E.g. ({"returnCode":100,"returnStatus":"INVALID_LOAN_ACCOUNT_ID"})

		// Parse the response. (Though, as no exception was thrown here, must be a "SUCCESS" response)
		boolean deletionStatus = false;
		MambuApiResponseMessage response = new MambuApiResponseMessage(jsonResponse);
		if (response.getReturnCode() == 0) {
			deletionStatus = true;
		}

		return deletionStatus;
	}

	/****
	 * Reject a loan account if the user has permission to reject loan accounts.
	 * 
	 * @param accountId
	 *            the id of the account
	 * @param notes
	 *            the reason why the account was reject
	 * 
	 * @return LoanAccount
	 * 
	 * @throws MambuApiException
	 */
	public LoanAccount rejectLoanAccount(String accountId, String notes) throws MambuApiException {
		// E.g. format: POST "type=REJECT" /api/loans/KHGJ593/transactions

		if (accountId == null || accountId.trim().isEmpty()) {
			throw new IllegalArgumentException("Account ID must not  be null or empty");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_REJECT);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		LoanAccount laonAccount = GsonUtils.createGson().fromJson(jsonResponse, LoanAccount.class);

		return laonAccount;
	}

	// A disbursment transaction, returns Transaction object
	/***
	 * 
	 * Disburse a loan account with a given disbursal date and some extra details
	 * 
	 * @param accountId
	 * @param disbursalDate
	 * @param firstRepaymentDate
	 * @param bankNumber
	 * @param receiptNumber
	 * @param checkNumber
	 * @param bankAccountNumber
	 * @param bankRoutingNumber
	 * @param notes
	 * @param amount
	 * @param paymentMethod
	 * 
	 * @return Loan Transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction disburseLoanAccount(String accountId, String amount, String disbursalDate,
			String firstRepaymentDate, String paymentMethod, String bankNumber, String receiptNumber,
			String checkNumber, String bankAccountNumber, String bankRoutingNumber, String notes)
			throws MambuApiException {
		// Example: POST
		// "type=DISBURSMENT&date=2012-10-04&firstRepaymentDate=2012-10-08&notes=using transactions"
		// /api/loans/KHGJ593/transactions

		if (accountId == null || accountId.trim().isEmpty()) {
			throw new IllegalArgumentException("Account ID must not  be null or empty");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_DISBURSMENT);
		paramsMap.addParam(AMOUNT, amount);

		paramsMap.addParam(DATE, disbursalDate);
		paramsMap.addParam(FIRST_REPAYMENT_DATE, firstRepaymentDate);
		paramsMap.addParam(PAYMENT_METHOD, paymentMethod);
		paramsMap.addParam(BANK_NUMBER, bankNumber);
		paramsMap.addParam(RECEIPT_NUMBER, receiptNumber);
		paramsMap.addParam(CHECK_NUMBER, checkNumber);
		paramsMap.addParam(BANK_ACCOUNT_NUMBER, bankAccountNumber);
		paramsMap.addParam(BANK_ROUTING_NUMBER, bankRoutingNumber);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		LoanTransaction transaction = GsonUtils.createGson().fromJson(jsonResponse, LoanTransaction.class);

		return transaction;

	}

	/***
	 * Get a loan account with Details by its id
	 * 
	 * @param accountId
	 *            the id of the account
	 * 
	 * @return the loan account
	 * 
	 *         Note: the returned account doesn't have guarantees information.
	 * 
	 * @throws MambuApiException
	 */

	public LoanAccount getLoanAccountDetails(String accountId) throws MambuApiException {

		if (accountId == null || accountId.trim().isEmpty()) {
			throw new IllegalArgumentException("Account ID must not  be null or empty");
		}
		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId));
		ParamsMap paramsMap = new ParamsMap();

		paramsMap.put(FULL_DETAILS, "true");

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.GET);

		LoanAccount account = GsonUtils.createGson().fromJson(jsonResponse, LoanAccount.class);
		return account;
	}

	/***
	 * Create a new LoanAccount using LoanAccountExpanded object and sending it as a Json api. This API allows creating
	 * LoanAccount with details, including creating custom fields.
	 * 
	 * 
	 * @param loan
	 *            LoanAccountExtended object containing LoanAccount. LoanAccount encodedKey must be null for account
	 *            creation
	 * 
	 * @return newly created loan account with full details including custom fields
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public LoanAccountExpanded createLoanAccount(LoanAccountExpanded loan) throws MambuApiException {

		if (loan == null || loan.getLoanAccount() == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		LoanAccount inputAccount = loan.getLoanAccount();
		String encodedKey = inputAccount.getEncodedKey();
		if (encodedKey != null) {
			throw new IllegalArgumentException("Cannot create Account, the encoded key must be null");
		}
		// Convert object to json
		// parse LoanAccountExpanded object into json string using specific date
		// time format
		final String dateTimeFormat = APIData.yyyyMmddFormat;
		final String jsonData = GsonUtils.createGson(dateTimeFormat).toJson(loan, LoanAccountExpanded.class);

		ParamsMap params = new ParamsMap();
		// Add json string as JSON_OBJECT
		params.put(APIData.JSON_OBJECT, jsonData);

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(LOANS + "/"));

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.POST, ContentType.JSON);

		LoanAccountExpanded account = GsonUtils.createGson().fromJson(jsonResponse, LoanAccountExpanded.class);

		return account;
	}

	/***
	 * Update an existent LoanAccount using LoanAccountExpanded object and sending it as a Json api. This API allows
	 * updating LoanAccount with details. As of Mambu 3.4 only custom fields can be updated.
	 * 
	 * 
	 * @param loan
	 *            LoanAccountExtended object containing LoanAccount. LoanAccount encodedKey must be NOT null for account
	 *            update
	 * 
	 * @return updated object containing both the LoanAccount and its CustomInformation fields
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public LoanAccountExpanded updateLoanAccount(LoanAccountExpanded loan) throws MambuApiException {

		if (loan == null || loan.getLoanAccount() == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		LoanAccount inputAccount = loan.getLoanAccount();
		String encodedKey = inputAccount.getEncodedKey();
		if (encodedKey == null) {
			throw new IllegalArgumentException("Cannot update Account, the encoded key must be NOT null");
		}
		// Convert object to json
		// parse LoanAccountExpanded object into json string using specific date
		// time format
		final String dateTimeFormat = APIData.yyyyMmddFormat;
		final String jsonData = GsonUtils.createGson(dateTimeFormat).toJson(loan, LoanAccountExpanded.class);

		// System.out.println("Input Loan Details In json format=" + jsonData);

		ParamsMap params = new ParamsMap();
		// Add json string as JSON_OBJECT
		params.put(APIData.JSON_OBJECT, jsonData);

		// create the api call. Since Mambu 3.6 the account id or encoded key should be used in update API calls
		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + encodedKey));

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.POST, ContentType.JSON);

		LoanAccountExpanded account = GsonUtils.createGson().fromJson(jsonResponse, LoanAccountExpanded.class);

		return account;
	}

	/***
	 * Get loan account Transactions by Loan id and offset and limit
	 * 
	 * @param accountId
	 *            the id of the account offset - first transaction number limit - last transaction number Note: if
	 *            offset and limit both equal null, all transactions are returned (Note: transaction are sorted by date)
	 * 
	 * @return the list of loan account transactions
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<LoanTransaction> getLoanAccountTransactions(String accountId, String offset, String limit)
			throws MambuApiException {

		if (accountId == null || accountId.trim().isEmpty()) {
			throw new IllegalArgumentException("Account ID must not  be null or empty");
		}
		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse;

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(APIData.OFFSET, offset);
		paramsMap.put(APIData.LIMIT, limit);

		jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.GET);

		Type collectionType = new TypeToken<List<LoanTransaction>>() {
		}.getType();

		List<LoanTransaction> transactions = (List<LoanTransaction>) GsonUtils.createGson().fromJson(jsonResponse,
				collectionType);

		return transactions;
	}

	/****
	 * Repayments on a loan account if the user has permission to repay loans, the maximum exposure is not exceeded for
	 * the client, the account was in Approved state
	 * 
	 * @param accountId
	 * @param amount
	 * @param date
	 * @param paymentMethod
	 * @param bankNumber
	 * @param receiptNumber
	 * @param checkNumber
	 * @param bankAccountNumber
	 * @param bankRoutingNumber
	 * @param notes
	 * 
	 * @return LoanTransaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction makeLoanRepayment(String accountId, String amount, String date, String notes,
			String paymentMethod, String receiptNumber, String bankNumber, String checkNumber,
			String bankAccountNumber, String bankRoutingNumber) throws MambuApiException {

		// E.g. format: POST "type=APPROVAL" /api/loans/KHGJ593/transactions
		if (accountId == null || accountId.trim().isEmpty()) {
			throw new IllegalArgumentException("Account ID must not  be null or empty");
		}

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_REPAYMENT);
		paramsMap.addParam(AMOUNT, amount);
		paramsMap.addParam(DATE, date);

		paramsMap.addParam(PAYMENT_METHOD, paymentMethod);
		paramsMap.addParam(RECEIPT_NUMBER, receiptNumber);
		paramsMap.addParam(BANK_NUMBER, bankNumber);
		paramsMap.addParam(CHECK_NUMBER, checkNumber);
		paramsMap.addParam(BANK_ACCOUNT_NUMBER, bankAccountNumber);
		paramsMap.addParam(BANK_ROUTING_NUMBER, bankRoutingNumber);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		LoanTransaction transaction = GsonUtils.createGson().fromJson(jsonResponse, LoanTransaction.class);

		return transaction;
	}

	/****
	 * Apply FEE to a loan account
	 * 
	 * @param accountId
	 *            the id of the account
	 * @param amount
	 * @param repaymentNumber
	 * @param notes
	 * 
	 * @return Loan Transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction applyFeeToLoanAccount(String accountId, String amount, String repaymentNumber, String notes)
			throws MambuApiException {

		if (accountId == null || accountId.trim().isEmpty()) {
			throw new IllegalArgumentException("Account ID must not  be null or empty");
		}

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_FEE);
		paramsMap.addParam(AMOUNT, amount);
		paramsMap.addParam(REPAYMENT_NUMBER, repaymentNumber);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		LoanTransaction transaction = GsonUtils.createGson().fromJson(jsonResponse, LoanTransaction.class);

		return transaction;
	}

	/***
	 * Get the loan accounts by branch is, credit officer, accountState
	 * 
	 * @param branchId
	 *            branchID The ID of the branch to which the loan accounts are assigned to
	 * @param creditOfficerUserName
	 *            The username of the credit officer to whom the loans are assigned to
	 * @param accountState
	 *            The desired state of the accounts to filter on (eg: APPROVED)
	 * @param limit
	 * 
	 * @return the list of loan accounts matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<LoanAccount> getLoanAccountsByBranchOfficerState(String branchId, String creditOfficerUserName,
			String accountState, String offset, String limit) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/"));

		ParamsMap params = new ParamsMap();
		params.addParam(BRANCH_ID, branchId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.addParam(ACCOUNT_STATE, accountState);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		String jsonResponse;

		jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<LoanAccount>>() {
		}.getType();

		List<LoanAccount> accounts = (List<LoanAccount>) GsonUtils.createGson().fromJson(jsonResponse, collectionType);
		return accounts;
	}

	// Loan Products
	/***
	 * Get a list of Loan Products
	 * 
	 * @return the List of Loan Products
	 * 
	 * @throws MambuApiException
	 */
	public List<LoanProduct> getLoanProducts(String offset, String limit) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(LOANPRODUCTS + "/"));

		ParamsMap params = new ParamsMap();
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		String jsonResposne = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<LoanProduct>>() {
		}.getType();

		List<LoanProduct> products = GsonUtils.createGson().fromJson(jsonResposne, collectionType);

		return products;
	}

	/***
	 * Get a Loan Product by Product id
	 * 
	 * @param productId
	 *            the id of the loan product
	 * 
	 * @return the Loan Product
	 * 
	 * @throws MambuApiException
	 */
	public LoanProduct getLoanProduct(String productId) throws MambuApiException {

		if (productId == null || productId.trim().isEmpty()) {
			throw new IllegalArgumentException("Product ID must not  be null or empty");
		}
		String urlString = new String(mambuAPIService.createUrl(LOANPRODUCTS + "/" + productId));
		String jsonResposne = mambuAPIService.executeRequest(urlString, Method.GET);

		LoanProduct product = GsonUtils.createGson().fromJson(jsonResposne, LoanProduct.class);
		return product;
	}

	/***
	 * Get all documents for a specific Loan Account
	 * 
	 * @param accountId
	 *            the encoded key or id of the loan account for which attached documents are to be retrieved
	 * 
	 * @return documents documents attached to the entity
	 * 
	 * @throws MambuApiException
	 */
	public List<Document> getLoanAccountDocuments(String accountId) throws MambuApiException {

		if (accountId == null || accountId.trim().isEmpty()) {
			throw new IllegalArgumentException("Account ID must not be null or empty");
		}

		return new DocumentsService(mambuAPIService).getDocuments(LOANS, accountId);
	}
}
