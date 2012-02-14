/**
 * 
 */
package com.mambu.apisdk.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utill class for gson formatting
 * 
 * @author ipenciuc
 * 
 */
public class GsonUtils {

	private static GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	/***
	 * Creates a GSON response from the builder
	 * 
	 * @return the GSON response
	 */
	public static Gson createResponse() {
		return gsonBuilder.create();
	}

}
