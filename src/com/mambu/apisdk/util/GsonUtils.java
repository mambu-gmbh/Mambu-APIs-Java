/**
 * 
 */
package com.mambu.apisdk.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * Utill class for gson formatting
 * 
 * @author ipenciuc
 * 
 */
public class GsonUtils {

	public static final String defaultDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssZ";

	/**
	 * Creates a GSON instance with default date/time format
	 * 
	 * @return the GSON instance
	 */
	public static Gson createGson() {
		// Create with the default date/time format
		return new GsonBuilder().setDateFormat(defaultDateTimeFormat).create();
	}

	/**
	 * Create GsonBuilder with default date/time format
	 * 
	 * @return GsonBuilder
	 */
	public static GsonBuilder createGsonBuilder() {
		return new GsonBuilder().setDateFormat(defaultDateTimeFormat);
	}

	/**
	 * Create GsonBuilder specifying custom date/time format
	 * 
	 * @param dateTimeFormat
	 *            date/time format. If null, default date/time format is used
	 * @return GsonBuilder
	 */
	public static GsonBuilder createGsonBuilder(String dateTimeFormat) {
		if (dateTimeFormat == null) {
			dateTimeFormat = defaultDateTimeFormat;
		}
		return new GsonBuilder().setDateFormat(dateTimeFormat);
	}

	/***
	 * Creates a GSON instance from the builder specifying custom date/time format
	 * 
	 * @return the GSON instance
	 */
	public static Gson createGson(String dateTimeFormat) {
		// Create with the specified dateTimeFormat
		GsonBuilder gsonBuilder = createGsonBuilder(dateTimeFormat);
		return gsonBuilder.create();
	}

	/**
	 * Convenience method to create Gson instance for serialising objects and using the date time format and
	 * serialisation strategies as specified in ApiDefinition
	 * 
	 * @param apiDefinition
	 *            api definition
	 * @return gson with custom inclusion strategies and custom serializers added
	 */
	public static Gson createSerializerGson(ApiDefinition apiDefinition) {

		String dateTimeFormat = apiDefinition.getJsonDateTimeFormat();
		GsonBuilder gsonBuilder = GsonUtils.createGsonBuilder(dateTimeFormat);

		// Add optional serialisation Exclusion Strategies
		List<ExclusionStrategy> serializationExclusionStrategies = apiDefinition.getSerializationExclusionStrategies();
		if (serializationExclusionStrategies != null) {
			for (ExclusionStrategy exclusionStrategy : serializationExclusionStrategies) {
				gsonBuilder.addSerializationExclusionStrategy(exclusionStrategy);
			}
		}
		// Add optional JsonSerializer adapters to the builder as specified in ApiDefinition
		HashMap<Class<?>, JsonSerializer<?>> serializers = apiDefinition.getJsonSerializers();
		if (serializers != null && serializers.size() != 0) {
			for (Map.Entry<Class<?>, JsonSerializer<?>> entry : serializers.entrySet()) {
				// Register each type adapter.
				// NOTE: register as Type Hierarchy adapter, otherwise if doesn't seem to work on Android if registering
				// just as a "registerTypeAdapter()"
				gsonBuilder.registerTypeHierarchyAdapter(entry.getKey(), entry.getValue());
			}
		}
		return gsonBuilder.create();
	}

	/**
	 * Convenience method to create Gson instance for deserializing Mambu responses and using the default date time
	 * format and custom deserializing strategies as specified in ApiDefinition
	 * 
	 * @param apiDefinition
	 *            api definition
	 * @return gson with the default date time format and custom deserializers added
	 */
	public static Gson createDeserializerGson(ApiDefinition apiDefinition) {

		String dateTimeFormat = GsonUtils.defaultDateTimeFormat;
		GsonBuilder gsonBuilder = GsonUtils.createGsonBuilder(dateTimeFormat);

		// Add optional JsonDeserializer type adapters to the builder as specified in ApiDefinition
		HashMap<Class<?>, JsonDeserializer<?>> deserializers = apiDefinition.getJsonDeserializers();
		if (deserializers != null && deserializers.size() > 0) {
			for (Map.Entry<Class<?>, JsonDeserializer<?>> entry : deserializers.entrySet()) {
				gsonBuilder.registerTypeAdapter(entry.getKey(), entry.getValue());
			}
		}
		return gsonBuilder.create();
	}

}
