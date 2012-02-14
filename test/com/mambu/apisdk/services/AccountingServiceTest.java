/**
 * 
 */
package com.mambu.apisdk.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.mambu.apisdk.MambuAPIServiceTest;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.RequestExecutor.Method;

/**
 * @author ipenciuc
 * 
 */
public class AccountingServiceTest extends MambuAPIServiceTest {

	private AccountingService service;

	@Before
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new AccountingService(super.mambuApiService);
	}

	@Test
	public void testGetGLAccountById() throws MambuApiException {

		// execute
		service.getGLAccount("1000");

		// verify
		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/glaccounts/1000", Method.GET);

	}

	@Test
	public void testGetGLAccountDateRange() throws MambuApiException {

		// execute
		service.getGLAccount("100", "2001-01-01", "2005-01-01");

		// verify
		Mockito.verify(executor).executeRequest(
				"https://demo.mambutest.com/api/glaccounts/100?from=2001-01-01&to=2005-01-01", Method.GET);
	}

}
