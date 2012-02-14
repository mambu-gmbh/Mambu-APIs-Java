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
public class OrganizationServiceTest extends MambuAPIServiceTest {

	private OrganizationService service;

	@Before
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new OrganizationService(super.mambuApiService);
	}

	@Test
	public void testGetCurrency() throws MambuApiException {

		// execute
		service.getCurrency();

		// verify
		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/currencies", Method.GET);
	}
}
