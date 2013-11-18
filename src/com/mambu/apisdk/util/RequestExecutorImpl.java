package com.mambu.apisdk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

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
	private final String UTF8_charset = HTTP.UTF_8;
	private final String wwwFormUrlEncodedContentType = "application/x-www-form-urlencoded; charset=UTF-8";

	// TODO: add charset when https://mambucom.jira.com/browse/MBU-4137 is fixed
	private final String jsonContentType = "application/json"; // "application/json; charset=UTF-8";

	private final String APPLICATION_KEY = APIData.APPLICATION_KEY; // as per JIRA issue MBU-3236

	private final static Logger LOGGER = Logger.getLogger(RequestExecutorImpl.class.getName());

	@Inject
	public RequestExecutorImpl(URLHelper urlHelper) {
		this.urlHelper = urlHelper;
	}

	// Without params and with default contentType (ContentType.WWW_FORM)
	@Override
	public String executeRequest(String urlString, Method method) throws MambuApiException {
		// invoke with default contentType (WWW_FORM)
		return executeRequest(urlString, null, method, ContentType.WWW_FORM);
	}

	// With params and with default contentType (ContentType.WWW_FORM)
	@Override
	public String executeRequest(String urlString, ParamsMap params, Method method) throws MambuApiException {
		// invoke with default contentType (WWW_FORM)
		return executeRequest(urlString, params, method, ContentType.WWW_FORM);
	}

	// With specifying the Content Type but without params
	/*
	 * Use this method to specify the requests's Content Type (must be used if content type is not WWW_FORM, for example
	 * for the json content type)
	 */
	@Override
	public String executeRequest(String urlString, Method method, ContentType contentTypeFormat)
			throws MambuApiException {
		// No params version
		return executeRequest(urlString, null, method, contentTypeFormat);
	}

	/*
	 * Use this method to specify the requests's Content Type (must be used if content type is not WWW_FORM, for example
	 * for the json content type)
	 */
	@Override
	public String executeRequest(String urlString, ParamsMap params, Method method, ContentType contentTypeFormat)
			throws MambuApiException {

		// Add 'Application Key', if it was set by the application
		// Mambu may handle API requests differently for different Application Keys

		String applicationKey = MambuAPIFactory.getApplicationKey();
		if (applicationKey != null) {
			// add application key to the params map
			if (params == null)
				params = new ParamsMap();
			params.addParam(APPLICATION_KEY, applicationKey);

			// Log the App key (the first 3 and last 3 chars only)
			final int keyLength = applicationKey.length();
			final int printLength = 3;
			// Mambu App Keys are very long but just to prevent any errors need to ensure there is enough to print
			if (keyLength >= printLength)
				LOGGER.info("Added Application key=" + applicationKey.substring(0, printLength) + "..."
						+ applicationKey.substring(keyLength - printLength, keyLength));
		}

		String response = "";
		try {
			switch (method) {
			case GET:
				response = executeGetRequest(urlString, params);
				break;
			case POST:
				response = executePostRequest(urlString, params, contentTypeFormat);
				break;
			case DELETE:
				response = executeDeleteRequest(urlString, params);
				break;
			default:
				throw new IllegalArgumentException("Only methods GET, POST and DELETE are supported, not "
						+ method.name() + ".");
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
	private String executePostRequest(String urlString, ParamsMap params, ContentType contentTypeFormat)
			throws MalformedURLException, IOException, MambuApiException {

		// Get properly formatted ContentType
		final String contentType = getFormattedContentTypeString(contentTypeFormat);

		String response = "";
		Integer errorCode = null;

		HttpParams httpParameters = new BasicHttpParams();
		HttpClient httpClient = new DefaultHttpClient(httpParameters);
		HttpPost httpPost = new HttpPost(urlString);
		httpPost.setHeader("Content-Type", contentType);
		httpPost.setHeader("Authorization", "Basic " + encodedAuthorization);

		LOGGER.info("POST With ContentType=" + contentType + " URL=" + urlString);

		if (params != null && params.size() > 0) {
			switch (contentTypeFormat) {

			case WWW_FORM:
				// convert parms to a list for HttpEntity
				List<NameValuePair> httpParams = getListFromParams(params);
				// use UTF-8 to encode

				HttpEntity postEntity = new UrlEncodedFormEntity(httpParams, UTF8_charset);

				httpPost.setEntity(postEntity);

				LOGGER.info("WWW_FORM: Params as URL string=" + params.getURLString());
				break;

			case JSON:
				// Parameter (json string) is expected as JSON_OBJECT parameter name
				final String jsonString = params.get(APIData.JSON_OBJECT);

				// Add APPKEY to jsonString (see MBU-3892, implemented in 3.3 release)
				String jsonWithAppKey = addAppKeyToJson(jsonString, params);

				// Format jsonEntity
				StringEntity jsonEntity = new StringEntity(jsonWithAppKey, UTF8_charset);

				httpPost.setEntity(jsonEntity);

				LOGGER.info("JSON: jsonString=" + jsonWithAppKey);
				break;
			}

		}

		// execute
		HttpResponse httpResponse = httpClient.execute(httpPost);

		// get status code
		int status = httpResponse.getStatusLine().getStatusCode();

		InputStream content = null;
		HttpEntity entity = httpResponse.getEntity();

		// if there is an entity - get content
		if (entity != null) {

			content = entity.getContent();
			if (content != null)
				response = readStream(content);
		}

		if (status != HttpURLConnection.HTTP_OK && status != HttpURLConnection.HTTP_CREATED) {

			errorCode = status;
			LOGGER.info("Error status=" + status + " Error response=" + response);
		}

		if (errorCode == null)
			// For logging only: log successful response
			LOGGER.info("Status=" + status + "\nResponse=" + response);

		// check if we hit an error
		if (errorCode != null) {
			// pass to MambuApiException the content that goes with the error code
			LOGGER.warning("Creating exception, error code=" + errorCode + " response=" + response);
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
			urlString = new String((urlHelper.createUrlWithParams(urlString, params)));
		}
		LOGGER.info("GET with URL with params=" + urlString);

		HttpParams httpParameters = new BasicHttpParams();

		HttpClient httpClient = new DefaultHttpClient(httpParameters);

		HttpGet httpGet = new HttpGet(urlString);
		// add Authorozation header
		httpGet.setHeader("Authorization", "Basic " + encodedAuthorization);
		// setHeader("Content-Type") not need for GET requests

		// execute
		HttpResponse httpResponse = httpClient.execute(httpGet);

		// get status
		int status = httpResponse.getStatusLine().getStatusCode();
		Header responseContentType = httpResponse.getFirstHeader("Content-Type");
		String contentTypeStr = "";
		if (responseContentType != null) {
			contentTypeStr = responseContentType.getValue();
			LOGGER.info("contentType in Response=" + contentTypeStr);
		} else {
			LOGGER.info("response is NULL, so no contentType");
		}

		InputStream content = null;
		// Get the response Entity
		HttpEntity entity = httpResponse.getEntity();

		if (entity != null) {
			content = entity.getContent();
			if (content != null)
				response = readStream(content);
		}
		// if status is not Ok - set error code
		if (status != HttpURLConnection.HTTP_OK) {
			errorCode = status;
			LOGGER.info("Error status=" + status + " Error response=" + response);
		}

		if (errorCode == null)
			// For logging only: log successful response
			LOGGER.info("Status=" + status + "\nResponse=" + response);

		// check if we hit an error
		if (errorCode != null) {
			// pass to MambuApiException the content that goes with the error code
			LOGGER.warning("Creating exception, error code=" + errorCode + " response=" + response);
			throw new MambuApiException(errorCode, response);
		}

		return response;
	}
	/***
	 * Execute a DELETE request as per the interface specification
	 * 
	 * @param urlString
	 * 
	 * @param params
	 *            ParamsMap with parameters
	 */
	private String executeDeleteRequest(String urlString, ParamsMap params) throws MalformedURLException, IOException,
			MambuApiException {
		String response = "";
		Integer errorCode = null;

		if (params != null && params.size() > 0) {
			urlString = new String((urlHelper.createUrlWithParams(urlString, params)));
		}

		LOGGER.info("DELETE with URL with params=" + urlString);

		HttpParams httpParameters = new BasicHttpParams();

		HttpClient httpClient = new DefaultHttpClient(httpParameters);

		HttpDelete httpDelete = new HttpDelete(urlString);
		httpDelete.setHeader("Authorization", "Basic " + encodedAuthorization);

		// execute
		HttpResponse httpResponse = httpClient.execute(httpDelete);

		// get status
		int status = httpResponse.getStatusLine().getStatusCode();

		InputStream content = null;
		// Get the response Entity
		HttpEntity entity = httpResponse.getEntity();

		if (entity != null) {
			content = entity.getContent();
			if (content != null)
				response = readStream(content);
		}
		// if status is not Ok - set error code
		if (status != HttpURLConnection.HTTP_OK) {
			errorCode = status;
			LOGGER.info("Error status=" + status + " Error response=" + response);
		}

		if (errorCode == null)
			// For logging only: log successful response
			LOGGER.info("Status=" + status + "\nResponse=" + response);

		// check if we hit an error
		if (errorCode != null) {
			// pass to MambuApiException the content that goes with the error code
			LOGGER.warning("Creating exception, error code=" + errorCode + " response=" + response);
			throw new MambuApiException(errorCode, response);
		}

		return response;
	}

	/**
	 * Reads a stream into a String
	 * 
	 * @param content
	 * 
	 * @return
	 * 
	 * @throws IOException
	 */
	private String readStream(InputStream content) throws IOException {

		String response = "";

		// read the response content
		BufferedReader in = new BufferedReader(new InputStreamReader(content, UTF8_charset));
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

	/**
	 * Convert Params Map into a List<NameValuePair> for HttpPpost
	 * 
	 * @param params
	 * 
	 * @return List<NameValuePair>
	 * 
	 * @throws
	 */
	private List<NameValuePair> getListFromParams(ParamsMap params) {

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.size());

		for (Map.Entry<String, String> entry : params.entrySet()) {
			// only put the parameter in the URL if its value is not null
			if (entry.getValue() != null) {
				nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));

			}
		}
		return nameValuePairs;
	}

	/**
	 * Get the formatted content type string for the content type enum value
	 */
	private String getFormattedContentTypeString(ContentType contentTypeFormat) {
		switch (contentTypeFormat) {
		case WWW_FORM:
			return wwwFormUrlEncodedContentType;
		case JSON:
			return jsonContentType;
		default:
			return wwwFormUrlEncodedContentType;
		}
	}

	/**
	 * Add json formatted appKey value to the original json string
	 * 
	 * @param jsonString
	 *            original json string
	 * 
	 * @param params
	 *            the ParamsMap containing the appKey value (optionally)
	 * 
	 * @return jsonStringWithAppKey json string with appKey added
	 */
	private String addAppKeyToJson(String jsonString, ParamsMap params) {
		if (params == null)
			return jsonString;

		String appKey = params.get(APPLICATION_KEY);
		if (appKey == null || appKey.length() == 0)
			return jsonString;

		// First Compile the following string: {"appKey":"appKeyValue",
		// This formatted appKey string will be placed in front (instead of the first "{") in the original json
		final String appKeyStart = "{\"" + APPLICATION_KEY + "\":\"";
		final String appKeyEnd = "\", ";
		final String appKeyString = appKeyStart.concat(appKey).concat(appKeyEnd);

		// Put this formatted appKeyString in place of the first "{"
		final int beginIndex = jsonString.indexOf("{");
		final String jsonWithAppKey = appKeyString.concat(jsonString.substring(beginIndex + 1));

		return jsonWithAppKey;
	}
}
