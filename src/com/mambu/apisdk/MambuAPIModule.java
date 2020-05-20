package com.mambu.apisdk;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.mambu.apisdk.model.ApiKey;
import com.mambu.apisdk.model.ApplicationProtocol;
import com.mambu.apisdk.model.Domain;
import com.mambu.apisdk.model.Password;
import com.mambu.apisdk.model.Protocol;
import com.mambu.apisdk.model.UserAgentHeader;
import com.mambu.apisdk.model.Username;
import com.mambu.apisdk.util.RequestExecutor;
import com.mambu.apisdk.util.RequestExecutorImpl;
import com.mambu.core.shared.helper.StringUtils;

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
	private final String protocol;
	private final String userAgentHeader;
	private final String apiKey;

	/***
	 * Constructor required for setting up the date used for the wrapper to connect to the remote server
	 * 
	 * @param protocol
	 *            the protocol used for communication
	 * @param domain
	 *            the domain of the server
	 * @param username
	 *            the username required for the connection
	 * @param password
	 *            the password required for the connection
	 * @param userAgent
	 *            the User Agent
	 */
	MambuAPIModule(Protocol protocol, String domain, String username, String password, String userAgent) {

		this.protocol = Preconditions.checkNotNull(protocol, "protocol cannot be null").name();
		this.domain = Preconditions.checkNotNull(domain, "domain cannot be null");
		this.username = Preconditions.checkNotNull(username, "username cannot be null");
		this.password = Preconditions.checkNotNull(password, "password cannot be null");
		this.apiKey = StringUtils.EMPTY_STRING;
		this.userAgentHeader = Preconditions.checkNotNull(userAgent, "userAgentHeader cannot be null");

	}

	/***
	 * Constructor required for setting up the date used for the wrapper to connect to the remote server
	 *
	 * @param protocol
	 *            the protocol used for communication
	 * @param domain
	 *            the domain of the server
	 * @param apiKey
	 *            the apiKey required for the authentication
	 * @param userAgent
	 *            the User Agent
	 */
	MambuAPIModule(Protocol protocol, String domain, String apiKey, String userAgent) {

		this.protocol = Preconditions.checkNotNull(protocol, "protocol cannot be null").name();
		this.domain = Preconditions.checkNotNull(domain, "domain cannot be null");
		this.username = StringUtils.EMPTY_STRING;
		this.password = StringUtils.EMPTY_STRING;
		this.apiKey = Preconditions.checkNotNull(apiKey, "api key cannot be null");
		this.userAgentHeader = Preconditions.checkNotNull(userAgent, "userAgentHeader cannot be null");

	}

	/***
	 * Define the bindings used in the wrapper application
	 */
	@Override
	protected void configure() {

		bindConstant().annotatedWith(ApplicationProtocol.class).to(protocol);

		bindConstant().annotatedWith(Username.class).to(username);
		bindConstant().annotatedWith(Password.class).to(password);

		bindConstant().annotatedWith(ApiKey.class).to(apiKey);

		bindConstant().annotatedWith(Domain.class).to(domain);
		bindConstant().annotatedWith(UserAgentHeader.class).to(userAgentHeader);
		bind(RequestExecutor.class).to(RequestExecutorImpl.class);
	}

}
