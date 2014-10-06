package demo;

import com.mambu.apisdk.MambuAPIServiceFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.apisdk.services.UsersService;
import com.mambu.clients.shared.model.Client;
import com.mambu.core.shared.model.User;

/**
 * Demonstration of how to use {@link MambuAPIServiceFactory} in a multi-tenant fashion, where a factory is bound to the
 * credentials.
 */
public class DemoTestMultiTenantService {

	private static Client demoClient;
	private static Client demoClient2;

	public static void main(String[] args) {
		try {
			DemoUtil.setUp();
			demoClient = DemoUtil.getDemoClient();
			demoClient2 = DemoUtil.getDemoClient(true);

			testGetClientMultiTenant();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Multi-Tenant");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	}

	public static void testGetClientMultiTenant() throws MambuApiException {

		System.out.println("\nIn testGetClientMultiTenant");

		// service factory object bound to an existing Mambu sandbox account
		MambuAPIServiceFactory serviceFactory = DemoUtil.getAPIServiceFactory();

		// tenant specific client service
		ClientsService clientService = serviceFactory.getClientService();

		Client myClient = clientService.getClient(demoClient.getEncodedKey());

		// accessing clients with clientService works
		System.out.println("Client Service by ID Ok, ID=" + myClient.getId());

		// service factory object bound to a non-existing Mambu sandbox account
		MambuAPIServiceFactory serviceFactory2 = MambuAPIServiceFactory.getFactory(
				"non-existing-subdomain.sandbox.mambu.com", "non-existing-user", "non-existing-password");

		// tenant specific client service
		ClientsService clientService2 = serviceFactory2.getClientService();

		try {
			clientService2.getClient(demoClient.getEncodedKey());
			// the above statement should throw an exception and therefore the below code should not be executed
			// accessing clients with clientService2 should not work
			System.out.println("Client Service by ID NOT Ok, should have thrown an exception");
		} catch (MambuApiException e) {
			// expected exception due to invalid subdomain
			System.out
					.println("Client Service by ID Ok, an exception was caught as expected due to an invalid subdomain");
		}

		myClient = clientService.getClient(demoClient.getEncodedKey());

		// after creating another service factory object with different credentials, the original service factory still
		// works
		System.out.println("Client Service by ID still Ok, ID=" + myClient.getId());

		// service factory object bound to another existing Mambu account
		MambuAPIServiceFactory serviceFactory3 = DemoUtil.getAPIServiceFactory(true);
		ClientsService clientService3 = serviceFactory3.getClientService();

		// another demo client exists in another Mambu account
		myClient = clientService3.getClient(demoClient2.getEncodedKey());
		System.out.println("Client Service by ID Ok, ID=" + myClient.getId());
		String otherClientId = myClient.getId();

		// the first demo client can still be retrieved
		myClient = clientService.getClient(demoClient.getEncodedKey());
		if (otherClientId.equals(myClient.getId())) {
			System.out.println("Client Service by ID *NOT* Ok since both clients have same ID, ID=" + myClient.getId());
		} else {
			System.out.println("Client Service by ID Ok since different ID, ID=" + myClient.getId());
		}
	}

	public static void testGetUserMultiTenant() throws MambuApiException {

		System.out.println("\nIn testGetUserMultiTenant");

		// service factory object bound to an existing Mambu sandbox account
		MambuAPIServiceFactory serviceFactory = DemoUtil.getAPIServiceFactory();

		// tenant specific user service
		UsersService usersService = serviceFactory.getUsersService();

		User myUser = usersService.getUserByUsername(DemoUtil.demoUsername);

		// accessing clients with clientService works
		System.out.println("User Service by Username Ok, ID=" + myUser.getId());

		// service factory object bound to a non-existing Mambu sandbox account
		MambuAPIServiceFactory serviceFactory2 = MambuAPIServiceFactory.getFactory(
				"non-existing-subdomain.sandbox.mambu.com", "non-existing-user", "non-existing-password");

		// tenant specific client service
		UsersService usersService2 = serviceFactory2.getUsersService();

		try {
			usersService2.getUserByUsername(DemoUtil.demoUsername);
			// the above statement should throw an exception and therefore the below code should not be executed
			// accessing users with usersService2 should not work
			System.out.println("Client Service by ID NOT Ok, should have thrown an exception");
		} catch (MambuApiException e) {
			// expected exception due to invalid subdomain
			System.out
					.println("User Service by Username Ok, an exception was caught as expected due to an invalid subdomain");
		}

		myUser = usersService.getUserByUsername(DemoUtil.demoUsername);

		// after creating another service factory object with different credentials, the original service factory still
		// works
		System.out.println("User Service by Username still Ok, ID=" + myUser.getId());

	}

}
