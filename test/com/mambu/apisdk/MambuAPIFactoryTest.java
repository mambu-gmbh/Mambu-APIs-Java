package com.mambu.apisdk;

import static com.mambu.apisdk.MambuAPIFactory.DEFAULT_USER_AGENT_HEADER_VALUE;
import static com.mambu.apisdk.MambuAPIFactory.setUpWithApiKey;
import static com.mambu.apisdk.model.Protocol.HTTPS;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author cezarrom
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Guice.class, MambuAPIFactory.class})
public class MambuAPIFactoryTest {


	private static final String SOME_DOMAIN = "someDomain";
	private static final String SOME_API_KEY = "someApiKey";
	private static final String SOME_USER_AGENT = "someUserAgent";

	@Mock
	private Injector injectorMock;

	@Mock
	private MambuAPIModule mambuAPIModuleMock;

	@Before
	public void setUp() throws Exception {

		whenNew(MambuAPIModule.class).withAnyArguments()
				.thenReturn(mambuAPIModuleMock);

		mockStatic(Guice.class);

		when(Guice.createInjector(mambuAPIModuleMock)).thenReturn(injectorMock);
	}

	@Test
	public void givenProtocolAndDomainAndApiKeyWhenSetUpWithApiKeyThenCorrespondingMambuAPIModuleIsCreated() throws Exception {

		setUpWithApiKey(HTTPS, SOME_DOMAIN, SOME_API_KEY);

		verifyNew(MambuAPIModule.class).withArguments(HTTPS, SOME_DOMAIN, SOME_API_KEY, DEFAULT_USER_AGENT_HEADER_VALUE);

	}

	@Test
	public void givenDomainAndApiKeyWhenSetUpWithApiKeyThenCorrespondingMambuAPIModuleIsCreated() throws Exception {

		setUpWithApiKey(SOME_DOMAIN, SOME_API_KEY);

		verifyNew(MambuAPIModule.class).withArguments(HTTPS, SOME_DOMAIN, SOME_API_KEY, DEFAULT_USER_AGENT_HEADER_VALUE);

	}

	@Test
	public void givenProtocolAndDomainAndApiKeyAndUserAgentWhenSetUpWithApiKeyThenCorrespondingMambuAPIModuleIsCreated() throws Exception {

		setUpWithApiKey(HTTPS, SOME_DOMAIN, SOME_API_KEY, SOME_USER_AGENT);

		verifyNew(MambuAPIModule.class).withArguments(HTTPS, SOME_DOMAIN, SOME_API_KEY, SOME_USER_AGENT);

	}

	@Test
	public void givenDomainAndApiKeyAndUserAgentWhenSetUpWithApiKeyThenCorrespondingMambuAPIModuleIsCreated() throws Exception {

		setUpWithApiKey(SOME_DOMAIN, SOME_API_KEY, SOME_USER_AGENT);

		verifyNew(MambuAPIModule.class).withArguments(HTTPS, SOME_DOMAIN, SOME_API_KEY, SOME_USER_AGENT);

	}
}