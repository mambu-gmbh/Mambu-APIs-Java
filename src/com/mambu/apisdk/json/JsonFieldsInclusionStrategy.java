package com.mambu.apisdk.json;

import java.util.HashMap;
import java.util.Set;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * This class extends ExclusionStrategy and allows specifying a class and only those class fields that must be
 * "included" into message when serialising/deserializing JSON messages. All other fields for the same class will not be
 * included into the message. Adding multiple classes with their fields to the same instance of this class is supported.
 * 
 * The instance of this class can then be added as ExclusionStrategy to the GsonBuilder, e.g.
 * gsonBuilder.addSerializationExclusionStrategy(exclusionStrategy);
 * 
 * 
 * @author mdanilkis
 * 
 */
public class JsonFieldsInclusionStrategy implements ExclusionStrategy {
	private HashMap<Class<?>, Set<String>> allowedFieldsMap;

	/**
	 * Initialize JsonFieldsInclusionStrategy specifying a class and set of fields allowed for this class. All other
	 * fields for this class are to be excluded
	 * 
	 * @param fieldClazz
	 *            class
	 * @param allowedNames
	 *            a set of allowed fields.
	 */
	public JsonFieldsInclusionStrategy(Class<?> fieldClazz, Set<String> allowedNames) {
		allowedFieldsMap = new HashMap<>();
		addInclusion(fieldClazz, allowedNames);

	}

	/**
	 * Add additional Exclusion Strategy specifying a class and set of fields allowed for this class. All other fields
	 * for this class are to be excluded
	 * 
	 * @param fieldClazz
	 *            class
	 * @param allowedNames
	 *            a set of allowed fields.
	 */
	public void addInclusion(Class<?> fieldClazz, Set<String> allowedNames) {
		allowedFieldsMap.put(fieldClazz, allowedNames);
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}

	/**
	 * Implements shouldSkipField to include only those fields for each class that were specified for each of the
	 * specified classes, stored in allowedFieldsMap
	 * 
	 * @param f
	 *            field attributes
	 */
	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		Class<?> clazz = f.getDeclaringClass();
		if (clazz == null || allowedFieldsMap.get(clazz) == null) {
			return true;
		}
		Set<String> allowedFields = allowedFieldsMap.get(clazz);
		boolean shouldSkip = allowedFields == null || !allowedFields.contains(f.getName());
		return shouldSkip;
	}
}
