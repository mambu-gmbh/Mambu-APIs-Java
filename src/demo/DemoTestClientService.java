package demo;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;

import com.mambu.clients.shared.model.Client;

import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.core.shared.model.Gender;

/**
 * 
 */

/**
 * @author ipenciuc
 * 
 */
public class DemoTestClientService {

	private static String CLIENT_ID = "729859576"; 

	private static String GROUP_ID = "842485684";

	public static void main(String[] args) {
		// get Logging properties file
		try {

			FileInputStream loggingFile = new FileInputStream("logging.properties");

			LogManager.getLogManager().readConfiguration(loggingFile);

		} catch (IOException e) {
			System.out.println("  Exception reading property file in Demo Test Clients");
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
		}

		try {

			MambuAPIFactory.setUp("demo.mambucloud.com", "api", "api");		
		
			testGetClient();
			testGetClientDetails();

			testGetClientbyFullName();
			testGetClientByLastNameBirthday();
			testGetClientByDocIdLastName();

			testGetGroup();
			testGetGroupDetails();

			testCreateBasicClient();
			testCreateFullDetailsClient();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Clients");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	}

	public static void testGetClient() throws MambuApiException {
		System.out.println("In testGetClient");
		ClientsService clientService = MambuAPIFactory.getClientService();

		Client myClient = clientService.getClient(CLIENT_ID);

		System.out.println("Client Service by ID Ok, ID=" + myClient.getId());

	}

	public static void testGetClientbyFullName() throws MambuApiException {
		System.out.println("In testGetClientbyFullName");

		ClientsService clientService = MambuAPIFactory.getClientService();
		;
		String lastname = "Chernaya"; // Chernaya FullClient
		String firstName = "Irina"; // Irina API

		List<Client> myCLients = clientService.getClientByFullName(lastname, firstName);

		int clientsTotal = myCLients.size();
		System.out.println("testGetClientbyFullName OK  by Full Name; total=" + clientsTotal);
		if (clientsTotal > 0) {
			System.out.println("testGetClientbyFullName OK, Full Name=" + myCLients.get(0).getFullName());
		}

	}

	public static void testGetClientByLastNameBirthday() throws MambuApiException {
		System.out.println("In testGetClientByLastNameBirthday");

		ClientsService clientService = MambuAPIFactory.getClientService();

		String birthDay = "1990-01-12"; // yyy-MM-dd
		String lastName = "Chernaya";

		List<Client> myCLients = clientService.getClientByLastNameBirthday(lastName, birthDay);

		int clientsTotal = myCLients.size();
		System.out.println("testGetClientByLastNameBirthday OK, total clients=" + clientsTotal);
		if (clientsTotal > 0) {
			System.out.println("testGetClientByLastNameBirthday, first Full Name=" + myCLients.get(0).getFullName());
		}

	}

	public static void testGetClientDetails() throws MambuApiException {
		System.out.println("In testGetClientDetails");

		ClientsService clientService = MambuAPIFactory.getClientService();

		ClientExpanded clientDetails = clientService.getClientDetails(CLIENT_ID);

		System.out.println("testGetClientDetails Ok, name=" + clientDetails.getClient().getFullName()
				+ ".  and Name +id " + clientDetails.getClient().getFullNameWithId());

	}

	public static void testGetClientByDocIdLastName() throws MambuApiException {
		String lastName = "Chernaya";
		String documentId = "BW777889900";
		System.out.println("In testGetClientByDocIdLastName: " + documentId + " " + lastName);
		;
		ClientsService clientService = MambuAPIFactory.getClientService();

		List<Client> myCLients = clientService.getClientByLastNameDocId(lastName, documentId);

		int clientsTotal = myCLients.size();
		System.out.println("testGetClientByDocIdLastName OK,, total clients=" + clientsTotal);
		if (clientsTotal > 0) {
			System.out
					.println("testGetClientByDocIdLastName, first client Full Name=" + myCLients.get(0).getFullName());
		}

	}

	public static void testGetGroup() throws MambuApiException {
		System.out.println("In testGetGroup");
		ClientsService clientService = MambuAPIFactory.getClientService();

		System.out.println("testGetGroup OK, name=" + clientService.getGroup(GROUP_ID).getGroupName());

	}

	public static void testGetGroupDetails() throws MambuApiException {
		System.out.println("In testGetGroupDetails");

		ClientsService clientService = MambuAPIFactory.getClientService();

		System.out.println("testGetGroupDetails Ok, name="
				+ clientService.getGroupDetails(GROUP_ID).getGroup().getGroupName());

	}

	public static void testCreateBasicClient() throws MambuApiException {
		System.out.println("In testCreateBasicClient");

		ClientsService clientService = MambuAPIFactory.getClientService();

		Client client = clientService.createClient("API", "Client");

		System.out.println("Client created, OK, full name= " + client.getFullName());

	}

	public static void testCreateFullDetailsClient() throws MambuApiException {
		System.out.println("In test Create Full Details Client");

		ClientsService clientService = MambuAPIFactory.getClientService();
		String firstName = "FirstName";
		String lastName = "LastName";
		String homephone = null;
		String mobilephone = null;
		String gender = Gender.MALE.toString();
		String birthdate = "1982-01-12"; // format: "yyyy-MM-dd"
		String email = null;
		String notes = null;

		Client client = clientService.createClient(firstName, lastName, homephone, mobilephone, gender, birthdate,
				email, notes);
		// MD: was incorrect date birthday date format: should be "yyyy-MM-dd" ("09-03-1980",)

		System.out.println("Client created, id=" + client.getId() + "  Full name=" + client.getFullName());

	}
}
