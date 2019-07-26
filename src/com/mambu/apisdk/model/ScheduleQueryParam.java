package com.mambu.apisdk.model;

/**
 * Shows possible values of schedule query parameters
 * 
 * @author acostros
 */
public enum ScheduleQueryParam {
	
	PERIODIC_PAYMENT("periodicPayment"),

	ORGANIZATION_COMMISSION("organizationCommission");

	private String paramName;

	ScheduleQueryParam(String paramName) {
		this.paramName = paramName;
	}

	public String getParamName() {

		return paramName;
	}
}
