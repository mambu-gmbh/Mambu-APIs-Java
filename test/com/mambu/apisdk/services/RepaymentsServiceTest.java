/**
 * 
 */
package com.mambu.apisdk.services;

import com.mambu.apisdk.MambuAPIServiceTest;
import com.mambu.apisdk.exception.MambuApiException;

/**
 * @author ipenciuc
 * 
 */
public class RepaymentsServiceTest extends MambuAPIServiceTest {

	@SuppressWarnings("unused")
	private RepaymentsService service;

	@Override
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new RepaymentsService(super.mambuApiService);
	}
}
