package com.mambu.apisdk.util;

import java.io.IOException;
import java.net.MalformedURLException;

import com.mambu.apisdk.exception.MambuApiException;

/**
 * Interface for executing url requests
 * 
 * @author edanilkis
 * 
 */
public interface RequestExecutor {

	public enum Method {
		GET,
		POST,
		PUT,
		DELETE,
	}
	
	public void setAuthorization(String username, String password);
	
	/**
	 * Executs a given url with a given request method
	 * 
	 * @param urlString
	 *            the url to execute on. eg: https://demo.mambu.com/api/clients
	 * @param method
	 *            the method for execution: one of GET, POST, PUT or Delete
	 * @return
	 * @throws MambuApiException
	 */
	public String executeRequest(String urlString, Method method) throws MalformedURLException, IOException, MambuApiException;
}
