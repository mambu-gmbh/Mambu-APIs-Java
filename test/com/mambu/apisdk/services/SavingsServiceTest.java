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
public class SavingsServiceTest extends MambuAPIServiceTest {

	@SuppressWarnings("unused")
	private SavingsService service;

	@Override
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new SavingsService(super.mambuApiService);
	}
}
