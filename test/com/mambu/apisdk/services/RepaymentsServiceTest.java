/**
 * 
 */
package com.mambu.apisdk.services;

import org.junit.Test;
import org.mockito.Mockito;

import com.mambu.apisdk.MambuAPIServiceTest;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;

/**
 * @author ipenciuc
 * 
 */
public class RepaymentsServiceTest extends MambuAPIServiceTest {

	private RepaymentsService service;

	@Override
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new RepaymentsService(super.mambuApiService);
	}

	@Test
	public void testGetLoanRapayments() throws MambuApiException {

		// execute

		final String accountId = "accountId_1234";
		service.getLoanAccountRepayments(accountId);

		// verify

		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/loans/" + accountId + "/repayments",
				null, Method.GET, ContentType.WWW_FORM);
	}

	@Test
	public void testGetRapaymentsDueFromTo() throws MambuApiException {

		// execute
		final String dueFromString = "2014-02-01";
		final String dueToString = "2014-07-05";
		service.getRapaymentsDueFromTo(dueFromString, dueToString);

		// verify
		ParamsMap paramsMap = new ParamsMap();
		paramsMap.put(APIData.DUE_FROM, dueFromString);
		paramsMap.put(APIData.DUE_TO, dueToString);
		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/repayments", paramsMap, Method.GET,
				ContentType.WWW_FORM);
	}

}
