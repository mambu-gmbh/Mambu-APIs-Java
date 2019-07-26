package com.mambu.apisdk.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Schedule query parameters model, used to create extra query params for preview schedule API
 * 
 * @author acostros
 *
 */
public class ScheduleQueryParams {

	private Map<ScheduleQueryParam, String> params;

	private ScheduleQueryParams() {
		params = new HashMap<>();
	}

	/**
	 * Create new instance of ScheduleQueryParams
	 * 
	 * @return newly created parameters instance
	 */
	public static ScheduleQueryParams instance() {

		return new ScheduleQueryParams();
	}

	/**
	 * Add new query parameters
	 * 
	 * @param paramName
	 *            the name of the query parameter to be added
	 * @param paramValue
	 *            the value of the parameters
	 */
	public void addQueryParam(ScheduleQueryParam paramName, String paramValue) {

		params.put(paramName, paramValue);
	}

	public Map<ScheduleQueryParam, String> getParams() {

		return params;
	}

}
