/**
 * 
 */
package com.mambu.apisdk.services;

import com.google.inject.Inject;
import com.mambu.accounting.shared.model.GLAccount;
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
	 * Returns the gl account response witha given url & parameters
	 * 
	 * @param url
	 * @return
	 * @throws MambuApiException
	 */
	private GLAccount getGLAccountResponse(String url) throws MambuApiException {
		String urlString = new String(mambuAPIService.createUrl(url));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);
		GLAccount glAccount = GsonUtils.createResponse().fromJson(jsonResponse, GLAccount.class);

		return glAccount;
	}
}
