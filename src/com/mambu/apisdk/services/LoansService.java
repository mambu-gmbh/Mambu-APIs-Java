/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.api.server.handler.loan.model.JSONLoanRepayments;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.LoanAccountExpanded;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.apisdk.util.ServiceHelper;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.docs.shared.model.Document;
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanProduct;
import com.mambu.loans.shared.model.LoanTransaction;
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
	private static final String BANK_NUMBER = APIData.BANK_NUMBER;
	private static final String RECEIPT_NUMBER = APIData.RECEIPT_NUMBER;
	private static final String CHECK_NUMBER = APIData.CHECK_NUMBER;
	private static final String BANK_ACCOUNT_NUMBER = APIData.BANK_ACCOUNT_NUMBER;
	private static final String BANK_ROUTING_NUMBER = APIData.BANK_ROUTING_NUMBER;
	private static final String NOTES = APIData.NOTES;
	//
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

	// Loan Products API requests
	// Get Loan Product Details
	private final static ApiDefinition getProduct = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, LoanProduct.class);
	// Get Lists of Loan Products
	private final static ApiDefinition getProductsList = new ApiDefinition(ApiType.GET_LIST, LoanProduct.class);
	// Get schedule for Loan Products. GET /api/loanproducts/<ID>/schedule?loanAmount=50. Returns JSONLoanRepayments
	private final static ApiDefinition getProductSchedule = new ApiDefinition(ApiType.GET_OWNED_ENTITY,
			LoanProduct.class, JSONLoanRepayments.class);

	// Update Custom Field value for a Loan Account. PATCH /api/loans/accointId/custominformation/customFieldId
	private final static ApiDefinition updateAccountCustomField = new ApiDefinition(ApiType.PATCH_OWNED_ENTITY,
			LoanAccount.class, CustomFieldValue.class);
	// Delete Custom Field for a Loan Account. DELETE /api/loans/accointId/custominformation/customFieldId
	private final static ApiDefinition deleteAccountCustomField = new ApiDefinition(ApiType.DELETE_OWNED_ENTITY,
			LoanAccount.class, CustomFieldValue.class);

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

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_APPROVAL);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountChange, accountId, paramsMap);
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
	 * 
	 * @return loan transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction lockLoanAccount(String accountId, String notes) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_LOCK);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);
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

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_UNLOCK);
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

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_REJECT);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountChange, accountId, paramsMap);
	}

	/***
	 * 
	 * @deprecated As of release 3.8, replaced by
	 *             {@link #disburseLoanAccount(String, String, String, String, String, TransactionDetails)}
	 * 
	 *             Disburse a loan account with a given disbursal date and some extra details.
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
	@Deprecated
	public LoanTransaction disburseLoanAccount(String accountId, String amount, String disbursalDate,
			String firstRepaymentDate, String paymentMethod, String bankNumber, String receiptNumber,
			String checkNumber, String bankAccountNumber, String bankRoutingNumber, String notes)
			throws MambuApiException {
		// Example: POST
		// "type=DISBURSMENT&date=2012-10-04&firstRepaymentDate=2012-10-08&notes=using transactions"
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

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);

	}

	/***
	 * 
	 * Disburse a loan account with a given disbursal date and some extra transaction details
	 * 
	 * @param accountId
	 *            account ID
	 * @param amount
	 *            disbursement amount
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
	// TODO: Disbursements for Loans with tranches is NOT supported. See MBU-7214 (issue is closed as Incomplete)
	public LoanTransaction disburseLoanAccount(String accountId, String amount, String disbursalDate,
			String firstRepaymentDate, String notes, TransactionDetails transactionDetails) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_DISBURSMENT);

		// Add transactionDetails to the paramsMap
		ServiceHelper.addAccountTransactionParams(paramsMap, amount, disbursalDate, notes, transactionDetails);

		// Add also firstRepaymentDate
		paramsMap.addParam(FIRST_REPAYMENT_DATE, firstRepaymentDate);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);

	}

	/***
	 * Undo Disburse for a loan account. If the account has multiple tranches, reverses the last tranche
	 * 
	 * @param accountId
	 * 
	 * @return Loan Transaction
	 * 
	 * @throws MambuApiException
	 */
	public LoanTransaction undoDisburseLoanAccount(String accountId) throws MambuApiException {
		// Example POST "type=DISBURSMENT_ADJUSTMENT" /api/loans/{id}/transactions/
		// Available since Mambu 3.9. See MBU-7189

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, APIData.DISBURSMENT_ADJUSTMENT);

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

		return serviceExecutor.executeJson(updateAccount, loan, encodedKey);
	}

	/***
	 * Update loan terms for an existent LoanAccount This API allows updating LoanAccount terms only. Use
	 * updateLoanAccount() to update custom fields for a loan account
	 * 
	 * @param loan
	 *            LoanAccountExtended object containing LoanAccount. Either LoanAccount encoded key or its ID must be
	 *            NOT null for updating account
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
		ParamsMap params = ServiceHelper.makeParamsForGetByCustomView(customViewKey, offset, limit);
		return serviceExecutor.execute(getAllLoanTransactions, params);

	}

	/****
	 * @deprecated As of release 3.8, replaced by
	 *             {@link #makeLoanRepayment(String, String, String, String, TransactionDetails)}
	 * 
	 *             Repayments on a loan account if the user has permission to repay loans, the maximum exposure is not
	 *             exceeded for the client, the account was in Approved state.
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
	@Deprecated
	public LoanTransaction makeLoanRepayment(String accountId, String amount, String date, String notes,
			String paymentMethod, String receiptNumber, String bankNumber, String checkNumber,
			String bankAccountNumber, String bankRoutingNumber) throws MambuApiException {

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

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);
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
	 * @param limit
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
	 * @param limit
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
		ParamsMap params = ServiceHelper.makeParamsForGetByCustomView(customViewKey, offset, limit);
		return serviceExecutor.execute(getAccountsList, params);

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
	 *            the id of the loan product
	 * @param account
	 *            loan account containing parameters for determining loan schedule
	 * 
	 *            Only the following loan account parameters are currently supported: loanAmount (mandatory),
	 *            anticipatedDisbursement, firstRepaymentDate, interestRate, repaymentInstallments, gracePeriod,
	 *            repaymentPeriodUnit, repaymentPeriodCount, principalRepaymentInterval
	 * 
	 *            See MBU-6789 and MBU-7676 for more details
	 * 
	 * @return the List of Repayments
	 * 
	 * @throws MambuApiException
	 */
	public List<Repayment> getLoanProductSchedule(String productId, LoanAccount account) throws MambuApiException {
		// E.g. GET /api/loanproducts/{ID}/schedule?loanAmount=1250&anticipatedDisbursement=2015-02-10&interestRate=4

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

	/***
	 * Update custom field value for a Loan Account. This method allows to set new value for a specific custom field
	 * 
	 * @param accountId
	 *            the encoded key or id of the Mambu Loan Account for which the custom field is updated
	 * @param customFieldId
	 *            the encoded key or id of the custom field to be updated
	 * @param fieldValue
	 *            the new value of the custom field
	 * 
	 * @throws MambuApiException
	 */
	public boolean updateLoanAccountCustomField(String accountId, String customFieldId, String fieldValue)
			throws MambuApiException {
		// Execute request for PATCH API to update custom field value for a Loan Account. See MBU-6661
		// e.g. PATCH "{ "value": "10" }" /host/api/loans/accointId/custominformation/customFieldId

		// Make ParamsMap with JSON request for Update API
		ParamsMap params = ServiceHelper.makeParamsForUpdateCustomField(customFieldId, fieldValue);
		return serviceExecutor.execute(updateAccountCustomField, accountId, customFieldId, params);

	}

	/***
	 * Delete custom field for a Loan Account
	 * 
	 * @param accountId
	 *            the encoded key or id of the Mambu Loan Account
	 * @param customFieldId
	 *            the encoded key or id of the custom field to be deleted
	 * 
	 * @throws MambuApiException
	 */
	public boolean deleteLoanAccountCustomField(String accountId, String customFieldId) throws MambuApiException {
		// Execute request for DELETE API to delete custom field for a Loan Account. See MBU-6661
		// e.g. DELETE /host/api/loans/accointId/custominformation/customFieldId

		return serviceExecutor.execute(deleteAccountCustomField, accountId, customFieldId, null);

	}

}
