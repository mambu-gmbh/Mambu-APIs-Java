/**
 * 
 */
package com.mambu.apisdk.services;

import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.mambu.apisdk.MambuAPIServiceTest;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;

/**
 * @author ipenciuc
 * 
 */
public class ClientServiceTest extends MambuAPIServiceTest {

	private ClientsService service;

	@Override
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new ClientsService(super.mambuApiService);

	}

	/***
	 * Test the retrieval of a client with a given id
	 * 
	 */
	@Test
	public void testGetClient() throws MambuApiException {

		// execute
		service.getClient("abc123");

		// verify
		verify(executor).executeRequest("https://demo.mambutest.com/api/clients/abc123", null, Method.GET,
				ContentType.WWW_FORM);
	}

	/***
	 * Test the retrieval of more/all clients
	 * 
	 */
	@Test
	public void testGetClientsActive() throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam("state", "ACTIVE");

		// execute
		service.getClients(true, null, null);

		// verify
		verify(executor).executeRequest("https://demo.mambutest.com/api/clients", params, Method.GET,
				ContentType.WWW_FORM);
	}

	/***
	 * Test the retrieval of more/all clients
	 * 
	 */
	@Test
	public void testGetClientsInactive() throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam("state", "INACTIVE");

		// execute
		service.getClients(false, null, null);

		// verify
		verify(executor).executeRequest("https://demo.mambutest.com/api/clients", params, Method.GET,
				ContentType.WWW_FORM);
	}

	/***
	 * Test the retrieval of more/all clients
	 * 
	 */
	@Test
	public void testGetClientsActivePaged() throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam("state", "ACTIVE");
		params.addParam("offset", "0");
		params.addParam("limit", "50");

		// execute
		service.getClients(true, 0, 50);

		// verify
		verify(executor).executeRequest("https://demo.mambutest.com/api/clients", params, Method.GET,
				ContentType.WWW_FORM);
	}

	/***
	 * Test the retrieval of more/all clients
	 * 
	 */
	@Test
	public void testGetClientsInactivePaged() throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam("state", "INACTIVE");
		params.addParam("offset", "0");
		params.addParam("limit", "50");

		// execute
		service.getClients(false, 0, 50);

		// verify
		verify(executor).executeRequest("https://demo.mambutest.com/api/clients", params, Method.GET,
				ContentType.WWW_FORM);
	}

	/***
	 * Test the retrieval of a full details client with a given id
	 * 
	 */
	@Test
	public void testGetClientDetails() throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam("fullDetails", "true");

		// execute
		service.getClientDetails("abc123");

		// verify
		verify(executor).executeRequest("https://demo.mambutest.com/api/clients/abc123", params, Method.GET,
				ContentType.WWW_FORM);

	}
}
