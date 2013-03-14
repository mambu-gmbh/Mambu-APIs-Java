/**
 * 
 */
package com.mambu.apisdk.services;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.APIData.ACCOUNT_TYPE;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
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
	 * @return the savings account
	 * 
	 * @throws MambuApiException
	 * 
	 */
	public SavingsAccount getSavingsAccount(String accountId) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/" + accountId));
		String jsonResposne = mambuAPIService.executeRequest(urlString, Method.GET);

		SavingsAccount account = GsonUtils.createResponse().fromJson(jsonResposne, SavingsAccount.class);

		return account;
	}

	/***
	 * Get a savings account with full details by its id
	 * 
	 * @param accountId
	 *            the id of the account
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

		SavingsAccount account = GsonUtils.createResponse().fromJson(jsonResposne, SavingsAccount.class);

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

		List<SavingsAccount> accounts = (List<SavingsAccount>) GsonUtils.createResponse().fromJson(jsonResponse,
				collectionType);

		return accounts;
	}

	/****
	 * Approve a Savings account if the user has permission to approve savings, the maximum exposure is not exceeded for
	 * the client, the account was in Pending Approval state and if the number of savings is not exceeded
	 * 
	 * @param accountId
	 *            the id of the account
	 * @return
	 * @throws MambuApiException
	 * 
	 * 
	 */
	// TODO: This API is not implemented yet. Keep it private until the API is implemented
	private SavingsAccount approveSavingsAccount(String accountId, String notes) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_APPROVAL);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/" + accountId + "/" + TRANSACTIONS));
		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		SavingsAccount savingsAccount = GsonUtils.createResponse().fromJson(jsonResponse, SavingsAccount.class);

		return savingsAccount;
	}

	/***
	 * Get Savings Account Transactions by an account id and offset and limit
	 * 
	 * @param accountId
	 *            the id of the account
	 * @param offset
	 *            - first transaction number
	 * @param limit
	 *            - last transaction number Note: if offset and limit both equal null, all transactions are returned
	 *            (Note: transaction are sorted by date)
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

		List<SavingsTransaction> transactions = (List<SavingsTransaction>) GsonUtils.createResponse().fromJson(
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

		SavingsTransaction transaction = GsonUtils.createResponse().fromJson(jsonResponse, SavingsTransaction.class);

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

		SavingsTransaction transaction = GsonUtils.createResponse().fromJson(jsonResponse, SavingsTransaction.class);

		return transaction;
	}

	/****
	 * Make a Transfer from an account.
	 * 
	 * @param accountId
	 *            the id of the account the amount to transfer from
	 * @param destinationAccountKey
	 *            - the id of the account to transfer to
	 * @param destinationAccountType
	 *            (SavingsService.Loan or SavingsService.Savings)
	 * @param amunt
	 *            - amount to transfer
	 * @param notes
	 * 
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */

	public SavingsTransaction makeTransfer(String fromAccountId, String destinationAccountKey,
			APIData.ACCOUNT_TYPE destinationAccountType, String amount, String notes) throws MambuApiException {

		// E.g .format: POST "type=TYPE_TRANSFER" /api/savings/KHGJ593/transactions

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

		SavingsTransaction transaction = GsonUtils.createResponse().fromJson(jsonResponse, SavingsTransaction.class);

		return transaction;
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

		List<SavingsAccount> accounts = (List<SavingsAccount>) GsonUtils.createResponse().fromJson(jsonResponse,
				collectionType);

		return accounts;
	}

	/***
	 * Get the Savings accounts by branch is, credit officer, accountState
	 * 
	 * @param Parameters
	 *            branchID The ID of the branch to which the accounts are assigned to
	 * @param creditOfficerUsername
	 *            -=the username of the credit officer to whom the accounts are assigned to
	 * @param accountState
	 *            -the state of the accounts to filter on (eg: APPROVED)
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

		List<SavingsAccount> accounts = (List<SavingsAccount>) GsonUtils.createResponse().fromJson(jsonResponse,
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
	 * 
	 */
	public List<SavingsProduct> getSavingsProducts() throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(SAVINGSRODUCTS + "/"));
		String jsonResposne = mambuAPIService.executeRequest(urlString, Method.GET);

		Type collectionType = new TypeToken<List<SavingsProduct>>() {}.getType();

		List<SavingsProduct> products = GsonUtils.createResponse().fromJson(jsonResposne, collectionType);

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
	 * 
	 */
	public SavingsProduct getSavingsProduct(String productId) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(SAVINGSRODUCTS + "/" + productId));
		String jsonResposne = mambuAPIService.executeRequest(urlString, Method.GET);

		SavingsProduct product = GsonUtils.createResponse().fromJson(jsonResposne, SavingsProduct.class);
		return product;
	}

}
