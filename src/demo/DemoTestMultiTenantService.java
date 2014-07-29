package demo;

import com.mambu.apisdk.MambuAPIServiceFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.clients.shared.model.Client;

/**
 * Demonstration of how to use {@link MambuAPIServiceFactory} in a multi-tenant fashion, where a factory is bound to the
 * credentials.
 */
public class DemoTestMultiTenantService {

	private static Client demoClient;

	public static void main(String[] args) {
		try {
			DemoUtil.setUp();
			demoClient = DemoUtil.getDemoClient();

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
			// accessing clients with clientService2 does not work as expected
			System.out.println("Client Service by ID NOT Ok");
		} catch (MambuApiException e) {
			System.out.println("Client Service by ID Ok, invalid subdomain");
		}

		myClient = clientService.getClient(demoClient.getEncodedKey());

		// after creating another service factory object with different credentials, the original service factory still
		// works
		System.out.println("Client Service by ID still Ok, ID=" + myClient.getId());

	}

}
