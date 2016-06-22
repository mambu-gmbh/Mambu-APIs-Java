package com.mambu.apisdk.json;

import com.google.gson.JsonObject;
import com.mambu.api.server.helper.json.JsonUtils;

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
	 * 
	 * NOTE: The addValueIfNotNullValue() method is copied from Mambu model classes (it's defined a a private method in
	 * there)
	 * 
	 * @param jsonResult
	 *            json result
	 * @param propertyName
	 *            property name
	 * @param value
	 *            value for the propertyName
	 */
	public static void addValueIfNotNullValue(JsonObject jsonResult, String propertyName, Object value) {
		if (value != null) {
			jsonResult.add(propertyName, JsonUtils.toJson(value));
		}
	}
}
