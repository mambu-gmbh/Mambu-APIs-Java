/**
 * 
 */
package com.mambu.apisdk.services;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mambu.accounts.shared.model.AccountState;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanTransaction;

/**
 * Service class which handles API operations like retrieval, creation or changing state of loan accounts
 * 
 * @author ipenciuc
 * 
 */
@Singleton
public class LoansService {

	public static final String LOANS = "loans";
	public static final String CLIENTS = "clients";
	public static final String GROUPS = "groups";

	public static final String ACTION = "action";
	public static final String APPROVE = "approve";
	public static final String DISBURSE = "disburse";

	public static final String DISBURSAL_DATE = "disbursalDate";
	public static final String FIRST_REPAYMENT_DATE = "firstRepaymentDate";

	public static final String TYPE = "type";
	public static final String BANK_NUMBER = "bankNumber";
	public static final String RECEIPT_NUMBER = "receiptNumber";
	public static final String CHECK_NUMBER = "checkNumber";
	public static final String BANK_ACCOUNT_NUMBER = "bankAccountNumber";
	public static final String BANK_ROUTING_NUMBER = "bankRoutingNumber";
	public static final String NOTES = "notes";
	//
	public static final String TRANSACTIONS = "transactions";
	public static final String TYPE_REPAYMENT = "REPAYMENT";
	public static final String TYPE_DISBURSMENT = "DISBURSMENT";
	public static final String TYPE_APPROVAL = "APPROVAL";
	public static final String TYPE_FEE = "FEE";

	public static final String AMOUNT = "amount";
	public static final String DATE = "date";
	public static final String PAYMENT_METHOD = "method";
	public static final String CASH_METHOD = "CASH";
	public static final String RECEIPT_METHOD = "RECEIPT";
	public static final String CHECK_METHOD = "CHECK";
	public static final String BANK_TRANSFER_METHOD = "BANK_TRANSFER";
	public static final String REPAYMENT_NUMBER = "repayment";
	// Loan filters
	public static final String BRANCH_ID = "branchId";
	public static final String CREDIT_OFFICER_USER_NAME = "creditOfficerUsername";
	public static final String ACCOUNT_STATE = "accountState";

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
	 * @return the loan account
	 * 
	 * @throws MambuApiException
	 * 
	 */
	public LoanAccount getLoanAccount(String accountId) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId));
		String jsonResposne = mambuAPIService.executeRequest(urlString, Method.GET);

		LoanAccount account = GsonUtils.createResponse().fromJson(jsonResposne, LoanAccount.class);
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

		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/" + clientId + "/" + LOANS));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		Type collectionType = new TypeToken<List<LoanAccount>>() {}.getType();

		List<LoanAccount> accounts = (List<LoanAccount>) GsonUtils.createResponse().fromJson(jsonResponse,
				collectionType);
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

		String urlString = new String(mambuAPIService.createUrl(GROUPS + "/" + groupId + "/" + LOANS));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		Type collectionType = new TypeToken<List<LoanAccount>>() {}.getType();

		List<LoanAccount> accounts = (List<LoanAccount>) GsonUtils.createResponse().fromJson(jsonResponse,
				collectionType);

		return accounts;
	}

	// Loan approval which returns LoanAccount object
	/****
	 * Approve a loan account if the user has permission to approve loans, the maximum exposure is not exceeded for the
	 * client, the account was in Pending Approval state and if the number of loans is not exceeded
	 * 
	 * @param accountId
	 *            the id of the account
	 * @return LoanAccount
	 * @throws MambuApiException
	 */

	public LoanAccount approveLoanAccount(String accountId, String notes) throws MambuApiException {
		// E.g. format: POST "type=APPROVAL" /api/loans/KHGJ593/transactions

		ParamsMap paramsMap = new ParamsMap();
		// MD
		paramsMap.addParam(TYPE, TYPE_APPROVAL);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		LoanAccount laonAccount = GsonUtils.createResponse().fromJson(jsonResponse, LoanAccount.class);

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
	 * @param type
	 * @param bankNumber
	 * @param receiptNumber
	 * @param checkNumber
	 * @param bankAccountNumber
	 * @param bankRoutingNumber
	 * @param notes
	 * 
	 * @return Loan Transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction disburseLoanAccount(String accountId, String amount, String disbursalDate,
			String firstRepaymentDate, String paymentMethod, String bankNumber, String receiptNumber,
			String checkNumber, String bankAccountNumber, String bankRoutingNumber, String notes)
			throws MambuApiException {
		// Example: POST "type=DISBURSMENT&date=2012-10-04&firstRepaymentDate=2012-10-08&notes=using transactions"
		// /api/loans/KHGJ593/transactions

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

		LoanTransaction transaction = GsonUtils.createResponse().fromJson(jsonResponse, LoanTransaction.class);

		return transaction;

	}

	/***
	 * Get a loan account with Details by its id
	 * 
	 * @param accountId
	 *            the id of the account
	 * @return the loan account
	 * 
	 * @throws MambuApiException
	 * 
	 */
	public LoanAccount getLoanAccountDetails(String accountId) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId));
		ParamsMap paramsMap = new ParamsMap();
		
		paramsMap.put("fullDetails", "true");

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.GET);

		LoanAccount account = GsonUtils.createResponse().fromJson(jsonResponse, LoanAccount.class);
		return account;
	}

	/***
	 * Get loan account Transactions by Loan id and offset and limit
	 * 
	 * @param accountId
	 *            the id of the account offset - first transaction number limit - last transaction number Note: if
	 *            offset and limit both equal null, all transactions are returned (Note: transaction are sorted by date)
	 * @return the list of loan account transactions
	 * 
	 * @throws MambuApiException
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<LoanTransaction> getLoanAccountTransactions(String accountId, String offset, String limit)
			throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse;

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put("offset", offset);
		paramsMap.put("limit", limit);

		jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.GET);

		Type collectionType = new TypeToken<List<LoanTransaction>>() {}.getType();

		List<LoanTransaction> transactions = (List<LoanTransaction>) GsonUtils.createResponse().fromJson(jsonResponse,
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

		LoanTransaction transaction = GsonUtils.createResponse().fromJson(jsonResponse, LoanTransaction.class);

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

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_FEE);
		paramsMap.addParam(AMOUNT, amount);
		paramsMap.addParam(REPAYMENT_NUMBER, repaymentNumber);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId + "/" + TRANSACTIONS));

		String jsonResponse = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		LoanTransaction transaction = GsonUtils.createResponse().fromJson(jsonResponse, LoanTransaction.class);

		return transaction;
	}

	/***
	 * Get the loan accounts by branch is, credit officer, accountState
	 * 
	 * @param Parameters
	 *            branchID The ID of the branch to which the loan accounts are assigned to
	 * @param creditOfficerUsername
	 *            - The username of the credit officer to whom the loans are assigned to
	 * @param accountState
	 *            - The desired state of the accounts to filter on (eg: APPROVED) *
	 * @return the list of loan accounts matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	@SuppressWarnings("unchecked")
	public List<LoanAccount> getLoanAccountsByBranchOfficerState(String branchId, String creditOfficerUserName,
			String accountState) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/"));

		ParamsMap params = new ParamsMap();
		params.addParam(BRANCH_ID, branchId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.addParam(ACCOUNT_STATE, accountState);

		String jsonResponse;

		jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<LoanAccount>>() {}.getType();

		List<LoanAccount> accounts = (List<LoanAccount>) GsonUtils.createResponse().fromJson(jsonResponse,
				collectionType);
		return accounts;
	}

}
