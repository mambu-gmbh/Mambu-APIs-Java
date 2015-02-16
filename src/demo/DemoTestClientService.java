package demo;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.mambu.accounts.shared.model.AccountHolderType;
import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.apisdk.MambuAPIFactory;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.ClientsService;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.ClientState;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.clients.shared.model.GroupMember;
import com.mambu.clients.shared.model.GroupRole;
import com.mambu.clients.shared.model.GroupRoleName;
import com.mambu.clients.shared.model.IdentificationDocument;
import com.mambu.core.shared.model.Address;
import com.mambu.core.shared.model.ClientRole;
import com.mambu.core.shared.model.ClientRolePermission;
import com.mambu.core.shared.model.CustomField;
import com.mambu.core.shared.model.CustomField.Type;
import com.mambu.core.shared.model.CustomFieldValue;
import com.mambu.core.shared.model.User;
import com.mambu.docs.shared.model.Document;
import com.mambu.docs.shared.model.OwnerType;

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

			testUpdateDeleteCustomFields(); // Available since 3.8

			GroupExpanded createdGroup = testCreateGroup(); // Available since 3.9

			// TODO: uncomment testUpdateGroup() to test UPDATE group API when it's ready in 3.10 (MBU-7337)
			// testUpdateGroup(createdGroup);// To be available in 3.10

			testGetClientTypes(); // Available since 3.9
			testGetGroupsRoles();// Available since 3.9

			uploadClientProfileFiles(); // Available since 3.9
			getClientProfileFiles(); // Available since 3.9
			deleteClientProfileFiles(); // Available since 3.9

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

			System.out.println("Successfully returned " + clientService.getClients(true, 0, 10).size()
					+ " clients(active,page size of 10)...");
			System.out.println("Successfully returned " + clientService.getClients(false, 0, 10).size()
					+ " clients (inactive, page size of 10)...");
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

		ClientExpanded clientDetails = clientService.getClientDetails(demoClient.getId());
		Client client = clientDetails.getClient();

		System.out.println("testGetClientDetails Ok, name=" + client.getFullName() + "\tName +id "
				+ client.getFullNameWithId() + "\tClient Type=" + client.getClientRole().getName());

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

	private static final String apiTestFirstNamePrefix = "Demo Name ";
	private static final String apiTestLastNamePrefix = "API Client";

	public static void testCreateJsonClient() throws MambuApiException {
		System.out.println("\nIn testCreateJsonClient");

		ClientsService clientService = MambuAPIFactory.getClientService();
		int randomIndex = (int) (Math.random() * 10000);
		NEW_CLIENT_ID = Integer.toString(randomIndex);
		Client clientIn = new Client(apiTestFirstNamePrefix + NEW_CLIENT_ID, apiTestLastNamePrefix + NEW_CLIENT_ID);
		clientIn.setId(NEW_CLIENT_ID);
		clientIn.setLoanCycle(null);
		clientIn.setGroupLoanCycle(null);
		clientIn.setToInactive();
		clientIn.setToActive(new Date());

		Date today = new Date();
		Date tomorrow = new Date(today.getTime() + (1000 * 60 * 60 * 24));
		clientIn.setCreationDate(tomorrow);
		clientIn.setEmailAddress("apiDemo@apidemo.mambu.com");
		clientIn.setHomePhone("778.271.7033");
		clientIn.setMiddleName(" Middle ");
		clientIn.setMobilePhone1("1-778-2344");
		clientIn.setMobilePhone2("2-778-2344");

		// Birthday
		Calendar calendar = Calendar.getInstance();
		calendar.set(1983, 8, 15); // format: year, month, day_of_month
		Date birthdate = calendar.getTime();

		clientIn.setBirthDate(birthdate);

		// Set client role. Required since Mambu 3.9
		List<ClientRole> clientTypes = clientService.getClientTypes(AccountHolderType.CLIENT);
		ClientRole cientRole = clientTypes.get(0);
		clientIn.setClientRole(cientRole);

		// Add client assignments, they might be mandatory
		String assignedTo = (demoUser.isCreditOfficer()) ? demoUser.getEncodedKey() : null;
		clientIn.setAssignedUserKey(assignedTo);
		clientIn.setAssignedBranchKey(demoUser.getAssignedBranchKey());
		clientIn.setAssignedCentreKey(demoUser.getAssignedCentreKey());

		// Create Expanded Client
		ClientExpanded clExpanded = new ClientExpanded(clientIn);
		// Add address
		List<Address> addresses = new ArrayList<Address>();
		Address address = new Address();
		address.setLine1("Line 1 street address");
		address.setLine2("Line 2 street address");
		address.setCity("Berlin");
		address.setIndexInList(0);
		addresses.add(address);
		clExpanded.setAddresses(addresses);
		// ADd doc IDs
		List<IdentificationDocument> idDocs = new ArrayList<IdentificationDocument>();
		IdentificationDocument doc = new IdentificationDocument();
		doc.setDocumentId("DFG6778899");
		doc.setDocumentType("Passport");
		idDocs.add(doc);
		clExpanded.setIdDocuments(idDocs);
		// Use helper to make test custom fields which are valid for the client's role
		List<CustomFieldValue> clientCustomInformation = DemoUtil.makeForEntityCustomFieldValues(Type.CLIENT_INFO,
				cientRole.getEncodedKey());
		// Add All custom fields
		clExpanded.setCustomFieldValues(clientCustomInformation);

		// Create in Mambu using Json API
		clientCreated = clientService.createClient(clExpanded);

		System.out.println("Client created, OK, ID=" + clientCreated.getClient().getId() + " Full name= "
				+ clientCreated.getClient().getFullName() + " First, Last=" + clientCreated.getClient().getFirstName());

		List<Address> addressOut = clientCreated.getAddresses();
		System.out.println("\nClient address, total=" + addressOut.size());

	}

	private static final String updatedSuffix = "_ApiUpdated";

	public static void testUpdateClient() throws MambuApiException {
		System.out.println("\nIn testUpdateClient");
		ClientsService clientService = MambuAPIFactory.getClientService();

		ClientExpanded clientUpdated = clientCreated;
		Client client = clientUpdated.getClient();
		client.setFirstName(client.getFirstName() + updatedSuffix);
		client.setLastName(client.getLastName() + updatedSuffix);

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
		return;
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

	public static void testGetGroupsRoles() throws MambuApiException {
		System.out.println("\nIn testGetGroupsRoles");

		ClientsService clientService = MambuAPIFactory.getClientService();

		List<GroupRoleName> groupRoleNames = clientService.getGroupRoleNames();

		int totalRoles = (groupRoleNames == null) ? 0 : groupRoleNames.size();
		if (totalRoles == 0) {
			System.out.println("No group roles returned");
			return;
		}
		for (GroupRoleName role : groupRoleNames) {
			System.out.println("Group Role=" + role.getName() + "\tGroup Role Name Key=" + role.getEncodedKey());

			// Now test getting details for a specific role
			GroupRoleName roleDeatils = clientService.getGroupRoleName(role.getEncodedKey());
			System.out.println("OK for full Group Role details. Group Role Name=" + roleDeatils.getName());

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

		// Get Current custom field values first. We need an entity with full details
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
			String newValue = DemoUtil.makeNewCustomFieldValue(value).getValue();

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

	// Test getting client types
	public static void testGetClientTypes() throws MambuApiException {
		System.out.println("\nIn testGetClientTypes");

		ClientsService clientService = MambuAPIFactory.getClientService();

		AccountHolderType clientType = null; // AccountHolderType.CLIENT;
		// Get Client Types from Mambu via API
		List<ClientRole> clientTypes = clientService.getClientTypes(clientType);
		// Log response details
		logClientTypes(clientTypes, clientType);
	}

	// Log full details for the returned client types
	private static void logClientTypes(List<ClientRole> clientTypes, AccountHolderType holderType) {

		int totalClientTypes = (clientTypes == null) ? 0 : clientTypes.size();
		System.out.println("Total client types returned=" + totalClientTypes + " for the holder type=" + holderType);
		if (totalClientTypes == 0) {
			return;
		}
		// Log details for the returned types
		for (ClientRole clientType : clientTypes) {
			System.out.println("\nRole Name=" + clientType.getName() + "\tClient Type=" + clientType.getClientType());
			Map<ClientRolePermission, Boolean> permissionsMap = clientType.getPermissions();
			if (permissionsMap == null) {
				System.out.println("Null permissions map returned for client type=" + clientType.getName());
				continue;
			}
			System.out.println("Permissions for " + clientType.getName());
			for (ClientRolePermission permission : permissionsMap.keySet()) {
				System.out.println(permission + " = " + permissionsMap.get(permission));
			}
		}
	}

	// Test getting client profile picture and client profile signature file
	public static void getClientProfileFiles() throws MambuApiException {
		System.out.println("\nIn getClientProfileFiles");

		ClientsService clientService = MambuAPIFactory.getClientService();

		String clientKey = demoClient.getEncodedKey();
		// Get client picture
		String profileDocument = clientService.getClientProfilePicture(clientKey);
		int responseLength = (profileDocument == null) ? 0 : profileDocument.length();
		System.out.println("\nProfile picture file returned. Total chars=" + responseLength);

		// Get client signature
		String signatureDocument = clientService.getClientSignatureFile(clientKey);
		responseLength = (signatureDocument == null) ? 0 : signatureDocument.length();
		System.out.println("\nSignature file returned. Total chars=" + responseLength);

	}

	// Test uploading client profile picture and signature files
	public static void uploadClientProfileFiles() throws MambuApiException {
		System.out.println("\nIn uploadClientProfileFiles");
		// Our Test file to upload.
		final String filePath = "./test/data/IMG_1.JPG";

		// Encode this file
		String encodedString = DemoUtil.encodeFileIntoBase64String(filePath);
		if (encodedString == null) {
			System.out.println("Failed encoding the file");
			return;
		}

		System.out.println("Encoded Length=" + encodedString.length());

		JSONDocument jsonDocument = new JSONDocument();

		Document document = new Document();
		document.setCreatedByUserKey(demoUser.getId());
		document.setDescription("Testing uploading profile file");
		document.setDocumentHolderKey(demoClient.getEncodedKey());
		document.setDocumentHolderType(OwnerType.CLIENT);
		document.setOriginalFilename(filePath);
		document.setType("jpg");

		jsonDocument.setDocument(document);

		// Set the encoded strings
		jsonDocument.setDocumentContent(encodedString);

		String clientKey = demoClient.getEncodedKey();
		ClientsService clientService = MambuAPIFactory.getClientService();

		// Upload profile picture
		jsonDocument.getDocument().setName("Profile Picture for " + demoClient.getFullName());
		boolean uploadPictureStatus = clientService.uploadClientProfilePicture(clientKey, jsonDocument);
		System.out.println("Profile Picture upload status=" + uploadPictureStatus);

		// Upload signature
		document.setName("Signature for " + demoClient.getFullName());
		final String signatureFile = "./test/data/signature.png";
		encodedString = DemoUtil.encodeFileIntoBase64String(signatureFile);
		jsonDocument.setDocumentContent(encodedString);
		document.setOriginalFilename(signatureFile);
		document.setType("PNG");
		boolean uploadSignatureStatus = clientService.uploadClientSignatureFile(clientKey, jsonDocument);
		System.out.println("Signature upload status=" + uploadSignatureStatus);

	}

	// Test deleting client profile picture and client profile signature file
	public static void deleteClientProfileFiles() throws MambuApiException {
		System.out.println("\nIn deleteClientProfileFiles");

		ClientsService clientService = MambuAPIFactory.getClientService();
		String clientKey = demoClient.getEncodedKey();

		// Delete picture
		boolean deleteStatus = clientService.deleteClientProfilePicture(clientKey);
		System.out.println("Deleting profile picture Status=" + deleteStatus);

		// Delete signature
		deleteStatus = clientService.deleteClientSignatureFile(clientKey);
		System.out.println("Deleting profile signature Status=" + deleteStatus);

	}

	private static final String apiTestGroupNamePrefix = "API Test Group ";

	public static GroupExpanded testCreateGroup() throws MambuApiException {
		System.out.println("\nIn testCreateGroup");

		ClientsService clientService = MambuAPIFactory.getClientService();

		// Get available ClientRole(s) first
		AccountHolderType holderType = AccountHolderType.GROUP;
		List<ClientRole> groupTypes = clientService.getClientTypes(holderType);

		// Create demo group
		Group theGroup = new Group();
		// Create unique ID
		int randomIndex = (int) (Math.random() * 1000000);
		String groupName = apiTestGroupNamePrefix + Integer.toString(randomIndex);
		theGroup.setId(null);
		ClientRole groupType = groupTypes.get(0);

		theGroup.setClientRole(groupType);
		theGroup.setAssignedBranchKey(demoUser.getAssignedBranchKey());
		theGroup.setAssignedCentreKey(demoUser.getAssignedCentreKey());
		theGroup.setAssignedUserKey(demoUser.getEncodedKey());
		theGroup.setCreationDate(new Date());
		theGroup.setEmailAddress("apiGroup@gmail.test");
		theGroup.setGroupName(groupName); // make the same as name
		theGroup.setHomePhone("604-5555-8889");
		theGroup.setMobilePhone1("777-444-5555");
		theGroup.setNotes("Created by API user " + demoUser.getFullName());

		GroupExpanded groupDetails = new GroupExpanded(theGroup);
		// Set Custom Fields

		// Make test custom fields for our group role
		List<CustomFieldValue> clientCustomInformation = DemoUtil.makeForEntityCustomFieldValues(Type.GROUP_INFO,
				groupType.getEncodedKey());
		// Add All custom fields
		groupDetails.setCustomFieldValues(clientCustomInformation);

		// Set addresses
		List<Address> addresses = new ArrayList<Address>();
		Address address = new Address();
		address.setCity("Berlin");
		address.setCountry("Germany");
		address.setLine1("Line 1 street address");
		address.setLine2("Line 2 street address");
		address.setPostcode("9876");
		// Latitude/Longitude - available since 3.9, see MBU-7067
		address.setLatitude(new BigDecimal(52.5243700));
		address.setLongitude(new BigDecimal(13.4105300));
		addresses.add(address);
		groupDetails.setAddresses(addresses);
		// Set Group Members
		List<GroupMember> groupMembers = new ArrayList<GroupMember>();
		GroupMember groupMember = new GroupMember();
		groupMember.setClientKey(demoClient.getEncodedKey());
		groupMember.setCreationDate(new Date());
		groupMembers.add(groupMember);
		groupDetails.setGroupMembers(groupMembers);

		// Set Group Roles
		// Get existent roles first
		List<GroupRoleName> allGroupRoles = clientService.getGroupRoleNames();
		if (allGroupRoles != null && allGroupRoles.size() > 0) {
			// Assign group member to the first available group role
			GroupRole useRole = new GroupRole(allGroupRoles.get(0).getEncodedKey(), groupMembers.get(0).getClientKey());
			// Add this role to group details
			List<GroupRole> groupRoles = new ArrayList<GroupRole>();
			groupRoles.add(useRole);
			groupDetails.setGroupRoles(groupRoles);
		}

		// Send API request to create new group
		GroupExpanded createdGroup = clientService.createGroup(groupDetails);
		System.out.println("Group Created. Encoded Key=" + createdGroup.getEncodedKey() + "\tName and Id="
				+ createdGroup.getGroup().getGroupNameWithId());

		return createdGroup;
	}

	// TODO: test this API when MBU-7337 is implemented in 3.10
	public static void testUpdateGroup(GroupExpanded goupExpanded) throws MambuApiException {
		System.out.println("\nIn testUpdateGroup");

		if (goupExpanded == null || goupExpanded.getGroup() == null) {
			System.out.println("Cannot update: group details are NULL");
			return;
		}
		ClientsService clientService = MambuAPIFactory.getClientService();

		// Update some group details
		Group updatedGroup = goupExpanded.getGroup();
		updatedGroup.setGroupName(updatedGroup.getGroupName() + updatedSuffix);
		updatedGroup.setId(updatedGroup.getId() + updatedSuffix);
		updatedGroup.setHomePhone(updatedGroup.getHomePhone() + "-22");
		updatedGroup.setNotes(updatedGroup.getNotes() + updatedSuffix);
		Address updatedAddress = goupExpanded.getAddresses().get(0);
		if (updatedAddress == null) {
			updatedAddress = new Address();
		}
		updatedAddress.setLine1(updatedAddress.getLine1() + updatedSuffix);
		updatedGroup.setNotes(updatedGroup.getNotes() + updatedSuffix);

		// Send API request to update this group
		GroupExpanded updatedGroupExpaneded = clientService.updateGroup(goupExpanded);
		System.out.println("Group Updated. Name=" + goupExpanded.getGroup().getGroupNameWithId() + "\tName and Id="
				+ updatedGroupExpaneded.getGroup().getGroupNameWithId());
	}
}
