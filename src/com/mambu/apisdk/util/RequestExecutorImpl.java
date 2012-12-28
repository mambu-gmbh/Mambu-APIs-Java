package com.mambu.apisdk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;

/**
 * Implementation of executing url requests with basic authorization
 * 
 * @author edanilkis
 * 
 */
@Singleton
public class RequestExecutorImpl implements RequestExecutor {

	private URLHelper urlHelper;
	private String encodedAuthorization;
	private String charset = "UTF-8";
	
	private final String APPLICATION_KEY = "appkey"; //as per JIRA issue MBU-3236

	private final static Logger LOGGER = Logger.getLogger(RequestExecutorImpl.class.getName());

	@Inject
	public RequestExecutorImpl(URLHelper urlHelper) {
		this.urlHelper = urlHelper;
	}

	@Override
	public String executeRequest(String urlString, Method method) throws MambuApiException {
		return executeRequest(urlString, null, method);
	}

	@Override
	public String executeRequest(String urlString, ParamsMap params, Method method) throws MambuApiException {
		// Add 'Application Key', if it was set by the application
		// Mambu may handle API requests differently for different Application Keys

		String applicationKey = MambuAPIFactory.getApplicationKey();
		if (applicationKey != null) {
			// add application key to the params map
			if (params == null)
				params = new ParamsMap();
			params.addParam(APPLICATION_KEY, applicationKey);
			LOGGER.info("Added Application key=" + applicationKey);
		}

		String response = "";
		try {
			switch (method) {
			case GET:				
				response = executeGetRequest(urlString, params);
				break;
			case POST:
				response = executePostRequest(urlString, params);
				break;
			}
		} catch (MalformedURLException e) {
			LOGGER.severe("MalformedURLException: " + e.getMessage());
			throw new MambuApiException(e);
		} catch (IOException e) {
			LOGGER.warning("IOException: message= " + e.getMessage());
			throw new MambuApiException(e);
		}
		return response;
	}

	/**
	 * Executes a POST request as per the interface specification
	 */
	private String executePostRequest(String urlString, ParamsMap params) throws MalformedURLException, IOException,
			MambuApiException {

		String response = "";
		Integer errorCode = null;

		URL url = new URL(urlString);

		// set up the connection
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(Method.POST.name());
		connection.setDoOutput(true);

		connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

		LOGGER.info("URL=" + urlString + "Author=" + encodedAuthorization);
		// put the params in the request body
		if (params != null && params.size() > 0) {
			LOGGER.info(" Params string:" + params.getURLString());
			OutputStream output = connection.getOutputStream();
			output.write(params.getURLString().getBytes(charset));
		}

		int status = ((HttpURLConnection) connection).getResponseCode();

		InputStream content;

		// ensure it's an ok response or a successfully created one
		if (status != HttpURLConnection.HTTP_OK && status != HttpURLConnection.HTTP_CREATED) {
			LOGGER.info("Error status=" + status);
			errorCode = status;
			// if there was an error, read the error message
			content = connection.getErrorStream();
		} else {
			LOGGER.info("Status OK=" + status);
			content = connection.getInputStream();
		}
		if (content != null) {
			response = readStream(content);
			if (response != null)
				LOGGER.info("Response=" + response);
		}
		// check if we hit an error
		if (errorCode != null) {
			LOGGER.warning("Throwing exception, error code=" + errorCode + " response=" + response);
			throw new MambuApiException(errorCode, response);
		}

		return response;
	}

	/***
	 * Execute a GET request as per the interface specification
	 * 
	 * @param urlString
	 */
	private String executeGetRequest(String urlString, ParamsMap params) throws MalformedURLException, IOException,
			MambuApiException {
		String response = "";
		Integer errorCode = null;

		if (params != null && params.size() > 0) {
			urlString = urlHelper.createUrlWithParams(urlString, params);
		}
		LOGGER.info("Url string with params from Helper=" + urlString);
		URL url = new URL(urlString);

		// set up the connection
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(Method.GET.toString());
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

		int status = ((HttpURLConnection) connection).getResponseCode();

		InputStream content;

		// ensure it's an ok response
		if (status != HttpURLConnection.HTTP_OK) {
			LOGGER.info("Error status=" + status);
			errorCode = status;
			// if there was an error, read the error message
			content = connection.getErrorStream();
		} else {
			LOGGER.info("Status OK=" + status);
			content = connection.getInputStream();
		}

		// check for null stream and read it
		if (content != null) {
			response = readStream(content);
			LOGGER.info("Response=" + response);
		}

		// check if we hit an error
		if (errorCode != null) {
			LOGGER.warning("Throwing exception, error code=" + errorCode + " response=" + response);
			throw new MambuApiException(errorCode, response);
		}

		return response;
	}

	/**
	 * Reads a stream into a String
	 * 
	 * @param content
	 * @return
	 * @throws IOException
	 */
	private String readStream(InputStream content) throws IOException {
		String response = "";
		// read the response content
		BufferedReader in = new BufferedReader(new InputStreamReader(content));
		String line;
		while ((line = in.readLine()) != null) {
			response += line;
		}
		return response;
	}

	@Override
	public void setAuthorization(String username, String password) {
		// encode the username and password
		String userNamePassword = username + ":" + password;
		encodedAuthorization = new String(Base64.encodeBase64(userNamePassword.getBytes()));

	}

}
