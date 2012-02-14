/**
 * 
 */
package com.mambu.apisdk;

import com.google.inject.AbstractModule;
import com.mambu.apisdk.model.Domain;
import com.mambu.apisdk.model.Password;
import com.mambu.apisdk.model.Username;
import com.mambu.apisdk.util.RequestExecutor;
import com.mambu.apisdk.util.RequestExecutorImpl;

/**
 * Configuration class for the Guice bindings
 * 
 * @author ipenciuc
 * 
 */
public class MambuAPIModule extends AbstractModule {

	private final String username;
	private final String password;
	private final String domain;

	/***
	 * Constructor required for setting up the date used for the wrapper to connect to the remote server
	 * 
	 * @param domain
	 *            the domain of the server
	 * @param username
	 *            the username required for the connection
	 * @param password
	 *            the password required for the connection
	 */
	public MambuAPIModule(String domain, String username, String password) {

		this.domain = domain;
		this.username = username;
		this.password = password;

	}
	/***
	 * Define the bindings used in the wrapper application
	 */
	@Override
	protected void configure() {

		bindConstant().annotatedWith(Username.class).to(username);
		bindConstant().annotatedWith(Password.class).to(password);
		bindConstant().annotatedWith(Domain.class).to(domain);

		bind(RequestExecutor.class).to(RequestExecutorImpl.class);

	}

}
