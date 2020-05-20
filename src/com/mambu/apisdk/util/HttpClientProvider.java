package com.mambu.apisdk.util;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.HttpClients;

import com.google.inject.Singleton;

/**
 * @author cezarrom
 */
@Singleton
public class HttpClientProvider {

	private static final String TLS_V1_2 = "TLSv1.2";


	/**
	 * Creates an httpClient used to run the API calls
	 *
	 * @return newly created httpClient
	 */
	public HttpClient createCustomHttpClient() {

		HttpClient httpClient = HttpClients.custom()
				// set cookies validation on ignore
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build())
				.setSSLSocketFactory(createSslConnectionSocketFactory())
				.build();

		return httpClient;
	}

	/**
	 * Creates custom SSLConnectionSocketFactory and set it to use only the TLSv1.2 as supported protocol
	 *
	 * @return newly created SSLConnectionSocketFactory
	 */
	private SSLConnectionSocketFactory createSslConnectionSocketFactory() {

		SSLConnectionSocketFactory sslConnFactory = new
				SSLConnectionSocketFactory(SSLContexts.createDefault(),
				new String[]{TLS_V1_2}, null,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		return sslConnFactory;
	}
}
