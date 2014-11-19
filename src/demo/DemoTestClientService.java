package demo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.ClientState;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.clients.shared.model.IdentificationDocument;
import com.mambu.core.shared.model.Address;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;

/**
 * 
 */

/**
 * @author ipenciuc
 * 
 */
public class DemoTestClientService {

	private static String NEW_CLIENT_ID;
	private static ClientExpanded clientCreated;

	private static Client demoClient;
	private static Group demoGroup;
	private static User demoUser;

	public static void main(String[] args) {

		DemoUtil.setUp();

		try {
			demoUser = DemoUtil.getDemoUser();
			demoClient = DemoUtil.getDemoClient();
			demoGroup = DemoUtil.getDemoGroup();

			testCreateJsonClient();

			testGetClient();
			testUpdateClient();
			testGetClientDetails();

			testGetClients();

			testGetClientbyFullName();
			testGetClientByLastNameBirthday();
			testGetClientByDocIdLastName();

			testGetGroup();
			testGetGroupDetails();

			testGetClientsByBranchCentreOfficerState();
			testGetGroupsByBranchCentreOfficer();

			testGetDocuments();

			// Available since 3.8
			testUpdateDeleteCustomFields();

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Clients");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	}

	public static void testGetClient() throws MambuApiException {
		System.out.println("\nIn testGetClient");
		ClientsService clientService = MambuAPIFactory.getClientService();

		Client myClient = clientService.getClient(demoClient.getEncodedKey());

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

		String lastname = demoClient.getLastName(); // Chernaya FullClient
		String firstName = demoClient.getFirstName(); // Irina API

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
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		String birthDay = df.format(demoClient.getBirthDate()); // yyy-MM-dd
		String lastName = demoClient.getLastName();

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

		String lastName = demoClient.getLastName();

		// Get Doc ID for testing from the demo client
		ClientsService clientService = MambuAPIFactory.getClientService();
		// Get demo client with details
		ClientExpanded clientDetails = clientService.getClientDetails(demoClient.getId());
		// Get doc id, if any
		List<IdentificationDocument> docs = clientDetails.getIdDocuments();
		String documentId = (docs == null || docs.size() == 0) ? null : docs.get(0).getDocumentId();

		System.out.println("In testGetClientByDocIdLastName: " + documentId + " " + lastName);

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

		System.out.println("testGetGroup OK, name=" + clientService.getGroup(demoGroup.getId()).getGroupName());

	}

	public static void testGetGroupDetails() throws MambuApiException {
		System.out.println("\nIn testGetGroupDetails");

		ClientsService clientService = MambuAPIFactory.getClientService();

		System.out.println("testGetGroupDetails Ok, name="
				+ clientService.getGroupDetails(demoGroup.getId()).getGroup().getGroupName());

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

		ClientExpanded clientExpandedResult = clientService.updateClient(clientUpdated);

		System.out.println("Client Update OK, ID=" + clientExpandedResult.getClient().getId() + "\tLastName="
				+ clientExpandedResult.getClient().getLastName() + "\tFirst Name ="
				+ clientExpandedResult.getClient().getFirstName());

	}

	public static void testGetClientsByBranchCentreOfficerState() throws MambuApiException {
		System.out.println("\nIn testGetClientsByBranchCentreOfficerState");

		ClientsService clientService = MambuAPIFactory.getClientService();

		String branchId = demoClient.getAssignedBranchKey();
		String centreId = demoClient.getAssignedCentreKey(); // Centre ID filter is available since 3.7
		String creditOfficerUserName = demoUser.getUsername();
		String clientState = ClientState.ACTIVE.name(); // ACTIVE PENDING_APPROVAL BLACKLISTED INACTIVE
		String offset = "0";
		String limit = "10";
		List<Client> clients = clientService.getClientsByBranchCentreOfficerState(branchId, centreId,
				creditOfficerUserName, clientState, offset, limit);

		if (clients != null)
			System.out.println("Got  Clients for the branch, officer, centre, state, total clients=" + clients.size());
		for (Client client : clients) {
			System.out.println("Client Name=" + client.getFullName() + "\tBranchId=" + client.getAssignedBranchKey()
					+ "\tCentreId=" + client.getAssignedCentreKey() + "\tCredit Officer id="
					+ client.getAssignedUserKey());
		}
	}

	public static void testGetGroupsByBranchCentreOfficer() throws MambuApiException {
		System.out.println("\nIn testGetGroupsByBranchCentreOfficer");

		ClientsService clientService = MambuAPIFactory.getClientService();

		String branchId = demoClient.getAssignedBranchKey();
		String centreId = demoClient.getAssignedCentreKey(); // Centre ID filter is available since 3.7
		String creditOfficerUserName = demoUser.getUsername();
		String offset = "0";
		String limit = "10";
		List<Group> groups = clientService.getGroupsByBranchCentreOfficer(branchId, centreId, creditOfficerUserName,
				offset, limit);

		if (groups != null)
			System.out.println("Got  Groups for the branch, officer, total groups=" + groups.size());
		for (Group group : groups) {
			System.out.println("Group Name=" + group.getGroupName() + "\tBranchId=" + group.getAssignedBranchKey()
					+ "\tCentreId=" + group.getAssignedCentreKey() + "\tCredit Officer id="
					+ group.getAssignedUserKey());
		}
	}

	public static void testGetDocuments() throws MambuApiException {
		System.out.println("\nIn testGetDocuments");

		// Getting Documents for a Client
		ClientsService clientService = MambuAPIFactory.getClientService();
		List<Document> documents = clientService.getClientDocuments(demoClient.getId());

		// Log returned documents using DemoTestDocumentsService helper
		System.out.println("Documents returned for a Client with ID=" + demoClient.getId());
		DemoTestDocumentsService.logDocuments(documents);

		// Getting Documents for a Group
		documents = clientService.getGroupDocuments(demoGroup.getId());

		// Log returned documents using DemoTestDocumentsService helper
		System.out.println("Documents returned for a Group with ID=" + demoGroup.getId());
		DemoTestDocumentsService.logDocuments(documents);

	}

	// Update Custom Field values for the client and for the group and delete the first custom field
	public static void testUpdateDeleteCustomFields() throws MambuApiException {
		System.out.println("\nIn testUpdateDeleteCustomFields");

		List<CustomFieldValue> customFieldValues;
		System.out.println("\nUpdating demo Client custom fields...");
		customFieldValues = updateCustomFields(Client.class);

		System.out.println("\nDeleting first custom field for a demo Client...");
		deleteCustomField(Client.class, customFieldValues);

		System.out.println("\n\nUpdating demo Group custom fields...");
		customFieldValues = updateCustomFields(Group.class);

		System.out.println("\nDeleting first custom field for a demo Group...");
		deleteCustomField(Group.class, customFieldValues);

	}

	// Private helper to Update all custom fields for a client or group
	private static List<CustomFieldValue> updateCustomFields(Class<?> entityClass) throws MambuApiException {
		ClientsService clientService = MambuAPIFactory.getClientService();

		String entityName = entityClass.getSimpleName();
		boolean forClient = (entityClass.equals(Client.class)) ? true : false;
		String entityId = (forClient) ? demoClient.getId() : demoGroup.getId();

		// Get Current custom field values first. We need and entity with full details
		List<CustomFieldValue> customFieldValues;
		if (forClient) {
			ClientExpanded clientExpanded = clientService.getClientDetails(entityId);
			customFieldValues = clientExpanded.getCustomFieldValues();
		} else {
			GroupExpanded groupExpanded = clientService.getGroupDetails(entityId);
			customFieldValues = groupExpanded.getCustomFieldValues();
		}

		if (customFieldValues == null || customFieldValues.size() == 0) {
			System.out.println("WARNING: No Custom fields defined for demo " + entityName + " with ID=" + entityId
					+ ". Nothing to update");
			return null;
		}
		// Update custom field values
		for (CustomFieldValue value : customFieldValues) {
			// TODO: re-test getting fieldID via getCustomFieldId() for Group when MBU-6923 is fixed. Null for Group
			String testFieldId = value.getCustomFieldId(); // returns null for Group

			// Use customFieldId from the CustomField, this always works
			CustomField field = value.getCustomField();
			String fieldId = field.getId();

			// Create valid new value for a custom field
			String newValue = DemoUtil.makeNewCustomFieldValue(value);

			// Update Custom Field value
			boolean updateStatus;
			System.out.println("\nUpdating Custom Field with ID=" + fieldId + " for " + entityName + " with ID="
					+ entityId + "\tField's other ID=" + testFieldId);

			updateStatus = (forClient) ? clientService.updateClientCustomField(entityId, fieldId, newValue)
					: clientService.updateGroupCustomField(entityId, fieldId, newValue);

			String statusMessage = (updateStatus) ? "Success" : "Failure";
			System.out.println(statusMessage + " updating Custom Field, ID=" + fieldId + " for demo " + entityName
					+ " with ID=" + entityId + " New value=" + newValue);

		}

		return customFieldValues;
	}

	// Private helper to Delete the first custom field for a client or group
	private static void deleteCustomField(Class<?> entityClass, List<CustomFieldValue> customFieldValues)
			throws MambuApiException {

		String entityName = entityClass.getSimpleName();
		boolean forClient = (entityClass.equals(Client.class)) ? true : false;
		String entityId = (forClient) ? demoClient.getId() : demoGroup.getId();

		if (customFieldValues == null || customFieldValues.size() == 0) {
			System.out.println("WARNING: No Custom fields defined for demo " + entityName + " with ID=" + entityId
					+ ". Nothing to delete");
			return;
		}

		// Delete the first field on the list
		String customFieldId = customFieldValues.get(0).getCustomField().getId();

		ClientsService clientService = MambuAPIFactory.getClientService();
		boolean deleteStatus = (forClient) ? clientService.deleteClientCustomField(entityId, customFieldId)
				: clientService.deleteGroupCustomField(entityId, customFieldId);

		String statusMessage = (deleteStatus) ? "Success" : "Failure";
		System.out.println(statusMessage + " deleting Custom Field, ID=" + customFieldId + " for demo " + entityName
				+ " with ID=" + entityId);
	}
}
