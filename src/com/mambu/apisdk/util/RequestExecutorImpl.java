package com.mambu.apisdk.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

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
	private final static String UTF8_charset = StandardCharsets.UTF_8.name();
	private final static String wwwFormUrlEncodedContentType = "application/x-www-form-urlencoded; charset=UTF-8";

	// Added charset charset=UTF-8, MBU-4137 is now fixed
	private final static String jsonContentType = "application/json; charset=UTF-8";

	private final static String APPLICATION_KEY = APIData.APPLICATION_KEY; // as per JIRA issue MBU-3236

	private final static Logger LOGGER = Logger.getLogger(RequestExecutorImpl.class.getName());
	// Specify Logger Levels to be used for logging API request, response details as well as Mambu exceptions
	private final static Level requesLogLevel = Level.FINER; // Logging API Request level
	private final static Level responseLogLevel = Level.FINER; // Logging API Response level
	private final static Level exceptionLogLevel = Level.WARNING; // Logging Mambu exceptions level
	// Log curl template (equivalent to the actual API request) at FINEST level
	private final static Level curlRequestTemplateLogLevel = Level.FINEST;

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

		// Pagination parameters for POST with JSON are to be provided with the URL. See MBU-8975
		urlString = urlHelper.addJsonPaginationParams(urlString, method, contentTypeFormat, params);

		// Log API Request details
		logApiRequestDetails(urlString, params, method, contentTypeFormat);
		// Optionally log a template for the "curl" command as if it would be executed with the request specific API
		// params
		logCurlRequestDetails(urlString, params, method, contentTypeFormat);

		// Add 'Application Key', if it was set by the application
		// Mambu may handle API requests differently for different Application Keys

		params = addAppKeyToParams(params);

		HttpClient httpClient = createCustomHttpClient();
				
		String response = "";
		HttpResponse httpResponse = null;
		try {
			httpResponse = executeRequestByMethod(urlString, params, method, contentTypeFormat, httpClient,
					httpResponse);

			// Process response
			response = processResponse(httpResponse, method, contentTypeFormat, urlString, params);

		} catch (MalformedURLException e) {
			LOGGER.severe("MalformedURLException: " + e.getMessage());
			throw new MambuApiException(e);
		} catch (IOException e) {
			LOGGER.warning("IOException: message= " + e.getMessage());
			throw new MambuApiException(e);
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		
		return response;
	}
	

	/**
	 * Gets the InputStream from the response and converts it into a ByteArrayOutputStream for laster use. (i.e executes
	 * a request in order to download content and returns it as a ByteArrayOutputStream)
	 * 
	 * @param urlString
	 *            the url to execute on. eg: https://demo.mambu.com/api/database/backup/LATEST
	 * @param params
	 *            the parameters eg: {clientId=id}, {JSON=jsonString}
	 * @param apiDefinition
	 *            the ApiDefinition holding details like HTTP method, content type and API return type
	 * @return A ByteArrayOutputStream from the InputStream of the HTTP response.
	 */
	@Override
	public ByteArrayOutputStream executeRequest(String urlString, ParamsMap params, ApiDefinition apiDefinition)
			throws MambuApiException {

		Method method = apiDefinition.getMethod();
		ContentType contentTypeFormat = apiDefinition.getContentType();

		// Log API Request details
		logApiRequestDetails(urlString, params, method, contentTypeFormat);
		// Optionally log a template for the "curl" command as if it would be executed with the request specific API
		// params
		logCurlRequestDetails(urlString, params, method, contentTypeFormat);

		// Add 'Application Key', if it was set by the application
		// Mambu may handle API requests differently for different Application Keys
		params = addAppKeyToParams(params);

		HttpClient httpClient = createCustomHttpClient();
				
		ByteArrayOutputStream byteArrayOutputStreamResponse = null;
		HttpResponse httpResponse = null;
		try {
			httpResponse = executeRequestByMethod(urlString, params, method, contentTypeFormat, httpClient,
					httpResponse);

			// Process response
			byteArrayOutputStreamResponse = processInputStreamResponse(httpResponse, method, contentTypeFormat,
					urlString, params);

		} catch (MalformedURLException e) {
			LOGGER.severe("MalformedURLException: " + e.getMessage());
			throw new MambuApiException(e);
		} catch (IOException e) {
			LOGGER.warning("IOException: message= " + e.getMessage());
			throw new MambuApiException(e);
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

		return byteArrayOutputStreamResponse;
	}
	
	/**
	 * Creates an httpClient used to run the API calls
	 * 
	 * @return newly created httpClient
	 */
	private HttpClient createCustomHttpClient() {

		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.client.protocol.ResponseProcessCookies", "fatal");

		HttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom()
						.setCookieSpec(CookieSpecs.STANDARD).build())
			        .build();
		return httpClient;
	}

	/**
	 * Process and return the response to an HTTP request. Throw MambuApiException if request failed. Logs the response
	 * details. Currently used to download DB backup dumps.
	 * 
	 * @param httpResponse
	 *            HTTP response
	 * @param method
	 *            The HTTP method
	 * @param contentType
	 *            The content type
	 * @param urlString
	 *            The URL string for the HTTP request
	 * @param params
	 *            The parameters map
	 * @return A ByteArrayOutputStream for the response`s InputStream.
	 * @throws MambuApiException
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	private ByteArrayOutputStream processInputStreamResponse(HttpResponse httpResponse, Method method,
			ContentType contentType, String urlString, ParamsMap params)
			throws MambuApiException, UnsupportedOperationException, IOException {

		// get status
		int status = httpResponse.getStatusLine().getStatusCode();

		ByteArrayOutputStream response = null;
		String responseMessage = "";
		// Get the response Entity
		HttpEntity entity = httpResponse.getEntity();
		if (entity != null && status == HttpURLConnection.HTTP_OK) {
			response = getByteArrayOutputStream(entity.getContent());
			responseMessage = "DB backup stream successfully obtained";

		} else {
			// read the content for the error message
			String errorMessage = null;
			errorMessage = processResponse(httpResponse, method, contentType, urlString, params);
			responseMessage = errorMessage;
		}

		// Log Mambu response
		if (LOGGER.isLoggable(responseLogLevel)) {
			logApiResponse(responseLogLevel, urlString, status, responseMessage);
		}

		// if status is Ok - return the response
		if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_CREATED) {
			return response;
		}

		// Set error code and throw Mambu Exception
		Integer errorCode = status;

		// Log raising exception
		logExceptionForProcessingResponse(method, contentType, urlString, params, "", errorCode);

		// pass to MambuApiException the content that goes with the error code
		throw new MambuApiException(errorCode, "Couldn`t obtain stream content");
	}

	/**
	 * Converts the InputStream passed as parameter to this method into a ByteArrayOutputStream
	 * 
	 * @param inputStream
	 *            The InputStream to be transformed
	 * @return A ByteArrayOutputStream
	 * @throws IOException
	 */
	private ByteArrayOutputStream getByteArrayOutputStream(InputStream inputStream) throws IOException {

		byte[] byteArray = IOUtils.toByteArray(inputStream);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(byteArray.length);
		baos.write(byteArray, 0, byteArray.length);

		return baos;
	}

	/**
	 * Logs the exception details in case an error occurred while processing the response.
	 * 
	 * @param method
	 *            The HTTP method
	 * @param contentType
	 *            The content type
	 * @param urlString
	 *            The URL
	 * @param params
	 *            the parameters for the URL
	 * @param response
	 *            The String response
	 * @param errorCode
	 *            The error code received from Mambu
	 */
	private static void logExceptionForProcessingResponse(Method method, ContentType contentType, String urlString,
			ParamsMap params, String response, Integer errorCode) {

		if (LOGGER.isLoggable(exceptionLogLevel)) {
			// Remove appKey from the URL string when logging exception
			String urlLogString = urlString;
			String appKeyValue = MambuAPIFactory.getApplicationKey();
			if (appKeyValue != null) {
				urlLogString = urlLogString.replace(appKeyValue, "...");
			}
			LOGGER.log(exceptionLogLevel, "Creating exception, error code=" + errorCode + " for url=" + urlLogString);
			// if response was not logged - log it now with the exception
			if (!LOGGER.isLoggable(responseLogLevel)) {
				LOGGER.log(exceptionLogLevel, "Mambu Response: " + response);
			}
			// If the request was not logged yet - log it now for this exception to see all needed request details
			if (!LOGGER.isLoggable(requesLogLevel)) {
				// Request was not log. Log it now with the exception
				LOGGER.log(exceptionLogLevel, "Request causing Mambu exception:");
				logApiRequest(exceptionLogLevel, method, contentType, urlLogString, params);
			}
		}
	}

	/**
	 * Adds the application key to the parameter map received as parameter to this
	 * 
	 * @param paramsMap
	 *            The parameters map where the application key will be added. The application key will be added only if
	 *            it was specified.
	 * @return The updated parameters map
	 */
	private ParamsMap addAppKeyToParams(ParamsMap paramsMap) {

		String applicationKey = MambuAPIFactory.getApplicationKey();
		if (applicationKey != null) {
			// add application key to the params map
			if (paramsMap == null) {
				paramsMap = new ParamsMap();
			}
			paramsMap.addParam(APPLICATION_KEY, applicationKey);

			// Log that Application key was added
			logAppKey(applicationKey);

		}
		return paramsMap;
	}

	/**
	 * Logs the Curl details for the request.
	 * 
	 * NOTE: This method logs output only when the Logger level is set to FINEST.
	 * 
	 * @param urlString
	 *            The URL as String
	 * @param params
	 *            The parameters for the URL
	 * @param method
	 *            The HTTP method
	 * @param contentTypeFormat
	 *            The content type
	 */
	private void logCurlRequestDetails(String urlString, ParamsMap params, Method method,
			ContentType contentTypeFormat) {

		if (LOGGER.isLoggable(curlRequestTemplateLogLevel)) {
			logCurlCommandForRequest(method, contentTypeFormat, urlString, params);
		}
	}

	/**
	 * Logs to the details of an API request
	 * 
	 * @param urlString
	 *            The URL to be printed in logs
	 * @param params
	 *            The parameters to be logged
	 * @param method
	 *            HTTP method to be logged
	 * @param contentTypeFormat
	 *            The content type to be logged
	 * 
	 */
	private void logApiRequestDetails(String urlString, ParamsMap params, Method method,
			ContentType contentTypeFormat) {

		if (LOGGER.isLoggable(requesLogLevel)) {
			logApiRequest(requesLogLevel, method, contentTypeFormat, urlString, params);
		}
	}

	/**
	 * Delegates the request executions to more specialized methods based on HTTP method type. Returns the HTTP response
	 * after executing the requests.
	 * 
	 * @param urlString
	 *            URL string for the HTTP request
	 * @param params
	 *            parameters map
	 * @param method
	 *            HTTP method
	 * @param contentTypeFormat
	 *            content type
	 * @param httpClient
	 *            HTTP client executing the request
	 * @param httpResponse
	 *            HTTP response
	 * @return HTTP response
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws MambuApiException
	 */
	private HttpResponse executeRequestByMethod(String urlString, ParamsMap params, Method method,
			ContentType contentTypeFormat, HttpClient httpClient, HttpResponse httpResponse)
			throws MalformedURLException, IOException, MambuApiException {

		switch (method) {
		case GET:
			httpResponse = executeGetRequest(httpClient, urlString, params);
			break;
		case POST:
			httpResponse = executePostRequest(httpClient, urlString, params, contentTypeFormat);
			break;
		case PATCH:
			httpResponse = executePatchRequest(httpClient, urlString, params);
			break;
		case DELETE:
			httpResponse = executeDeleteRequest(httpClient, urlString, params);
			break;
		default:
			throw new IllegalArgumentException(
					"Only methods GET, POST PATCH and DELETE are supported, not " + method.name() + ".");
		}
		return httpResponse;
	}

	/**
	 * Executes a POST request as per the interface specification
	 */
	private HttpResponse executePostRequest(HttpClient httpClient, String urlString, ParamsMap params,
			ContentType contentTypeFormat) throws MalformedURLException, IOException, MambuApiException {

		// Get properly formatted ContentType
		final String contentType = getFormattedContentTypeString(contentTypeFormat);

		HttpPost httpPost = new HttpPost(urlString);
		httpPost.setHeader("Content-Type", contentType);
		httpPost.setHeader("Authorization", "Basic " + encodedAuthorization);

		if (params != null && params.size() > 0) {
			switch (contentTypeFormat) {

			case WWW_FORM:
				// convert parms to a list for HttpEntity
				List<NameValuePair> httpParams = getListFromParams(params);

				// use UTF-8 to encode
				HttpEntity postEntity = new UrlEncodedFormEntity(httpParams, UTF8_charset);

				httpPost.setEntity(postEntity);

				break;

			case JSON:

				// Make jsonEntity
				StringEntity jsonEntity = makeJsonEntity(params);

				httpPost.setEntity(jsonEntity);

				break;
			}
		}

		// execute
		HttpResponse httpResponse = httpClient.execute(httpPost);

		return httpResponse;

	}

	/**
	 * Executes a PATCH request as per the interface specification
	 */
	private HttpResponse executePatchRequest(HttpClient httpClient, String urlString, ParamsMap params)
			throws MalformedURLException, IOException, MambuApiException {

		// PATCH request is using json ContentType
		final String contentType = jsonContentType;

		// HttpPatch is available since org.apache.httpcomponents v4.2
		HttpPatch httpPatch = new HttpPatch(urlString);
		httpPatch.setHeader("Content-Type", contentType);
		httpPatch.setHeader("Authorization", "Basic " + encodedAuthorization);

		// Format jsonEntity
		StringEntity jsonEntity = makeJsonEntity(params);
		httpPatch.setEntity(jsonEntity);

		// execute
		HttpResponse httpResponse = httpClient.execute(httpPatch);

		return httpResponse;

	}

	/***
	 * Execute a GET request as per the interface specification
	 * 
	 * @param httpClient
	 *            http client
	 * @param urlString
	 *            url string
	 * @param params
	 *            Params Map
	 * @return Http Response
	 */
	private HttpResponse executeGetRequest(HttpClient httpClient, String urlString, ParamsMap params)
			throws MalformedURLException, IOException, MambuApiException {

		if (params != null && params.size() > 0) {
			urlString = new String((URLHelper.makeUrlWithParams(urlString, params)));
		}

		HttpGet httpGet = new HttpGet(urlString);
		// add Authorozation header
		httpGet.setHeader("Authorization", "Basic " + encodedAuthorization);
		// setHeader("Content-Type") not need for GET requests

		// execute
		HttpResponse httpResponse = httpClient.execute(httpGet);

		return httpResponse;

	}

	/***
	 * Execute a DELETE request as per the interface specification
	 * 
	 * @param httpClient
	 *            http client
	 * 
	 * @param urlString
	 * 
	 * @param params
	 *            ParamsMap with parameters
	 * @return Http Response
	 */
	private HttpResponse executeDeleteRequest(HttpClient httpClient, String urlString, ParamsMap params)
			throws MalformedURLException, IOException, MambuApiException {

		if (params != null && params.size() > 0) {
			urlString = new String((URLHelper.makeUrlWithParams(urlString, params)));
		}

		HttpDelete httpDelete = new HttpDelete(urlString);
		httpDelete.setHeader("Authorization", "Basic " + encodedAuthorization);

		// execute
		HttpResponse httpResponse = httpClient.execute(httpDelete);

		return httpResponse;

	}

	/**
	 * Make StringEntity for HTTP requests from the JSON string supplied in the ParamsMap
	 * 
	 * @param params
	 *            ParamsMap with JSON string
	 */
	private static StringEntity makeJsonEntity(ParamsMap params) throws UnsupportedEncodingException {

		if (params == null) {
			throw new IllegalArgumentException("JSON requests require non NULL ParamsMap with JSON string");
		}
		// Parameter (json string) is expected as JSON_OBJECT parameter
		String jsonString = params.get(APIData.JSON_OBJECT);

		if (jsonString == null) {
			throw new IllegalArgumentException("JSON string cannot be NULL");
		}

		// Add APPKEY to jsonString (see MBU-3892, implemented in 3.3 release)
		jsonString = addAppKeyToJson(jsonString, params);

		// Format jsonEntity
		StringEntity jsonEntity = new StringEntity(jsonString, UTF8_charset);

		return jsonEntity;

	}

	/**
	 * Process and return the response to an HTTP request. Throw MambuApiException if request failed. Log response
	 * 
	 * @param httpResponse
	 *            HTTP response
	 * @param method
	 *            method
	 * @param contentType
	 *            content type
	 * 
	 * @param urlString
	 *            URL string for the HTTP request
	 * @param params
	 *            Params Map
	 * @return HTTP response string
	 */
	private static String processResponse(HttpResponse httpResponse, Method method, ContentType contentType,
			String urlString, ParamsMap params) throws IOException, MambuApiException {

		// get status
		int status = httpResponse.getStatusLine().getStatusCode();

		InputStream content = null;
		String response = "";

		// Get the response Entity
		HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			content = entity.getContent();
			if (content != null) {
				response = readStream(content);
			}
		}

		// Log Mambu response
		if (LOGGER.isLoggable(responseLogLevel)) {
			logApiResponse(responseLogLevel, urlString, status, response);
		}

		// if status is Ok - return the response
		if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_CREATED) {
			return response;
		}

		// Set error code and throw Mambu Exception
		Integer errorCode = status;

		// Log raising exception
		logExceptionForProcessingResponse(method, contentType, urlString, params, response, errorCode);

		// pass to MambuApiException the content that goes with the error code
		throw new MambuApiException(errorCode, response);

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
	private static String readStream(InputStream content) throws IOException {

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
	private static List<NameValuePair> getListFromParams(ParamsMap params) {

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
	private static String getFormattedContentTypeString(ContentType contentTypeFormat) {

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
	private static String addAppKeyToJson(String jsonString, ParamsMap params) {

		if (params == null) {
			return jsonString;
		}

		String appKey = params.get(APPLICATION_KEY);
		return ServiceHelper.addAppkeyValueToJson(appKey, jsonString);

	}

	/**
	 * Log API request details. This is a helper method for using consistent formating when using Java Logger to print
	 * the details of the API request
	 * 
	 * @param logerLevel
	 *            allowed logger level
	 * @param method
	 *            request's method
	 * @param contentType
	 *            request's content type
	 * @param urlString
	 *            request's url
	 * @param params
	 *            the ParamsMap.
	 * 
	 *            The method shall be invoked before the appKey is added to the map to avoid printing appKey details
	 * 
	 */
	private static void logApiRequest(Level logerLevel, Method method, ContentType contentType, String urlString,
			ParamsMap params) {

		if (!LOGGER.isLoggable(logerLevel) || method == null) {
			return;
		}

		// Log Method and URL.
		// Log params if applicable
		// Log Json for Json requests
		String requestDetails = method.name() + " with URL=";
		String jsonString = null;
		String urlWithParams = null;
		switch (method) {
		case GET:
			// For GET add params to the url as in to be sent request itself
			urlWithParams = new String((URLHelper.makeUrlWithParams(urlString, params)));
			requestDetails = requestDetails + urlWithParams;
			break;

		case POST:
		case PATCH:
			switch (contentType) {
			case WWW_FORM:
				// Log URL and params as separate items
				requestDetails = requestDetails + urlString;
				if (params != null) {
					String postParams = params.getURLString();
					requestDetails = requestDetails + "\nParams=" + postParams;
				}
				break;
			case JSON:
				// Log URL and Json string
				requestDetails = requestDetails + urlString;
				if (params != null) {
					jsonString = params.get(APIData.JSON_OBJECT);
				}
				break;
			}

			break;
		case DELETE:
			// For DELETE ads params to the url as in to be sent request itself
			urlWithParams = new String((URLHelper.makeUrlWithParams(urlString, params)));
			requestDetails = requestDetails + urlWithParams;
			break;

		default:
			break;

		}
		// Add content type to logging, if not NULL
		if (contentType != null) {
			requestDetails = requestDetails + " (contentType=" + contentType + ")";
		}
		// Remove appKey from the URL string when logging exception
		String appKeyValue = MambuAPIFactory.getApplicationKey();
		if (requestDetails != null && appKeyValue != null) {
			requestDetails = requestDetails.replace(appKeyValue, "...");
		}
		// Now we can Log URL and Params
		LOGGER.log(logerLevel, requestDetails);
		// For Jsons - log the Json string
		if (jsonString != null) {
			logJsonInput(logerLevel, jsonString);
		}

	}

	/**
	 * Make and log curl command template corresponding to the API params supplied in the request. This curl pattern can
	 * be used for subsequent testing and troubleshooting: to execute Mmabu API requests as curl commands with exactly
	 * the same request params and to compare wrapper built requests with the curl patterns required by Mambu for this
	 * API.
	 * 
	 * NOTE: This method logs output only when the Logger level is set to FINEST.
	 * 
	 * Log output example: curl -G -H "Content-type: application/x-www-form-urlencoded; charset=UTF-8" -d 'appkey=...'
	 * https://user:pwd@tenant.mambu.com/api/loans?offset=0&limit=5
	 * 
	 * @param method
	 *            request's method
	 * @param contentType
	 *            request's content type
	 * @param urlString
	 *            request's url
	 * @param urlWithParams
	 *            url string with added params for www-form-urlencoded requests
	 * @param params
	 *            the ParamsMap.
	 */
	private static void logCurlCommandForRequest(Method method, ContentType contentType, String urlString,
			ParamsMap params) {

		if (method == null) {
			return;
		}
		// Make method options and url string
		String apiMethod = "";
		switch (method) {
		case GET:
			apiMethod = " -G";
			break;
		case POST:
			apiMethod = " -X POST";
			break;
		case PATCH:
			apiMethod = " -X PATCH";
			break;
		case DELETE:
			apiMethod = " -X DELETE";
			break;
		}
		String url = urlString;
		// Add content type header
		contentType = (contentType == null) ? ContentType.WWW_FORM : contentType;
		String contentHeader = " -H \"Content-type: " + getFormattedContentTypeString(contentType) + "\"";

		// Make curl command
		String curlCommand = "curl" + apiMethod + contentHeader;

		// Add appkey param (as a placeholder only)
		String appKeyValue = MambuAPIFactory.getApplicationKey();
		final String emptyAppKey = "...";

		// Make url command required for the contentType
		String urlParams = "";
		switch (contentType) {
		case WWW_FORM:
			// Add appkey to the params
			if (appKeyValue != null) {
				appKeyValue = emptyAppKey;
				urlParams = "appkey=" + appKeyValue;
			}
			if (params != null && params.size() > 0) {
				String paramsString = params.getURLString();
				if (urlParams.length() > 0) {
					urlParams = urlParams + "&";
				}
				urlParams = urlParams + paramsString;
			}
			break;
		case JSON:
			// Add appkey to the JSON
			String jsonString = (params == null) ? "{}" : params.get(APIData.JSON_OBJECT);
			if (appKeyValue != null) {
				jsonString = ServiceHelper.addAppkeyValueToJson(appKeyValue, jsonString);
				final String appKey = "\"" + APIData.APPLICATION_KEY + "\":\"" + appKeyValue + "\",";
				final String logAppKey = "\"" + APIData.APPLICATION_KEY + "\":\"" + emptyAppKey + "\",";

				jsonString = jsonString.replace(appKey, logAppKey);

			}
			// Add JSON to the command line
			curlCommand = curlCommand + " -d '" + jsonString + "' ";
			break;
		}
		// Add placeholder for the user's credentials
		url = url.replace("://", "://user:pwd@");
		if (urlParams.length() > 0) {
			url = url + "?" + urlParams;
		}

		// Make final curl command and log it on a separate line
		curlCommand = "\n" + curlCommand + " '" + url + "'";
		LOGGER.log(curlRequestTemplateLogLevel, curlCommand);

	}

	// Strings and constants used for logging formatting
	final static String documentContentParam = "\"documentContent\":";
	final static String documentRoot = "\"document\":";
	final static String documentsApiEndpoint = "/" + APIData.DOCUMENTS + "/";
	final static String moreIndicator = "...\"";
	final static int howManyEncodedCharsToShow = 20;
	// must be long enough to show full string for boolean API responses
	final static int howManyDocumentResponseCharsToShow = 50;

	/**
	 * Log Json string details. This is a helper method for modifying the original Json string to remove details that
	 * are needed for logging (for example, encoded data when sending documents via Json)
	 * 
	 * @param logerLevel
	 *            allowed logger level for logging JSON content
	 * @param jsonString
	 *            json string in the API request
	 * 
	 */
	private static void logJsonInput(Level logerLevel, String jsonString) {

		if (!LOGGER.isLoggable(logerLevel) || jsonString == null) {
			return;
		}

		// handle some special cases to have user friendly output
		// Handle jsons with encoded documents. Encoded data is of no use for logging. Strip it out

		// Documents API case - remove base64 encoding
		// Find the documentContent tag (containing base64 string) and remove extra content
		if (jsonString.contains(documentRoot)) {
			int contentStarts = jsonString.indexOf(documentContentParam);
			if (contentStarts != -1) {
				// Get everything up to the documentContent plus some more
				final int encodedCharsToShow = 20;
				// Also add "..." to indicate that the output was truncated
				jsonString = jsonString.substring(0, contentStarts + documentContentParam.length() + encodedCharsToShow)
						+ moreIndicator + "}";
			}
		}

		LOGGER.log(logerLevel, "Input JsonString=" + jsonString);

	}

	/**
	 * Log API response details. This is a helper method for using consistent formating when using Java Logger to print
	 * the details of the API response
	 * 
	 * @param logerLevel
	 *            allowed logger level
	 * @param urlString
	 *            url request string
	 * @param status
	 *            response status
	 * @param response
	 *            response string
	 */
	private static void logApiResponse(Level logerLevel, String urlString, int status, String response) {

		// Log response details
		if (status != HttpURLConnection.HTTP_OK && status != HttpURLConnection.HTTP_CREATED) {
			// Error status. Log as error
			LOGGER.log(exceptionLogLevel, "Error status=" + status + " Error response=" + response);
		} else {
			if (!LOGGER.isLoggable(logerLevel)) {
				return;
			}
			// Log success Response.
			// Handle special cases where response contains encoded strings which we don't need to see in the logger
			// (for example, base64 encoded data when getting images and files)
			// Find ";base64,";
			final String encodedDataIndicator = APIData.BASE64_ENCODING_INDICATOR;
			final int encodedDataStart = response.indexOf(encodedDataIndicator);
			// Document APIs may also return very long strings with encoded content (but without the
			// BASE64_ENCODING_INDICATOR as for image API)
			final boolean isDocumentApiResponse = (urlString.contains(documentsApiEndpoint)) ? true : false;
			if (encodedDataStart != -1) {
				// This is a response containing base64 encoded data. Strip the bulk of it out
				int totalCharsToShow = encodedDataStart + encodedDataIndicator.length() + howManyEncodedCharsToShow;
				// Get the needed part of this response and add "..." indicator
				response = response.substring(0, totalCharsToShow) + moreIndicator;
			} else if (isDocumentApiResponse) {
				// It's a document API response. Could be also very long. Limit the output
				if (response.length() > howManyDocumentResponseCharsToShow) {
					response = response.substring(0, howManyDocumentResponseCharsToShow) + moreIndicator;
				}
			}

			// Log API response Status and the Response string
			LOGGER.log(logerLevel, "Response Status=" + status + "\tMessage length=" + response.length()
					+ "\nResponse message=" + response + "");
		}

	}

	/**
	 * Log Application Key details. This is a helper method for logging partial application key details to indicate the
	 * that application key is used when building the API request
	 * 
	 * @param applicationKey
	 *            Application Key string
	 */
	private static void logAppKey(String applicationKey) {

		if (!LOGGER.isLoggable(Level.FINEST)) {
			return;
		}
		final int keyLength = applicationKey.length();
		final int printLength = 3;
		// Mambu App Keys are very long but just to prevent any errors need to ensure there is enough to print
		if (keyLength >= printLength) {
			LOGGER.finest("Added Application key=" + applicationKey.substring(0, printLength) + "..."
					+ applicationKey.substring(keyLength - printLength, keyLength));
		}

	}

}
