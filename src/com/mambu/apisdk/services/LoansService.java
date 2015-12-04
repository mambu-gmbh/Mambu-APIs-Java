/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.Date;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.accountsecurity.shared.model.InvestorFund;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraints;
import com.mambu.api.server.handler.funds.model.JSONInvestorFunds;
import com.mambu.api.server.handler.loan.model.JSONLoanRepayments;
import com.mambu.api.server.handler.tranches.model.JSONTranches;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.LoanAccountExpanded;
import com.mambu.apisdk.services.CustomViewsService.CustomViewResultType;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.DateUtils;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.apisdk.util.ServiceHelper;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.docs.shared.model.Document;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanTranche;
import com.mambu.loans.shared.model.LoanTransaction;
import com.mambu.loans.shared.model.LoanTransactionType;
import com.mambu.loans.shared.model.Repayment;

/**
 * Service class which handles API operations like retrieval, creation or changing state of loan accounts. See full
 * Mambu Loan API documentation at: http://api.mambu.com/customer/portal/articles/1162283-loans-api?b_id=874
 * 
 * @author ipenciuc
 * 
 */
@Singleton
public class LoansService {

	private static final String FIRST_REPAYMENT_DATE = APIData.FIRST_REPAYMENT_DATE;

	private static final String TYPE = APIData.TYPE;
	private static final String NOTES = APIData.NOTES;
	//
	private static final String TYPE_REPAYMENT = APIData.TYPE_REPAYMENT;
	private static final String TYPE_DISBURSEMENT = APIData.TYPE_DISBURSEMENT;
	private static final String TYPE_APPROVAL = APIData.TYPE_APPROVAL;
	private static final String TYPE_REQUEST_APPROVAL = APIData.TYPE_REQUEST_APPROVAL;
	private static final String TYPE_UNDO_APPROVAL = APIData.TYPE_UNDO_APPROVAL;
	private static final String TYPE_FEE = APIData.TYPE_FEE;
	private static final String TYPE_LOCK = APIData.TYPE_LOCK;
	private static final String TYPE_UNLOCK = APIData.TYPE_UNLOCK;
	private static final String TYPE_WRITE_OFF = APIData.TYPE_WRITE_OFF;
	private static final String TYPE_DISBURSMENT_ADJUSTMENT = APIData.TYPE_DISBURSMENT_ADJUSTMENT;
	private static final String TYPE_PENALTY_ADJUSTMENT = APIData.TYPE_PENALTY_ADJUSTMENT;
	private static final String ORIGINAL_TRANSACTION_ID = APIData.ORIGINAL_TRANSACTION_ID;

	private static final String AMOUNT = APIData.AMOUNT;
	private static final String REPAYMENT_NUMBER = APIData.REPAYMENT_NUMBER;
	// Loan filters
	private static final String BRANCH_ID = APIData.BRANCH_ID;
	public static final String CENTRE_ID = APIData.CENTRE_ID;
	private static final String CREDIT_OFFICER_USER_NAME = APIData.CREDIT_OFFICER_USER_NAME;
	private static final String ACCOUNT_STATE = APIData.ACCOUNT_STATE;
	// Loan Schedule
	public static final String SCHEDULE = APIData.SCHEDULE;

	// Our serviceExecutor
	private ServiceExecutor serviceExecutor;

	// Create API definitions for services provided by LoanService
	// Get Account Details
	private final static ApiDefinition getAccount = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, LoanAccount.class);
	// Get Lists of Accounts
	private final static ApiDefinition getAccountsList = new ApiDefinition(ApiType.GET_LIST, LoanAccount.class);
	// Get Accounts for a Client
	private final static ApiDefinition getAccountsForClient = new ApiDefinition(ApiType.GET_OWNED_ENTITIES,
			Client.class, LoanAccount.class);
	// Get Accounts for a Group
	private final static ApiDefinition getAccountsForGroup = new ApiDefinition(ApiType.GET_OWNED_ENTITIES, Group.class,
			LoanAccount.class);
	// Get Documents for an Account
	private final static ApiDefinition getAccountDocuments = new ApiDefinition(ApiType.GET_OWNED_ENTITIES,
			LoanAccount.class, Document.class);
	// Get Account Transactions (transactions for a specific loan account)
	private final static ApiDefinition getAccountTransactions = new ApiDefinition(ApiType.GET_OWNED_ENTITIES,
			LoanAccount.class, LoanTransaction.class);
	// Get All Loan Transactions (transactions for all loan accounts)
	private final static ApiDefinition getAllLoanTransactions = new ApiDefinition(ApiType.GET_RELATED_ENTITIES,
			LoanAccount.class, LoanTransaction.class);
	// Post Account Transactions. Params map defines the transaction type. Return LoanTransaction
	private final static ApiDefinition postAccountTransaction = new ApiDefinition(ApiType.POST_OWNED_ENTITY,
			LoanAccount.class, LoanTransaction.class);
	// Post Account state change. Params map defines the account change transaction. Return LoanAccount
	private final static ApiDefinition postAccountChange = new ApiDefinition(ApiType.POST_ENTITY_ACTION,
			LoanAccount.class, LoanTransaction.class);
	// Delete Account
	private final static ApiDefinition deleteAccount = new ApiDefinition(ApiType.DELETE_ENTITY, LoanAccount.class);
	// Create Account
	private final static ApiDefinition createAccount = new ApiDefinition(ApiType.CREATE_JSON_ENTITY,
			LoanAccountExpanded.class);
	// Update Account. Used to update custom fields for loan accounts only. POST JSON /api/loans/loanId
	private final static ApiDefinition updateAccount = new ApiDefinition(ApiType.POST_ENTITY, LoanAccountExpanded.class);
	// Patch Account. Used to update loan terms only. PATCH JSON /api/loans/loanId
	private final static ApiDefinition patchAccount = new ApiDefinition(ApiType.PATCH_ENTITY, LoanAccount.class);
	// Update Loan Tranches. Returns updated LoanAccount. POST /api/loans/loanId/tranches
	private final static ApiDefinition updateAccountTranches = new ApiDefinition(ApiType.POST_ENTITY_ACTION,
			LoanAccount.class, LoanTranche.class);
	// Update Loan Investor Funds. Returns updated LoanAccount. POST /api/loans/loanId/funds
	private final static ApiDefinition updateAccountFunds = new ApiDefinition(ApiType.POST_ENTITY_ACTION,
			LoanAccount.class, InvestorFund.class);
	// Loan Products API requests
	// Get Loan Product Details
	private final static ApiDefinition getProduct = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, LoanProduct.class);
	// Get Lists of Loan Products
	private final static ApiDefinition getProductsList = new ApiDefinition(ApiType.GET_LIST, LoanProduct.class);
	// Get schedule for Loan Products. GET /api/loanproducts/<ID>/schedule?loanAmount=50. Returns JSONLoanRepayments
	private final static ApiDefinition getProductSchedule = new ApiDefinition(ApiType.GET_OWNED_ENTITY,
			LoanProduct.class, JSONLoanRepayments.class);

	/***
	 * Create a new loan service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public LoansService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
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
		return serviceExecutor.execute(getAccount, accountId);
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
	public List<LoanAccount> getLoanAccountsForClient(String clientId) throws MambuApiException {
		return serviceExecutor.execute(getAccountsForClient, clientId);
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
	// TODO: Solidarity Group Loans are NOT included into the returned list of Group Accounts. Only Pure Group Loans are
	// Implemented in MBU-1045.
	public List<LoanAccount> getLoanAccountsForGroup(String groupId) throws MambuApiException {
		return serviceExecutor.execute(getAccountsForGroup, groupId);
	}

	// TODO: Support Posting transactions in JSON. See MBU-3076. API documentation now allows for JSON transactions
	/****
	 * Approve a loan account if the user has permission to approve loans, the maximum exposure is not exceeded for the
	 * client, the account was in Pending Approval state and if the number of loans is not exceeded
	 * 
	 * @param accountId
	 *            the id of the account
	 * @param notes
	 *            transaction notes
	 * @return loanAccount
	 * 
	 *         Note: The account object in the response doesn't contain custom fields
	 * 
	 * @throws MambuApiException
	 */
	public LoanAccount approveLoanAccount(String accountId, String notes) throws MambuApiException {
		// E.g. format: POST "type=APPROVAL" /api/loans/KHGJ593/transactions

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_APPROVAL);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountChange, accountId, paramsMap);
	}

	/****
	 * Request Approval for a loan account to transition an account from a Partial Application state to a Pending
	 * Approval state
	 * 
	 * @param accountId
	 *            the encoded key or id of the account. Must not be null
	 * @param notes
	 *            transaction notes
	 * @return loan account
	 * 
	 * @throws MambuApiException
	 */
	public LoanAccount requestApprovalLoanAccount(String accountId, String notes) throws MambuApiException {
		// Available since Mambu 3.13. See MBU-9814
		// E.g. format: POST "type=PENDING_APPROVAL" /api/loans/KHGJ593/transactions

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_REQUEST_APPROVAL);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountChange, accountId, paramsMap);
	}

	/****
	 * Undo Approve for a loan account
	 * 
	 * @param accountId
	 *            the id of the account
	 * @param notes
	 *            transaction notes
	 * @return loanAccount
	 * 
	 *         Note: The account object in the response doesn't contain custom fields
	 * 
	 * @throws MambuApiException
	 */
	public LoanAccount undoApproveLoanAccount(String accountId, String notes) throws MambuApiException {
		// E.g. format: POST "type=UNDO_APPROVAL" /api/loans/{id}/transactions

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_UNDO_APPROVAL);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountChange, accountId, paramsMap);
	}

	/****
	 * Lock loan account
	 * 
	 * @param accountId
	 *            the id of the account
	 * @param notes
	 *            transaction notes
	 * @return a list of loan transactions performed when locking account
	 * 
	 * @throws MambuApiException
	 */
	public List<LoanTransaction> lockLoanAccount(String accountId, String notes) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_LOCK);
		paramsMap.addParam(NOTES, notes);

		// See MBU-8370. Unlock account API now returns a list of transactions
		ApiDefinition postAccountTransaction = new ApiDefinition(ApiType.POST_OWNED_ENTITY, LoanAccount.class,
				LoanTransaction.class);
		postAccountTransaction.setApiReturnFormat(ApiReturnFormat.COLLECTION);
		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);
	}

	/****
	 * Unlock loan account
	 * 
	 * @param accountId
	 *            the id of the account
	 * @param notes
	 *            transaction notes
	 * @return a list of loan transactions performed when unlocking account
	 * 
	 * @throws MambuApiException
	 */
	public List<LoanTransaction> unlockLoanAccount(String accountId, String notes) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_UNLOCK);
		paramsMap.addParam(NOTES, notes);

		// See MBU-8370. Unlock account API now returns a list of transactions
		ApiDefinition postAccountTransaction = new ApiDefinition(ApiType.POST_OWNED_ENTITY, LoanAccount.class,
				LoanTransaction.class);
		postAccountTransaction.setApiReturnFormat(ApiReturnFormat.COLLECTION);
		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);
	}

	/****
	 * Write of loan account
	 * 
	 * @param accountId
	 *            the encoded key or id of the account. Must not be null
	 * @param notes
	 *            transaction notes
	 * @return loan transaction
	 * @throws MambuApiException
	 */
	public LoanTransaction writeOffLoanAccount(String accountId, String notes) throws MambuApiException {
		// POST "type=WRITE_OFF" /api/loans/{ID}/transactions
		// See MBU-10423
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_WRITE_OFF);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);
	}

	/***
	 * Delete Loan Account by its Id
	 * 
	 * @param accountId
	 * 
	 * @return status
	 * 
	 * @throws MambuApiException
	 */
	public boolean deleteLoanAccount(String accountId) throws MambuApiException {
		return serviceExecutor.execute(deleteAccount, accountId);
	}

	/****
	 * Reject a loan account if the user has permission to reject loan accounts.
	 * 
	 * @param accountId
	 *            the id of the account. Mandatory
	 * @param notes
	 *            the reason why the account was reject
	 * 
	 * @return LoanAccount
	 * 
	 * @throws MambuApiException
	 */
	public LoanAccount rejectLoanAccount(String accountId, String notes) throws MambuApiException {
		// E.g. format: POST "type=REJECT" /api/loans/KHGJ593/transactions
		return closeLoanAccount(accountId, APIData.CLOSER_TYPE.REJECT, notes);
	}

	/****
	 * Withdraw (close) loan account if the user has permission to withdraw loan accounts.
	 * 
	 * @param accountId
	 *            the id of the account. Mandatory
	 * @param notes
	 *            the reason why the account was withdrawn
	 * 
	 * @return LoanAccount
	 * 
	 * @throws MambuApiException
	 */
	public LoanAccount withdrawLoanAccount(String accountId, String notes) throws MambuApiException {
		// E.g. format: POST "type=WITHDRAW" /api/loans/KHGJ593/transactions
		// Available since Mambu 3.3. See MBU-3090
		return closeLoanAccount(accountId, APIData.CLOSER_TYPE.WITHDRAW, notes);
	}

	/****
	 * Close Loan account specifying the type of closer (withdraw or reject)
	 * 
	 * @param accountId
	 *            the id of the account to close. Mandatory
	 * 
	 * @param closerType
	 *            type of closer (withdraw or reject). Mandatory
	 * @param notes
	 *            closer reason notes
	 * @return loan account
	 * 
	 * @throws MambuApiException
	 */

	public LoanAccount closeLoanAccount(String accountId, APIData.CLOSER_TYPE closerType, String notes)
			throws MambuApiException {
		// E.g. POST "type=WITHDRAW" /api/loans/KHGJ593/transactions
		// or POST "type=REJECT" /api/loans/KHGJ593/transactions
		// Available since Mambu 3.3 See MBU-3090 for details.
		if (closerType == null) {
			throw new IllegalArgumentException("Closer Type must not  be null");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, closerType.name());
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountChange, accountId, paramsMap);
	}

	/***
	 * 
	 * Disburse a loan account with a given disbursal date and some extra transaction details
	 * 
	 * @param accountId
	 *            account ID. Must not be null
	 * @param amount
	 *            disbursement amount. Loan amount can be null for all loan product types except REVOLVING_CREDIT. See
	 *            MBU-1054
	 * @param disbursalDate
	 *            disbursement date
	 * @param firstRepaymentDate
	 *            first repayment date
	 * @param notes
	 *            transaction notes
	 * @param transactionDetails
	 *            transaction details, including transaction channel and channel fields
	 * 
	 * @return Loan Transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction disburseLoanAccount(String accountId, String amount, String disbursalDate,
			String firstRepaymentDate, String notes, TransactionDetails transactionDetails) throws MambuApiException {

		// Disbursing loan account with tranches is available since Mambu 3.13. See MBU-10045
		// Disbursing Revolving Credit loans is available since Mambu 3.14 . See MBU-10547
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_DISBURSEMENT);

		// Add transactionDetails to the paramsMap
		ServiceHelper.addAccountTransactionParams(paramsMap, amount, disbursalDate, notes, transactionDetails);

		// Add also firstRepaymentDate
		paramsMap.addParam(FIRST_REPAYMENT_DATE, firstRepaymentDate);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);

	}

	// TODO: Implement MBU-8811 Disburse with activation fees when MBU-8992 is ready

	/***
	 * Undo Disburse for a loan account. If the account has multiple tranches, reverses the last tranche
	 * 
	 * @param accountId
	 *            account encoded key or id. Must not be null
	 * @param notes
	 *            transaction notes
	 * @return Loan Transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction undoDisburseLoanAccount(String accountId, String notes) throws MambuApiException {
		// Example POST "type=DISBURSMENT_ADJUSTMENT&notes=undo+notes" /api/loans/{id}/transactions/
		// Available since Mambu 3.9. See MBU-7189

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_DISBURSMENT_ADJUSTMENT);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);

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
		return serviceExecutor.execute(getAccount, accountId);
	}

	/***
	 * Create a new LoanAccount using LoanAccountExpanded object and sending it as a JSON API. This API allows creating
	 * LoanAccount with details, including creating custom fields.
	 * 
	 * @param loan
	 *            LoanAccountExtended object containing LoanAccount. LoanAccount encodedKey must be null for account
	 *            creation
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
		return serviceExecutor.executeJson(createAccount, loan);
	}

	/***
	 * Update an existent LoanAccount using LoanAccountExpanded object and sending it as a JSON API. This API allows
	 * updating LoanAccount with details. As of Mambu 3.4 only custom fields can be updated.
	 * 
	 * @param loan
	 *            LoanAccountExtended object containing LoanAccount. LoanAccount encodedKey or id must be NOT null for
	 *            account update
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
		String encodedKey = inputAccount.getEncodedKey() != null ? inputAccount.getEncodedKey() : inputAccount.getId();
		if (encodedKey == null) {
			throw new IllegalArgumentException("Cannot update Account: the encoded key or id must NOT be null");
		}

		return serviceExecutor.executeJson(updateAccount, loan, encodedKey);
	}

	/***
	 * Update loan terms for an existent LoanAccount This API allows updating LoanAccount terms only. Use
	 * updateLoanAccount() to update custom fields for a loan account
	 * 
	 * @param loan
	 *            LoanAccount object. Either LoanAccount encoded key or its ID must be NOT null for updating account
	 * 
	 *            Note that only some loan terms can be updated. See MBU-7758 for details.
	 * 
	 *            Loan Account fields available for patching are: loanAmount, interestRate. interestSpread,
	 *            repaymentInstallments, repaymentPeriodCount, repaymentPeriodUnit, expectedDisbursementDate,
	 *            firstRepaymentDate, gracePeriod, principalRepaymentInterval, penaltyRate, periodicPayment
	 * 
	 * @returns success or failure
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public boolean patchLoanAccount(LoanAccount loan) throws MambuApiException {
		// Example: PATCH JSON /api/loans/{ID}
		// See MBU-7758 for details
		if (loan == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		// The encodedKey or account Id must be not null
		String encodedKey = loan.getEncodedKey();
		String accountId = loan.getId();
		if (encodedKey == null && accountId == null) {
			throw new IllegalArgumentException("Cannot update Account, the encodedKey or ID must be NOT null");
		}

		String id = (accountId != null) ? accountId : encodedKey;
		ParamsMap params = ServiceHelper.makeParamsForLoanTermsPatch(loan);
		return serviceExecutor.execute(patchAccount, id, params);

	}

	/***
	 * Update tranches for an existent LoanAccount
	 * 
	 * @param accountId
	 *            the encoded key or id of the loan account. Must not be null.
	 * @param tranches
	 *            tranches for a loan account. Must not be null. Existent tranches with a valid "encodedKey" field will
	 *            be updated. Tranches with null "encodedKey" are treated as new tranches and will be created. Tranches
	 *            that are not specified in the call will be deleted. Already disbursed tranches cannot be changed or
	 *            deleted, they can be omitted when updating tranches.
	 * @return loan account with updated tranches
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public LoanAccount updateLoanAccountTranches(String accountId, List<LoanTranche> tranches) throws MambuApiException {
		// Available since Mambu 3.12.3. See MBU-9996

		// Example: POST api/loans/ABC123/tranches { tranches":[
		// edit a tranche
		// { "encodedKey":"40288a134f219912014f21991d8c0004", "amount":"400",
		// "expectedDisbursementDate":"2015-07-01T00:00:00+0000" }
		// add a tranche
		// {"amount":"500", "expectedDisbursementDate":"2015-08-11T00:00:00+0000"}
		// ]}

		if (tranches == null) {
			throw new IllegalArgumentException("Tranches must not be NULL");
		}

		// This API doesn't accept tranche "index" field as an allowed field. Set it to null
		for (LoanTranche tranche : tranches) {
			tranche.setIndex(null);
		}

		// Create JSONTranches object to be used for JSON format { tranches":[tranche, tranche]}
		JSONTranches jsonTranches = new JSONTranches();
		jsonTranches.setTranches(tranches);

		// Set ContentType to JSON (Update tranches API uses JSON format)
		updateAccountTranches.setContentType(ContentType.JSON);
		return serviceExecutor.executeJson(updateAccountTranches, jsonTranches, accountId);
	}

	/***
	 * Update funds for an existent Loan Account
	 * 
	 * @param accountId
	 *            the encoded key or id of the loan account. Account must not yet be disbursed. Must not be null.
	 * @param funds
	 *            funds to be updated. Must not be null
	 * @return loan account with updated funds
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public LoanAccount updateLoanAccountFunds(String accountId, List<InvestorFund> funds) throws MambuApiException {
		// Available since Mambu 3.13. See MBU-9885. MBU-11017 and MBU-11014

		// Example: POST api/loans/ABC123/funds { funds":[
		// // edit a fund
		// {"encodedKey": "40288a5d4f3fbac9014f3fd02745001d",
		// "guarantorKey": "40288a5d4f273153014f2731afe40102", "savingsAccountKey":
		// "40288a5d4f3fbac9014f3fcf822c0014","amount": "50"},
		// add a fund
		// {guarantorKey": "40288a5d4f273153014f2731afe40103","savingsAccountKey": "40288a5d4f3fbac9014f3fcf822c0015","amount": "100"}
		// ]}

		if (funds == null) {
			throw new IllegalArgumentException("Funds must not be NULL");
		}

		JSONInvestorFunds ivestorFunds = new JSONInvestorFunds();
		ivestorFunds.setFunds(funds);

		// Set ContentType to JSON (Update funds API uses JSON format)
		updateAccountFunds.setContentType(ContentType.JSON);
		return serviceExecutor.executeJson(updateAccountFunds, ivestorFunds, accountId);
	}

	/***
	 * Get loan account Transactions by Loan id and offset and limit
	 * 
	 * @param accountId
	 *            the id of the account offset - first transaction number limit - last transaction number Note: if
	 *            offset and limit both equal null, all transactions are returned (Note: transaction are sorted by date)
	 * 
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return the list of loan account transactions
	 * 
	 * @throws MambuApiException
	 */
	public List<LoanTransaction> getLoanAccountTransactions(String accountId, String offset, String limit)
			throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(APIData.OFFSET, offset);
		paramsMap.put(APIData.LIMIT, limit);

		return serviceExecutor.execute(getAccountTransactions, accountId, paramsMap);
	}

	/**
	 * Requests a list of loan transactions for a custom view, limited by offset/limit
	 * 
	 * @param customViewKey
	 *            the key of the Custom View to filter loan transactions
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return the list of Mambu loan transactions
	 * 
	 * @throws MambuApiException
	 */
	public List<LoanTransaction> getLoanTransactionsByCustomView(String customViewKey, String offset, String limit)
			throws MambuApiException {
		// Example GET loan/transactions?viewfilter=123&offset=0&limit=100
		String branchId = null;
		String centreId = null;
		String creditOfficerName = null;
		CustomViewResultType resultType = CustomViewResultType.BASIC;

		ParamsMap params = CustomViewsService.makeParamsForGetByCustomView(customViewKey, resultType, branchId,
				centreId, creditOfficerName, offset, limit);
		return serviceExecutor.execute(getAllLoanTransactions, params);

	}

	/**
	 * Get loan transactions by specifying filter constraints
	 * 
	 * @param filterConstraints
	 *            filter constraints. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return list of loan transactions matching filter constraint
	 * @throws MambuApiException
	 */
	public List<LoanTransaction> getLoanTransactions(JSONFilterConstraints filterConstraints, String offset,
			String limit) throws MambuApiException {
		// Available since Mambu 3.12. See MBU-8988 for more details
		// POST {JSONFilterConstraints} /api/loans/transactions/search?offset=0&limit=5

		ApiDefinition apiDefintition = SearchService
				.makeApiDefinitionforSearchByFilter(MambuEntityType.LOAN_TRANSACTION);

		// POST Filter JSON with pagination params map
		return serviceExecutor.executeJson(apiDefintition, filterConstraints, null, null,
				ServiceHelper.makePaginationParams(offset, limit));

	}

	/****
	 * Make Repayment for a loan account
	 * 
	 * @param accountId
	 *            account ID
	 * @param amount
	 *            transaction amount
	 * @param date
	 *            transaction date
	 * @param notes
	 *            transaction notes
	 * @param transactionDetails
	 *            transaction details, including transaction channel and channel fields
	 * 
	 * @return LoanTransaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction makeLoanRepayment(String accountId, String amount, String date, String notes,
			TransactionDetails transactionDetails) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_REPAYMENT);

		// Add transactionDetails to the paramsMap
		ServiceHelper.addAccountTransactionParams(paramsMap, amount, date, notes, transactionDetails);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);
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

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);
	}

	/****
	 * Apply Interest to a loan account on a given date
	 * 
	 * @param accountId
	 *            the id of the account. Mandatory
	 * @param date
	 *            date. Mandatory.
	 * @param notes
	 *            notes
	 * @return Loan Transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction applyInterestToLoanAccount(String accountId, Date date, String notes)
			throws MambuApiException {
		// Example: POST "type=INTEREST_APPLIED&date=2011-09-01" /api/loans/KHGJ593/transactions
		// Available since Mambu 3.1. See MBU-2938

		if (date == null) {
			throw new IllegalArgumentException("Date cannot be null");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, APIData.TYPE_INTEREST_APPLIED);
		paramsMap.addParam(APIData.DATE, DateUtils.format(date));
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);
	}

	/***
	 * Get the loan accounts by branch id, centreId, credit officer, accountState
	 * 
	 * @param branchId
	 *            branchID The ID of the branch to which the loan accounts are assigned to
	 * @param centreId
	 *            The ID of the centre to which the loan accounts are assigned to. If both branchId and centreId are
	 *            provided then this centre must be assigned to the branchId
	 * @param creditOfficerUserName
	 *            The username of the credit officer to whom the loans are assigned to
	 * @param accountState
	 *            The desired state of the accounts to filter on (eg: APPROVED)
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return the list of loan accounts matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	public List<LoanAccount> getLoanAccountsByBranchCentreOfficerState(String branchId, String centreId,
			String creditOfficerUserName, String accountState, String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam(BRANCH_ID, branchId);
		params.addParam(CENTRE_ID, centreId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.addParam(ACCOUNT_STATE, accountState);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		return serviceExecutor.execute(getAccountsList, params);
	}

	/***
	 * Get loan accounts by branch id, credit officer, accountState. This is a convenience method to filter loan
	 * accounts without specifying centre id (centre id filtering is available only since Mambu 3.7,see MBU-5946)
	 * 
	 * @param branchId
	 *            branchID The ID of the branch to which the loan accounts are assigned to
	 * @param creditOfficerUserName
	 *            The username of the credit officer to whom the loans are assigned to
	 * @param accountState
	 *            The desired state of the accounts to filter on (eg: APPROVED)
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return the list of loan accounts matching these parameters
	 * 
	 * @throws MambuApiException
	 */

	public List<LoanAccount> getLoanAccountsByBranchOfficerState(String branchId, String creditOfficerUserName,
			String accountState, String offset, String limit) throws MambuApiException {
		final String centreId = null;
		return getLoanAccountsByBranchCentreOfficerState(branchId, centreId, creditOfficerUserName, accountState,
				offset, limit);

	}

	/**
	 * Requests a list of loan accounts for a custom view, limited by offset/limit
	 * 
	 * @param customViewKey
	 *            the key of the Custom View to filter loan accounts
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return the list of Mambu loan accounts
	 * 
	 * @throws MambuApiException
	 */
	public List<LoanAccount> getLoanAccountsByCustomView(String customViewKey, String offset, String limit)
			throws MambuApiException {
		String branchId = null;
		String centreId = null;
		String creditOfficerName = null;
		CustomViewResultType resultType = CustomViewResultType.BASIC;
		ParamsMap params = CustomViewsService.makeParamsForGetByCustomView(customViewKey, resultType, branchId,
				centreId, creditOfficerName, offset, limit);
		return serviceExecutor.execute(getAccountsList, params);

	}

	/**
	 * Get loan accounts by specifying filter constraints
	 * 
	 * @param filterConstraints
	 *            filter constraints. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return list of loan accounts matching filter constraints
	 * @throws MambuApiException
	 */
	public List<LoanAccount> getLoanAccounts(JSONFilterConstraints filterConstraints, String offset, String limit)
			throws MambuApiException {
		// Available since Mambu 3.12. See MBU-8988 for more details
		// POST {JSONFilterConstraints} /api/loans/search?offset=0&limit=5

		ApiDefinition apiDefintition = SearchService.makeApiDefinitionforSearchByFilter(MambuEntityType.LOAN_ACCOUNT);

		// POST Filter JSON with pagination params map
		return serviceExecutor.executeJson(apiDefintition, filterConstraints, null, null,
				ServiceHelper.makePaginationParams(offset, limit));

	}

	// Loan Products
	/***
	 * Get a list of Loan Products
	 * 
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return the List of Loan Products
	 * 
	 * @throws MambuApiException
	 */
	public List<LoanProduct> getLoanProducts(String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		return serviceExecutor.execute(getProductsList, params);
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
		return serviceExecutor.execute(getProduct, productId);
	}

	/***
	 * Get repayment schedule preview for a Loan Product
	 * 
	 * @param productId
	 *            the id of the loan product. Must not be null.
	 * @param account
	 *            loan account containing parameters for determining loan schedule
	 * 
	 *            Only the following loan account parameters are currently supported: loanAmount (mandatory),
	 *            anticipatedDisbursement, firstRepaymentDate, interestRate, repaymentInstallments, gracePeriod,
	 *            repaymentPeriodUnit, repaymentPeriodCount, principalRepaymentInterval, fixedDaysOfMonth
	 * 
	 *            Loan repayment schedule preview is not available for Revolving Credit products. See MBU-10545
	 * 
	 *            See MBU-6789, MBU-7676 and MBU-10802 for more details
	 * 
	 * @return the List of Repayments
	 * 
	 * @throws MambuApiException
	 */
	public List<Repayment> getLoanProductSchedule(String productId, LoanAccount account) throws MambuApiException {
		// E.g. GET /api/loanproducts/{ID}/schedule?loanAmount=1250&anticipatedDisbursement=2015-02-10&interestRate=4
		// E.g. GET /api/loanproducts/{ID}/schedule?loanAmount=1250&fixedDaysOfMonth=2,10,20

		if (account == null) {
			throw new IllegalArgumentException("Loan Account cannot be null");
		}
		if (account.getLoanAmount() == null || account.getLoanAmount().isZero()) {
			throw new IllegalArgumentException("Loan Amount must be not null and not zero. It is "
					+ account.getLoanAmount());
		}
		// Add applicable params to the map
		ParamsMap params = ServiceHelper.makeParamsForLoanSchedule(account);

		// The API returns a JSONLoanRepayments object containing a list of repayments
		JSONLoanRepayments jsonRepayments = serviceExecutor.execute(getProductSchedule, productId, params);
		// Return list of repayments
		return jsonRepayments.getRepayments();
	}

	/***
	 * Get all documents for a specific Loan Account
	 * 
	 * @deprecated Starting from 3.14 use
	 *             {@link DocumentsService#getDocuments(MambuEntityType, String, Integer, Integer)}. This methods
	 *             supports pagination parameters
	 * @param accountId
	 *            the encoded key or id of the loan account for which attached documents are to be retrieved
	 * 
	 * @return documents documents attached to the entity
	 * 
	 * @throws MambuApiException
	 */
	public List<Document> getLoanAccountDocuments(String accountId) throws MambuApiException {
		return serviceExecutor.execute(getAccountDocuments, accountId);
	}

	/****
	 * Reverse loans transactions for a loan account
	 * 
	 * @param accountId
	 *            the id or encoded key of the loan account. Mandatory
	 * @param originalTransactionType
	 *            Original transaction type to be reversed. The following transaction types can be currently reversed:
	 *            PENALTY_APPLIED. Must not be null.
	 * @param originalTransactionId
	 *            the id or the encodedKey of the transaction to be reversed. Must not be null.
	 * @param notes
	 *            transaction notes
	 * @return Loan Transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction reverseLoanTransaction(String accountId, LoanTransactionType originalTransactionType,
			String originalTransactionId, String notes) throws MambuApiException {

		// PENALTY_APPLIED reversal is available since 3.13. See MBU-9998 for more details
		// POST "type=PENALTY_ADJUSTMENT&notes=reason&originalTransactionId=123" /api/loans/{id}/transactions/

		// originalTransactionType is mandatory
		if (originalTransactionType == null) {
			throw new IllegalArgumentException("Transaction Type cannot be null");
		}
		// originalTransactionId is mandatory
		if (originalTransactionId == null || originalTransactionId.isEmpty()) {
			throw new IllegalArgumentException("Original Transaction ID must not be null or empty");
		}
		// Get reversal transaction type for the original transaction type
		String transactionTypeParam;
		switch (originalTransactionType) {
		case PENALTY_APPLIED:
			transactionTypeParam = TYPE_PENALTY_ADJUSTMENT;
			break;

		default:
			throw new IllegalArgumentException("Reversal for Loan Transaction Type " + originalTransactionType.name()
					+ " is not supported");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, transactionTypeParam);
		paramsMap.addParam(ORIGINAL_TRANSACTION_ID, originalTransactionId);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);
	}

	/****
	 * Convenience method to Reverse loan transaction by providing the original loan transaction
	 * 
	 * @param originalTransaction
	 *            The following loan transactions types currently can be reversed: PENALTY_APPLIED. Mandatory.
	 * @param notes
	 *            transaction notes
	 * @return Loan Transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction reverseLoanTransaction(LoanTransaction originalTransaction, String notes)
			throws MambuApiException {

		// PENALTY_APPLIED reversal is available since 3.13. See MBU-9998 for more details
		// Example: POST "type=PENALTY_ADJUSTMENT&notes=reason&originalTransactionId=123" /api/loans/{id}/transactions/

		if (originalTransaction == null) {
			throw new IllegalArgumentException("Original Transaction cannot be null");
		}
		// Get original transaction Key from the original transaction. Either encoded key or transaction id can be used
		String transactionId = String.valueOf(originalTransaction.getTransactionId());
		if (transactionId == null || transactionId.isEmpty()) {
			// If no ID, try getting the encoded key.
			String transactionKey = originalTransaction.getEncodedKey();
			if (transactionKey == null) {
				throw new IllegalArgumentException(
						"Original Transaction must have either the encoded key or id not null and not empty");
			}
			// Use encoded key
			transactionId = transactionKey;
		}
		// Get account id and original transaction type from the original transaction
		String accountId = originalTransaction.getParentAccountKey();
		LoanTransactionType transactionType = originalTransaction.getType();

		return reverseLoanTransaction(accountId, transactionType, transactionId, notes);
	}
}
