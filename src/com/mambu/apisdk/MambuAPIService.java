package com.mambu.apisdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.Domain;
import com.mambu.apisdk.model.Password;
import com.mambu.apisdk.model.Username;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.apisdk.util.URLHelper;

/**
 * Mambu service to call the APIs
 * 
 * @author edanilkis
 * 
 */
@Singleton
public class MambuAPIService {

	private RequestExecutor executor;
	private URLHelper urlHelper;

	/**
	 * Creates a Mambu API Service class
	 * 
	 * @param username
	 *            username to connect with to the apis
	 * @param password
	 *            password to connect with to the apis
	 * @param domainName
	 *            based domain name for the tenant (eg: mytenant.mambu.com)
	 */
	@Inject
	public MambuAPIService(@Domain String domainName, @Username String username, @Password String password,
			RequestExecutor executor, URLHelper urlHelper) {

		this.urlHelper = urlHelper;
		this.executor = executor;

		executor.setAuthorization(username, password);
	}

	/**
	 * Executes the request for a given url string using a specified method See more info here:
	 * http://stackoverflow.com/questions/2793150/how-to-use-java -net-urlconnection-to-fire-and-handle-http-requests
	 * 
	 * 
	 * @param urlString
	 * @param method
	 * 
	 * @return HTTP response String. The response string for the http request. It is either an application specific
	 *         response (with the content being specific for each request) or an error response for the http request.
	 * 
	 * @throws MambuApiException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public String executeRequest(String urlString, Method method) throws MambuApiException {

		return executor.executeRequest(urlString, method);
	}

	/**
	 * Executes the request for a given url and some parameters using a specified method See more info here:
	 * http://stackoverflow.com/questions/2793150/how-to-use-java -net-urlconnection-to-fire-and-handle-http-requests
	 * 
	 * @param urlString
	 * @param params
	 * @param method
	 * 
	 * @return String
	 * 
	 * @throws MambuApiException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public String executeRequest(String urlString, ParamsMap params, Method method) throws MambuApiException {

		return executor.executeRequest(urlString, params, method);
	}

	/**
	 * Executes the request for a given url, some parameters using a specified method and also a specified contentType
	 * format. See more info here: http://stackoverflow.com/questions/2793150/how-to-use-java
	 * -net-urlconnection-to-fire-and-handle-http-requests
	 * 
	 * @param urlString
	 * @param params
	 * @param method
	 * @param contentTypeFormat
	 * 
	 * @return String
	 * 
	 * @throws MambuApiException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public String executeRequest(String urlString, ParamsMap params, Method method,
			RequestExecutor.ContentType contentTypeFormat) throws MambuApiException {

		return executor.executeRequest(urlString, params, method, contentTypeFormat);
	}

	/**
	 * Delegates the execution to a RequestExecutor. Used for requests that requires downloading content through the API
	 * (like zip archives). It gets the InputStream from the response and converts it into a ByteArrayOutputStream for
	 * laster use.
	 * 
	 * @param urlString
	 *            The URL string
	 * @param params
	 *            The parameters map
	 * @param method
	 *            The HTTP method
	 * @param contentTypeFormat
	 *            The request content type
	 * 
	 * @return ByteArrayOutputStream of the response content.
	 * 
	 * @throws MambuApiException
	 */
	public ByteArrayOutputStream executeRequest(String urlString, ParamsMap params, ApiDefinition apiDefinition)
			throws MambuApiException {

		return executor.executeRequest(urlString, params, apiDefinition);
	}

	/**
	 * Executes the request for a given url (with parameters) using a specified method and specified contentType format.
	 * See more info here: http://stackoverflow.com/questions/2793150/how-to-use-java
	 * -net-urlconnection-to-fire-and-handle-http-requests
	 * 
	 * @param urlString
	 * @param method
	 * @param contentTypeFormat
	 * 
	 * @return String
	 * 
	 * @throws MambuApiException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public String executeRequest(String urlString, Method method, RequestExecutor.ContentType contentTypeFormat)
			throws MambuApiException {

		return executor.executeRequest(urlString, method, contentTypeFormat);
	}

	/**
	 * Creates the URL for the request executor
	 * 
	 * @param details
	 * 
	 * @return String
	 */
	public String createUrl(String details) {

		return urlHelper.createUrl(details);
	}

	/**
	 * Returns an url containing limit/offset params.
	 * 
	 * @param details
	 *            URL details
	 * @param offset
	 *            offset to start from
	 * @param limit
	 *            max. number of entries
	 * 
	 * @return URL for a limited query
	 */
	public String createUrl(String details, int offset, int limit) {

		String url = this.createUrl(details);

		if (limit != -1)
			url += (url.contains("?") ? "&" : "?") + "limit=" + limit;
		if (offset != -1)
			url += (url.contains("?") ? "&" : "?") + "offset=" + offset;

		return (url);
	}
}
