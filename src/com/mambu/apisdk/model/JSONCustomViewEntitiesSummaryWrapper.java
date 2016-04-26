package com.mambu.apisdk.model;

import com.mambu.api.server.handler.customviews.model.CustomViewEntitiesSummaryWrapper;

/**
 * JSON wrapper for CustomViewEntitiesSummaryWrapper class. This wrapper is used for deserializing GET Custom View
 * Summary response. See MBU-11879- As a Developer, I want to get results summaries for custom view APIs
 * 
 * 
 * Response example : { "summary":{ "count":"120", "totals":{ "INTEREST_DUE":"100.12", "PRINCIPAL_BALANCE":"400" } }}
 * 
 * @author mdanilkis
 * 
 */
public class JSONCustomViewEntitiesSummaryWrapper {

	CustomViewEntitiesSummaryWrapper summary;

	public JSONCustomViewEntitiesSummaryWrapper(CustomViewEntitiesSummaryWrapper summary) {
		this.summary = summary;
	}

	public void setSummary(CustomViewEntitiesSummaryWrapper summary) {
		this.summary = summary;
	}

	public CustomViewEntitiesSummaryWrapper getSummary() {
		return summary;
	}
}
