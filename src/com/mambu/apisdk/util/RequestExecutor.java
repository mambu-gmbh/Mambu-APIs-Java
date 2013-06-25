package com.mambu.apisdk.util;

import com.mambu.apisdk.exception.MambuApiException;

/**
 * Interface for executing url requests
 * 
 * @author edanilkis
 * 
 */
public interface RequestExecutor {

	public enum Method {
		GET, POST, PUT, DELETE, POST_JSON
	}

	/***
	 * Set the credentials required for the server authorization
	 * 
	 * @param username
	 * @param password
	 */
	public void setAuthorization(String username, String password);

	/**
	 * Executes a request with given url and request method
	 * 
	 * @param urlString
	 *            the url to execute on. eg: https://demo.mambu.com/api/clients
	 * @return
	 * @throws MambuApiException
	 */
	public String executeRequest(String urlString, Method method) throws MambuApiException;

	/**
	 * Executes a request with given url, some params and a request method
	 * 
	 * @param urlString
	 *            the url to execute on. eg: https://demo.mambu.com/api/clients
	 * @param paramsMap
	 *            the parameters eg: {clientId=id}
	 * @return
	 * @throws MambuApiException
	 */
	public String executeRequest(String urlString, ParamsMap params, Method method) throws MambuApiException;

}
