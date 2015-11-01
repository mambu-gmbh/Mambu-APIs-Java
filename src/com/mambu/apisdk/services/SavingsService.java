/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.List;

import com.google.inject.Inject;
import com.mambu.accounts.shared.model.Account.Type;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraints;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.apisdk.util.ServiceHelper;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.docs.shared.model.Document;
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
	private static final String TYPE_DEPOSIT = APIData.TYPE_DEPOSIT;
	private static final String TYPE_WITHDRAWAL = APIData.TYPE_WITHDRAWAL;
	private static final String TYPE_TRANSFER = APIData.TYPE_TRANSFER;
	private static final String TYPE_FEE = APIData.TYPE_FEE;
	private static final String TYPE_DEPOSIT_ADJUSTMENT = APIData.TYPE_DEPOSIT_ADJUSTMENT;
	private static final String TYPE_WITHDRAWAL_ADJUSTMENT = APIData.TYPE_WITHDRAWAL_ADJUSTMENT;
	private static final String TYPE_TRANSFER_ADJUSTMENT = APIData.TYPE_TRANSFER_ADJUSTMENT;

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
	// Get Documents for an Account
	private final static ApiDefinition getAccountDocuments = new ApiDefinition(ApiType.GET_OWNED_ENTITIES,
			SavingsAccount.class, Document.class);
	// Post Account Transactions. Params map defines the transaction type
	private final static ApiDefinition postAccountTransaction = new ApiDefinition(ApiType.POST_OWNED_ENTITY,
			SavingsAccount.class, SavingsTransaction.class);
	// Post Account state change. Params map defines the account change transaction
	private final static ApiDefinition postAccountChange = new ApiDefinition(ApiType.POST_ENTITY_ACTION,
			SavingsAccount.class, SavingsTransaction.class);
	// Get Accounts Transactions (transactions for a specific savings account)
	private final static ApiDefinition getAccountTransactions = new ApiDefinition(ApiType.GET_OWNED_ENTITIES,
			SavingsAccount.class, SavingsTransaction.class);
	// Get All Savings Transactions (transactions for all savings accounts)
	private final static ApiDefinition getAllSavingsTransactions = new ApiDefinition(ApiType.GET_RELATED_ENTITIES,
			SavingsAccount.class, SavingsTransaction.class);
	// Delete Account
	private final static ApiDefinition deleteAccount = new ApiDefinition(ApiType.DELETE_ENTITY, SavingsAccount.class);
	// Create Account
	private final static ApiDefinition createAccount = new ApiDefinition(ApiType.CREATE_JSON_ENTITY,
			JSONSavingsAccount.class);
	// Update Account
	private final static ApiDefinition updateAccount = new ApiDefinition(ApiType.POST_ENTITY, JSONSavingsAccount.class);
	// Patch Account. Used to update savings terms only. PATCH JSON /api/savings/savingsId
	private final static ApiDefinition patchAccount = new ApiDefinition(ApiType.PATCH_ENTITY, SavingsAccount.class);
	// Loan Products API requests
	// Get Loan Product Details
	private final static ApiDefinition getProduct = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, SavingsProduct.class);
	// Get Lists of Loan Products
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
	 * Requests a list of savings transactions for a custom view, limited by offset/limit
	 * 
	 * @param customViewKey
	 *            the key of the Custom View to filter savings transactions
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return the list of Mambu savings transactions
	 * 
	 * @throws MambuApiException
	 */
	public List<SavingsTransaction> getSavingsTransactionsByCustomView(String customViewKey, String offset, String limit)
			throws MambuApiException {
		// Example GET savings/transactions?viewfilter=567&offset=0&limit=100
		ParamsMap params = ServiceHelper.makeParamsForGetByCustomView(customViewKey, offset, limit);
		return serviceExecutor.execute(getAllSavingsTransactions, params);
	}

	/**
	 * Get savings transactions by specifying filter constraints
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
	public List<SavingsTransaction> getSavingsTransactions(JSONFilterConstraints filterConstraints, String offset,
			String limit) throws MambuApiException {
		// Available since Mambu 3.12. See MBU-8988 for more details
		// POST {JSONFilterConstraints} /api/savings/transactions/search?offset=0&limit=5

		ApiDefinition apiDefintition = SearchService
				.makeApiDefinitionforSearchByFilter(MambuEntityType.SAVINGS_TRANSACTION);

		// POST Filter JSON with pagination params map
		return serviceExecutor.executeJson(apiDefintition, filterConstraints, null, null,
				ServiceHelper.makePaginationParams(offset, limit));

	}

	/****
	 * Make a withdrawal from an account.
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
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */
	public SavingsTransaction makeWithdrawal(String accountId, String amount, String date, String notes,
			TransactionDetails transactionDetails) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_WITHDRAWAL);

		// Add transactionDetails to the paramsMap
		ServiceHelper.addAccountTransactionParams(paramsMap, amount, date, notes, transactionDetails);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);

	}

	/****
	 * Make a deposit to an account.
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
	 * @return Savings Transaction
	 * 
	 * @throws MambuApiException
	 */
	public SavingsTransaction makeDeposit(String accountId, String amount, String date, String notes,
			TransactionDetails transactionDetails) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_DEPOSIT);

		// Add transactionDetails to the paramsMap
		ServiceHelper.addAccountTransactionParams(paramsMap, amount, date, notes, transactionDetails);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);
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
	 * Reverse savings transactions for a savings account
	 * 
	 * @param accountId
	 *            the id of the savings account. Mandatory
	 * @param originalTransactionType
	 *            Original transaction type to be reversed. The following transaction types can be reversed: DEPOSIT,
	 *            WITHDRAWAL and TRANSFER. Mandatory.
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
			transactionTypeParam = TYPE_DEPOSIT_ADJUSTMENT;
			break;
		case WITHDRAWAL:
			transactionTypeParam = TYPE_WITHDRAWAL_ADJUSTMENT;
			break;
		case TRANSFER:
			transactionTypeParam = TYPE_TRANSFER_ADJUSTMENT;
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
		// Example. POST "type=TYPE_WITHDRAWAL_ADJUSTMENT&notes=reason&originalTransactionId=123"
		// /api/savings/67/transactions/

		if (originalTransaction == null) {
			throw new IllegalArgumentException("Original Transaction cannot be null");
		}
		// Get original transaction Key from the original transaction. Either encoded key or transaction id can be used
		String transactionKey = originalTransaction.getEncodedKey();
		if (transactionKey == null || transactionKey.isEmpty()) {
			// Try getting the id
			long transId = originalTransaction.getTransactionId();
			if (transId == 0) {
				throw new IllegalArgumentException(
						"Original Transaction must have either the encoded key or id not null or empty");
			}
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
	 * Close Savings account specifying the type of closer (withdraw or reject)
	 * 
	 * Note: available since Mambu 3.4 See MBU-4581 for details.
	 * 
	 * @param accountId
	 *            the id of the account to withdraw
	 * 
	 * @param type
	 *            type of closer (withdraw or reject)
	 * @param notes
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
	 * Requests a list of savings accounts for a custom view, limited by offset/limit only
	 * 
	 * @param customViewKey
	 *            the key of the Custom View to filter savings accounts
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return the list of Mambu savings accounts
	 * 
	 * @throws MambuApiException
	 */
	public List<SavingsAccount> getSavingsAccountsByCustomView(String customViewKey, String offset, String limit)
			throws MambuApiException {
		ParamsMap params = ServiceHelper.makeParamsForGetByCustomView(customViewKey, offset, limit);
		return serviceExecutor.execute(getAccountsList, params);

	}

	/**
	 * Get savings accounts by specifying filter constraints
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
		// POST {JSONFilterConstraints} /api/savings/search?offset=0&limit=5

		ApiDefinition apiDefintition = SearchService
				.makeApiDefinitionforSearchByFilter(MambuEntityType.SAVINGS_ACCOUNT);

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
	 * @param savingsAccount
	 *            JSONSavingsAccount object containing SavingsAccount. SavingsAccount's encodedKey must be null for
	 *            account create
	 * 
	 * @return savingsAccount
	 * 
	 * @throws MambuApiException
	 */
	public JSONSavingsAccount createSavingsAccount(JSONSavingsAccount account) throws MambuApiException {

		if (account == null || account.getSavingsAccount() == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		SavingsAccount inputAccount = account.getSavingsAccount();
		String encodedKey = inputAccount.getEncodedKey();
		if (encodedKey != null) {
			throw new IllegalArgumentException("Cannot create  Account, the encoded key must be null");
		}
		return serviceExecutor.executeJson(createAccount, account);
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
	public JSONSavingsAccount updateSavingsAccount(JSONSavingsAccount account) throws MambuApiException {

		if (account == null || account.getSavingsAccount() == null) {
			throw new IllegalArgumentException("Account must not be NULL");
		}

		SavingsAccount inputAccount = account.getSavingsAccount();
		String encodedKey = inputAccount.getEncodedKey();
		if (encodedKey == null) {
			throw new IllegalArgumentException("Cannot update  Account, the encoded key must be NOT null");
		}

		return serviceExecutor.executeJson(updateAccount, account, encodedKey);
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

		String id = (accountId != null) ? accountId : encodedKey;
		ParamsMap params = ServiceHelper.makeParamsForSavingsTermsPatch(savings);
		return serviceExecutor.execute(patchAccount, id, params);

	}

	/***
	 * Get all documents for a specific Savings Account
	 * 
	 * @param accountId
	 *            the encoded key or id of the savings account for which attached documents are to be retrieved
	 * 
	 * @return documents documents attached to the entity
	 * 
	 * @throws MambuApiException
	 */
	public List<Document> getSavingsAccountDocuments(String accountId) throws MambuApiException {
		return serviceExecutor.execute(getAccountDocuments, accountId);
	}

}
