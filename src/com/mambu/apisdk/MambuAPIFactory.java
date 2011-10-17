package com.mambu.apisdk;

import com.mambu.apisdk.exception.MambuApiException;

/**
 * Factor for creating Mambu API Services
 * 
 * @author edanilkis
 * 
 */
public class MambuAPIFactory {

	/**
	 * Creates a mambu API services for a given username, password and domain
	 * 
	 * @param username
	 *            username to connect with to the apis
	 * @param password
	 *            password to connect with to the apis
	 * @param domainName
	 *            based domain name for the tenant (eg: mytenant.mambu.com)
	 * @return the api service
	 * @throws MambuApiException 
	 */
	public static MambuAPIService crateService(String username,
			String password, String domainName) throws MambuApiException {
		return new MambuAPIService(username, password, domainName);
	}

}
