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

	/****
	 * Approve a loan account if the user has permission to approve loans, the maximum exposure is not exceeded for the
	 * client, the account was in Pending Approval state and if the number of loans is not exceeded
	 * 
	 * @param accountId
	 *            the id of the account
	 * @return
	 * @throws MambuApiException
	 */
	public String approveLoanAccount(String accountId, String notes) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(ACTION, APPROVE);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId));
		String response = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		return response;
	}

	/***
	 * Disburse a loan account with a given disbursal date
	 * 
	 * @param accountId
	 *            the id of the loan account
	 * @param disbursalDate
	 *            the disbursal date
	 * @return
	 * @throws MambuApiException
	 */
	private String disburseLoanAccount(String accountId, String disbursalDate) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(ACTION, DISBURSE);
		paramsMap.addParam(DISBURSAL_DATE, disbursalDate);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId));
		String response = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		return response;
	}

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
	 * @return
	 * @throws MambuApiException
	 */
	private String disburseLoanAccount(String accountId, String disbursalDate, String firstRepaymentDate, String type,
			String bankNumber, String receiptNumber, String checkNumber, String bankAccountNumber,
			String bankRoutingNumber, String notes) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(ACTION, DISBURSE);
		paramsMap.addParam(DISBURSAL_DATE, disbursalDate);
		paramsMap.addParam(FIRST_REPAYMENT_DATE, firstRepaymentDate);
		paramsMap.addParam(TYPE, type);
		paramsMap.addParam(BANK_NUMBER, bankNumber);
		paramsMap.addParam(RECEIPT_NUMBER, receiptNumber);
		paramsMap.addParam(CHECK_NUMBER, checkNumber);
		paramsMap.addParam(BANK_ACCOUNT_NUMBER, bankAccountNumber);
		paramsMap.addParam(BANK_ROUTING_NUMBER, bankRoutingNumber);
		paramsMap.addParam(NOTES, notes);

		String urlString = new String(mambuAPIService.createUrl(LOANS + "/" + accountId));
		String response = mambuAPIService.executeRequest(urlString, paramsMap, Method.POST);

		return response;
	}

	/***
	 * Change the state of a loan account to Approved or Active
	 * 
	 * @param accountId
	 * @param state
	 * @return
	 * @throws MambuApiException
	 */
	public String aproveOrDisburseLoanAccount(String accountId, AccountState state, String disbursementDate)
			throws MambuApiException {

		String response = "";

		switch (state) {
		case PENDING_APPROVAL:
			response = approveLoanAccount(accountId, null);
			break;

		case APPROVED:
			response = disburseLoanAccount(accountId, disbursementDate);
			break;
		}

		return response;
	}

	/***
	 * Change the state of a loan account to Approved or Active. Also, add some extra parameters
	 * 
	 * @param accountId
	 * @param state
	 * @param disbursalDate
	 * @param firstRepaymentDate
	 * @param type
	 * @param bankNumber
	 * @param receiptNumber
	 * @param checkNumber
	 * @param bankAccountNumber
	 * @param bankRoutingNumber
	 * @param notes
	 * @return
	 * @throws MambuApiException
	 */
	public String aproveOrDisburseLoanAccount(String accountId, AccountState state, String disbursalDate,
			String firstRepaymentDate, String type, String bankNumber, String receiptNumber, String checkNumber,
			String bankAccountNumber, String bankRoutingNumber, String notes) throws MambuApiException {

		String response = "";

		switch (state) {
		case PENDING_APPROVAL:
			response = approveLoanAccount(accountId, notes);
			break;

		case APPROVED:
			response = disburseLoanAccount(accountId, disbursalDate, firstRepaymentDate, type, bankNumber,
					receiptNumber, checkNumber, bankAccountNumber, bankRoutingNumber, notes);
			break;
		}
		return response;
	}
}
