/**
 * 
 */
package com.mambu.apisdk.services;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;

/**
 * Service class which handles API operations like retrieval, creation or changing state of savings accounts
 * 
 * @author ipenciuc
 * 
 */
public class SavingsService {

	@SuppressWarnings("unused")
	private MambuAPIService mambuAPIService;

	/***
	 * Create a new savings service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public SavingsService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

}
