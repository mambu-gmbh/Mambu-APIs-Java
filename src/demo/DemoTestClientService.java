package demo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.IdentificationDocument;
import com.mambu.core.shared.model.Address;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.Gender;

/**
 * 
 */

/**
 * @author ipenciuc
 * 
 */
public class DemoTestClientService {

	private static String CLIENT_ID = "987ABCD";
	private static String NEW_CLIENT_ID = "";
	private static String GROUP_ID = "700439187";

	private static String BRANCH_ID = "2";
	private static String CREDIT_OFFICER_USER_NAME = "demo";
	private static String CLIENT_STATE = "ACTIVE"; // PENDING_APPROVAL BLACKLISTED INACTIVE
	private static ClientExpanded clientCreated;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {

			testCreateJsonClient();
			testCreateBasicClient();
			testCreateFullDetailsClient();

			testGetClient();
			testUpdateClient();
			testGetClientDetails();

			testGetClients();

			testGetClientbyFullName();
			testGetClientByLastNameBirthday();
			testGetClientByDocIdLastName();

			testGetGroup();
			testGetGroupDetails();

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

	public static void testGetClients() {
		try {
			System.out.println("\nIn testGetClients");
			ClientsService clientService = MambuAPIFactory.getClientService();

			System.out
					.println("Successfully returned " + clientService.getClients(true).size() + " clients(active)...");
			System.out.println("Successfully returned " + clientService.getClients(false).size()
					+ " clients(inactive)...");

			System.out.println("Successfully returned " + clientService.getClients(true, 0, 10).size()
					+ " clients(active,page size of 10)...");
			System.out.println("Successfully returned " + clientService.getClients(false, 0, 10).size()
					+ " clients(inactive,page size of 10)...");
		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Clients");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	}

	public static void testGetClientbyFullName() throws MambuApiException {
		System.out.println("\nIn testGetClientbyFullName");

		ClientsService clientService = MambuAPIFactory.getClientService();

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

		ClientExpanded clientDetails = clientService.getClientDetails(NEW_CLIENT_ID);

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

		Client clientIn = new Client("Миша456", "Lastname456"); // MikeNew

		// Миша_98713

		int randomIndex = (int) (Math.random() * 1000000);
		NEW_CLIENT_ID = "Миша_98713" + Integer.toString(randomIndex);
		clientIn.setId(NEW_CLIENT_ID);
		clientIn.setLoanCycle(null);
		clientIn.setGroupLoanCycle(null);
		clientIn.setToInactive();
		clientIn.setToActive(new Date());

		Date today = new Date();
		Date tomorrow = new Date(today.getTime() + (1000 * 60 * 60 * 24));
		clientIn.setCreationDate(tomorrow);
		clientIn.setEmailAddress("MD_Json@test.ca");
		clientIn.setHomePhone("604.271.7033");
		clientIn.setMiddleName(" Middle ");
		clientIn.setMobilePhone1("1-778-2344");
		clientIn.setMobilePhone2("2-778-2344");

		// Birthday
		Calendar calendar = Calendar.getInstance();
		calendar.set(1983, 8, 15); // format: year, month, day_of_month
		Date birthdate = calendar.getTime();

		clientIn.setBirthDate(birthdate);

		// Create Expanded Client
		ClientExpanded clExpanded = new ClientExpanded(clientIn);
		// Add address
		List<Address> addresses = new ArrayList<Address>();
		Address address = new Address();
		address.setLine1("Line1JsonAddress");
		address.setCity("Kharkiv");
		address.setIndexInList(0);
		addresses.add(address);
		clExpanded.setAddresses(addresses);
		// ADd doc IDs
		List<IdentificationDocument> idDocs = new ArrayList<IdentificationDocument>();
		IdentificationDocument doc = new IdentificationDocument();
		doc.setDocumentId("PasspId");
		doc.setDocumentType("Passport");
		idDocs.add(doc);
		clExpanded.setIdDocuments(idDocs);
		List<CustomFieldValue> clientCustomInformation = new ArrayList<CustomFieldValue>();

		CustomFieldValue custField1 = new CustomFieldValue();
		String customFieldId = "Family_Size_Clients";
		String customFieldValue = "15";

		custField1.setCustomFieldId(customFieldId);
		custField1.setValue(customFieldValue);
		// Add new field to the list
		clientCustomInformation.add(custField1);

		CustomFieldValue custField2 = new CustomFieldValue();
		customFieldId = "From_Hollywood_Clients";
		customFieldValue = "TRUE";

		custField2.setCustomFieldId(customFieldId);
		custField2.setValue(customFieldValue);

		// Add new field to the list
		clientCustomInformation.add(custField2);
		CustomFieldValue custField3 = new CustomFieldValue();
		customFieldId = "Custom_Field_6_Clients";
		customFieldValue = "Custom_Field_6_Value";

		custField3.setCustomFieldId(customFieldId);
		custField3.setValue(customFieldValue);
		// Add new field to the list
		clientCustomInformation.add(custField3);
		// Add All custom fields
		clExpanded.setCustomFieldValues(clientCustomInformation);

		// Create in Mambu using Json API
		clientCreated = clientService.createClient(clExpanded);

		System.out.println("Client created, OK, ID=" + clientCreated.getClient().getId() + " Full name= "
				+ clientCreated.getClient().getFullName() + " First, Last=" + clientCreated.getClient().getFirstName());

		List<Address> addressOut = clientCreated.getAddresses();
		System.out.println("\nClient address, total=" + addressOut.size());

	}
	public static void testUpdateClient() throws MambuApiException {
		System.out.println("\nIn testUpdateClient");
		ClientsService clientService = MambuAPIFactory.getClientService();

		ClientExpanded clientUpdated = clientCreated;
		Client client = clientUpdated.getClient();
		client.setFirstName("Updated Name");
		client.setLastName("Updated Last Name");
		client.setId("newId123");

		ClientExpanded clientExpandedResult = clientService.updateClient(clientUpdated);

		System.out.println("Client Update OK, ID=" + clientExpandedResult.getClient().getId() + "\tLastName="
				+ clientExpandedResult.getClient().getLastName() + "\tFirst Name ="
				+ clientExpandedResult.getClient().getFirstName());

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
		String firstName = new String("AFirst" + Integer.toString((int) Math.random()));
		// String lastName = "Асин"; // "\u00c1\u00c9" - Spanish Unicode letters
		String lastName = "Acin"; // "\u00c1\u00c9" - Spanish Unicode letters

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
