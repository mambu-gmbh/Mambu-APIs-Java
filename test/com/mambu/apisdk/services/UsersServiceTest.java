package com.mambu.apisdk.services;

import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.mambu.apisdk.MambuAPIServiceTest;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.Method;

/**
 * @author ipenciuc
 * 
 */
public class UsersServiceTest extends MambuAPIServiceTest {

	private UsersService service;

	@Override
	public void setUp() throws MambuApiException {
		super.setUp();

		service = new UsersService(super.mambuApiService);
	}

	/***
	 * Test the retrieval of a full list of users
	 * 
	 */
	@Test
	public void testGetUsers() throws MambuApiException {

		// execute
		service.getUsers();

		// verify
		verify(executor).executeRequest("https://demo.mambutest.com/api/users", Method.GET);
	}

	/***
	 * Test the retrieval of a paginated list of users, filtered by branch
	 * 
	 */
	@Test
	public void testGetUsersFilteredByBranch() throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam("branchID", "branch_id");
		params.addParam("offset", "50");
		params.addParam("limit", "100");

		// execute
		service.getUsers("branch_id", "50", "100");

		// verify
		verify(executor).executeRequest("https://demo.mambutest.com/api/users", params, Method.GET);
	}
}