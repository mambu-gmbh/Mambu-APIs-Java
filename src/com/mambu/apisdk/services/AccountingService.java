/**
 * 
 */
package com.mambu.apisdk.services;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.mambu.accounting.shared.model.GLAccount;
import com.mambu.accounting.shared.model.GLJournalEntry;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.RequestExecutor.Method;

/**
 * Service class which handles the API operations available for the accounting
 * 
 * @author ipenciuc
 * 
 */
public class AccountingService {

	private MambuAPIService mambuAPIService;

	/***
	 * Create a new accounting service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public AccountingService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/**
	 * Requests a gl account by its gl code
	 * 
	 * @param glCode
	 * @return the Mambu gl account
	 * @throws MambuApiException
	 */
	public GLAccount getGLAccount(String glCode) throws MambuApiException {

		// create the api call
		String url = APIData.GLACCOUNTS + "/" + glCode;
		GLAccount glAccount = getGLAccountResponse(url);

		return glAccount;

	}

	/**
	 * Requests a gl account by its gl code with a balance over a certain date range
	 * 
	 * @param glCode
	 * @return the Mambu gl account
	 * @throws MambuApiException
	 */
	public GLAccount getGLAccount(String glCode, String fromDate, String toDate) throws MambuApiException {

		// create the api call
		String url = APIData.GLACCOUNTS + "/" + glCode + "?" + "from=" + fromDate + "&to=" + toDate;

		GLAccount glAccount = getGLAccountResponse(url);
		return glAccount;

	}

	/**
	 * Returns all GLJournalEntries of a specific date-range, using default limits.
	 * 
	 * @param fromDate
	 *            range starting from
	 * @param toDate
	 *            range ending at
	 * @return a List of GLJournalEntries
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
	 * @return a List of GLJournalEntries
	 * @throws MambuApiException
	 *             in case of an error
	 */
	public List<GLJournalEntry> getGLJournalEntries(Date fromDate, Date toDate, int offset, int limit)
			throws MambuApiException {
		String url = mambuAPIService.createUrl(String.format("%s?from=%s&to=%s", APIData.GLJOURNALENTRIES,
				APIData.URLDATE_FORMATTER.format(fromDate), APIData.URLDATE_FORMATTER.format(toDate)), offset, limit);
		String jsonResponse = mambuAPIService.executeRequest(url, Method.GET);
		Type collectionType = new TypeToken<List<GLJournalEntry>>() {}.getType();

		return ((List<GLJournalEntry>) GsonUtils.createGson().fromJson(jsonResponse, collectionType));
	}

	/**
	 * Returns the gl account response with given url & parameters
	 * 
	 * @param url
	 * @return
	 * @throws MambuApiException
	 */
	private GLAccount getGLAccountResponse(String url) throws MambuApiException {
		String urlString = new String(mambuAPIService.createUrl(url));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);
		GLAccount glAccount = GsonUtils.createGson().fromJson(jsonResponse, GLAccount.class);

		return glAccount;
	}
}
