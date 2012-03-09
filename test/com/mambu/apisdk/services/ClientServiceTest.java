/**
 * 
 */
package com.mambu.apisdk.services;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

import com.mambu.apisdk.MambuAPIServiceTest;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.Method;

/**
 * @author ipenciuc
 * 
 */
public class ClientServiceTest extends MambuAPIServiceTest {

	private ClientsService service;

	@Before
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new ClientsService(super.mambuApiService);

	}

	/***
	 * Test the retrieval of a client with a given id
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws MambuApiException
	 */
	@Test
	public void testGetClient() throws MambuApiException {

		// execute
		service.getClient("abc123");

		// verify
		verify(executor).executeRequest("https://demo.mambutest.com/api/clients/abc123", Method.GET);
	}

	@Test
	public void testGetClientDetails() throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam("fullDetails", "true");

		// execute
		service.getClientDetails("abc123");

		// verify
		verify(executor).executeRequest("https://demo.mambutest.com/api/clients/abc123", params, Method.GET);

	}
}
