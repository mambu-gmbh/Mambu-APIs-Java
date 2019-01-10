/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.Date;
import java.util.List;

import com.google.inject.Inject;
import com.mambu.accounts.shared.model.Account.Type;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraints;
import com.mambu.api.server.handler.loan.model.JSONApplyManualFee;
import com.mambu.api.server.handler.loan.model.JSONTransactionRequest;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.json.SavingsAccountPatchJsonSerializer;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
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
import com.mambu.loans.shared.model.LoanAccount;
import com.mambu.loans.shared.model.LoanTransactionType;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;
import com.mambu.savings.shared.model.SavingsTransaction;
import com.mambu.savings.shared.model.SavingsTransactionType;

/**
 * Service class which handles API operations like retrieval, creation or changing state of savings accounts. See full
 * Mambu Savings API documentation at http://api.mambu.com/customer/portal/articles/1162285-savings-api?b_id=874
 * 
 * @author ipenciuc
 * 
 */
public class SavingsService {

	private static final String TYPE = APIData.TYPE;
	private static final String TYPE_TRANSFER = APIData.TYPE_TRANSFER;
	private static final String TYPE_FEE = APIData.TYPE_FEE;
	private static final String TYPE_DEPOSIT_ADJUSTMENT = APIData.TYPE_DEPOSIT_ADJUSTMENT;

	private static final String ORIGINAL_TRANSACTION_ID = APIData.ORIGINAL_TRANSACTION_ID;

	private static final String TYPE_APPROVAL = APIData.TYPE_APPROVAL;
	private static final String TYPE_UNDO_APPROVAL = APIData.TYPE_UNDO_APPROVAL;

	private static final String AMOUNT = APIData.AMOUNT;
	private static final String NOTES = APIData.NOTES;
	// Savings filters
	private static final String BRANCH_ID = APIData.BRANCH_ID;
	public static final String CENTRE_ID = APIData.CENTRE_ID;
	private static final String CREDIT_OFFICER_USER_NAME = APIData.CREDIT_OFFICER_USER_NAME;
	private static final String ACCOUNT_STATE = APIData.ACCOUNT_STATE;

	private static final String OFFSET = APIData.OFFSET;
	private static final String LIMIT = APIData.LIMIT;

	private static final String TO_SAVINGS = APIData.TO_SAVINGS;
	private static final String TO_LOAN = APIData.TO_LOAN;

	// Service helper
	private ServiceExecutor serviceExecutor;

	// Create API definitions for services provided by LoanService
	private final static ApiDefinition getAccount = new ApiDefinition(ApiType.GET_ENTITY, SavingsAccount.class);
	// Get Account Details
	private final static ApiDefinition getAccountDetails = new ApiDefinition(ApiType.GET_ENTITY_DETAILS,
			SavingsAccount.class);
	// Get Lists of Accounts
	private final static ApiDefinition getAccountsList = new ApiDefinition(ApiType.GET_LIST, SavingsAccount.class);
	// Get Accounts for a Client
	private final static ApiDefinition getAccountsForClient = new ApiDefinition(ApiType.GET_OWNED_ENTITIES,
			Client.class, SavingsAccount.class);
	// Get Accounts for a Group
	private final static ApiDefinition getAccountsForGroup = new ApiDefinition(ApiType.GET_OWNED_ENTITIES, Group.class,
			SavingsAccount.class);
	// Post Account Transactions. Params map defines the transaction type
	private final static ApiDefinition postAccountTransaction = new ApiDefinition(ApiType.POST_OWNED_ENTITY,
			SavingsAccount.class, SavingsTransaction.class);
	// Post Account state change. Params map defines the account change transaction
	private final static ApiDefinition postAccountChange = new ApiDefinition(ApiType.POST_ENTITY_ACTION,
			SavingsAccount.class, SavingsTransaction.class);
	// Post a transaction in a saving account in order to start its maturity
	private final static ApiDefinition postStartMaturityTransaction = new ApiDefinition(ApiType.POST_OWNED_ENTITY, 
			SavingsAccount.class, SavingsTransaction.class, SavingsAccount.class);

	// Get Accounts Transactions (transactions for a specific savings account)
	private final static ApiDefinition getAccountTransactions = new ApiDefinition(ApiType.GET_OWNED_ENTITIES,
			SavingsAccount.class, SavingsTransaction.class);
	// Delete Account
	private final static ApiDefinition deleteAccount = new ApiDefinition(ApiType.DELETE_ENTITY, SavingsAccount.class);
	// Create Account
	private final static ApiDefinition createAccount = new ApiDefinition(ApiType.CREATE_JSON_ENTITY,
			JSONSavingsAccount.class);
	// Update Account
	private final static ApiDefinition updateAccount = new ApiDefinition(ApiType.POST_ENTITY, JSONSavingsAccount.class);
	// Patch Account. Used to update savings terms only. PATCH JSON /api/savings/savingsId
	private final static ApiDefinition patchAccount;
	static {
		patchAccount = new ApiDefinition(ApiType.PATCH_ENTITY, SavingsAccount.class);
		// Use SavingsAccountPatchJsonSerializer
		patchAccount.addJsonSerializer(SavingsAccount.class, new SavingsAccountPatchJsonSerializer());
	}
	// Products API requests
	// Get Savings Product Details
	private final static ApiDefinition getProduct = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, SavingsProduct.class);
	// Get Lists of Savings Products
	private final static ApiDefinition getProducts = new ApiDefinition(ApiType.GET_LIST, SavingsProduct.class);

	/***
	 * Create a new savings service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public SavingsService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
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
		return serviceExecutor.execute(getAccount, accountId);
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
		return serviceExecutor.execute(getAccountDetails, accountId);
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
	public List<SavingsAccount> getSavingsAccountsForClient(String clientId) throws MambuApiException {
		return serviceExecutor.execute(getAccountsForClient, clientId);
	}

	/****
	 * Approve Savings account
	 * 
	 * 
	 * @param accountId
	 *            the id of the account
	 * 
	 * @return the approved SavingsAccount from Mambu
	 * 
	 *         Note: the returned account object doesn't contain custom fields.
	 * 
	 * @throws MambuApiException
	 */
	public SavingsAccount approveSavingsAccount(String accountId, String notes) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_APPROVAL);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountChange, accountId, paramsMap);
	}

	/****
	 * Undo Approve for a savings account
	 * 
	 * @param accountId
	 *            the id of the savings account
	 * 
	 * @return savingsAccount
	 * 
	 *         Note: The account object in the response doesn't contain custom fields
	 * 
	 * @throws MambuApiException
	 */
	public SavingsAccount undoApproveSavingsAccount(String accountId, String notes) throws MambuApiException {
		// E.g. format: POST "type=UNDO_APPROVAL" /api/savings/{id}/transactions

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_UNDO_APPROVAL);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountChange, accountId, paramsMap);
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
	public List<SavingsTransaction> getSavingsAccountTransactions(String accountId, String offset, String limit)
			throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(OFFSET, offset);
		paramsMap.put(LIMIT, limit);

		return serviceExecutor.execute(getAccountTransactions, accountId, paramsMap);
	}

	/**
	 * Get savings transactions by specifying filter constraints
	 * 
	 * Note: This method is deprecated, you may use the getSavingsTransactionsWithFullDetails in order to obtain savings
	 * transactions with full details (custom fields included) or getSavingsAccountsWithBasicDetails to obtain the savings
	 * transactions in basic details level.
	 * 
	 * @param filterConstraints
	 *            filter constraints. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return list of savings transactions matching filter constraints
	 * @throws MambuApiException
	 */
	@Deprecated
	public List<SavingsTransaction> getSavingsTransactions(JSONFilterConstraints filterConstraints, String offset,
			String limit) throws MambuApiException {
		
		return getSavingsTransactionsWithFullDetails(filterConstraints, offset, limit);
	}
	
	/**
	 * Get savings transactions by specifying filter constraints with all the details (custom fields included)
	 * 
	 * @param filterConstraints
	 *            filter constraints. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return list of savings transactions matching filter constraints
	 * @throws MambuApiException
	 */
	public List<SavingsTransaction> getSavingsTransactionsWithFullDetails(JSONFilterConstraints filterConstraints, String offset,
			String limit) throws MambuApiException {

		// Available since Mambu 3.12. See MBU-8988 for more details
		// POST {JSONFilterConstraints} /api/savings/transactions/search?offset=0&limit=5
		ApiDefinition apiDefintition = makeSearchTransactionsWithFullApiDefinition();
		
		// POST Filter JSON with pagination params map
		return serviceExecutor.executeJson(apiDefintition, filterConstraints, null, null,
				ServiceHelper.makePaginationParams(offset, limit));
	}
	
	/**
	 * Get savings transactions by specifying filter constraints with all the details (no custom fields)
	 * 
	 * @param filterConstraints
	 *            filter constraints. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return list of savings transactions matching filter constraints
	 * @throws MambuApiException
	 */
	public List<SavingsTransaction> getSavingsTransactionsWithBasicDetails(JSONFilterConstraints filterConstraints, String offset,
			String limit) throws MambuApiException {

		// Available since Mambu 3.12. See MBU-8988 for more details
		// POST {JSONFilterConstraints} /api/savings/transactions/search?offset=0&limit=5
		ApiDefinition apiDefintition = SearchService
				.makeApiDefinitionForSearchByFilter(MambuEntityType.SAVINGS_TRANSACTION);
		
		// POST Filter JSON with pagination params map
		return serviceExecutor.executeJson(apiDefintition, filterConstraints, null, null,
				ServiceHelper.makePaginationParams(offset, limit));
	}

	/****
	 * Make a withdrawal from an account.
	 * 
	 * @param accountId
	 *            account ID or encoded key. Must not be null
	 * @param amount
	 *            transaction amount
	 * @param date
	 *            transaction date
	 * @param transactionDetails
	 *            transaction details
	 * @param customInformation
	 *            transaction custom fields
	 * @param notes
	 *            transaction notes
	 * 
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */
	public SavingsTransaction makeWithdrawal(String accountId, Money amount, Date date,
			TransactionDetails transactionDetails, List<CustomFieldValue> customInformation, String notes)
			throws MambuApiException {

		JSONTransactionRequest transactionRequest = ServiceHelper.makeJSONTransactionRequest(amount, date, null,
				transactionDetails, null, customInformation, notes);

		return serviceExecutor.executeJSONTransactionRequest(accountId, transactionRequest, Type.SAVINGS,
				SavingsTransactionType.WITHDRAWAL.name());
	}

	/****
	 * Make a deposit to an account.
	 * 
	 * @param accountId
	 *            account ID or encoded key. Must not be null
	 * @param amount
	 *            transaction amount
	 * @param date
	 *            transaction date
	 * @param transactionDetails
	 *            transaction details
	 * @param customInformation
	 *            transaction custom fields
	 * @param notes
	 *            transaction notes
	 * 
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */
	public SavingsTransaction makeDeposit(String accountId, Money amount, Date date,
			TransactionDetails transactionDetails, List<CustomFieldValue> customInformation, String notes)
			throws MambuApiException {

		JSONTransactionRequest transactionRequest = ServiceHelper.makeJSONTransactionRequest(amount, date, null,
				transactionDetails, null, customInformation, notes);

		return serviceExecutor.executeJSONTransactionRequest(accountId, transactionRequest, Type.SAVINGS,
				SavingsTransactionType.DEPOSIT.name());
	}

	/**
	 * Make transfer from an account
	 * 
	 * @param fromAccountId
	 *            the id of the account the amount to transfer from
	 * @param destinationAccountKey
	 *            the id of the account to transfer to
	 * @param destinationAccountType
	 *            type of the account (Type.Loan or Type.Savings)
	 * @param amount
	 *            amount to transfer
	 * @param notes
	 * 
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */
	public SavingsTransaction makeTransfer(String fromAccountId, String destinationAccountKey,
			Type destinationAccountType, String amount, String notes) throws MambuApiException {

		// E.g .format: POST "type=TYPE_TRANSFER"
		// /api/savings/KHGJ593/transactions
		if (fromAccountId == null || fromAccountId.trim().isEmpty()) {
			throw new IllegalArgumentException("From Account ID  must not  be null or empty");
		}

		if (destinationAccountKey == null || destinationAccountKey.trim().isEmpty()) {
			throw new IllegalArgumentException("Destination Account ID  must not  be null or empty");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_TRANSFER);

		if (destinationAccountType == Type.LOAN) {
			paramsMap.addParam(TO_LOAN, destinationAccountKey);
		} else {
			paramsMap.addParam(TO_SAVINGS, destinationAccountKey);
		}
		paramsMap.addParam(AMOUNT, amount);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountTransaction, fromAccountId, paramsMap);
	}

	/****
	 * Apply Arbitrary FEE to a savings account
	 * 
	 * @param accountId
	 *            the id of the account
	 * @param amount
	 *            fee amount
	 * @param notes
	 *            transaction notes
	 * 
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */
	public SavingsTransaction applyFeeToSavingsAccount(String accountId, String amount, String notes)
			throws MambuApiException {

		if (amount == null || amount.trim().isEmpty()) {
			throw new IllegalArgumentException("Amount must not  be null or empty");
		}

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_FEE);
		paramsMap.addParam(AMOUNT, amount);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);
	}

	/****
	 * Apply Predefined Fee to to a savings account
	 * 
	 * @param accountId
	 *            the id or the encoded key of the account. Must not be null
	 * @param fees
	 *            fees. Only Manual Predefined Fees are currently supported. Must not be null. Must contain exactly one
	 *            fee.
	 * 
	 *            Note: Once MBU-12865 is implemented this method will support both predefined fees and arbitrary fees
	 *            and the (@link #applyFeeToLoanAccount(String, String, String, String)} method used for
	 *            arbitrary fees can be deprecated
	 * @param notes
	 *            transaction notes
	 * 
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */
	public SavingsTransaction applyFeeToSavingsAccount(String accountId, List<CustomPredefinedFee> fees, String notes)
			throws MambuApiException {
		//
		if (fees == null || fees.size() != 1) {
			throw new IllegalArgumentException("There must be exactly one fee present");
		}
		// Support for manual predefined fees available since Mambu 4.1. See MBU-12273
		// Example: POST /api/savings/SAVINGS_ID/transactions
		// {"type":"FEE", "fees":[{"encodedKey":"8a80816752715c34015278bd4792084b","amount":"20" }],
		// "notes":"test" ]

		// Create JSONTransactionRequest for Apply FEE API - need to specify only fees and notes
		Integer repaymentNumber = null; // not applicable for savings account
		JSONApplyManualFee transactionRequest = ServiceHelper.makeJSONApplyManualFeeRequest(fees, repaymentNumber,
				notes);
		return serviceExecutor.executeJSONTransactionRequest(accountId, transactionRequest, Type.SAVINGS,
				LoanTransactionType.FEE.name());
	}

	/**
	 * Convenience method to execute Savings Account transaction by providing JSONTransactionRequest
	 * 
	 * @param accountId
	 *            account id or encoded key. Must not be null
	 * @param transactionType
	 *            savings transaction type. Must not be null. Supported types are: FEE_APPLIED, DEPOSIT and WITHDRAWAL
	 * @param transactionRequest
	 *            JSON transaction request
	 * @return savings transaction
	 * @throws MambuApiException
	 */
	public SavingsTransaction executeJSONTransactionRequest(String accountId, SavingsTransactionType transactionType,
			JSONTransactionRequest transactionRequest) throws MambuApiException {
		//
		if (transactionRequest == null || transactionType == null) {
			throw new IllegalArgumentException("Transaction request and transactionType must not be null");
		}

		String methodName = transactionType.name();
		switch (transactionType) {
		case FEE_APPLIED:
			// Mambu expects the transaction name to be "FEE", the same as for Loans
			methodName = LoanTransactionType.FEE.name();
			break;
		case DEPOSIT:
		case WITHDRAWAL:
			break;
		default:
			throw new IllegalArgumentException("Transaction  type " + transactionType + " is not supported");
		}
		// Post Transaction of type FEE. Note, using LoanTransactionType.FEE enum to match the expected "FEE"
		// transaction type string (method)
		return serviceExecutor.executeJSONTransactionRequest(accountId, transactionRequest, Type.SAVINGS, methodName);

	}

	/****
	 * Reverse savings transactions for a savings account
	 * 
	 * @param accountId
	 *            the id of the savings account. Mandatory
	 * @param originalTransactionType
	 *            Original transaction type to be reversed. The following transaction types can be reversed: DEPOSIT,
	 *            WITHDRAWAL, TRANSFER, FEE. Mandatory.
	 * @param originalTransactionId
	 *            the id or the encodedKey of the transaction to be reversed. Mandatory
	 * @param notes
	 *            transaction notes
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */
	public SavingsTransaction reverseSavingsTransaction(String accountId,
			SavingsTransactionType originalTransactionType, String originalTransactionId, String notes)
			throws MambuApiException {

		// Available since 3.10. See MBU-7933, MBU-7935, MBU-7936 for more details
		// Example POST "type=DEPOSIT_ADJUSTMENT&notes=reason&originalTransactionId=123" /api/savings/67/transactions/

		// Available since 4.2 for FEE reversal. See MBU-13192. type":"FEE_ADJUSTED"

		// Note: When posting reversal transaction to Mambu the required reversal transaction type is supplied by the
		// wrapper : DEPOSIT_ADJUSTMENT, WITHDRAWAL_ADJUSTMENT or TRANSFER_ADJUSTMENT

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
		case DEPOSIT:
			// TODO: we cannot use SavingsTransactionType for DEPOSIT reversal: the API expects this string:
			// DEPOSIT_ADJUSTMENT but the SavingsTransactionType defines it as ADJUSTMENT
			// (i.e.SavingsTransactionType.ADJUSTMENT). So it cannot be used. Define "DEPOSIT_ADJUSTMENT" in ApiData.
			transactionTypeParam = TYPE_DEPOSIT_ADJUSTMENT;
			break;
		case WITHDRAWAL:
			transactionTypeParam = SavingsTransactionType.WITHDRAWAL_ADJUSTMENT.name();
			break;
		case TRANSFER:
			transactionTypeParam = SavingsTransactionType.TRANSFER_ADJUSTMENT.name();
			break;
		case FEE_APPLIED:
			// See MBU-13192 in 4.2
			transactionTypeParam = SavingsTransactionType.FEE_ADJUSTED.name();
			break;
		default:
			throw new IllegalArgumentException("Reversal for Savings Transaction Type "
					+ originalTransactionType.name() + " is not supported");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, transactionTypeParam);
		paramsMap.addParam(ORIGINAL_TRANSACTION_ID, originalTransactionId);
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);
	}

	/****
	 * Reverse transaction for a savings account by providing the original savings transaction
	 * 
	 * @param originalTransaction
	 *            The following transactions can be reversed: DEPOSIT, WITHDRAWAL and TRANSFER. Mandatory.
	 * @param notes
	 *            transaction notes
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */
	public SavingsTransaction reverseSavingsTransaction(SavingsTransaction originalTransaction, String notes)
			throws MambuApiException {

		// Available since 3.10. See MBU-7933, MBU-7935, MBU-7936 for more details
		// Available since 4.2 for FEE reversal. See MBU-13192. type":"FEE_ADJUSTED"
		// Example. POST "type=TYPE_WITHDRAWAL_ADJUSTMENT&notes=reason&originalTransactionId=123"
		// /api/savings/67/transactions/

		if (originalTransaction == null) {
			throw new IllegalArgumentException("Original Transaction cannot be null");
		}
		// Get original transaction Key from the original transaction. Either encoded key or transaction id can be used
		String transactionKey = originalTransaction.getEncodedKey();
		if (transactionKey == null || transactionKey.isEmpty()) {
			// Try getting the id
			if (originalTransaction.getTransactionId() == null || originalTransaction.getTransactionId() == 0) {
				throw new IllegalArgumentException(
						"Original Transaction must have either the encoded key or id not null or empty");
			}
			long transId = originalTransaction.getTransactionId();
			transactionKey = String.valueOf(transId);
		}
		// Get account id and original transaction type from the original transaction
		String accountId = originalTransaction.getParentAccountKey();
		SavingsTransactionType transactionType = originalTransaction.getType();

		return reverseSavingsTransaction(accountId, transactionType, transactionKey, notes);
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
	public boolean deleteSavingsAccount(String accountId) throws MambuApiException {
		return serviceExecutor.execute(deleteAccount, accountId);
	}

	/****
	 * Close Savings account specifying the type of closer (withdraw, reject or close)
	 * 
	 * Note: available since Mambu 3.4 See MBU-4581 and MBU-10976 for details.
	 * 
	 * @param accountId
	 *            the id of the account to withdraw. Must not be null
	 * @param closerType
	 *            type of closer (withdraw, reject or close). Must not be null
	 * @param notes
	 *            optional notes
	 * 
	 * @return savings account
	 * 
	 * @throws MambuApiException
	 */
	public SavingsAccount closeSavingsAccount(String accountId, APIData.CLOSER_TYPE closerType, String notes)
			throws MambuApiException {

		if (closerType == null) {
			throw new IllegalArgumentException("Closer Type must not  be null");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, closerType.name());
		paramsMap.addParam(NOTES, notes);

		return serviceExecutor.execute(postAccountChange, accountId, paramsMap);
	}
	
	/**
	 * Starts maturity for a saving account. (A transaction will be created and posted into Mambu in order to accomplish
	 * this)
	 * 
	 * Example: POST "{"type":"START_MATURITY", "notes":"123", "date":"2017-01-12"}" /api/savings/{ID}/transactions/
	 * 
	 * @param accountId
	 *            The id of the saving account that maturity will be started for. Must not be NULL.
	 * 
	 * @param date
	 *            The date used to indicate when the maturity starts.
	 * 
	 * @param notes
	 *            Some notes that will be posted on the transaction that will be created for starting the maturity for
	 *            the account.
	 * 
	 * @return the SavingAccount the maturity was started for
	 * 
	 * @throws MambuApiException
	 */
	public SavingsAccount startMaturity(String accountId, Date date, String notes) throws MambuApiException {

		if (accountId == null) {
			throw new IllegalArgumentException("The account id must not be null");
		}
		JSONTransactionRequest transactionRequest = ServiceHelper.makeJSONTransactionRequest(null, date, null, null,
				null, null, notes);

		ParamsMap paramsMap = ServiceHelper.makeParamsForTransactionRequest(APIData.START_MATURITY, transactionRequest);

		postStartMaturityTransaction.setContentType(ContentType.JSON);
		return serviceExecutor.execute(postStartMaturityTransaction, accountId, paramsMap);
	}

	/****
	 * Undo Close Savings account. Supports UNDO_REJECT, UNDO_WITHDRAWN, UNDO_CLOSE
	 * 
	 * @param savingsAccount
	 *            closed savings account. Must not be null and must be in one of the supported closed states.
	 * @param notes
	 *            undo closer reason notes
	 * @return savings account
	 * 
	 * @throws MambuApiException
	 */

	public SavingsAccount undoCloseSavingsAccount(SavingsAccount savingsAccount, String notes) throws MambuApiException {
		// Available since Mambu 4.2. See MBU-13193 for details.
		// Supports UNDO_REJECT, UNDO_WITHDRAWN, UNDO_CLOSE

		// E.g. POST "type=UNDO_REJECT&notes=notes" /api/savings/ABCD123/transactions
		// E.g. POST "type=UNDO_WITHDRAWN" /api/savings/ABCD123/transactions
		// E.g. POST "type=UNDO_CLOSE" /api/savings/ABCD123/transactions

		if (savingsAccount == null || savingsAccount.getId() == null || savingsAccount.getAccountState() == null) {
			throw new IllegalArgumentException("Account, its ID and account state must not  be null");
		}

		// Get the transaction type based on how the account was closed
		String undoCloserTransactionType = ServiceHelper.getUndoCloserTransactionType(savingsAccount);
		if (undoCloserTransactionType == null) {
			throw new IllegalArgumentException(
					"Account is not in a state to perform UNDO close via API. Account State="
							+ savingsAccount.getAccountState());
		}

		// Create params map with expected API's params
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, undoCloserTransactionType);
		paramsMap.addParam(NOTES, notes);

		// Execute API
		String accountId = savingsAccount.getId();
		return serviceExecutor.execute(postAccountChange, accountId, paramsMap);
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
	public List<SavingsAccount> getSavingsAccountsForGroup(String groupId) throws MambuApiException {
		return serviceExecutor.execute(getAccountsForGroup, groupId);
	}

	/***
	 * Get the Savings accounts by branch id, centreId, credit officer, accountState
	 * 
	 * @param branchId
	 *            The ID of the branch to which the accounts are assigned to
	 * @param centreId
	 *            The ID of the centre to which the loan accounts are assigned to. If both branchId and centreId are
	 *            provided then this centre must be assigned to the branchId
	 * @param creditOfficerUserName
	 *            the username of the credit officer to whom the accounts are assigned to
	 * @param accountState
	 *            the state of the accounts to filter on (e.g: APPROVED)
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return the list of Savings accounts matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	public List<SavingsAccount> getSavingsAccountsByBranchCentreOfficerState(String branchId, String centreId,
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
	 * Get the Savings accounts by branch id, credit officer, accountState
	 * 
	 * @param branchId
	 *            The ID of the branch to which the accounts are assigned to provided then this centre must be assigned
	 *            to the branchId
	 * @param creditOfficerUserName
	 *            the username of the credit officer to whom the accounts are assigned to
	 * @param accountState
	 *            the state of the accounts to filter on (e.g: APPROVED)
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return the list of Savings accounts matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	public List<SavingsAccount> getSavingsAccountsByBranchOfficerState(String branchId, String creditOfficerUserName,
			String accountState, String offset, String limit) throws MambuApiException {
		String centreId = null;
		return getSavingsAccountsByBranchCentreOfficerState(branchId, centreId, creditOfficerUserName, accountState,
				offset, limit);

	}

	/**
	 * Get savings accounts by specifying filter constraints.
	 * 
	 * 
	 * Also notice that the full details one may come with a performance hit so use it only if needed. 
	 * 
	 * @param filterConstraints
	 *            filter constraints. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return list of savings accounts matching filter constraint
	 * @throws MambuApiException
	 */
	public List<SavingsAccount> getSavingsAccounts(JSONFilterConstraints filterConstraints, String offset, String limit)
			throws MambuApiException {

		// Available since Mambu 3.12. See MBU-8988 for more details
		// POST {JSONFilterConstraints} /api/savings/search?offset=0&limit=5&fullDetails=true
		ApiDefinition apiDefintition = SearchService
				.makeApiDefinitionForSearchByFilter(MambuEntityType.SAVINGS_ACCOUNT);

		// POST Filter JSON with pagination params map
		return serviceExecutor.executeJson(apiDefintition, filterConstraints, null, null,
						ServiceHelper.makePaginationParams(offset, limit));
	}
	
	// Savings Products
	/***
	 * Get a list of Savings Products
	 * 
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return the List of Savings Products
	 * 
	 * @throws MambuApiException
	 */
	public List<SavingsProduct> getSavingsProducts(String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		return serviceExecutor.execute(getProducts, params);
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
		return serviceExecutor.execute(getProduct, productId);
	}

	/***
	 * Create new SavingsAccount using JSONSavingsAccount object and sending it via the JSON API. This API allows
	 * creating SavingsAccount with details, including creating custom field values.
	 * 
	 * 
	 * @param jsonSavingsAccount
	 *            JSONSavingsAccount object containing SavingsAccount. Must be not null. SavingsAccount's encodedKey
	 *            must be null for account create
	 * 
	 * @return created JSONSavingsAccount
	 * 
	 * @throws MambuApiException
	 */
	public JSONSavingsAccount createSavingsAccount(JSONSavingsAccount jsonSavingsAccount) throws MambuApiException {
		// Example: POST // {"savingsAccount":
		// { "accountHolderKey":"123", "accountHolderType":"CLIENT”,… },
		// "customInformation":[{ "customFieldID":"fieldId_1","value":"true" }, ….]
		// }
		if (jsonSavingsAccount == null || jsonSavingsAccount.getSavingsAccount() == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		SavingsAccount inputAccount = jsonSavingsAccount.getSavingsAccount();
		String encodedKey = inputAccount.getEncodedKey();
		if (encodedKey != null) {
			throw new IllegalArgumentException("Cannot create  Account, the encoded key must be null");
		}
		return serviceExecutor.executeJson(createAccount, jsonSavingsAccount);
	}

	/***
	 * Convenience method to create new SavingsAccount using SavingsAccount object.
	 * 
	 * @param savingsAccount
	 *            SavingsAccount object. Must not be null. SavingsAccount's encodedKey must be null for account create
	 * 
	 * @return created savings account
	 * 
	 * @throws MambuApiException
	 */
	public SavingsAccount createSavingsAccount(SavingsAccount savingsAccount) throws MambuApiException {

		if (savingsAccount == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}
		// Create JSONSavingsAccount to use in Mambu API. Mambu expects the following format:
		// {"savingsAccount":{.....}, "customInformation":[{field1},{field2}]}
		JSONSavingsAccount jsonSavingsAccount = new JSONSavingsAccount(savingsAccount);
		// In API request custom fields must be provided in the "customInformation" field
		jsonSavingsAccount.setCustomInformation(savingsAccount.getCustomFieldValues());
		// Clear custom fields at the account level, no need to send them in two places
		savingsAccount.setCustomFieldValues(null);

		// Submit API request to Mambu
		JSONSavingsAccount createdJsonAccount = createSavingsAccount(jsonSavingsAccount);
		// Get Savings account
		SavingsAccount createdAccount = null;
		if (createdJsonAccount != null && createdJsonAccount.getSavingsAccount() != null) {
			createdAccount = createdJsonAccount.getSavingsAccount();
			// Move custom fields from the returned JSONSavingsAccount into the created savings account
			createdAccount.setCustomFieldValues(createdJsonAccount.getCustomInformation());
		}

		return createdAccount;
	}

	/***
	 * Update an existent SavingsAccount using JSONSavingsAccount object and sending it via the JSON API. This API
	 * allows updating JSONSavingsAccount with details. As of Mambu 3.4 only custom fields can be updated.
	 * 
	 * 
	 * @param jsonSavingsAccount
	 *            JSONSavingsAccount object containing SavingsAccount. SavingsAccount encodedKey or id must be NOT null
	 *            for account update
	 * 
	 * @return updated JSONSavingsAccount
	 * 
	 * 
	 * @throws MambuApiException
	 */
	public JSONSavingsAccount updateSavingsAccount(JSONSavingsAccount jsonSavingsAccount) throws MambuApiException {

		if (jsonSavingsAccount == null || jsonSavingsAccount.getSavingsAccount() == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		SavingsAccount inputAccount = jsonSavingsAccount.getSavingsAccount();
		String encodedKey = inputAccount.getEncodedKey() != null ? inputAccount.getEncodedKey() : inputAccount.getId();
		if (encodedKey == null) {
			throw new IllegalArgumentException("Cannot update Account: the encoded key or id must NOT be null");
		}

		return serviceExecutor.executeJson(updateAccount, jsonSavingsAccount, encodedKey);
	}

	/***
	 * Convenience method to Update an existent SavingsAccount. As of Mambu 3.4 only custom fields can be updated.
	 * 
	 * @param savingsAccount
	 *            savings account object to be updated. Must not be null. SavingsAccount encodedKey or id must be NOT
	 *            null for account update
	 * 
	 * @return savingsAccount
	 * @throws MambuApiException
	 */
	public SavingsAccount updateSavingsAccount(SavingsAccount savingsAccount) throws MambuApiException {

		if (savingsAccount == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		String encodedKey = savingsAccount.getEncodedKey() != null ? savingsAccount.getEncodedKey() : savingsAccount
				.getId();
		if (encodedKey == null) {
			throw new IllegalArgumentException("Cannot update Account: the encoded key or id must NOT be null");
		}
		// Create JSONSavingsAccount to use in Mambu API. Mambu expects the following format:
		// {"savingsAccount":{.....}, "customInformation":[{field1},{field2}]}
		JSONSavingsAccount jsonSavingsAccount = new JSONSavingsAccount(savingsAccount);
		// In API request custom fields must be provided in the "customInformation" field
		jsonSavingsAccount.setCustomInformation(savingsAccount.getCustomFieldValues());
		// Clear custom fields at the account level, no need to send them in two places
		savingsAccount.setCustomFieldValues(null);

		// Submit updated account request to Mambu
		JSONSavingsAccount updatedJsonAccount = updateSavingsAccount(jsonSavingsAccount);
		// Get Savings Account
		SavingsAccount updatedSavingsAccount = null;
		if (updatedJsonAccount != null && updatedJsonAccount.getSavingsAccount() != null) {
			updatedSavingsAccount = updatedJsonAccount.getSavingsAccount();
			// Set updated custom fields in the returned savings account
			updatedSavingsAccount.setCustomFieldValues(updatedJsonAccount.getCustomInformation());
		}
		return updatedSavingsAccount;
	}

	/***
	 * Update savings terms for an existent savings account This API allows updating SavingsAccount terms only. Use
	 * updateSavingsAccount() to update custom fields for a savings account
	 * 
	 * @param savings
	 *            SavingsAccount object. Either account's encoded key or its ID must be NOT null for updating account
	 * 
	 *            Note that only some savings terms can be updated. As of Mambu 3.14 the following fields can be
	 *            updated: interestRate, maxWidthdrawlAmount, recommendedDepositAmount, targetAmount,
	 *            overdraftInterestSpread, overdraftLimit, overdraftExpiryDate.
	 * 
	 *            See MBU-10447 for more details
	 * 
	 * @returns success or failure
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public boolean patchSavingsAccount(SavingsAccount savings) throws MambuApiException {
		// Example: PATCH JSON /api/savings/{ID}
		// See MBU-10447 for details
		if (savings == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		// The encodedKey or account Id must be not null
		String encodedKey = savings.getEncodedKey();
		String accountId = savings.getId();
		if (encodedKey == null && accountId == null) {
			throw new IllegalArgumentException("Cannot update Account, the encodedKey or ID must be NOT null");
		}

		String id = accountId != null ? accountId : encodedKey;
		return serviceExecutor.executeJson(patchAccount, savings, id);
	}

	/**
	 * Get all loan accounts funded by a deposit investor account
	 * 
	 * @param savingsId
	 *            encoded key or an id of an investor funding savings account. Must not be null
	 * @return all loan accounts funded by the deposit account
	 * @throws MambuApiException
	 */
	public List<LoanAccount> getFundedLoanAccounts(String savingsId) throws MambuApiException {
		// Example: GET /api/savings/{SAVINGS_ID}/funding
		// Available since Mambu 3.14. See MBU-10905

		if (savingsId == null) {
			throw new IllegalArgumentException("Savings Account ID must not be null");
		}
		String urlPath = APIData.SAVINGS + "/" + savingsId + "/" + APIData.FUNDING;
		ApiDefinition apiDefinition = new ApiDefinition(urlPath, ContentType.WWW_FORM, Method.GET, LoanAccount.class,
				ApiReturnFormat.COLLECTION);
		return serviceExecutor.execute(apiDefinition, savingsId);

	}
	
	private ApiDefinition makeSearchTransactionsWithFullApiDefinition() {

		ApiDefinition apiDefinition = SearchService
				.makeApiDefinitionForSearchByFilter(MambuEntityType.SAVINGS_TRANSACTION);
		apiDefinition.setWithFullDetails(true);
		
		return apiDefinition;
	}

}
