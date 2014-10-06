/**
 * 
 */
package com.mambu.apisdk.services;

import java.math.BigDecimal;
import java.util.HashMap;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiReturnFormat;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.intelligence.shared.model.Intelligence.Indicator;

/**
 * Service class which handles API operations for financial indicators
 * 
 * @author ipenciuc
 * 
 */
public class IntelligenceService {

	private ServiceExecutor serviceExecutor;

	private final static ApiDefinition getIndicator = new ApiDefinition(ApiType.GET_ENTITY, Indicator.class);

	/***
	 * Create a new intelligence service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public IntelligenceService(MambuAPIService mambuAPIService) {
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
	}

	/**
	 * Requests a mambu indicator value as a BigDecimal value
	 * 
	 * @param indicator
	 * 
	 * @return the big decimal indicator value
	 * 
	 * @throws MambuApiException
	 */
	public BigDecimal getIndicator(Indicator indicator) throws MambuApiException {

		if (indicator == null) {
			throw new IllegalArgumentException("Indicator must not be null");
		}
		// IntelligenceService returns a map. Use COLLECTION: for the Indicator class ServiceHelper is set to map
		// COLLECTION to a HashMap<String, String> type
		getIndicator.setApiReturnFormat(ApiReturnFormat.COLLECTION);
		// Execute
		HashMap<String, String> result = serviceExecutor.execute(getIndicator, indicator.name());

		if (result != null) {
			String resultString = result.get(indicator.name());
			return new BigDecimal(resultString);
		} else {
			return null;
		}

	}
}
