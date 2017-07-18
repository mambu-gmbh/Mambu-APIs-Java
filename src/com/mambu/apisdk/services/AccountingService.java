/**
 * 
 */
package com.mambu.apisdk.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.google.inject.Inject;
import com.mambu.accounting.shared.model.EntryType;
import com.mambu.accounting.shared.model.GLAccount;
import com.mambu.accounting.shared.model.GLAccountType;
import com.mambu.accounting.shared.model.GLJournalEntry;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraints;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.ApiGLJournalEntry;
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

/**
 * Service class which handles the API operations available for the accounting
 * 
 * @author ipenciuc
 * 
 */
public class AccountingService {

	// Our ServiceExecutor
	private ServiceExecutor serviceExecutor;
	// Create API definitions for this service
	// Get GLAccount
	private final static ApiDefinition getGLAccount = new ApiDefinition(ApiType.GET_ENTITY, GLAccount.class);
	// Get a list of GLAccounts
	private final static ApiDefinition getGLAccounts = new ApiDefinition(ApiType.GET_LIST, GLAccount.class);
	// Get a list of GLJournalEntry
	private final static ApiDefinition getGLJournalEntries = new ApiDefinition(ApiType.GET_LIST, GLJournalEntry.class);

	/***
	 * Create a new accounting service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public AccountingService(MambuAPIService mambuAPIService) {

		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}

	/**
	 * Requests a gl account by its gl code
	 * 
	 * @param glCode
	 *            accounts's gl code
	 * 
	 * @return the gl account
	 * 
	 * @throws MambuApiException
	 */
	public GLAccount getGLAccount(String glCode) throws MambuApiException {

		// Example GET/api/glaccount/1234123
		// See MBU-1543
		return serviceExecutor.execute(getGLAccount, glCode);
	}

	/**
	 * Requests a gl account by its gl code with a balance over a certain date range
	 * 
	 * @param glCode
	 *            gl code. Must not be null
	 * 
	 * @return the Mambu gl account
	 * 
	 * @throws MambuApiException
	 */
	public GLAccount getGLAccount(String glCode, String fromDate, String toDate) throws MambuApiException {

		// Example GET /api/glaccount/1234123?from=2011-10-04&to=2011-11-04
		// See MBU-1543
		ParamsMap params = new ParamsMap();
		params.put(APIData.FROM, fromDate);
		params.put(APIData.TO, toDate);

		return serviceExecutor.execute(getGLAccount, glCode, params);
	}

	/**
	 * Requests gl accounts by account type
	 * 
	 * @param accountType
	 *            account type. Must not be null
	 * 
	 * @return a list of Mambu gl accounts
	 * 
	 * @throws MambuApiException
	 */
	public List<GLAccount> getGLAccounts(GLAccountType accountType) throws MambuApiException {

		// Example GET /api/glaccount?type=ASSET
		// See MBU-1543.

		if (accountType == null) {
			throw new IllegalArgumentException("Account type must not be null");
		}
		ParamsMap params = new ParamsMap();
		params.put(APIData.TYPE, accountType.name());

		return serviceExecutor.execute(getGLAccounts, params);
	}

	/**
	 * Returns all GLJournalEntries of a specific date-range, using default limits.
	 * 
	 * @param branchId
	 *            branch Id
	 * @param fromDate
	 *            range starting from
	 * @param toDate
	 *            range ending at
	 * 
	 * @return a List of GLJournalEntries
	 * 
	 * @throws MambuApiException
	 *             in case of an error
	 */
	public List<GLJournalEntry> getGLJournalEntries(String branchId, Date fromDate, Date toDate)
			throws MambuApiException {

		// GET /api/gljournalentries?from=1875-05-20&to=1875-05-25&branchID=ABC123
		// See MBU-1736
		return (this.getGLJournalEntries(branchId, fromDate, toDate, -1, -1));
	}

	/**
	 * Returns all GLJournalEntries of a specific date-range
	 * 
	 * @param branchID
	 *            branch Id
	 * @param fromDate
	 *            range starting from. Must not be null
	 * @param toDate
	 *            range ending at. Must not be null
	 * @param offset
	 *            offset to start pagination. If null, the default value of 0 (zero) will be used.
	 * @param limit
	 *            page-size. If null, the default value of 50 (fifty) will be used.
	 * 
	 * @return a List of GLJournalEntries
	 * 
	 * @throws MambuApiException
	 *             in case of an error
	 */
	public List<GLJournalEntry> getGLJournalEntries(String branchID, Date fromDate, Date toDate, Integer offset,
			Integer limit) throws MambuApiException {

		// GET /api/gljournalentries?from=1875-05-20&to=1875-05-25&branchID=ABC123&offset=50&limit=50
		// See MBU-1736
		if (fromDate == null || toDate == null) {
			throw new IllegalArgumentException("fromDate and toDate must not be null");
		}

		ParamsMap params = new ParamsMap();
		params.put(APIData.BRANCH_ID, branchID);
		params.put(APIData.FROM, DateUtils.FORMAT.format(fromDate));
		params.put(APIData.TO, DateUtils.FORMAT.format(toDate));
		if (offset != null) {
			params.put(APIData.OFFSET, Integer.toString(offset));
		}
		if (limit != null) {
			params.put(APIData.LIMIT, Integer.toString(limit));
		}

		return serviceExecutor.execute(getGLJournalEntries, params);
	}

	/**
	 * Get GL journal entries by specifying filter constraints
	 * 
	 * @param filterConstraints
	 *            filter constraints. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * @return list of GL journal entries matching filter constraints
	 * @throws MambuApiException
	 */
	public List<GLJournalEntry> getGLJournalEntries(JSONFilterConstraints filterConstraints, String offset,
			String limit) throws MambuApiException {

		// POST {JSONFilterConstraints} /api/gljournalentries/search?offset=0&limit=5
		// See MBU-12099
		ApiDefinition apiDefinition = SearchService
				.makeApiDefinitionforSearchByFilter(MambuEntityType.GL_JOURNAL_ENTRY);

		// POST Filter JSON with pagination params map
		return serviceExecutor.executeJson(apiDefinition, filterConstraints, null, null,
				ServiceHelper.makePaginationParams(offset, limit));
	}

	/**
	 * Post GL Journal Entries
	 * 
	 * @param entries
	 *            a list of entries with the GL transaction details. Must not be null. At least one debit and one credit
	 *            entry must be specified. Any number of journal entries may be posted with a given date and branch id
	 *            as long as the standard accounting rules apply. For each entry its glCode, entryType and amount must
	 *            not be null.
	 * @param branchId
	 *            a branch id.
	 * @param date
	 *            The date of the posting of the journal entry. Must be not null
	 * @param notes
	 *            transaction notes
	 * @return created journal entries
	 * @throws MambuApiException
	 */
	public List<GLJournalEntry> postGLJournalEntries(List<ApiGLJournalEntry> entries, String branchId, String date,
			String notes) throws MambuApiException {

		// POST "branchId=2&date=2010-02-03&debitAccount1=100001&debitAmount1=30&creditAccount1=100002&creditAmount1=30"
		// /api/gljournalentries
		// See MBU-1737

		validateParamsForPostingGLEntries(entries, date);

		ParamsMap params = populateBaseParams(entries, branchId, date, notes);

		return executePostGLJournalEntries(params);
	}

	/**
	 * Post GL Journal Entries
	 * 
	 * @param entries
	 *            a list of entries with the GL transaction details. Must not be null. At least one debit and one credit
	 *            entry must be specified. Any number of journal entries may be posted with a given date and branch id
	 *            as long as the standard accounting rules apply. For each entry its glCode, entryType and amount must
	 *            not be null.
	 * @param branchId
	 *            a branch id.
	 * @param date
	 *            The date of the posting of the journal entry. Must be not null
	 * @param notes
	 *            transaction notes
	 * @param transactionId
	 *            the transaction id
	 * @return created journal entries
	 * @throws MambuApiException
	 */
	public List<GLJournalEntry> postGLJournalEntries(List<ApiGLJournalEntry> entries, String branchId, String date,
			String notes, String transactionId) throws MambuApiException {

		// POST
		// "branchId=2&date=2010-02-03&debitAccount1=100001&debitAmount1=30&creditAccount1=100002&creditAmount1=30&transactionID=9284"
		// /api/gljournalentries
		// See MBU-15973

		validateParamsForPostingGLEntries(entries, date);

		ParamsMap params = populateBaseParams(entries, branchId, date, notes);

		if (transactionId != null) {
			params.put(APIData.TRANSACTION_ID, transactionId);
		}

		return executePostGLJournalEntries(params);
	}

	/**
	 * Executes the posting of GL journal entries
	 * 
	 * @param params
	 *            the parameters of the call
	 * @return a list containing successful posted entries
	 * 
	 * @throws MambuApiException
	 */
	private List<GLJournalEntry> executePostGLJournalEntries(ParamsMap params) throws MambuApiException {

		// Create ApiDefinition
		ApiDefinition apiDefinition = new ApiDefinition(APIData.GLJOURNALENTRIES, ContentType.WWW_FORM, Method.POST,
				GLJournalEntry.class, ApiReturnFormat.COLLECTION);

		// Execute API
		List<GLJournalEntry> glEntries = serviceExecutor.execute(apiDefinition, params);
		return glEntries;
	}

	/**
	 * Populates all the basic parameters needed to post GL journal entries
	 * 
	 * @param entries
	 *            the GL entries to be posted
	 * @param branchId
	 *            the branch id
	 * @param date
	 *            the date
	 * @param notes
	 *            the notes
	 * @return a map containing the parameters for posting journal entries
	 */
	private ParamsMap populateBaseParams(List<ApiGLJournalEntry> entries, String branchId, String date, String notes) {

		ParamsMap params = new ParamsMap();
		int debitIndex = 1;
		int creditIndex = 1;

		for (ApiGLJournalEntry entry : entries) {
			String glCode = entry.getGlCode();
			EntryType entryType = entry.getEntryType();
			BigDecimal amount = entry.getAmount();
			if (glCode == null || entryType == null || amount == null) {
				throw new IllegalArgumentException(
						"GlCode " + glCode + " EntryType=" + entryType + " and Amount=" + amount + " must not be null");
			}

			String accountParam = null;
			String amountParam = null;
			switch (entryType) {
			case DEBIT:
				accountParam = APIData.DEBIT_ACCOUNT + debitIndex;
				amountParam = APIData.DEBIT_AMOUNT + debitIndex;
				debitIndex++;
				break;
			case CREDIT:
				accountParam = APIData.CREDIT_ACCOUNT + creditIndex;
				amountParam = APIData.CREDIT_AMOUNT + creditIndex;
				creditIndex++;
				break;
			}

			params.put(accountParam, glCode);
			params.put(amountParam, String.valueOf(amount.doubleValue()));

		}

		// Add date, barnchId, and notes
		params.put(APIData.DATE, date);
		params.put(APIData.BRANCH_ID, branchId);
		params.put(APIData.NOTES, notes);

		return params;
	}

	/**
	 * Checks whether the parameters needed for posting GL entries are valid
	 * 
	 * @param entries
	 *            the entries to be validated
	 * @param date
	 *            the date needed to be validated
	 */
	private void validateParamsForPostingGLEntries(List<ApiGLJournalEntry> entries, String date) {

		if (entries == null || entries.size() < 2) {
			throw new IllegalArgumentException("At least one debit and one credit entry is required");
		}
		if (date == null) {
			throw new IllegalArgumentException("Date must not be null");
		}
	}
}
