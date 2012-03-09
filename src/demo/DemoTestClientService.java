package demo;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;

/**
 * 
 */

/**
 * @author ipenciuc
 * 
 */
public class DemoTestClientService {

	private static String CLIENT_ID = "850373088";
	private static String GROUP_ID = "411039390";

	public static void main(String[] args) {

		try {
			MambuAPIFactory.setUp("demo.mambu.com", "api", "api");

			testGetClient();
			testGetGroup();
			testGetGroupDetails();
			testCreateBasicClient();
			testCreateFullDetailsClient();
		} catch (MambuApiException e) {
			System.out.println(e.getCause());
			System.out.println(e.getMessage());
		}
	}

	public static void testGetClient() throws MambuApiException {

		ClientsService clientService = MambuAPIFactory.getClientService();

		System.out.println(clientService.getClient(CLIENT_ID).getFullName());

	}

	public static void testGetGroup() throws MambuApiException {

		ClientsService clientService = MambuAPIFactory.getClientService();

		System.out.println(clientService.getGroup(GROUP_ID).getGroupName());

	}

	public static void testGetGroupDetails() throws MambuApiException {

		ClientsService clientService = MambuAPIFactory.getClientService();

		System.out.println(clientService.getGroupDetails(GROUP_ID).getGroup().getGroupName());

	}

	public static void testCreateBasicClient() throws MambuApiException {

		ClientsService clientService = MambuAPIFactory.getClientService();

		System.out.println(clientService.createClient("API", "Client"));

	}

	public static void testCreateFullDetailsClient() throws MambuApiException {

		ClientsService clientService = MambuAPIFactory.getClientService();

		System.out.println(clientService.createClient("API", "FullClient", "09", "23", "MALE", "09-03-1980",
				"client@mambu.com", "some notes").toString());

	}
}
