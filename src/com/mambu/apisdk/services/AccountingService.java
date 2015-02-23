/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.Date;
import java.util.List;

import com.google.inject.Inject;
import com.mambu.accounting.shared.model.GLAccount;
import com.mambu.accounting.shared.model.GLJournalEntry;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.DateUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.ServiceExecutor;

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
	// Get List of GLJournalEntry
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
	 * 
	 * @return the Mambu gl account
	 * 
	 * @throws MambuApiException
	 */
	public GLAccount getGLAccount(String glCode) throws MambuApiException {
		return serviceExecutor.execute(getGLAccount, glCode);
	}

	/**
	 * Requests a gl account by its gl code with a balance over a certain date range
	 * 
	 * @param glCode
	 * 
	 * @return the Mambu gl account
	 * 
	 * @throws MambuApiException
	 */
	public GLAccount getGLAccount(String glCode, String fromDate, String toDate) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.put(APIData.FROM, fromDate);
		params.put(APIData.TO, toDate);

		return serviceExecutor.execute(getGLAccount, glCode, params);
	}

	/**
	 * Returns all GLJournalEntries of a specific date-range, using default limits.
	 * 
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
	public List<GLJournalEntry> getGLJournalEntries(Date fromDate, Date toDate) throws MambuApiException {
		return (this.getGLJournalEntries(fromDate, toDate, -1, -1));
	}

	/**
	 * Returns all GLJournalEntries of a specific date-range, using default limits.
	 * 
	 * @param fromDate
	 *            range starting from
	 * @param toDate
	 *            range ending at
	 * @param offset
	 *            offset to start pagination
	 * @param limit
	 *            page-size
	 * 
	 * @return a List of GLJournalEntries
	 * 
	 * @throws MambuApiException
	 *             in case of an error
	 */
	public List<GLJournalEntry> getGLJournalEntries(Date fromDate, Date toDate, int offset, int limit)
			throws MambuApiException {

		if (fromDate == null || toDate == null) {
			throw new IllegalArgumentException("fromDate and toDate must not be null");
		}

		ParamsMap params = new ParamsMap();
		params.put(APIData.FROM, DateUtils.FORMAT.format(fromDate));
		params.put(APIData.TO, DateUtils.FORMAT.format(toDate));
		params.put(APIData.OFFSET, Integer.toString(offset));
		params.put(APIData.LIMIT, Integer.toString(limit));

		return serviceExecutor.execute(getGLJournalEntries, params);
	}
}
