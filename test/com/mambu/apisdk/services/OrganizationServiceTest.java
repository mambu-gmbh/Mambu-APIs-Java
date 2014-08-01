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
import com.mambu.core.shared.model.CustomField;

/**
 * @author ipenciuc
 * 
 */
public class OrganizationServiceTest extends MambuAPIServiceTest {

	private OrganizationService service;

	@Override
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new OrganizationService(super.mambuApiService);
	}

	@Test
	public void testGetCurrency() throws MambuApiException {

		// execute
		try {
			service.getCurrency();
		} catch (MambuApiException e) {
			// Check if we received an expected exception and, if so, ignore it
			// The getCurrency() service may throw an exception even after a successful request if no entries are
			// returned, which is always the case in this test environment
			if (!(e.getErrorCode() == -1 && e.getMessage().equalsIgnoreCase(
					OrganizationService.baseCurrencyMustBeDefined))) {
				// re-throw
				throw e;
			}
		}
		// verify
		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/currencies", null, Method.GET,
				ContentType.WWW_FORM);
	}

	@Test
	public void testGetBranch() throws MambuApiException {

		// execute
		service.getBranch("branch_123");

		// verify
		ParamsMap params = new ParamsMap();
		params.put(APIData.FULL_DETAILS, "true");

		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/branches/branch_123", params,
				Method.GET, ContentType.WWW_FORM);
	}

	@Test
	public void testGetBranches() throws MambuApiException {

		// execute
		final String offset = "0";
		final String limit = "100";
		service.getBranches(offset, limit);

		// verify
		ParamsMap params = new ParamsMap();
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/branches", params, Method.GET,
				ContentType.WWW_FORM);
	}

	@Test
	public void testGetCentre() throws MambuApiException {

		// execute
		service.getCentre("centre_123");

		// verify
		ParamsMap params = new ParamsMap();
		params.put(APIData.FULL_DETAILS, "true");

		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/centres/centre_123", params,
				Method.GET, ContentType.WWW_FORM);
	}

	@Test
	public void testGetCentres() throws MambuApiException {

		final String branchId = "123";
		final String offset = "0";
		final String limit = "100";

		// execute

		service.getCentres(branchId, offset, limit);

		// verify
		ParamsMap params = new ParamsMap();
		params.addParam(APIData.BRANCH_ID, branchId);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/centres", params, Method.GET,
				ContentType.WWW_FORM);
	}

	@Test
	public void testGetCustomFields() throws MambuApiException {

		// execute
		service.getCustomField("field_123");

		// verify

		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/customfields/field_123", null,
				Method.GET, ContentType.WWW_FORM);
	}

	@Test
	public void testGetCustomFieldSets() throws MambuApiException {

		// execute
		final CustomField.Type type = CustomField.Type.CLIENT_INFO;
		service.getCustomFieldSets(type);

		// verify
		ParamsMap params = new ParamsMap();
		params.addParam(APIData.CUSTOM_FIELD_SETS_TYPE, type.name());

		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/customfieldsets", params, Method.GET,
				ContentType.WWW_FORM);
	}
}
