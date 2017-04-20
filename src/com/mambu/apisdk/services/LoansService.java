/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.Date;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mambu.accounts.shared.model.Account.Type;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.accountsecurity.shared.model.Guaranty;
import com.mambu.accountsecurity.shared.model.InvestorFund;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraints;
import com.mambu.api.server.handler.funds.model.JSONInvestorFunds;
import com.mambu.api.server.handler.guarantees.model.JSONGuarantees;
import com.mambu.api.server.handler.loan.model.JSONApplyManualFee;
import com.mambu.api.server.handler.loan.model.JSONLoanAccount;
import com.mambu.api.server.handler.loan.model.JSONLoanAccountResponse;
import com.mambu.api.server.handler.loan.model.JSONLoanRepayments;
import com.mambu.api.server.handler.loan.model.JSONRestructureEntity;
import com.mambu.api.server.handler.loan.model.JSONTransactionRequest;
import com.mambu.api.server.handler.loan.model.RestructureDetails;
import com.mambu.api.server.handler.tranches.model.JSONTranches;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.json.LoanAccountPatchJsonSerializer;
import com.mambu.apisdk.json.LoanProductScheduleJsonSerializer;
import com.mambu.apisdk.model.ApiLoanAccount;
import com.mambu.apisdk.model.SettlementAccount;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.DateUtils;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.apisdk.util.ServiceHelper;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.Money;
import com.mambu.loans.shared.model.CustomPredefinedFee;
import com.mambu.loans.shared.model.DisbursementDetails;
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

	private static final String TYPE = APIData.TYPE;
	private static final String NOTES = APIData.NOTES;
	//
	private static final String TYPE_APPROVAL = APIData.TYPE_APPROVAL;
	private static final String TYPE_REQUEST_APPROVAL = APIData.TYPE_REQUEST_APPROVAL;
	private static final String TYPE_UNDO_APPROVAL = APIData.TYPE_UNDO_APPROVAL;
	private static final String TYPE_FEE = APIData.TYPE_FEE;
	private static final String TYPE_LOCK = APIData.TYPE_LOCK;
	private static final String TYPE_UNLOCK = APIData.TYPE_UNLOCK;
	private static final String TYPE_WRITE_OFF = APIData.TYPE_WRITE_OFF;
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
	// Create an API definition to get loan account with full details and request returning as ApiLoanAccount
	ApiDefinition getApiLoanAccount = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, LoanAccount.class,
			ApiLoanAccount.class);
	// Get Lists of Accounts
	private final static ApiDefinition getAccountsList = new ApiDefinition(ApiType.GET_LIST, LoanAccount.class);
	// Get Accounts for a Client
	private final static ApiDefinition getAccountsForClient = new ApiDefinition(ApiType.GET_OWNED_ENTITIES,
			Client.class, LoanAccount.class);
	// Get Accounts for a Group
	private final static ApiDefinition getAccountsForGroup = new ApiDefinition(ApiType.GET_OWNED_ENTITIES, Group.class,
			LoanAccount.class);
	// Get Account Transactions (transactions for a specific loan account)
	private final static ApiDefinition getAccountTransactions = new ApiDefinition(ApiType.GET_OWNED_ENTITIES,
			LoanAccount.class, LoanTransaction.class);
	// Post Account Transactions. Params map defines the transaction type. Return LoanTransaction
	private final static ApiDefinition postAccountTransaction = new ApiDefinition(ApiType.POST_OWNED_ENTITY,
			LoanAccount.class, LoanTransaction.class);
	// // Post JSON Account Transactions. Returns LoanTransaction
	private final static ApiDefinition postAccountJSONTransaction;
	static {
		postAccountJSONTransaction = new ApiDefinition(ApiType.POST_OWNED_ENTITY, LoanAccount.class,
				LoanTransaction.class);
		postAccountJSONTransaction.setContentType(ContentType.JSON);
	}
	// Post Account state change. Params map defines the account change transaction. Return LoanAccount
	private final static ApiDefinition postAccountChange = new ApiDefinition(ApiType.POST_ENTITY_ACTION,
			LoanAccount.class, LoanTransaction.class);
	// Delete Account
	private final static ApiDefinition deleteAccount = new ApiDefinition(ApiType.DELETE_ENTITY, LoanAccount.class);
	// Create Account
	private final static ApiDefinition createAccount = new ApiDefinition(ApiType.CREATE_JSON_ENTITY,
			JSONLoanAccount.class);
	// Update Account. Used to update custom fields for loan accounts only. POST JSON /api/loans/loanId
	private final static ApiDefinition updateAccount = new ApiDefinition(ApiType.POST_ENTITY, JSONLoanAccount.class);
	// Patch Account. Used to update loan terms only. PATCH JSON /api/loans/loanId
	private final static ApiDefinition patchAccount;
	static {
		patchAccount = new ApiDefinition(ApiType.PATCH_ENTITY, LoanAccount.class);
		// Use LoanAccountPatchJsonSerializer to make the expected format
		patchAccount.addJsonSerializer(LoanAccount.class, new LoanAccountPatchJsonSerializer());
	}
	// Used to link loan accounts with savings accounts
	private final static ApiDefinition postSettlementForLoanAccount = new ApiDefinition(ApiType.POST_OWNED_ENTITY,
			LoanAccount.class, SettlementAccount.class);
	// Update Loan Tranches. Returns updated LoanAccount. POST /api/loans/loanId/tranches
	private final static ApiDefinition updateAccountTranches = new ApiDefinition(ApiType.POST_ENTITY_ACTION,
			LoanAccount.class, LoanTranche.class);
	// Update Loan Investor Funds. Returns updated LoanAccount. POST /api/loans/loanId/funds
	private final static ApiDefinition updateAccountFunds = new ApiDefinition(ApiType.POST_ENTITY_ACTION,
			LoanAccount.class, InvestorFund.class);
	// Update Loan Account Guarantees API. Returns LoanAccount. POST /api/loans/loanId/guarantees
	private final static ApiDefinition updateAccountGuarantees = new ApiDefinition(ApiType.POST_ENTITY_ACTION,
			LoanAccount.class, Guaranty.class);
	// Loan Products API requests
	// Get Loan Product Details
	private final static ApiDefinition getProduct = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, LoanProduct.class);
	// Get Lists of Loan Products
	private final static ApiDefinition getProductsList = new ApiDefinition(ApiType.GET_LIST, LoanProduct.class);
	// Get schedule for Loan Products. GET /api/loanproducts/<ID>/schedule?loanAmount=50. Returns JSONLoanRepayments
	private final static ApiDefinition getProductSchedule;
	static {
		getProductSchedule = new ApiDefinition(ApiType.GET_OWNED_ENTITY, LoanProduct.class, JSONLoanRepayments.class);
		// Use LoanProductScheduleJsonSerializer
		getProductSchedule.addJsonSerializer(LoanAccount.class, new LoanProductScheduleJsonSerializer());
	}

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
	 * Get a loan account with full details by its id
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

	/**
	 * Get full loan account details, including settlement accounts
	 * 
	 * @param accountId
	 *            the id or encoded key of a loan account. Must not be null
	 * @return JSON loan account response with loan account details and settlement savings accounts included
	 * @throws MambuApiException
	 */

	public JSONLoanAccountResponse getLoanAccountWithSettlementAccounts(String accountId) throws MambuApiException {

		// Example: GET /api/loans/accountId?fullDetails=true
		// For getting settlement accounts is available since 4.0 See MBU-11206
		// Note: This API uses GET Loan with full details request. The settlement accounts are also returned by this
		// method in an ApiLoanAccount object

		// Request loan account with settlement accounts. Deserialize as ApiLoanAccount matching the response format
		ApiLoanAccount apiLoanAccount = serviceExecutor.execute(getApiLoanAccount, accountId);

		// Return as JSONLoanAccountResponse, containing LoanAccount and a list of settlement accounts
		JSONLoanAccountResponse loanAccountResponse = null;
		if (apiLoanAccount != null) {
			loanAccountResponse = new JSONLoanAccountResponse(apiLoanAccount, apiLoanAccount.getSettlementAccounts());
		}
		return loanAccountResponse;
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

		// See MBU-8370. Lock account API now returns a list of transactions
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
	 * Close loan account with all obligations met
	 * 
	 * @param accountId
	 *            the id of the account. Must not be nul
	 * @param notes
	 *            the reason why the account is closed
	 * 
	 * @return LoanAccount
	 * 
	 * @throws MambuApiException
	 */
	public LoanAccount closeLoanAccount(String accountId, String notes) throws MambuApiException {

		// E.g. format: POST "type=CLOSE" /api/loans/KHGJ593/transactions
		// Available since Mambu 4.0. See MBU-10975
		return closeLoanAccount(accountId, APIData.CLOSER_TYPE.CLOSE, notes);
	}

	/****
	 * Close Loan account specifying the type of closer (withdraw, reject or close)
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
		// POST "type=REJECT" /api/loans/KHGJ593/transactions
		// POST "type=CLOSE" /api/loans/KHGJ593/transactions
		// Available since Mambu 3.3 See MBU-3090 and MBU-10975 for details.
		if (closerType == null) {
			throw new IllegalArgumentException("Closer Type must not  be null");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, closerType.name());
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountChange, accountId, paramsMap);
	}

	/****
	 * Undo Close Loan account. Supports UNDO_CLOSE, UNDO_WITHDRAWN, UNDO_REJECT
	 * 
	 * @param loanAccount
	 *            closed loan account. Must not be null and must be in one of the supported closed states.
	 * @param notes
	 *            undo closer reason notes
	 * @return loan account
	 * 
	 * @throws MambuApiException
	 */

	public LoanAccount undoCloseLoanAccount(LoanAccount loanAccount, String notes) throws MambuApiException {

		// Available since Mambu 4.2. See MBU-13190 for details.
		// Supports UNDO_CLOSE, UNDO_WITHDRAWN, UNDO_REJECT

		// E.g. POST "type=UNDO_REJECT" /api/loans/KHGJ593/transactions
		// E.g. POST "type=UNDO_WITHDRAWN" /api/loans/KHGJ593/transactions
		// E.g. POST "type=UNDO_CLOSE" /api/loans/KHGJ593/transactions

		if (loanAccount == null || loanAccount.getId() == null || loanAccount.getState() == null) {
			throw new IllegalArgumentException("Account, its ID and account state must not  be null");
		}

		// Get the transaction type based on how the account was closed
		String undoCloserTransactionType = ServiceHelper.getUndoCloserTransactionType(loanAccount);
		if (undoCloserTransactionType == null) {
			throw new IllegalArgumentException("Account is not in a state to perform UNDO close via API. Account State="
					+ loanAccount.getState() + " Sub-state=" + loanAccount.getSubState());
		}

		// Create params map with expected API's params
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, undoCloserTransactionType);
		paramsMap.addParam(NOTES, notes);

		// Execute API
		String accountId = loanAccount.getId();
		return serviceExecutor.execute(postAccountChange, accountId, paramsMap);
	}

	/**
	 * Convenience method to Disburse loan account using JSON Transaction and specifying Disbursement Details. The JSON
	 * disburse API request supports providing transaction details and disbursement fees. See MBU-11837
	 * 
	 * @param accountId
	 *            loan account id or encoded key. Must not be null
	 * @param amount
	 *            disbursement amount.
	 * @param disbursementDetails
	 *            disbursement details for the loan account containing optional transaction custom fields
	 * @param notes
	 *            transaction notes
	 * @return loan transaction
	 * @throws MambuApiException
	 */
	public LoanTransaction disburseLoanAccount(String accountId, Money amount, DisbursementDetails disbursementDetails,
			String notes) throws MambuApiException {

		// Disburse loan account using JSON format and optionally specifying transaction details and disbursement fees
		// See MBU-8811, MBU-10045, MBU-11853

		// Example: POST {"type":"DISBURSEMENT",
		// date":"2016-02-20T16:00:00-0800", "firstRepaymentDate":"2016-02-27T16:00:00-0800",
		// "method":"channel_id_1”, "checkNumber”:”123”,”bankAccountNumber”:”456”,
		// fees": [{"encodedKey":"feeKey1"}, {encodedKey":"feeKey2", "amount":"100.00"}], "notes":"notes"}

		// Get Transaction custom information
		List<CustomFieldValue> customInformation = disbursementDetails != null
				? disbursementDetails.getCustomFieldValues() : null;
		// Get JSONTransaction and return LoanAccount
		return disburseLoanAccount(accountId, amount, disbursementDetails, customInformation, notes);

	}

	/**
	 * Disburse loan account using JSON disburse API request specifying disbursement details and transaction custom
	 * information. The JSON disburse API request supports providing transaction details and disbursement fees.
	 * 
	 * @param accountId
	 *            loan account id or encoded key. Must not be null
	 * @param amount
	 *            disbursement amount.
	 * @param disbursementDetails
	 *            disbursement details for the loan account
	 * @param customInformation
	 *            transaction custom fields
	 * @param notes
	 *            transaction notes
	 * @return loan transaction
	 * @throws MambuApiException
	 */
	public LoanTransaction disburseLoanAccount(String accountId, Money amount, DisbursementDetails disbursementDetails,
			List<CustomFieldValue> customInformation, String notes) throws MambuApiException {

		// Disburse loan account using JSON format and optionally specifying transaction details, disbursement fees and
		// transaction custom fields
		// See MBU-8811, MBU-10045, MBU-11853,MBU-12098

		// Transaction Custom fields available since Mambu 4.1 See MBU-11837. Channel fields are also migrated to custom
		// fields, See MBU-12098
		// Example: POST {"type":"DISBURSEMENT",
		// date":"2016-02-20T16:00:00-0800", "firstRepaymentDate":"2016-02-27T16:00:00-0800",
		// "method":"channel_id_1”, "checkNumber”:”123”,”bankAccountNumber”:”456”,
		// fees": [{"encodedKey":"feeKey1"}, {encodedKey":"feeKey2", "amount":"100.00"}],
		// "customInformation":[{ "value":"Pending", "customFieldID":"Status" ], "notes":"notes"}

		// Create JSONTransactionRequest
		JSONTransactionRequest request = ServiceHelper.makeJSONTransactionRequest(amount, disbursementDetails,
				customInformation, notes);

		LoanTransaction loanTransaction = serviceExecutor.executeJSONTransactionRequest(accountId, request, Type.LOAN,
				LoanTransactionType.DISBURSMENT.name());
		return loanTransaction;
	}

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
		paramsMap.addParam(TYPE, LoanTransactionType.DISBURSMENT_ADJUSTMENT.name());
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);

	}

	/**
	 * POST action for loan account. Currently REFINANCE and RESCHEDULE actions are supported
	 * 
	 * @param accountId
	 *            the encoded key or id of the original loan account. Must not be null
	 * @param restructureEntity
	 *            restructure entity containing the action, loan account and restructure details. Must not be null. Loan
	 *            account must not be null. Action must not be null
	 * @return new loan account
	 * @throws MambuApiException
	 */
	public LoanAccount postLoanAccountRestructureAction(String accountId, JSONRestructureEntity restructureEntity)
			throws MambuApiException {

		// Available since Mambu 4.1. See MBU-12051, MBU-12052 and MBU-12217.
		// REFINANCE and RESCHEDULE actions are supported
		// E.g.: POST {JSONRestructureEntity} /api/loans/{LOAN_ID}/action

		if (accountId == null || restructureEntity == null || restructureEntity.getLoanAccount() == null) {
			throw new IllegalArgumentException(
					"Account ID, the Restructure Entity and its LoanAccount must not be null");
		}
		// Check if action is present
		if (restructureEntity.getAction() == null) {
			throw new IllegalArgumentException("Action must not be null");
		}
		// Create POST JSON ApiDefinition
		// URL path: /api/loans/{LOAN_ID}/action
		String urlPath = APIData.LOANS + "/" + accountId + "/" + APIData.ACTION;
		ApiDefinition postJsonAccountChange = new ApiDefinition(urlPath, ContentType.JSON, Method.POST,
				LoanAccount.class, ApiReturnFormat.OBJECT);
		// Execute request
		return serviceExecutor.executeJson(postJsonAccountChange, restructureEntity);
	}

	/**
	 * Convenience method to Reschedule loan account
	 * 
	 * @param accountId
	 *            the encoded key or id of the original loan account. Must not be null
	 * @param loanAccount
	 *            loan account with new details. Must not be null.
	 * @param customFieldValues
	 *            optional custom field values. Allowed are any of the original account custom fields, regardless of the
	 *            new product and any new custom fields applicable to the new product
	 * @param restructureDetails
	 *            optional restructure details
	 * @return new loan account
	 * @throws MambuApiException
	 */
	public LoanAccount rescheduleLoanAccount(String accountId, LoanAccount loanAccount,
			List<CustomFieldValue> customFieldValues, RestructureDetails restructureDetails) throws MambuApiException {

		// Available since Mambu 4.1 See MBU-12051 and MBU-12217
		// E.g.: POST {JSONRestructureEntity} /api/loans/{LOAN_ID}/action
		if (loanAccount == null) {
			throw new IllegalArgumentException("LoanAccount must not be null");
		}
		JSONRestructureEntity restructureEntity = new JSONRestructureEntity();
		restructureEntity.setAction(APIData.RESCHEDULE);
		restructureEntity.setLoanAccount(loanAccount);
		restructureEntity.setCustomInformation(customFieldValues);
		restructureEntity.setRestructureDetails(restructureDetails);

		return postLoanAccountRestructureAction(accountId, restructureEntity);
	}

	/**
	 * Convenience method to Refinance loan account
	 * 
	 * @param accountId
	 *            the encoded key or id of the original loan account. Must not be null
	 * @param loanAccount
	 *            loan account with new details. Must not be null
	 * @param customFieldValues
	 *            optional custom field values. Allowed are any of the original account custom fields, regardless of the
	 *            new product and any new custom fields applicable to the new product
	 * @param restructureDetails
	 *            mandatory restructure details. Must not be null
	 * @return new loan account
	 * @throws MambuApiException
	 */
	public LoanAccount refinanceLoanAccount(String accountId, LoanAccount loanAccount,
			List<CustomFieldValue> customFieldValues, RestructureDetails restructureDetails) throws MambuApiException {

		// Available since Mambu 4.1. See MBU-12052 and MBU-12217
		// E.g.: POST {JSONRestructureEntity} /api/loans/{LOAN_ID}/action
		if (loanAccount == null || restructureDetails == null) {
			throw new IllegalArgumentException("LoanAccount and Restructure Entity must not be null");
		}

		JSONRestructureEntity restructureEntity = new JSONRestructureEntity();
		restructureEntity.setAction(APIData.REFINANCE);
		restructureEntity.setLoanAccount(loanAccount);
		restructureEntity.setCustomInformation(customFieldValues);
		restructureEntity.setRestructureDetails(restructureDetails);

		return postLoanAccountRestructureAction(accountId, restructureEntity);
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
	 * Create new LoanAccount using LoanAccount object. This API allows creating LoanAccount with details, including
	 * creating custom fields.
	 * 
	 * The underlying API implementation uses JSONLoanAccount object.
	 * 
	 * @param loanAccount
	 *            LoanAccount object. LoanAccount encodedKey must be null for account creation
	 * @return newly created loan account with full details including custom fields
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public LoanAccount createLoanAccount(LoanAccount loanAccount) throws MambuApiException {

		if (loanAccount == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		if (loanAccount.getEncodedKey() != null) {
			throw new IllegalArgumentException("Cannot create Account, the encoded key must be null");
		}
		// Create JSONLoanAccount to use in Mambu API. Mambu expects the following format:
		// {"loanAccount":{.....}, "customInformation":[{field1},{field2}]}
		JSONLoanAccount jsonLoanAccount = new JSONLoanAccount(loanAccount);
		jsonLoanAccount.setCustomInformation(loanAccount.getCustomFieldValues());
		// Clear custom fields at the account level, no need to send them in two places
		loanAccount.setCustomFieldValues(null);

		// Send API request to Mambu
		JSONLoanAccount createdJsonAccount = serviceExecutor.executeJson(createAccount, jsonLoanAccount);
		// Get Loan account
		LoanAccount createdLoanAccount = null;
		if (createdJsonAccount != null && createdJsonAccount.getLoanAccount() != null) {
			createdLoanAccount = createdJsonAccount.getLoanAccount();
			// Copy returned custom information into the loan account
			createdLoanAccount.setCustomFieldValues(createdJsonAccount.getCustomInformation());

		}

		return createdLoanAccount;
	}

	/***
	 * Update an existent LoanAccount using LoanAccount object and sending it as a JSON API. This API allows updating
	 * LoanAccount with details. As of Mambu 3.4 only custom fields can be updated.
	 * 
	 * @param loanAccount
	 *            LoanAccount object containing LoanAccount. LoanAccount encodedKey or id must be NOT null for account
	 *            update
	 * 
	 * @return updated LoanAccount object with updated custom fields
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public LoanAccount updateLoanAccount(LoanAccount loanAccount) throws MambuApiException {

		if (loanAccount == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		String encodedKey = loanAccount.getEncodedKey() != null ? loanAccount.getEncodedKey() : loanAccount.getId();
		if (encodedKey == null) {
			throw new IllegalArgumentException("Cannot update Account: the encoded key or id must NOT be null");
		}

		// Mambu API expects the request in the following format:
		// {"loanAccount":{.....}, "customInformation":[{field1},{field2}]}
		// Create JSONLoanAccount object for the API request. Set custom information in JSONLoanAccount
		JSONLoanAccount jsonLoanAccount = new JSONLoanAccount(loanAccount);
		jsonLoanAccount.setCustomInformation(loanAccount.getCustomFieldValues());
		// Clear custom fields at the account level, no need to send them in two places
		loanAccount.setCustomFieldValues(null);

		// Submit update account request to Mambu providing JSONLoanAccount object
		JSONLoanAccount updatedJsonAccount = serviceExecutor.executeJson(updateAccount, jsonLoanAccount, encodedKey);
		// Get Loan Account
		LoanAccount updatedLoanAccount = null;
		if (updatedJsonAccount != null && updatedJsonAccount.getLoanAccount() != null) {
			updatedLoanAccount = updatedJsonAccount.getLoanAccount();
			// Copy returned custom information into the loan account
			updatedLoanAccount.setCustomFieldValues(updatedJsonAccount.getCustomInformation());

		}
		return updatedLoanAccount;
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

		String id = accountId != null ? accountId : encodedKey;
		return serviceExecutor.executeJson(patchAccount, loan, id);

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
	public LoanAccount updateLoanAccountTranches(String accountId, List<LoanTranche> tranches)
			throws MambuApiException {

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
		// "40288a5d4f3fbac9014f3fcf822c0014","amount": "50", "interestCommission":"3"},
		// add a fund
		// {guarantorKey": "40288a5d4f273153014f2731afe40103","savingsAccountKey":
		// "40288a5d4f3fbac9014f3fcf822c0015","amount": "100" ,"interestCommission":"3"}
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
	 * Update guarantees for an existent Loan Account
	 * 
	 * @param accountId
	 *            the encoded key or id of the loan account. Must not be null.
	 * @param guarantees
	 *            guarantees to be updated. Must not be null. The guarantees that have encodedKey will be edited. If the
	 *            encodedKey is not present, a new guaranty will be created. Existing guarantees that are not specified
	 *            in the update call will be deleted
	 * @return loan account with updated guarantees
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public LoanAccount updateLoanAccountGuarantees(String accountId, List<Guaranty> guarantees)
			throws MambuApiException {

		// Available since Mambu 4.0. See MBU-11315
		// Example: POST api/loans/ABC123/guarantees "guarantees":[{
		// "assetName": "car", "amount": "4000", "type": "ASSET", "customFieldValues": [ {…}]
		// }]

		if (guarantees == null) {
			throw new IllegalArgumentException("Guarantees must not be NULL");
		}

		JSONGuarantees jsonGuarantees = new JSONGuarantees();
		jsonGuarantees.setGuarantees(guarantees);

		// Set ContentType to JSON (Update guarantees API uses JSON format)
		updateAccountGuarantees.setContentType(ContentType.JSON);
		return serviceExecutor.executeJson(updateAccountGuarantees, jsonGuarantees, accountId);
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
	 * Make Repayment for a loan account. POST as JSON transaction
	 * 
	 * @param accountId
	 *            account ID or encoded key. Must not be null
	 * @param amount
	 *            transaction amount
	 * @param date
	 *            transaction date
	 * @param customInformation
	 *            transaction custom fields
	 * @param notes
	 *            transaction notes
	 * @param transactionDetails
	 *            transaction details, including transaction channel and channel fields
	 * 
	 * @return loan transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction makeLoanRepayment(String accountId, Money amount, Date date,
			TransactionDetails transactionDetails, List<CustomFieldValue> customInformation, String notes)
			throws MambuApiException {

		// POST {JSONTransactionRequest} /api/loans/accountId/transactions
		// Create JSONTransactionRequest
		JSONTransactionRequest request = ServiceHelper.makeJSONTransactionRequest(amount, date, null,
				transactionDetails, null, customInformation, notes);

		LoanTransaction loanTransaction = serviceExecutor.executeJSONTransactionRequest(accountId, request, Type.LOAN,
				LoanTransactionType.REPAYMENT.name());

		return loanTransaction;
	}

	/****
	 * Apply FEE to a loan account
	 * 
	 * @param accountId
	 *            the id or encoded key of the account
	 * @param amount
	 *            transaction amount
	 * @param repaymentNumber
	 *            repayment number
	 * @param notes
	 *            notes
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

	/**
	 * Apply Predefined Fee to a loan account
	 * 
	 * @param accountId
	 *            account id or encoded key. Must not be null
	 * @param fees
	 *            fees. Only Manual Predefined Fees are currently supported. Must not be null. Must contain exactly one
	 *            fee.
	 * 
	 *            Note: Once MBU-12865 is implemented this method will support both predefined fees and arbitrary fees
	 *            and the (@link #applyFeeToLoanAccount(String, String, String, String)} method used only for arbitrary
	 *            fees can be deprecated
	 * @param repaymentNumber
	 *            repayment number. Can be specified only for fixed loans
	 * @param notes
	 *            notes
	 * @return loan transaction
	 * @throws MambuApiException
	 */
	public LoanTransaction applyFeeToLoanAccount(String accountId, List<CustomPredefinedFee> fees,
			Integer repaymentNumber, String notes) throws MambuApiException {

		// Allows posting manual predefined fees.
		// Support for manual predefined fees available since Mambu 4.1. See MBU-12272

		// Example: POST /api/loans/LOAN_ID/transactions
		// {"type":"FEE",
		// "fees":[{"encodedKey":"8a80816752715c34015278bd4792084b","amount":"20" }],
		// "repayment":"2","notes":"test" ]

		if (fees == null || fees.size() != 1) {
			throw new IllegalArgumentException("There must be exactly one fee present");
		}
		// Create JSONTransactionRequest for Apply FEE API - need to specify only fees, repayment number and notes
		JSONApplyManualFee transactionRequest = ServiceHelper.makeJSONApplyManualFeeRequest(fees, repaymentNumber,
				notes);

		return serviceExecutor.executeJSONTransactionRequest(accountId, transactionRequest, Type.LOAN,
				LoanTransactionType.FEE.name());

	}

	/**
	 * Convenience method to execute Loan Account transaction by providing JSONTransactionRequest
	 * 
	 * @param accountId
	 *            account id or encoded key. Must not be null
	 * @param transactionType
	 *            loan transaction type. Must not be null. Supported types are: DISBURSMENT, FEE, REPAYMENT and
	 *            INTEREST_RATE_CHANGED
	 * @param transactionRequest
	 *            JSON transaction request
	 * 
	 * @return loan transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction executeJSONTransactionRequest(String accountId, LoanTransactionType transactionType,
			JSONTransactionRequest transactionRequest) throws MambuApiException {

		if (transactionRequest == null || transactionType == null) {
			throw new IllegalArgumentException("Transaction request and transactionType must not be null");
		}

		String methodName = transactionType.name();
		switch (transactionType) {
		case DISBURSMENT:
		case FEE:
		case REPAYMENT:
		case INTEREST_RATE_CHANGED: // Available since Mambu 4.3. See MBU-13714
		case PAYMENT_MADE: // Available since Mambu 4.6. See MBU-16269	
			break;
		default:
			throw new IllegalArgumentException("Transaction  type " + transactionType + " is not supported");
		}
		// Post Transaction
		return serviceExecutor.executeJSONTransactionRequest(accountId, transactionRequest, Type.LOAN, methodName);

	}

	/**
	 * Convenience method for updating/changing the interest rate on a loan account.
	 * 
	 * @param accountId
	 *            the id of the account.
	 * @param transactionRequest
	 *            JSON transaction request
	 * 
	 * @return loan transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction postInterestRateChange(String accountId, JSONTransactionRequest transactionRequest)
			throws MambuApiException {

		if (accountId == null || transactionRequest == null) {
			throw new IllegalArgumentException("Transaction request and account id must not be null");
		}

		return executeJSONTransactionRequest(accountId, LoanTransactionType.INTEREST_RATE_CHANGED, transactionRequest);

	}
	
	/**
	 * Convenience method for posting a payment made transaction on a loan account.
	 * 
	 * @param accountId
	 *            the id of the account.
	 * @param transactionRequest
	 *            JSON transaction request
	 * 
	 * @return newly posted transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction postPaymentMade(String accountId, JSONTransactionRequest transactionRequest)
			throws MambuApiException {

		if (accountId == null || transactionRequest == null) {
			throw new IllegalArgumentException("Transaction request and account id must not be null");
		}

		return executeJSONTransactionRequest(accountId, LoanTransactionType.PAYMENT_MADE, transactionRequest);

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
			throw new IllegalArgumentException(
					"Loan Amount must be not null and not zero. It is " + account.getLoanAmount());
		}

		// Add applicable params to the map
		ParamsMap params = ServiceHelper.makeParamsForLoanSchedule(account, getProductSchedule);
		// The API returns a JSONLoanRepayments object containing a list of repayments
		JSONLoanRepayments jsonRepayments = serviceExecutor.execute(getProductSchedule, productId, params);
		// Return list of repayments
		return jsonRepayments != null ? jsonRepayments.getRepayments() : null;
	}

	/****
	 * Reverse loans transactions for a loan account
	 * 
	 * @param accountId
	 *            the id or encoded key of the loan account. Mandatory
	 * @param originalTransactionType
	 *            Original transaction type to be reversed. The following transaction types can be currently reversed:
	 *            PENALTY_APPLIED (in 3.13), REPAYMENT, INTEREST_APPLIED, FEE (since 4.2). Must not be null.
	 * @param originalTransactionId
	 *            the id or the encodedKey of the transaction to be reversed. Must not be null.
	 * @param notes
	 *            transaction notes
	 * @return Loan Transaction
	 * 
	 * @throws MambuApiException
	 */
	// TODO: this method is using the www=form method. Can update it to use JSON API version, would need to create an
	// object with input params
	public LoanTransaction reverseLoanTransaction(String accountId, LoanTransactionType originalTransactionType,
			String originalTransactionId, String notes) throws MambuApiException {

		// PENALTY_APPLIED reversal is available since 3.13. See MBU-9998 for more details
		// POST "type=PENALTY_ADJUSTMENT&notes=reason&originalTransactionId=123" /api/loans/{id}/transactions/

		// Since 4.2 reversing REPAYMENT, FEE and INTEREST_APPLIED are available
		// See MBU-13187, MBU-13188, MBU-13189

		// Example: POST {"type": "REPAYMENT_ADJUSTMENT","originalTransactionId": "2", "notes": "cancel repayment" }
		// api/loans/{ID}/transactions

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
			// 3.13 See MBU-9998
			transactionTypeParam = LoanTransactionType.PENALTY_ADJUSTMENT.name();
			break;
		case REPAYMENT:
			// 4.2 See MBU-13187
			transactionTypeParam = LoanTransactionType.REPAYMENT_ADJUSTMENT.name();
			break;
		case FEE:
			// 4.2 See MBU-13188
			transactionTypeParam = LoanTransactionType.FEE_ADJUSTMENT.name();
			break;
		case INTEREST_APPLIED:
			// 4.2 See MBU-13189
			transactionTypeParam = LoanTransactionType.INTEREST_APPLIED_ADJUSTMENT.name();
			break;
		case WRITE_OFF:
			// 4.6 See MBU-13191
			 transactionTypeParam = LoanTransactionType.WRITE_OFF_ADJUSTMENT.name();
			 break;
		case PAYMENT_MADE:
			 transactionTypeParam = LoanTransactionType.PAYMENT_MADE_ADJUSTMENT.name();
			 break;
		default:
			throw new IllegalArgumentException(
					"Reversal for Loan Transaction Type " + originalTransactionType.name() + " is not supported");
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
	 *            The following loan transactions types currently can be reversed: PENALTY_APPLIED, REPAYMENT, FEE,
	 *            INTEREST_APPLIED. Mandatory.
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

		// Since 4.2 reversing REPAYMENT, FEE, INTEREST_APPLIED are available
		// See MBU-13187, MBU-13188, MBU-13189

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

	/**
	 * Updates or links a settlement account with a loan account.
	 * 
	 * Call: POST : /api/loans/{LOAN_KEY}/settlementAccounts/{SAVINGS_KEY}
	 * 
	 * Sample call: POST /api/loans/8a8086a756dfa8f30156e0bdbf060259/settlementAccounts/8a80866357976c62015798e0b60e00d0
	 * 
	 * Sample response: {"returnCode":0,"returnStatus":"SUCCESS"}
	 * 
	 * Available since Mambu 4.4
	 * 
	 * @param loanAccountKey
	 *            A string key representing the ID or the encoding key of the loan account. Must NOT be NULL.
	 * 
	 * @param savingsAccountKey
	 *            A string key representing the ID or the encoding key of the saving account Must NOT be NULL.
	 * 
	 * @return true if it succeeds linking the two accounts of the keys passed as parameters in a call to this method.
	 * @throws MambuApiException
	 */
	public Boolean addSettlementAccount(String loanAccountKey, String savingsAccountKey)
			throws MambuApiException {

		if (savingsAccountKey == null || loanAccountKey == null) {
			throw new IllegalArgumentException("loanAcountKey or savingsAccountKey must NOT be NULL");
		}
		
		postSettlementForLoanAccount.setApiReturnFormat(ApiReturnFormat.BOOLEAN);
		
		return serviceExecutor.execute(postSettlementForLoanAccount, loanAccountKey, savingsAccountKey, null);
	}

	/**
	 * Deletes the link between the loan account and saving account (known as settlement account)
	 * 
	 * Call: DELETE /api/loans/{LOAN_KEY}/settlementAccounts/{SAVINGS_KEY}
	 * 
	 * Sample call: DELETE /api/loans/STLACC_7832648/settlementAccounts/8a80866357976c62015798e0b60e00d0
	 * 
	 * Sample response: {"returnCode":0,"returnStatus":"SUCCESS"}
	 * 
	 * Available since Mambu 4.4
	 * 
	 * @param loanAccountKey
	 *            A string key representing the ID or the encoding key of the loan account. Must NOT be NULL.
	 * @param savingAccountKey
	 *            A string key representing the ID or the encoding key of the saving account. Must NOT be NULL.
	 * @return true if the linkage succeeded.
	 * @throws MambuApiException
	 */
	public Boolean deleteSettlementAccount(String loanAccountKey, String savingAccountKey)
			throws MambuApiException {

		if (savingAccountKey == null || loanAccountKey == null) {
			throw new IllegalArgumentException("loanAcountKey or savingAccountKey must NOT be NULL");
		}

		return serviceExecutor.deleteOwnedEntity(MambuEntityType.LOAN_ACCOUNT, loanAccountKey,
				MambuEntityType.SETTLEMENT_ACCOUNT, savingAccountKey);
	}

}
