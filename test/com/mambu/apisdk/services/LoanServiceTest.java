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
public class LoanServiceTest extends MambuAPIServiceTest {

	@SuppressWarnings("unused")
	private LoansService service;

	@Override
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new LoansService(super.mambuApiService);
	}
}
