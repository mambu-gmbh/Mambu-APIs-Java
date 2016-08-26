/**
 * 
 */
package com.mambu.apisdk.services;

import org.junit.Test;
import org.mockito.Mockito;

import com.mambu.accounting.shared.model.GLAccountType;
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
public class AccountingServiceTest extends MambuAPIServiceTest {

	private AccountingService service;

	@Override
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new AccountingService(super.mambuApiService);
	}

	@Test
	public void testGetGLAccountById() throws MambuApiException {

		// execute
		service.getGLAccount("1000");

		// verify
		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/glaccounts/1000", null, Method.GET,
				ContentType.WWW_FORM);

	}

	@Test
	public void testGetGLAccountDateRange() throws MambuApiException {

		// execute
		service.getGLAccount("100", "2001-01-01", "2005-01-01");

		// verify
		ParamsMap params = new ParamsMap();
		params.put(APIData.FROM, "2001-01-01");
		params.put(APIData.TO, "2005-01-01");

		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/glaccounts/100", params, Method.GET,
				ContentType.WWW_FORM);
	}

	@Test
	public void testGetGLAccountsByType() throws MambuApiException {

		// execute
		service.getGLAccounts(GLAccountType.ASSET);

		// verify
		ParamsMap params = new ParamsMap();
		params.put(APIData.TYPE, GLAccountType.ASSET.name());

		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/glaccounts", params, Method.GET,
				ContentType.WWW_FORM);
	}

	@Test
	public void testGetGLAccountsByTypeWithLimitAndOffset() throws MambuApiException {

		// execute
		service.getGLAccounts(GLAccountType.ASSET, 10, 5);

		// verify
		ParamsMap params = new ParamsMap();
		params.put(APIData.TYPE, GLAccountType.ASSET.name());
		params.put(APIData.LIMIT, "5");
		params.put(APIData.OFFSET, "10");

		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/glaccounts", params, Method.GET,
				ContentType.WWW_FORM);
	}

}
