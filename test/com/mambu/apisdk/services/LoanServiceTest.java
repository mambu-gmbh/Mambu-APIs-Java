/**
 * 
 */
package com.mambu.apisdk.services;

import org.junit.Before;

import com.mambu.apisdk.MambuAPIServiceTest;
import com.mambu.apisdk.exception.MambuApiException;

/**
 * @author ipenciuc
 * 
 */
public class LoanServiceTest extends MambuAPIServiceTest {

	@SuppressWarnings("unused")
	private LoanService service;

	@Before
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new LoanService(super.mambuApiService);
	}
}
