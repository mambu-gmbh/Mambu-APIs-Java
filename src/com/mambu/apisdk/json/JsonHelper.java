package com.mambu.apisdk.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A helper class to support commonly used JSON processing methods
 * 
 * @author mdanilkis
 * 
 */
public class JsonHelper {

	/**
	 * Add value to jsonResult if value is not null.
	 * 
	 * @param jsonResult
	 *            json result
	 * @param propertyName
	 *            property name
	 * @param value
	 *            value for the propertyName
	 */
	public static void addValueIfNotNullValue(JsonObject jsonResult, String propertyName, JsonElement value) {
		if (value != null) {
			jsonResult.add(propertyName, value);
		}
	}

}
