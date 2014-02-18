/**
 * 
 */
package com.mambu.apisdk.services;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.exception.MambuApiResponseMessage;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.APIData.ACCOUNT_TYPE;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;
import com.mambu.savings.shared.model.SavingsTransaction;

/**
 * Service class which handles API operations like retrieval, creation or changing state of savings accounts
 * 
 * @author ipenciuc
 * 
 */
public class SavingsService {

	private static final String SAVINGS = APIData.SAVINGS;
	private static final String CLIENTS = APIData.CLIENTS;
	private static final String GROUPS = APIData.GROUPS;
	private static final String FULL_DETAILS = APIData.FULL_DETAILS;

	private static final String TYPE = APIData.TYPE;
	private static final String TRANSACTIONS = APIData.TRANSACTIONS;
	private static final String TYPE_DEPOSIT = APIData.TYPE_DEPOSIT;
	private static final String TYPE_WITHDRAWAL = APIData.TYPE_WITHDRAWAL;
	private static final String TYPE_TRANSFER = APIData.TYPE_TRANSFER;
	private static final String TYPE_APPROVAL = APIData.TYPE_APPROVAL;

	private static final String AMOUNT = APIData.AMOUNT;
	private static final String DATE = APIData.DATE;

	private static final String PAYMENT_METHOD = APIData.PAYMENT_METHOD;

	private static final String BANK_NUMBER = APIData.BANK_NUMBER;

	private static final String RECEIPT_NUMBER = APIData.RECEIPT_NUMBER;
	private static final String CHECK_NUMBER = APIData.CHECK_NUMBER;
	private static final String BANK_ACCOUNT_NUMBER = APIData.BANK_ACCOUNT_NUMBER;
	private static final String BANK_ROUTING_NUMBER = APIData.BANK_ROUTING_NUMBER;
	private static final String NOTES = APIData.NOTES;
	// Savings filters
	private static final String BRANCH_ID = APIData.BRANCH_ID;
	private static final String CREDIT_OFFICER_USER_NAME = APIData.CREDIT_OFFICER_USER_NAME;
	private static final String ACCOUNT_STATE = APIData.ACCOUNT_STATE;

	private static final String OFFSET = APIData.OFFSET;
	private static final String LIMIT = APIData.LIMIT;

	private static final String TO_SAVINGS = APIData.TO_SAVINGS;
	private static final String TO_LOAN = APIData.TO_LOAN;

	// Savings products
	private static final String SAVINGSRODUCTS = APIData.SAVINGSRODUCTS;

	private MambuAPIService mambuAPIService;

	/***
	 * Create a new savings service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public SavingsService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/***
	 * Get a savings account by its id
	 * 
	 * @param accountId
	 *            the id of the account
	 * 
	 * @return the savings account
	 * 
	 * @throws MambuApiException
	 * 
	 */
	public SavingsAccount getSavingsAccount(String accountId) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/" + accountId));
		String jsonResposne = mambuAPIService.executeRequest(urlString, Method.GET);

		SavingsAccount account = GsonUtils.createGson().fromJson(jsonResposne, SavingsAccount.class);

		return account;
	}

	/***
	 * Get a savings account with full details by its id
	 * 
	 * @param accountId
	 *            the id of the account
	 * 
	 * @return the savings account
	 * 
	 * @throws MambuApiException
	 * 
	 */
	public SavingsAccount getSavingsAccountDetails(String accountId) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/" + accountId));
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(FULL_DETAILS, "true");

		String jsonResposne = mambuAPIService.executeRequest(urlString, paramsMap, Method.GET);

		SavingsAccount account = GsonUtils.createGson().fromJson(jsonResposne, SavingsAccount.class);

		return account;
	}

	/***
	 * Get all the savings accounts for a given client
	 * 
	 * @param clientId
	 *            the id of the client
	 * 
	 * @return the client's list of savings accounts
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<SavingsAccount> getSavingsAccountsForClient(String clientId) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/" + clientId + "/" + SAVINGS));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		Type collectionType = new TypeToken<List<SavingsAccount>>() {}.getType();

		List<SavingsAccount> accounts = (List<SavingsAccount>) GsonUtils.createGson().fromJson(jsonResponse,
				collectionType);

		return accounts;
	}

	/****
	 * Approve Savings account
	 * 
	 * @param accountId
	 *            the id of the account
	 * 
	 * @return the approved SavingsAccount from Mambu
	 * 
	 * @throws MambuApiException
	 */
	// TODO: Need to raise an issue in Mambu: Approve API Account object in the response doesn't contain custom fields.
	// Such response cannot be used for account details display
	public SavingsAccount approveSavingsAccount(String accountId, String notes) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_APPROVAL);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/" + accountId + "/" + TRANSACTIONS));
		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		SavingsAccount savingsAccount = GsonUtils.createGson().fromJson(jsonResponse, SavingsAccount.class);

		return savingsAccount;
	}

	/***
	 * Get Savings Account Transactions by an account id and offset and limit
	 * 
	 * @param accountId
	 *            the id of the account
	 * @param offset
	 *            first transaction number
	 * @param limit
	 *            last transaction number Note: if offset and limit both equal null, all transactions are returned
	 *            (Note: transaction are sorted by date)
	 * 
	 * @return the list of savings transactions
	 * 
	 * @throws MambuApiException
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<SavingsTransaction> getSavingsAccountTransactions(String accountId, String offset, String limit)
			throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse;

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(OFFSET, offset);
		paramsMap.put(LIMIT, limit);

		jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.GET);

		Type collectionType = new TypeToken<List<SavingsTransaction>>() {}.getType();

		List<SavingsTransaction> transactions = (List<SavingsTransaction>) GsonUtils.createGson().fromJson(
				jsonResponse, collectionType);

		return transactions;
	}

	// helper method
	private void addPaymentMethodAccountDetails(ParamsMap paramsMap, String amount, String date, String notes,
			String paymentMethod, String receiptNumber, String bankNumber, String checkNumber,
			String bankAccountNumber, String bankRoutingNumber) {

		paramsMap.addParam(AMOUNT, amount);
		if (date != null)
			paramsMap.addParam(DATE, date);
		if (paymentMethod != null) {
			paramsMap.addParam(PAYMENT_METHOD, paymentMethod);
			if (receiptNumber != null)
				paramsMap.addParam(RECEIPT_NUMBER, receiptNumber);
			if (bankNumber != null)
				paramsMap.addParam(BANK_NUMBER, bankNumber);
			if (checkNumber != null)
				paramsMap.addParam(CHECK_NUMBER, checkNumber);
			if (bankAccountNumber != null)
				paramsMap.addParam(BANK_ACCOUNT_NUMBER, bankAccountNumber);
			if (bankRoutingNumber != null)
				paramsMap.addParam(BANK_ROUTING_NUMBER, bankRoutingNumber);
		}

		paramsMap.addParam(NOTES, notes);

		return;
	}

	/****
	 * Make a withdrawal from an account.
	 * 
	 * @param accountId
	 *            the id of the account the amount to withdraw notes
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
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */

	public SavingsTransaction makeWithdrawal(String accountId, String amount, String date, String notes,
			String paymentMethod, String receiptNumber, String bankNumber, String checkNumber,
			String bankAccountNumber, String bankRoutingNumber) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_WITHDRAWAL);

		addPaymentMethodAccountDetails(paramsMap, amount, date, notes, paymentMethod, receiptNumber, bankNumber,
				checkNumber, bankAccountNumber, bankRoutingNumber);

		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		SavingsTransaction transaction = GsonUtils.createGson().fromJson(jsonResponse, SavingsTransaction.class);

		return transaction;
	}

	/****
	 * Make a deposit to an account.
	 * 
	 * @param accountId
	 *            the id of the account the amount to deposit notes
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
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */

	public SavingsTransaction makeDeposit(String accountId, String amount, String date, String notes,
			String paymentMethod, String receiptNumber, String bankNumber, String checkNumber,
			String bankAccountNumber, String bankRoutingNumber) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_DEPOSIT);

		addPaymentMethodAccountDetails(paramsMap, amount, date, notes, paymentMethod, receiptNumber, bankNumber,
				checkNumber, bankAccountNumber, bankRoutingNumber);

		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		SavingsTransaction transaction = GsonUtils.createGson().fromJson(jsonResponse, SavingsTransaction.class);

		return transaction;
	}

	/****
	 * Make a Transfer from an account.
	 * 
	 * @param fromAccountId
	 *            the id of the account the amount to transfer from
	 * @param destinationAccountKey
	 *            the id of the account to transfer to
	 * @param destinationAccountType
	 *            (SavingsService.Loan or SavingsService.Savings)
	 * @param amount
	 *            amount to transfer
	 * @param notes
	 * 
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */
	public SavingsTransaction makeTransfer(String fromAccountId, String destinationAccountKey,
			APIData.ACCOUNT_TYPE destinationAccountType, String amount, String notes) throws MambuApiException {

		// E.g .format: POST "type=TYPE_TRANSFER"
		// /api/savings/KHGJ593/transactions

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_TRANSFER);

		if (destinationAccountType == ACCOUNT_TYPE.LOAN) {
			paramsMap.addParam(TO_LOAN, destinationAccountKey);
		} else {
			paramsMap.addParam(TO_SAVINGS, destinationAccountKey);
		}

		paramsMap.addParam(AMOUNT, amount);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/" + fromAccountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		SavingsTransaction transaction = GsonUtils.createGson().fromJson(jsonResponse, SavingsTransaction.class);

		return transaction;
	}

	/***
	 * Delete Savings Account by its Id
	 * 
	 * Note: available since Mambu 3.4 See MBU-4581 for details.
	 * 
	 * @param accountId
	 * 
	 * @return status
	 * 
	 * @throws MambuApiException
	 */
	public boolean deleteAccount(String accountId) throws MambuApiException {

		if (accountId == null) {
			throw new IllegalArgumentException("Account ID  must not  be null");
		}
		// create the api call
		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/" + accountId));

		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.DELETE);

		// On success the response is: {"returnCode":0,"returnStatus":"SUCCESS"}
		// An exception can be thrown: E.g. ({"returnCode":980,"returnStatus":"INVALID_TASK_ID"})

		// Parse the response. (Though, as no exception was thrown here, must be a "SUCCESS" response)
		boolean deletionStatus = false;
		MambuApiResponseMessage response = new MambuApiResponseMessage(jsonResponse);
		if (response.getReturnCode() == 0) {
			deletionStatus = true;
		}

		return deletionStatus;
	}
	/****
	 * CLose Savings account specifying the type of closer (withdraw or reject)
	 * 
	 * Note: available since Mambu 3.4 See MBU-4581 for details.
	 * 
	 * @param accountId
	 *            the id of the account the amount to withdraw notes
	 * 
	 * @param type
	 *            type of closer (withdraw or reject)
	 * @param notes
	 * 
	 * @return Savings Account
	 * 
	 * @throws MambuApiException
	 */

	public SavingsAccount closeAccount(String accountId, APIData.CLOSER_TYPE closerType, String notes)
			throws MambuApiException {

		if (accountId == null) {
			throw new IllegalArgumentException("Account ID  must not  be null");
		}
		if (closerType == null) {
			throw new IllegalArgumentException("Closer Type must not  be null");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, closerType.name());
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		SavingsAccount account = GsonUtils.createGson().fromJson(jsonResponse, SavingsAccount.class);

		return account;
	}
	/***
	 * Get all the savings accounts for a given group
	 * 
	 * @param groupId
	 *            the id of the group
	 * 
	 * @return the group's list of Savings accounts
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<SavingsAccount> getSavingsAccountsForGroup(String groupId) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(GROUPS + "/" + groupId + "/" + SAVINGS));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		Type collectionType = new TypeToken<List<SavingsAccount>>() {}.getType();

		List<SavingsAccount> accounts = (List<SavingsAccount>) GsonUtils.createGson().fromJson(jsonResponse,
				collectionType);

		return accounts;
	}

	/***
	 * Get the Savings accounts by branch id, credit officer, accountState
	 * 
	 * @param branchId
	 *            The ID of the branch to which the accounts are assigned to
	 * @param creditOfficerUserName
	 *            the username of the credit officer to whom the accounts are assigned to
	 * @param accountState
	 *            the state of the accounts to filter on (e.g: APPROVED)
	 * 
	 * @return the list of Savings accounts matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<SavingsAccount> getSavingsAccountsByBranchOfficerState(String branchId, String creditOfficerUserName,
			String accountState, String offset, String limit) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/"));
		ParamsMap params = new ParamsMap();

		params.addParam(BRANCH_ID, branchId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.addParam(ACCOUNT_STATE, accountState);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		String jsonResponse;

		jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<SavingsAccount>>() {}.getType();

		List<SavingsAccount> accounts = (List<SavingsAccount>) GsonUtils.createGson().fromJson(jsonResponse,
				collectionType);
		return accounts;
	}

	// Savings Products
	/***
	 * Get a list of Savings Products
	 * 
	 * @return the List of Savings Products
	 * 
	 * @throws MambuApiException
	 */
	public List<SavingsProduct> getSavingsProducts(String offset, String limit) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(SAVINGSRODUCTS + "/"));

		ParamsMap params = new ParamsMap();
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		String jsonResposne = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<SavingsProduct>>() {}.getType();

		List<SavingsProduct> products = GsonUtils.createGson().fromJson(jsonResposne, collectionType);

		return products;
	}

	/***
	 * Get a Savings Product by Product id
	 * 
	 * @param productId
	 *            the id of the product
	 * @return the Savings Product
	 * 
	 * @throws MambuApiException
	 */
	public SavingsProduct getSavingsProduct(String productId) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(SAVINGSRODUCTS + "/" + productId));
		String jsonResposne = mambuAPIService.executeRequest(urlString, Method.GET);

		SavingsProduct product = GsonUtils.createGson().fromJson(jsonResposne, SavingsProduct.class);
		return product;
	}

	/***
	 * Create new SavingsAccount using JSONSavingsAccount object and sending it via the JSON API. This API allows
	 * creating SavingsAccount with details, including creating custom field values.
	 * 
	 * 
	 * @param savingsAccount
	 *            JSONSavingsAccount object containing SavingsAccount. SavingsAccount's encodedKey must be null for
	 *            account create
	 * 
	 * @return savingsAccount
	 * 
	 * @throws MambuApiException
	 */
	public JSONSavingsAccount createAccount(JSONSavingsAccount account) throws MambuApiException {

		if (account == null || account.getSavingsAccount() == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		SavingsAccount inputAccount = account.getSavingsAccount();
		String encodedKey = inputAccount.getEncodedKey();
		if (encodedKey != null) {
			throw new IllegalArgumentException("Cannot create  Account, the encoded key must be null");
		}
		// Convert object to json
		// parse SavingsAccount object into json string using specific date time format
		final String dateTimeFormat = APIData.yyyyMmddFormat;
		final String jsonData = GsonUtils.createGson(dateTimeFormat).toJson(account, JSONSavingsAccount.class);

		ParamsMap params = new ParamsMap();
		// Add json string as JSON_OBJECT
		params.put(APIData.JSON_OBJECT, jsonData);

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/"));

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.POST, ContentType.JSON);

		JSONSavingsAccount savingsAccount = GsonUtils.createGson().fromJson(jsonResponse, JSONSavingsAccount.class);

		return savingsAccount;
	}

	/***
	 * Update an existent SavingsAccount using JSONSavingsAccount object and sending it via the JSON API. This API
	 * allows updating JSONSavingsAccount with details. As of Mambu 3.4 only custom fields can be updated.
	 * 
	 * 
	 * @param savingsAccount
	 *            JSONSavingsAccount object containing SavingsAccount. SavingsAccount encodedKey must be NOT null for
	 *            account update
	 * 
	 * @return savingsAccount
	 * 
	 * 
	 * @throws MambuApiException
	 */
	public JSONSavingsAccount updateAccount(JSONSavingsAccount account) throws MambuApiException {

		if (account == null || account.getSavingsAccount() == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		SavingsAccount inputAccount = account.getSavingsAccount();
		String encodedKey = inputAccount.getEncodedKey();
		if (encodedKey == null) {
			throw new IllegalArgumentException("Cannot update  Account, the encoded key must be NOT null");
		}
		// Convert object to json
		// parse SavingsAccount object into json string using specific date time format
		final String dateTimeFormat = APIData.yyyyMmddFormat;
		final String jsonData = GsonUtils.createGson(dateTimeFormat).toJson(account, JSONSavingsAccount.class);

		// System.out.println("Input Savings Account In json format=" + jsonData);

		ParamsMap params = new ParamsMap();
		// Add json string as JSON_OBJECT
		params.put(APIData.JSON_OBJECT, jsonData);

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/"));

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.POST, ContentType.JSON);

		JSONSavingsAccount savingsAccount = GsonUtils.createGson().fromJson(jsonResponse, JSONSavingsAccount.class);

		return savingsAccount;
	}

}
