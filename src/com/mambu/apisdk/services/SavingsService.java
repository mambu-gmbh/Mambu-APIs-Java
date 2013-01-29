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
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsTransaction;

/**
 * Service class which handles API operations like retrieval, creation or changing state of savings accounts
 * 
 * @author ipenciuc
 * 
 */
public class SavingsService {

	public static final String SAVINGS = "savings";
	public static final String CLIENTS = "clients";
	public static final String GROUPS = "groups";
	public static final String FULL_DETAILS = "fullDetails";

	public static final String ACTION = "action";
	public static final String APPROVE = "approve";

	public static final String TYPE = "type";
	public static final String TRANSACTIONS = "transactions";
	public static final String TYPE_DEPOSIT = "DEPOSIT";
	public static final String TYPE_WITHDRAWAL = "WITHDRAWAL";
	public static final String TYPE_TRANSFER = "TRANSFER";
	public static final String TYPE_APPROVAL = "APPROVAL";

	public static final String AMOUNT = "amount";
	public static final String DATE = "date";

	public static final String PAYMENT_METHOD = "method";
	public static final String CASH_METHOD = "CASH";
	public static final String RECEIPT_METHOD = "RECEIPT";
	public static final String CHECK_METHOD = "CHECK";
	public static final String BANK_TRANSFER_METHOD = "BANK_TRANSFER";
	public static final String BANK_NUMBER = "bankNumber";

	public static final String RECEIPT_NUMBER = "receiptNumber";
	public static final String CHECK_NUMBER = "checkNumber";
	public static final String BANK_ACCOUNT_NUMBER = "bankAccountNumber";
	public static final String BANK_ROUTING_NUMBER = "bankRoutingNumber";
	public static final String NOTES = "notes";
	// Savings filters
	public static final String BRANCH_ID = "branchId";
	public static final String CREDIT_OFFICER_USER_NAME = "creditOfficerUsername";
	public static final String ACCOUNT_STATE = "accountState";

	//
	public enum ACCOUNT_TYPE {
		LOAN, SAVINGS
	};

	public static final String TO_SAVINGS = "toSavingsAccount";
	public static final String TO_LOAN = "toLoanAccount";

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
		paramsMap.put("offset", offset);
		paramsMap.put("limit", limit);

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
	// TODO: TO BE FIXED This API does NOT return a Savings Transaction object. Returns only a Success string
	// Response ={"returnCode":0,"returnStatus":"SUCCESS"}. Parsing to object fails

	public SavingsTransaction makeTransfer(String fromAccountId, String destinationAccountKey,
			ACCOUNT_TYPE destinationAccountType, String amount, String notes) throws MambuApiException {

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

		// TODO: This is Temp, until api is fixed to return Savings Transaction Object
		SavingsTransaction transaction = GsonUtils.createResponse().fromJson(jsonResponse, SavingsTransaction.class);
		boolean status = false;
		if (jsonResponse.contains("SUCCESS"))
			status = true;

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
			String accountState) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(SAVINGS + "/"));
		ParamsMap params = new ParamsMap();

		params.addParam(BRANCH_ID, branchId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.addParam(ACCOUNT_STATE, accountState);

		String jsonResponse;

		jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<SavingsAccount>>() {}.getType();

		List<SavingsAccount> accounts = (List<SavingsAccount>) GsonUtils.createResponse().fromJson(jsonResponse,
				collectionType);
		return accounts;
	}

}
