package com.mambu.apisdk;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor;
import com.mambu.apisdk.util.URLHelper;

/***
 * Class extended by all service-test classes
 * 
 * @author ipenciuc
 * 
 */
public class ServiceTestBase {

	protected MambuAPIService mambuApiService;
	protected RequestExecutor executor;
	private URLHelper mockUrlHelper;

	private String username = "user";
	private String password = "password";
	private String domain = "demo.mambutest.com";

	private String urlRoot = "https://demo.mambutest.com/api/";

	@Before
	public void setUp() throws MambuApiException {

		executor = Mockito.mock(RequestExecutor.class);
		mockUrlHelper = Mockito.mock(URLHelper.class);

		mambuApiService = new MambuAPIService(domain, username, password, null, executor, mockUrlHelper);

		when(mockUrlHelper.createUrl(Mockito.anyString())).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation)  {
				return urlRoot + invocation.getArguments()[0];
			}
		});

		when(mockUrlHelper.createUrlWithParams(anyString(), (ParamsMap) anyObject())).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation)  {
				return urlRoot + invocation.getArguments()[0] + "?"
						+ ((ParamsMap) invocation.getArguments()[1]).getURLString();
			}
		});

	}
}
