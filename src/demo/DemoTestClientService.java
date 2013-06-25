package demo;

import java.util.Date;
import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.core.shared.model.Gender;

/**
 * 
 */

/**
 * @author ipenciuc
 * 
 */
public class DemoTestClientService {

	private static String CLIENT_ID = "363317853"; // 250213653 078911120 046360136 729859576 078911120 250213653
													// 363317853

	private static String GROUP_ID = "588752540"; // 414659806 588752540

	private static String BRANCH_ID = "Richmond01";
	private static String CREDIT_OFFICER_USER_NAME = "MichaelD";
	private static String CLIENT_STATE = "ACTIVE"; // PENDING_APPROVAL BLACKLISTED INACTIVE

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			// testCreateJsonClient();

			testGetClient();
			testGetClientDetails();

			testCreateFullDetailsClient();

			testGetClientbyFullName();
			testGetClientByLastNameBirthday();
			testGetClientByDocIdLastName();

			testGetGroup();

			testGetGroupDetails();

			testCreateBasicClient();

			testGetClientsByBranchOfficerState();
			testGetGroupsByBranchOfficer();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Clients");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	}
	public static void testGetClient() throws MambuApiException {
		System.out.println("\nIn testGetClient");
		ClientsService clientService = MambuAPIFactory.getClientService();

		Client myClient = clientService.getClient(CLIENT_ID);

		System.out.println("Client Service by ID Ok, ID=" + myClient.getId());

	}

	public static void testGetClientbyFullName() throws MambuApiException {
		System.out.println("\nIn testGetClientbyFullName");

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
		System.out.println("\nIn testGetClientByLastNameBirthday");

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
		System.out.println("\nIn testGetClientDetails");

		ClientsService clientService = MambuAPIFactory.getClientService();

		ClientExpanded clientDetails = clientService.getClientDetails(CLIENT_ID);

		System.out.println("testGetClientDetails Ok, name=" + clientDetails.getClient().getFullName()
				+ ".  and Name +id " + clientDetails.getClient().getFullNameWithId());

	}

	public static void testGetClientByDocIdLastName() throws MambuApiException {
		System.out.println("\nIn testGetClientByDocIdLastName");

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
		System.out.println("\nIn testGetGroup");
		ClientsService clientService = MambuAPIFactory.getClientService();

		System.out.println("testGetGroup OK, name=" + clientService.getGroup(GROUP_ID).getGroupName());

	}

	public static void testGetGroupDetails() throws MambuApiException {
		System.out.println("\nIn testGetGroupDetails");

		ClientsService clientService = MambuAPIFactory.getClientService();

		System.out.println("testGetGroupDetails Ok, name="
				+ clientService.getGroupDetails(GROUP_ID).getGroup().getGroupName());

	}

	public static void testCreateJsonClient() throws MambuApiException {
		System.out.println("\nIn testCreateJsonClient");

		ClientsService clientService = MambuAPIFactory.getClientService();
		Client clientIn = new Client("FirstName1", "Lastname1");
		clientIn.setApprovedDate(new Date());
		clientIn.setEmailAddress("mjson@test.ca");
		clientIn.setHomePhone("604-271-7033");

		Client client = clientService.createClient(clientIn); // Spanish accented A \u00c1 and \u00c9
																// accented E

		System.out.println("Client created, OK, ID=" + client.getId() + " Full name= " + client.getFullName());

	}
	public static void testCreateBasicClient() throws MambuApiException {
		System.out.println("\nIn testCreateBasicClient");

		ClientsService clientService = MambuAPIFactory.getClientService();

		Client client = clientService.createClient("\u00c1\u00c9", "Client"); // Spanish accented A \u00c1 and \u00c9
																				// accented E
		// Client client = clientService.createClient("XYZ", "MD"); // Spanish accented A \u00c1 \u00c9 E

		System.out.println("Client created, OK, full name= " + client.getFullName());

	}
	public static void testCreateFullDetailsClient() throws MambuApiException {
		System.out.println("\nIn test Create Full Details Client");

		ClientsService clientService = MambuAPIFactory.getClientService();
		// String firstName = new String("\u0416" + "\u041A"); // Russian Unicode letetrs
		// String firstName = new String("AB" + "\u0416"); // Russian Unicode letetrs
		String firstName = new String("AFirst" + Integer.toString((int) Math.random()));
		String lastName = "Асин"; // "\u00c1\u00c9" - Spanish Unicode letters

		String homephone = "1-778-980-234";
		String mobilephone = "980-456-789";
		String gender = Gender.MALE.toString();
		String birthdate = "1982-01-12"; // format: "yyyy-MM-dd"
		String email = "abc@next.com";
		String notes = "created by API Demo program";

		Client client = clientService.createClient(firstName, lastName, homephone, mobilephone, gender, birthdate,
				email, notes);

		System.out.println("Client created, id=" + client.getId() + "  Full name=" + client.getFullName());

	}

	public static void testGetClientsByBranchOfficerState() throws MambuApiException {
		System.out.println("\nIn testGetClientsByBranchOfficerState");

		ClientsService clientService = MambuAPIFactory.getClientService();

		String branchId = BRANCH_ID;
		String creditOfficerUserName = CREDIT_OFFICER_USER_NAME;
		String clientState = CLIENT_STATE; // ACTIVE PENDING_APPROVAL BLACKLISTED INACTIVE
		String offset = "0";
		String limit = "1";
		List<Client> clients = clientService.getClientsByBranchOfficerState(branchId, creditOfficerUserName,
				clientState, offset, limit);

		if (clients != null)
			System.out.println("Got  Clients for the branch, officer, state, total clients=" + clients.size());
		for (Client client : clients) {
			System.out.println("Client Name=" + client.getFullName() + "  BranchId=" + client.getAssignedBranchKey()
					+ "   Credit Officer id=" + client.getAssignedUserKey());
		}
	}
	public static void testGetGroupsByBranchOfficer() throws MambuApiException {
		System.out.println("\nIn testGetGroupsByBranchOfficer");

		ClientsService clientService = MambuAPIFactory.getClientService();

		String branchId = null; // BRANCH_ID;// "RICHMOND_001"; //Berlin_001 REICHMOND_001
		String creditOfficerUserName = CREDIT_OFFICER_USER_NAME; //
		String offset = "1";
		String limit = "1";
		List<Group> groups = clientService.getGroupsByBranchOfficer(branchId, creditOfficerUserName, offset, limit);

		if (groups != null)
			System.out.println("Got  Groups for the branch, officer, total groups=" + groups.size());
		for (Group group : groups) {
			System.out.println("Group Name=" + group.getGroupName() + "  BranchId=" + group.getAssignedBranchKey()
					+ "   Credit Officer id=" + group.getAssignedUserKey());
		}
	}
}
