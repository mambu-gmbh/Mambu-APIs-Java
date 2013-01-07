package com.mambu.apisdk;

import java.io.IOException;
import java.net.MalformedURLException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.model.Domain;
import com.mambu.apisdk.model.Password;
import com.mambu.apisdk.model.Username;
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
	 * @throws MambuApiException
	 */
	@Inject
	public MambuAPIService(@Domain String domainName, @Username String username, @Password String password,
			RequestExecutor executor, URLHelper urlHelper) throws MambuApiException {

		this.urlHelper = urlHelper;
		this.executor = executor;

		executor.setAuthorization(username, password);
	}

	/**
	 * Executes the request for a given url string using a specified method See more info here:
	 * http://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
	 * 
	 * 
	 * @param urlString
	 * @param method
	 * @return
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
	 * @param paramsMap
	 * @param method
	 * @return
	 * @throws MambuApiException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public String executeRequest(String urlString, ParamsMap params, Method method) throws MambuApiException {

		return executor.executeRequest(urlString, params, method);
	}
	/**
	 * Creates the URL for the request executor
	 * 
	 * @param tenant
	 * @return
	 */
	public String createUrl(String details) {

		return urlHelper.createUrl(details);
	}
}
