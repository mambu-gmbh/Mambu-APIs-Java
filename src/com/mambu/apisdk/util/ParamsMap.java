/**
 * 
 */
package com.mambu.apisdk.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility class responsible for the creation and the formatting of a map of URL parameters. It extends
 * {@link LinkedHashMap} in order to maintain an order over the added parameters.
 * 
 * @author ipenciuc
 * 
 */
public class ParamsMap extends LinkedHashMap<String, String> {

	private static final long serialVersionUID = 1L;

	private static String APPENDER = "&";
	private final static Logger LOGGER = Logger.getLogger(RequestExecutorImpl.class.getName());

	/**
	 * Class constructor (only for serialization)
	 */
	public ParamsMap() {
	}

	/***
	 * Add a new URL param
	 * 
	 * @param key
	 *            the key of the param
	 * @param value
	 *            the value of the param
	 */
	public void addParam(String key, String value) {
		this.put(key, value);
	}

	/***
	 * Formats this map of params into a String ready to be used in an URL
	 * 
	 * @return the formatted String
	 */
	public String getURLString() {

		StringBuffer urlParams = new StringBuffer();

		for (Map.Entry<String, String> entry : this.entrySet()) {
			// only put the parameter in the URL if its value is not null
			String value = entry.getValue();

			if (value != null) {

				// Encode Params's value for url
				String encodedValue = value;

				try {
					// URL encode values
					encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.name());

				} catch (UnsupportedEncodingException e) {
					// Shouldn't happen as we only use HTTP.UTF_8, but just in case...
					LOGGER.severe("Params Map: UnsupportedEncodingException, Invalid Encoding: " + e.getMessage());

					// at least try removing spaces
					encodedValue = encodedValue.trim().replace(" ", "+");

				}
				urlParams.append(entry.getKey() + "=" + encodedValue);
				urlParams.append(APPENDER);
			}
		}

		if (urlParams.length() > 0)
			return urlParams.deleteCharAt(urlParams.length() - 1).toString();
		else
			return "";
	}
}
