/**
 * 
 */
package com.mambu.apisdk.services;

import java.math.BigDecimal;
import java.util.HashMap;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.intelligence.shared.model.Intelligence.Indicator;

/**
 * Service class which handles API operations for financial indicators
 * 
 * @author ipenciuc
 * 
 */
public class IntelligenceService {

	private MambuAPIService mambuAPIService;

	/***
	 * Create a new intelligence service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public IntelligenceService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

	/**
	 * Requests a mambu indicator value as a BigDecimal value
	 * 
	 * @param glCode
	 * @return the big decimal indicator value
	 * @throws MambuApiException
	 */
	public BigDecimal getIndicator(Indicator indicator) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(APIData.INDICATORS + "/" + indicator.toString()));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		HashMap<String, String> result = GsonUtils.createGson().fromJson(jsonResponse,
				new TypeToken<HashMap<String, String>>() {}.getType());
		if (result != null) {
			String resultString = result.get(indicator.toString());
			return new BigDecimal(resultString);
		} else {
			return null;
		}

	}
}
