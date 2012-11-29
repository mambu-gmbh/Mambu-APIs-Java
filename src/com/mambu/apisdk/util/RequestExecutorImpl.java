package com.mambu.apisdk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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
			throw new MambuApiException(e);
		} catch (IOException e) {
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

		// put the params in the request body
		if (params != null) {
			OutputStream output = connection.getOutputStream();
			output.write(params.getURLString().getBytes(charset));
		}

		int status = ((HttpURLConnection) connection).getResponseCode();
		InputStream content;

		// ensure it's an ok response or a successfully created one
		if (status != 200 && status != 201) {
			errorCode = status;
			// if there was an error, read the error message
			content = connection.getErrorStream();
		} else {
			content = connection.getInputStream();
		}

		response = readStream(content);

		// check if we hit an error
		if (errorCode != null) {
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

		if (params != null) {
			urlString = urlHelper.createUrlWithParams(urlString, params);
		}

		URL url = new URL(urlString);

		// set up the connection
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(Method.GET.toString());
		connection.setDoOutput(true);
		connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

		int status = ((HttpURLConnection) connection).getResponseCode();
		InputStream content;

		// ensure it's an ok response
		if (status != 200) {
			errorCode = status;
			// if there was an error, read the error message
			content = connection.getErrorStream();
		} else {
			content = connection.getInputStream();
		}

		response = readStream(content);

		// check if we hit an error
		if (errorCode != null) {
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
