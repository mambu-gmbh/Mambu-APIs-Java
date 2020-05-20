package com.mambu.apisdk;

import static com.mambu.core.shared.helper.StringUtils.EMPTY_STRING;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mambu.apisdk.util.RequestExecutor;
import com.mambu.apisdk.util.URLHelper;

/**
 * @author cezarrom
 */
@RunWith(MockitoJUnitRunner.class)
public class MambuAPIServiceTest {

	private final static String DOMAIN_NAME = "someDomainName";
	private final static String USERNAME = "someUsername";
	private final static String PASSWORD = "somePassword";
	private final static String APIKEY = "someApiKey";

	@Mock
	private RequestExecutor executorMock;
	@Mock
	private URLHelper urlHelperMock;


	@Test
	public void givenApiKeyWhenMambuAPIServiceIsCreatedThenAuthorizationIsBasedOnApiKey() {

		new MambuAPIService(DOMAIN_NAME, USERNAME, PASSWORD, APIKEY, executorMock, urlHelperMock);

		verify(executorMock).setAuthorization(APIKEY);

		verifyNoMoreInteractions(executorMock);

	}

	@Test
	public void givenApiKeyIsNotProvidedWhenMambuAPIServiceIsCreatedThenAuthorizationIsBasedOnBasicAuth() {

		new MambuAPIService(DOMAIN_NAME, USERNAME, PASSWORD, EMPTY_STRING, executorMock, urlHelperMock);

		verify(executorMock).setAuthorization(USERNAME, PASSWORD);

		verifyNoMoreInteractions(executorMock);

	}
}