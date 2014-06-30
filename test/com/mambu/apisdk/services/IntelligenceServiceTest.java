/**
 * 
 */
package com.mambu.apisdk.services;

import org.junit.Test;
import org.mockito.Mockito;

import com.mambu.apisdk.MambuAPIServiceTest;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.intelligence.shared.model.Intelligence.Indicator;

/**
 * @author ipenciuc
 * 
 */
public class IntelligenceServiceTest extends MambuAPIServiceTest {

	private IntelligenceService service;

	@Override
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new IntelligenceService(super.mambuApiService);
	}

	@Test
	public void testGetIndicator() throws MambuApiException {
		// execute
		service.getIndicator(Indicator.INTEREST_IN_SUSPENSE);

		// verify
		Mockito.verify(executor).executeRequest("https://demo.mambutest.com/api/indicators/INTEREST_IN_SUSPENSE", null,
				Method.GET, ContentType.WWW_FORM);
	}
}
