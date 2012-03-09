package com.mambu.apisdk.services;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;

/**
 * Service class which handles API operations like getting and creating users
 * 
 * @author ipenciuc
 * 
 */
public class UsersService {

	private MambuAPIService mambuAPIService;

	private static String USERS = "users";

	private static String FIRST_NAME = "firstName";
	private static String LAST_NAME = "lastName";
	public static String HOME_PHONE = "homephone";
	public static String MOBILE_PHONE = "mobilephone";
	public static String GENDER = "gender";
	public static String BIRTH_DATE = "birthdate";
	public static String EMAIL_ADDRESS = "email";
	public static String NOTES = "notes";

	/***
	 * Create a new users service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public UsersService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
	}
	
}
