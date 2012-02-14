/**
 * 
 */
package com.mambu.apisdk.services;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;


/**
 * Service class which handles API operations like entering and getting repayments
 * 
 * @author ipenciuc
 * 
 */
public class RepaymentsService {

	@SuppressWarnings("unused")
	private MambuAPIService mambuAPIService;

	/***
	 * Create a new repayments service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public RepaymentsService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}

}
