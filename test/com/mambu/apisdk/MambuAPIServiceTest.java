package com.mambu.apisdk;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.RequestExecutor;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.intelligence.shared.model.Intelligence.Indicator;


public class MambuAPIServiceTest {

	MambuAPIService service;
	RequestExecutor executor;
	String username = "user";
	String password = "password";
	String domain = "demo.mambutest.com";
	
	@Before
	public void setUp() throws Exception {
		executor = Mockito.mock(RequestExecutor.class); 
		
		service = new MambuAPIService(username, password, domain, executor);
		service.setProtocol("http");

	}

	@Test
	public void testGetClient() throws MalformedURLException, IOException, MambuApiException {
		
		//execute
		service.getClient("abc123");
		
		//verify
		Mockito.verify(executor).executeRequest("http://demo.mambutest.com/api/clients/abc123", Method.GET);
	}

	@Test
	public void testGetGLAccountById()  throws MalformedURLException, IOException, MambuApiException {

		//execute
		service.getGLAccount("1000");
		
		//verify
		Mockito.verify(executor).executeRequest("http://demo.mambutest.com/api/glaccounts/1000", Method.GET);
	}
		
	@Test
	public void testGetCurrency()  throws MalformedURLException, IOException, MambuApiException {

		//execute
		service.getCurrency();
		
		//verify
		Mockito.verify(executor).executeRequest("http://demo.mambutest.com/api/currencies", Method.GET);
	}

	@Test
	public void testGetGLAccountDateRange()  throws MalformedURLException, IOException, MambuApiException {
		
		//execute
		service.getGLAccount("100", "2001-01-01", "2005-01-01");
		
		//verify
		Mockito.verify(executor).executeRequest("http://demo.mambutest.com/api/glaccounts/100?from=2001-01-01&to=2005-01-01", Method.GET);	
	}

	@Test
	public void testGetIndicator() throws MalformedURLException, IOException, MambuApiException {
		//execute
		service.getIndicator(Indicator.INTEREST_IN_SUSPENSE);
		
		//verify
		Mockito.verify(executor).executeRequest("http://demo.mambutest.com/api/indicators/INTEREST_IN_SUSPENSE", Method.GET);	
	}

	@Test
	public void testGetClientDetails() throws MalformedURLException, IOException, MambuApiException {
		//execute
		service.getClientDetails("abc123");
		
		//verify
		Mockito.verify(executor).executeRequest("http://demo.mambutest.com/api/clients/abc123?fullDetails=true", Method.GET);

	}

}
