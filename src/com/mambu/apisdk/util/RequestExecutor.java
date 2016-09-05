package com.mambu.apisdk.util;

import java.io.ByteArrayOutputStream;

import com.mambu.apisdk.exception.MambuApiException;

/**
 * Interface for executing url requests
 * 
 * @author edanilkis
 * 
 */
public interface RequestExecutor {

	public enum Method {
		GET, POST, PATCH, DELETE
	}

	// Content Type. Currently supported either "x-www-form-urlencoded"
	// (default) or json
	public enum ContentType {
		WWW_FORM, JSON
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
	 * 
	 * @return Mambu Response String
	 * 
	 * @throws MambuApiException
	 */
	public String executeRequest(String urlString, Method method) throws MambuApiException;

	/**
	 * Executes a request with given url, some params and a request method. Defaults the content Type to WWW_FORM
	 * ("application/x-www-form-urlencoded; charset=UTF-8")
	 * 
	 * @param urlString
	 *            the url to execute on. eg: https://demo.mambu.com/api/clients
	 * @param method
	 *            the method (e.g. GET or PUT)
	 * @param params
	 *            the parameters eg: {clientId=id}
	 * 
	 * @return Mambu Response String
	 * 
	 * @throws MambuApiException
	 */
	public String executeRequest(String urlString, ParamsMap params, Method method) throws MambuApiException;

	/**
	 * Executes a request with given url and specifying the contentType, with some params and a request method.
	 * 
	 * @param urlString
	 *            the url to execute on. eg: https://demo.mambu.com/api/clients
	 * @param method
	 *            the method (e.g. GET or PUT)
	 * @param params
	 *            the parameters eg: {clientId=id}, {JSON=jsonString}
	 * @param contentTypeFormat
	 *            enum for the content type string (e.g WWW_FORM or JSON: will be using respectively the
	 *            "application/x-www-form-urlencoded; charset=UTF-8" or "application/json; charset=UTF-8";)
	 * 
	 * @return Mambu Response String
	 * 
	 * @throws MambuApiException
	 */
	public String executeRequest(String urlString, ParamsMap params, Method method, ContentType contentTypeFormat)
			throws MambuApiException;

	/**
	 * Executes a request with given url and specifying the contentType, (without params) and a request method.
	 * 
	 * @param urlString
	 *            the url to execute on. eg: https://demo.mambu.com/api/clients
	 * @param method
	 *            the method (e.g. GET or PUT)
	 * @param contentTypeFormat
	 *            enum for the content type string (e.g WWW_FORM or JSON: will be using respectively the
	 *            "application/x-www-form-urlencoded; charset=UTF-8" or "application/json; charset=UTF-8";)
	 * 
	 * @return Mambu Response String
	 * 
	 * @throws MambuApiException
	 */
	public String executeRequest(String urlString, Method method, ContentType contentTypeFormat)
			throws MambuApiException;

	/**
	 * Executes a request for a given URL Executes a request with given url and specifying the apiDefinition and some
	 * parameters.
	 * 
	 * @param urlString
	 *            the url to execute on. eg: https://demo.mambu.com/api/database/backup/LATEST
	 * @param params
	 *            the parameters eg: {clientId=id}, {JSON=jsonString}
	 * @param apiDefinition
	 *            the ApiDefinition holding details like HTTP method, content type and API return type
	 * @return A ByteArrayOutputStream from the InputStream of the HTTP response.
	 */
	public ByteArrayOutputStream executeRequest(String urlString, ParamsMap params, ApiDefinition apiDefinition)
			throws MambuApiException;

}
