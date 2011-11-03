package com.mambu.apisdk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

import com.mambu.apisdk.exception.MambuApiException;

/**
 * Implementation of executing url requests with basic authorization
 * 
 * @author edanilkis
 * 
 */
public class RequestExecutorImpl implements RequestExecutor {

	private String encodedAuthorization;

	/**
	 * Executes the request as per the interface specification
	 */
	@Override
	public String executeRequest(String urlString, Method method) throws MalformedURLException, IOException, MambuApiException {

		String response = "";
		Integer errorCode = null;

		// create the url
		URL url = new URL(urlString);

		// set up the connection
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method.toString());
		connection.setDoOutput(true);
		
		//add the authorization
		connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

		// get the status
		int status = ((HttpURLConnection) connection).getResponseCode();

		// setup the content
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
	 * Reads a stream as
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
