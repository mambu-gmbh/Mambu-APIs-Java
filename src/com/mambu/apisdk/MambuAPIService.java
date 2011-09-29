package com.mambu.apisdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import sun.misc.BASE64Encoder;

import com.mambu.apisdk.exception.MambuApiException;

/**
 * Mambu service to call the APIs
 * 
 * @author edanilkis
 * 
 */
public class MambuAPIService {

	private String domainName;
	private String protocol = "https";
	private String encodedAuthorization;

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
	MambuAPIService(String username, String password, String domainName) {
		this.domainName = domainName;

		String userNamePassword = username + ":" + password;
		BASE64Encoder enc = new BASE64Encoder();
		encodedAuthorization = enc.encode(userNamePassword.getBytes());

	}

	/**
	 * Requests a client which is then returned as the json response string
	 * 
	 * @param clientId
	 * @return the json response String
	 * @throws MambuApiException
	 */
	public String getClient(String clientId) throws MambuApiException {

		// create the api call
		String urlString = new String(createUrl("client" + "/" + clientId));
		String method = "POST";
		String response = executeRequest(urlString, method);

		return response;

	}

	/**
	 * Executes the request for a given url string using a specified method See
	 * more info here:
	 * http://stackoverflow.com/questions/2793150/how-to-use-java
	 * -net-urlconnection-to-fire-and-handle-http-requests
	 * 
	 * @param urlString
	 * @param method
	 * @return
	 * @throws MambuApiException
	 */
	private String executeRequest(String urlString, String method) throws MambuApiException {
		
		String response = "";
		Integer errorCode = null;
		
		try {
			//create the url
			URL url = new URL(urlString);

			// set up the connection
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod(method);
			connection.setDoOutput(true);
			connection.setRequestProperty("Authorization", "Basic "
					+ encodedAuthorization);

			// get the status
			int status = ((HttpURLConnection) connection).getResponseCode();

			// setup the content
			InputStream content;
			
			// ensure it's an ok response
			if (status != 200) {
				errorCode = status;
				//if there was an error, read the error message
				content = connection.getErrorStream();
			} else {
				content = connection.getInputStream();
			}

			//read the response content
			BufferedReader in = new BufferedReader(new InputStreamReader(
					content));
			String line;
			while ((line = in.readLine()) != null) {
				response += line;
			}
			
			//check if we hit an error
			if (errorCode != null) {
				throw new MambuApiException(errorCode, response);
			}

		} catch (MalformedURLException e) {
			throw new MambuApiException(e);
		} catch (IOException e) {
			throw new MambuApiException(e);
		}
		
		return response;
	}

	/**
	 * Creates the URL for the cron servlet
	 * 
	 * @param tenant
	 * @return
	 */
	protected String createUrl(String details) {
		details = details == null ? "" : details;
		return protocol + "://" + domainName + "/api/" + details;
	}

	/**
	 * Sets the protocol to use (eg: http or https)
	 * 
	 * @param protocol
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Gets the current protocol we are communicating over
	 * 
	 * @return
	 */
	public String getProtocol() {
		return protocol;
	}

}
