/**
 *
 */
package com.mambu.apisdk.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mambu.apisdk.model.ExcludeFromGson;

/**
 * Utill class for gson formatting
 *
 * @author ipenciuc
 *
 */
public class GsonUtils {

	public static String defaultDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static GsonBuilder gsonBuilder;

    static
    {
        ExclusionStrategy exclusionStrategy = new ExclusionStrategy()
        {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes)
            {
                return fieldAttributes.getAnnotations().contains(ExcludeFromGson.class);
            }

            @Override
            public boolean shouldSkipClass(Class<?> arg0)
            {
                // TODO Auto-generated method stub
                return false;
            }
        };

        gsonBuilder = new GsonBuilder().setDateFormat(defaultDateTimeFormat)
                .addDeserializationExclusionStrategy(exclusionStrategy)
                .addSerializationExclusionStrategy(exclusionStrategy);
    }

	/***
	 * Creates a GSON instance from the builder with the default date/time format
	 *
	 * @return the GSON instance
	 */
	public static Gson createGson() {
		// Create with default params
        return createGson(defaultDateTimeFormat);
	}

	/***
	 * Creates a GSON instance from the builder specifying custom date/time format
	 *
	 * @return the GSON instance
	 */
	public static Gson createGson(String dateTimeFormat) {
		// Create with the specified dateTimeFormat
		gsonBuilder = gsonBuilder.setDateFormat(dateTimeFormat);
		return gsonBuilder.create();
	}

}
