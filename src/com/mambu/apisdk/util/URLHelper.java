/**
 * 
 */
package com.mambu.apisdk.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mambu.apisdk.model.ApplicationProtocol;
import com.mambu.apisdk.model.Domain;
import com.mambu.apisdk.model.UserAgentHeader;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;

/**
 * Helper class for operations with the URL adresses
 * 
 * @author ipenciuc
 * 
 */
@Singleton
public class URLHelper {

	private static final String AMPERSAND_DELIMITER = "&";
	private String agentHeaderValue;
	private String domainName;
	private String protocol;
	private static String API_ENDPOINT = "/api/";
	private static String QUESTION_MARK_DELIMITER = "?";

	private final static Logger LOGGER = Logger.getLogger(URLHelper.class.getName());

	@Inject
	public URLHelper(@ApplicationProtocol String protocol, @Domain String domainName, @UserAgentHeader String userAgentHeaderValue) {
		this.protocol = protocol;
		this.domainName = domainName;
		this.agentHeaderValue = userAgentHeaderValue; 
	}

	/**
	 * Creates an URL String with an protocol, a domainName and some given details
	 * 
	 * @param details
	 *            the extra details
	 * @return the created URL String in url-encoded format
	 */
	public String createUrl(String details) {

		details = details == null ? "" : details;

		// URL String must be url-encoded to handle spaces and UTF-8 chars (See MBU-4669, implemented in Mambu 3.4)
		String encodedUrl;
		try {

			URI uri = createURI(details);
			encodedUrl = uri.toString();

			return encodedUrl;

		} catch (URISyntaxException e) {

			LOGGER.severe("Exception message=" + e.getMessage() + " Failed to create URI for Domain Name=" + domainName
					+ " with url details=" + details);
			return "";
		}

	}
	
	/**
	 * Creates an URI based on the connection details specified at the project level as well as on the details provided as parameter.
	 * 
	 * @param details
	 *            the details that are used for building the path
	 * 
	 * @return newly created URI
	 * 
	 * @throws URISyntaxException
	 */
	private URI createURI(String details) throws URISyntaxException {
		if (isDomainNameWithPort()) {
			
			return new URI(protocol, null, host(), port(), API_ENDPOINT + details, null, null);
		} else {
			return new URI(protocol, domainName, API_ENDPOINT + details, null);
		}
	}
	
	/**
	 * Extracts the host from the domain name.
	 * 
	 * @return connection host
	 */
	private String host() {
		return domainName.split(":")[0];
	}
	
	/**
	 * Extracts the port from the domain name.
	 * 
	 * @return connection port
	 */
	private int port() {
		return Integer.parseInt(domainName.split(":")[1]);
	}
	
	/**
	 * Checks whether the domain name contains a port value or not.
	 * 
	 * @return true if the domain name contains a port value, false otherwise
	 */
	private boolean isDomainNameWithPort() {
		return domainName.matches(".*:[0-9]+");
	}
	

	/***
	 * Appends some params to a given URL String
	 * 
	 * NOTE: this non-static version is used in SDK Mockito tests (see MambuAPIServiceTest). Mockito cannot use
	 * static methods
	 * 
	 * @param urlString
	 *            the already created URL String
	 * @param paramsMap
	 *            the params which must be added
	 * 
	 * @return the complete URL
	 */
	public String createUrlWithParams(String urlString, ParamsMap paramsMap) {

		return makeUrlWithParams(urlString, paramsMap);

	}

	/***
	 * Static helper to Append URL params to a given URL String
	 * 
	 * @param urlString
	 *            the already created URL String
	 * @param paramsMap
	 *            the params which must be added
	 * 
	 * @return the complete URL
	 */
	public static String makeUrlWithParams(String urlString, ParamsMap paramsMap) {

		String paramDelimiter = urlString.contains(QUESTION_MARK_DELIMITER) ?
				AMPERSAND_DELIMITER : QUESTION_MARK_DELIMITER;

		return paramsMap != null ? urlString + paramDelimiter + paramsMap.getURLString() : urlString;
	}

	/**
	 * Add pagination params to a given URL String for POST with application/json content type. Pagination parameters
	 * shall be be added to the URL string. See MBU-8975. Only "offset" and "limit" parameters are added. For example,
	 * in API call POST {JSONFilterConstraints} /api/loans/search?offset=0&limit=5
	 * 
	 * @param urlString
	 *            original URL string
	 * @param method
	 *            method
	 * @param contentTypeFormat
	 *            content type format
	 * @param params
	 *            input parameters. If pagination parameters are added to the URL string then they are removed from the
	 *            original params map
	 * @return URL string with pagination parameters added for POST with application/json content type
	 */
	public String addJsonPaginationParams(String urlString, Method method, ContentType contentTypeFormat,
			ParamsMap params) {

		// Add only for POST with ContentType.JSON (for ContentType.WWW_FORM all params will be added to the URL)
		if (params == null || !(method == Method.POST && contentTypeFormat == ContentType.JSON)) {
			return urlString;
		}

		if (params.get(APIData.OFFSET) == null && params.get(APIData.LIMIT) == null) {
			return urlString;
		}
		// Create temporary params map
		ParamsMap paginationParams = new ParamsMap();
		paginationParams.put(APIData.OFFSET, params.get(APIData.OFFSET));
		paginationParams.put(APIData.LIMIT, params.get(APIData.LIMIT));

		// Add offset/limit to the URL string
		String urlWithParams = makeUrlWithParams(urlString, paginationParams);

		// Remove pagination params already added to the URL
		params.remove(APIData.OFFSET);
		params.remove(APIData.LIMIT);

		return urlWithParams;
	}

	/**
	 *  Appends the details level query param to a given URL in case exist
	 *
	 * @param urlString	the String URL where the details level will be appended
	 * @param method the HTTP method of the request
	 * @param contentTypeFormat the content type of the request
	 * @param params the params of the request
	 * @return the appended URL
	 */
	public String addDetailsParam(String urlString, Method method, ContentType contentTypeFormat,
			ParamsMap params) {

		// Add only for POST with ContentType.JSON (for ContentType.WWW_FORM all params will be added to the URL)
		if (params == null || !(method == Method.POST && contentTypeFormat == ContentType.JSON)) {
			return urlString;
		}

		if (params.get(APIData.FULL_DETAILS) == null) {
			return urlString;
		}

		ParamsMap paginationParams = new ParamsMap();
		paginationParams.put(APIData.FULL_DETAILS, params.get(APIData.FULL_DETAILS));

		String urlWithParams = makeUrlWithParams(urlString, paginationParams);

		// Remove full detail params already added to the URL
		params.remove(APIData.FULL_DETAILS);
		
		return urlWithParams;
	}
	
	/**
	 * Gets the user agent header value as set once with the factory initialization 
	 * 
	 * @return String value representing the user agent header value 
	 */
	public String userAgentHeaderValue(){
		return agentHeaderValue;
	}
}
