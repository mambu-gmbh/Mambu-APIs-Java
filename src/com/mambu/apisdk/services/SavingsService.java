/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.List;

import com.google.inject.Inject;
import com.mambu.accounts.shared.model.TransactionDetails;
import com.mambu.api.server.handler.savings.model.JSONSavingsAccount;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.APIData.ACCOUNT_TYPE;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.apisdk.util.ServiceHelper;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.docs.shared.model.Document;
import com.mambu.savings.shared.model.SavingsAccount;
import com.mambu.savings.shared.model.SavingsProduct;
import com.mambu.savings.shared.model.SavingsTransaction;

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

	private static final String TYPE_APPROVAL = APIData.TYPE_APPROVAL;
	private static final String TYPE_UNDO_APPROVAL = APIData.TYPE_UNDO_APPROVAL;

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
	private final static ApiDefinition updateAccount = new ApiDefinition(ApiType.UPDATE_JSON, JSONSavingsAccount.class);

	// Loan Products API requests
	// Get Loan Product Details
	private final static ApiDefinition getProduct = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, SavingsProduct.class);
	// Get Lists of Loan Products
	private final static ApiDefinition getProducts = new ApiDefinition(ApiType.GET_LIST, SavingsProduct.class);
	// Update Custom Field value for a Savings Account
	private final static ApiDefinition updateAccountCustomField = new ApiDefinition(ApiType.PATCH_OWNED_ENTITY,
			SavingsAccount.class, CustomFieldValue.class);
	// Delete Custom Field for a Savings Account
	private final static ApiDefinition deleteAccountCustomField = new ApiDefinition(ApiType.DELETE_OWNED_ENTITY,
			SavingsAccount.class, CustomFieldValue.class);

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
	 * 
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

	/****
	 * @deprecated As of release 3.8, replaced by
	 *             {@link #makeWithdrawal(String, String, String, String, TransactionDetails)}
	 * 
	 *             Make a withdrawal from an account.
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
	@Deprecated
	public SavingsTransaction makeWithdrawal(String accountId, String amount, String date, String notes,
			String paymentMethod, String receiptNumber, String bankNumber, String checkNumber,
			String bankAccountNumber, String bankRoutingNumber) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_WITHDRAWAL);

		addPaymentMethodAccountDetails(paramsMap, amount, date, notes, paymentMethod, receiptNumber, bankNumber,
				checkNumber, bankAccountNumber, bankRoutingNumber);

		return serviceExecutor.execute(postAccountTransaction, accountId, paramsMap);

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
	 * 
	 * @deprecated As of release 3.8, replaced by
	 *             {@link #makeDeposit(String, String, String, String, TransactionDetails)}
	 * 
	 *             Make a deposit to an account.
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
	@Deprecated
	public SavingsTransaction makeDeposit(String accountId, String amount, String date, String notes,
			String paymentMethod, String receiptNumber, String bankNumber, String checkNumber,
			String bankAccountNumber, String bankRoutingNumber) throws MambuApiException {

		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_DEPOSIT);

		addPaymentMethodAccountDetails(paramsMap, amount, date, notes, paymentMethod, receiptNumber, bankNumber,
				checkNumber, bankAccountNumber, bankRoutingNumber);

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
		if (fromAccountId == null || fromAccountId.trim().isEmpty()) {
			throw new IllegalArgumentException("From Account ID  must not  be null or empty");
		}

		if (destinationAccountKey == null || destinationAccountKey.trim().isEmpty()) {
			throw new IllegalArgumentException("Destination Account ID  must not  be null or empty");
		}
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.addParam(TYPE, TYPE_TRANSFER);

		if (destinationAccountType == ACCOUNT_TYPE.LOAN) {
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
	 * 
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
	 * Requests a list of savings accounts for a custom view, limited by offset/limit
	 * 
	 * @param customViewKey
	 *            the key of the Custom View to filter savings accounts
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * 
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

	// Savings Products
	/***
	 * Get a list of Savings Products
	 * 
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

	/***
	 * Update custom field value for a Savings Account. This method allows to set new value for a specific custom field
	 * 
	 * @param accountId
	 *            the encoded key or id of the Mambu Savings Account for which the custom field is updated
	 * @param customFieldId
	 *            the encoded key or id of the custom field to be updated
	 * @param fieldValue
	 *            the new value of the custom field
	 * 
	 * @throws MambuApiException
	 */
	public boolean updateSavingsAccountCustomField(String accountId, String customFieldId, String fieldValue)
			throws MambuApiException {
		// Execute request for PATCH API to update custom field value for a Savings Account. See MBU-6661
		// e.g. PATCH "{ "value": "10" }" /host/api/savings/accointId/custominformation/customFieldId

		// Make ParamsMap with JSON request for Update API
		ParamsMap params = ServiceHelper.makeParamsForUpdateCustomField(customFieldId, fieldValue);
		return serviceExecutor.execute(updateAccountCustomField, accountId, customFieldId, params);

	}

	/***
	 * Delete custom field for a Savings Account
	 * 
	 * @param accountId
	 *            the encoded key or id of the Mambu Savings Account
	 * @param customFieldId
	 *            the encoded key or id of the custom field to be deleted
	 * 
	 * @throws MambuApiException
	 */
	public boolean deleteSavingsAccountCustomField(String accountId, String customFieldId) throws MambuApiException {
		// Execute request for DELETE API to delete custom field for a Savings Account
		// e.g. DELETE /host/api/savings/accointId/custominformation/customFieldId

		return serviceExecutor.execute(deleteAccountCustomField, accountId, customFieldId, null);

	}

	// private helper method
	@Deprecated
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
}
