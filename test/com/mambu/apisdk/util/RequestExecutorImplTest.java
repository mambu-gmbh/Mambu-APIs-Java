package com.mambu.apisdk.util;

import static com.mambu.apisdk.util.RequestExecutor.ContentType.WWW_FORM;
import static com.mambu.apisdk.util.RequestExecutor.Method.GET;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

/**
 * @author cezarrom
 */
@RunWith(MockitoJUnitRunner.class)
@PowerMockIgnore({ "javax.management.*", "com.sun.org.apache.xerces.*", "javax.xml.*",
		"org.xml.*", "org.w3c.dom.*", "com.sun.org.apache.xalan.*", "javax.activation.*" })
public class RequestExecutorImplTest {

	private static final String SOME_URL = "someUrl";
	private static final String SOME_USER_AGENT = "someUserAgent";
	private static final String SOME_API_KEY = "someApiKey";

	private static final String SOME_USER = "someUser";
	private static final String SOME_PASS = "somePass";

	private static final String APIKEY_HEADER_NAME = "apikey";
	private static final String AUTHORIZATION_HEADER_NAME = "Authorization";

	@Mock
	private URLHelper urlHelperMock;
	@Mock
	private HttpClientProvider httpClientProviderMock;
	@Mock
	private HttpClient httpClientMock;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private HttpResponse httpResponseMock;
	@Mock
	private ClientConnectionManager clientConnectionManagerMock;

	@InjectMocks
	private RequestExecutorImpl requestExecutor;

	@Before
	public void setUp() throws Exception {

		mockUrlHelperMock();
		mockHttpClientProvider();

	}

	@Test
	public void givenApiKeyBasedAuthenticationWhenExecuteRequestThenApiKeyHeaderIsPresent() throws Exception {

		// setup
		requestExecutor.setAuthorization(SOME_API_KEY);

		// execute
		requestExecutor.executeRequest(SOME_URL, GET);

		// verify
		HttpGet httpGet = getHttpGetArgumentCaptor().getValue();

		assertThat(httpGet.getFirstHeader(APIKEY_HEADER_NAME).getValue(), is(SOME_API_KEY));
		assertThat(httpGet.getFirstHeader(AUTHORIZATION_HEADER_NAME), is(nullValue()));
	}

	@Test
	public void givenBasicAuthenticationWhenExecuteRequestThenAuthorizationHeaderIsPresent() throws Exception {

		// setup
		requestExecutor.setAuthorization(SOME_USER, SOME_PASS);

		// execute
		requestExecutor.executeRequest(SOME_URL, GET);

		// verify
		HttpGet httpGet = getHttpGetArgumentCaptor().getValue();

		assertThat(httpGet.getFirstHeader(APIKEY_HEADER_NAME), is(nullValue()));
		assertThat(httpGet.getFirstHeader(AUTHORIZATION_HEADER_NAME).getValue(), is(getBasicAuthHeader()));
	}

	private ArgumentCaptor<HttpGet> getHttpGetArgumentCaptor() throws IOException {

		ArgumentCaptor<HttpGet> httpGetArgumentCaptor = ArgumentCaptor.forClass(HttpGet.class);
		verify(httpClientMock).execute(httpGetArgumentCaptor.capture());

		return httpGetArgumentCaptor;
	}

	private String getBasicAuthHeader() {
		String userNamePassword = SOME_USER + ":" + SOME_PASS;

		return "Basic " + new String(Base64.encodeBase64(userNamePassword.getBytes()));
	}

	private void mockHttpClientProvider() throws IOException {

		StringEntity entity = new StringEntity("some data");

		when(httpClientMock.getConnectionManager()).thenReturn(clientConnectionManagerMock);
		when(httpClientMock.execute(any(HttpGet.class))).thenReturn(httpResponseMock);
		when(httpResponseMock.getStatusLine().getStatusCode()).thenReturn(HTTP_OK);
		when(httpResponseMock.getEntity()).thenReturn(entity);

		when(httpClientProviderMock.createCustomHttpClient()).thenReturn(httpClientMock);
	}

	private void mockUrlHelperMock() {
		when(urlHelperMock.addJsonPaginationParams(SOME_URL, GET, WWW_FORM, null)).thenReturn(SOME_URL);
		when(urlHelperMock.addDetailsParam(SOME_URL, GET, WWW_FORM, null)).thenReturn(SOME_URL);
		when(urlHelperMock.userAgentHeaderValue()).thenReturn(SOME_USER_AGENT);
	}
}