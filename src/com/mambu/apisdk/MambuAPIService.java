package com.mambu.apisdk;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.HashMap;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mambu.accounting.shared.model.GLAccount;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.RequestExecutor;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.core.shared.model.Currency;
import com.mambu.intelligence.shared.model.Intelligence.Indicator;

/**
 * Mambu service to call the APIs
 * 
 * @author edanilkis
 * 
 */
public class MambuAPIService {

	private String domainName;
	private String protocol = "http";
	private RequestExecutor executor;

	// creat the gson deserializer
	private static GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

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
	public MambuAPIService(String username, String password, String domainName, RequestExecutor executor)
			throws MambuApiException {

		this.domainName = domainName;
		this.executor = executor;

		executor.setAuthorization(username, password);

	}

	/**
	 * Requests a client by their Mambu ID
	 * 
	 * @param accountId
	 * @return the Mambu client model
	 * @throws MambuApiException
	 */
	public Client getClient(String clientId) throws MambuApiException {

		// create the api call
		String urlString = new String(createUrl("clients" + "/" + clientId));
		String jsonResposne = executeRequest(urlString, Method.GET);
		Client clientResult = gsonBuilder.create().fromJson(jsonResposne, Client.class);
		return clientResult;

	}

	/**
	 * Requests a gl account by its gl code
	 * 
	 * @param glCode
	 * @return the Mambu gl account
	 * @throws MambuApiException
	 */
	public GLAccount getGLAccount(String glCode) throws MambuApiException {

		// create the api call
		String url = "glaccounts" + "/" + glCode;
		GLAccount glAccount = getGLAccountResponse(url);

		return glAccount;

	}

	/**
	 * Requests the organization currency
	 * 
	 * @return the Mambu gl account
	 * @throws MambuApiException
	 */
	public Currency getCurrency() throws MambuApiException {

		// create the api call
		String url = "currencies";
		String urlString = new String(createUrl(url));
		String jsonResponse = executeRequest(urlString, Method.GET);

		// conver to collection
		Currency[] currencies = gsonBuilder.create().fromJson(jsonResponse, Currency[].class);

		if (currencies != null && currencies.length > 0) {
			return currencies[0];
		} else {
			return null;
		}

	}

	/**
	 * Requests a gl account by its gl code with a balance over a certain date
	 * range
	 * 
	 * @param glCode
	 * @return the Mambu gl account
	 * @throws MambuApiException
	 */
	public GLAccount getGLAccount(String glCode, String fromDate, String toDate) throws MambuApiException {

		// create the api call
		String url = "glaccounts" + "/" + glCode + "?" + "from=" + fromDate + "&to=" + toDate;

		GLAccount glAccount = getGLAccountResponse(url);
		return glAccount;

	}

	/**
	 * Returns the gl account response witha given url & parameters
	 * 
	 * @param url
	 * @return
	 * @throws MambuApiException
	 */
	private GLAccount getGLAccountResponse(String url) throws MambuApiException {
		String urlString = new String(createUrl(url));
		String jsonResponse = executeRequest(urlString, Method.GET);
		GLAccount glAccount = gsonBuilder.create().fromJson(jsonResponse, GLAccount.class);
		return glAccount;
	}

	/**
	 * Requests a mambu indicator value as a BigDecimal value
	 * 
	 * @param glCode
	 * @return the big decimal indicator value
	 * @throws MambuApiException
	 */
	public BigDecimal getIndicator(Indicator indicator) throws MambuApiException {

		// create the api call
		String urlString = new String(createUrl("indicators" + "/" + indicator.toString()));
		String jsonResponse = executeRequest(urlString, Method.GET);
		HashMap<String, String> result = gsonBuilder.create().fromJson(jsonResponse,
				new TypeToken<HashMap<String, String>>() {
				}.getType());
		if (result != null) {
			String resultString = result.get(indicator.toString());
			return new BigDecimal(resultString);
		} else {
			return null;
		}

	}

	/**
	 * Returns a client with their full details such as addresses, cusotm
	 * fields,
	 * 
	 * @param clientId
	 * @return
	 * @throws MambuApiException
	 */
	public ClientExpanded getClientDetails(String clientId) throws MambuApiException {
		// create the api call
		String urlString = new String(createUrl("clients" + "/" + clientId + "?fullDetails=true"));
		String jsonResposne = executeRequest(urlString, Method.GET);
		ClientExpanded clientResult = gsonBuilder.create().fromJson(jsonResposne, ClientExpanded.class);
		return clientResult;

	}

	/**
	 * Executes the request for a given url string using a specified method See
	 * more info here:
	 * http://stackoverflow.com/questions/2793150/how-to-use-java
	 * -net-urlconnection-to-fire-and-handle-http-requests
	 * 
	 * TODO: Use HTTPClient instead?
	 * 
	 * @param urlString
	 * @param method
	 * @return
	 * @throws MambuApiException
	 */
	private String executeRequest(String urlString, Method method) throws MambuApiException {

		String response = "";
		try {

			response = executor.executeRequest(urlString, method);

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
