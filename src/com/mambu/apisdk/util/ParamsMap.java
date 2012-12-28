/**
 * 
 */
package com.mambu.apisdk.util;

import java.util.LinkedHashMap;
import java.util.Map;

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
			if (entry.getValue() != null) {
				urlParams.append(entry.getKey() + "=" + entry.getValue());
				urlParams.append(APPENDER);
			}
		}

		if (urlParams.length() > 0)
			return urlParams.deleteCharAt(urlParams.length() - 1).toString();
		else
			return "";
	}
}
