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
import com.mambu.apisdk.util.DateUtils;
import com.mambu.apisdk.util.MambuEntity;
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
import com.mambu.core.shared.model.CustomFieldType;
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

	private static String NEW_GROUP_ID;
	private static GroupExpanded createdGroup;

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

			testGetClientsByBranchCentreOfficerState();
			testGetGroupsByBranchCentreOfficer();

			createdGroup = testCreateGroup(); // Available since 3.9
			testUpdateGroup(createdGroup); // Available since 3.10

			testGetGroup();
			testGetGroupDetails();

			testGetClientTypes(); // Available since 3.9
			testGetGroupsRoles();// Available since 3.9

			testGetDocuments();

			testUpdateDeleteCustomFields(); // Available since 3.8

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

		String clientKey = demoClient.getEncodedKey();
		Client myClient = clientService.getClient(clientKey);

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

		String lastname = demoClient.getLastName();
		String firstName = demoClient.getFirstName();

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
		DateFormat df = new SimpleDateFormat(DateUtils.DATE_FORMAT);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		String birthDay = (demoClient.getBirthDate() == null) ? null : df.format(demoClient.getBirthDate()); // yyy-MM-dd
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
		String clientKey = demoClient.getId();
		ClientExpanded clientDetails = clientService.getClientDetails(clientKey);
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

		String groupId = NEW_GROUP_ID;
		System.out.println("testGetGroup OK, name=" + clientService.getGroup(groupId).getGroupName());

	}

	public static void testGetGroupDetails() throws MambuApiException {
		System.out.println("\nIn testGetGroupDetails");

		String groupId = NEW_GROUP_ID;

		ClientsService clientService = MambuAPIFactory.getClientService();

		GroupExpanded groupDetails = clientService.getGroupDetails(groupId);

		System.out.println("testGetGroupDetails Ok, Name=" + groupDetails.getGroup().getGroupName() + "\tID="
				+ groupDetails.getGroup().getId());

	}

	private static final String apiTestFirstNamePrefix = "Name ";
	private static final String apiTestLastNamePrefix = "API Client";

	// Test creating new client. Save newly created client in clientCreated object for testing updates
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
		doc.setDocumentId("DFG1234");
		doc.setDocumentType("Passport");
		doc.setIssuingAuthority("Vancouver");
		idDocs.add(doc);
		clExpanded.setIdDocuments(idDocs);
		// Use helper to make test custom fields which are valid for the client's role
		List<CustomFieldValue> clientCustomInformation = DemoUtil.makeForEntityCustomFieldValues(
				CustomFieldType.CLIENT_INFO, cientRole.getEncodedKey());
		// Add All custom fields
		clExpanded.setCustomFieldValues(clientCustomInformation);

		// Create in Mambu using Json API
		clientCreated = clientService.createClient(clExpanded);

		System.out.println("Client created, OK, ID=" + clientCreated.getClient().getId() + " Full name= "
				+ clientCreated.getClient().getFullName() + " First, Last=" + clientCreated.getClient().getFirstName());

		List<Address> addressOut = clientCreated.getAddresses();
		System.out.println("\nClient address, total=" + addressOut.size());

	}

	private static final String updatedSuffix = "_updated";

	// Test update client. This method updates previously created client saved in clientCreated object
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

		// Delegate tests to new since 3.11 DemoTestCustomFiledValueService
		// Test fields for a Client
		DemoTestCustomFiledValueService.testUpdateDeleteCustomFields(MambuEntity.CLIENT);

		// Test fields for a Group
		DemoTestCustomFiledValueService.testUpdateDeleteCustomFields(MambuEntity.GROUP);
	}

	// Test getting client types
	public static void testGetClientTypes() throws MambuApiException {
		System.out.println("\nIn testGetClientTypes");

		ClientsService clientService = MambuAPIFactory.getClientService();

		// Test Get All Client Types from Mambu via API
		List<ClientRole> allClientTypes = clientService.getClientTypes();
		// Log response details
		logClientTypes(allClientTypes, null);

		// Test Get Specific Client Types from Mambu via API
		AccountHolderType clientType = AccountHolderType.CLIENT; // or AccountHolderType.GROUP;
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
		String profileDocument = clientService.getClientProfilePictureFile(clientKey);
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

	private static final String apiTestGroupNamePrefix = "API Group ";

	// Test Creating new group. Return new group on success
	public static GroupExpanded testCreateGroup() throws MambuApiException {
		System.out.println("\nIn testCreateGroup");

		ClientsService clientService = MambuAPIFactory.getClientService();

		// Get available ClientRole(s) first
		AccountHolderType holderType = AccountHolderType.GROUP;
		List<ClientRole> groupTypes = clientService.getClientTypes(holderType);

		// Create demo group
		Group theGroup = new Group();
		// Create unique ID
		String randomIndex = Integer.toString((int) (Math.random() * 1000000));

		String groupName = apiTestGroupNamePrefix + randomIndex;
		theGroup.setId(randomIndex);
		ClientRole groupType = groupTypes.get(0);

		theGroup.setClientRole(groupType);
		theGroup.setAssignedBranchKey(demoUser.getAssignedBranchKey());
		theGroup.setAssignedCentreKey(demoUser.getAssignedCentreKey());
		theGroup.setAssignedUserKey(demoUser.getEncodedKey());
		theGroup.setCreationDate(new Date());
		theGroup.setGroupName(groupName); // make the same as name

		theGroup.setEmailAddress("apiGroup@gmail.com");
		theGroup.setHomePhone("604-5555-8889");
		theGroup.setMobilePhone1("777-444-5555");
		theGroup.setNotes("Created by API user " + demoUser.getFullName());

		GroupExpanded groupDetails = new GroupExpanded(theGroup);
		// Set Custom Fields

		// Make test custom fields for our group role
		List<CustomFieldValue> clientCustomInformation = DemoUtil.makeForEntityCustomFieldValues(
				CustomFieldType.GROUP_INFO, groupType.getEncodedKey());
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
		NEW_GROUP_ID = createdGroup.getGroup().getId();

		return createdGroup;
	}

	// Test updating Group. Pass existent group as a parameter
	public static void testUpdateGroup(GroupExpanded groupExpanded) throws MambuApiException {
		System.out.println("\nIn testUpdateGroup");

		if (groupExpanded == null || groupExpanded.getGroup() == null) {
			System.out.println("Cannot update: group details are NULL");
			return;
		}
		ClientsService clientService = MambuAPIFactory.getClientService();

		// Update some group details
		Group updatedGroup = groupExpanded.getGroup();
		updatedGroup.setGroupName(updatedGroup.getGroupName() + updatedSuffix);
		// Keep the same group ID, groupID cannot be modified

		updatedGroup.setHomePhone(updatedGroup.getHomePhone() + "-22");
		// TODO: GET Group and GET Group?fullDeatils do no return notes field. Update API may erase existent notes (see
		// MBU-8560)
		updatedGroup.setNotes(updatedGroup.getNotes() + updatedSuffix);

		List<Address> addresses = groupExpanded.getAddresses();
		Address currentAddress = (addresses == null || addresses.size() == 0) ? new Address() : addresses.get(0);
		List<Address> updatedAddresses = new ArrayList<Address>();
		Address updatedAddress = new Address();
		updatedAddress.setLine1(currentAddress.getLine1() + updatedSuffix);
		updatedAddress.setLine2(currentAddress.getLine2() + updatedSuffix);
		updatedAddress.setCity(currentAddress.getCity() + updatedSuffix);
		updatedAddress.setPostcode(currentAddress.getPostcode() + updatedSuffix);
		updatedAddress.setCountry(currentAddress.getCountry() + updatedSuffix);
		updatedAddress.setLatitude(currentAddress.getLatitude());
		updatedAddress.setLongitude(currentAddress.getLongitude());

		updatedAddresses.add(updatedAddress);
		groupExpanded.setAddresses(updatedAddresses);

		List<CustomFieldValue> customFields = groupExpanded.getCustomFieldValues();
		List<CustomFieldValue> updatedFields = new ArrayList<CustomFieldValue>();
		if (customFields != null) {
			for (CustomFieldValue value : customFields) {
				value = DemoUtil.makeNewCustomFieldValue(value);
				updatedFields.add(value);
			}
		}
		groupExpanded.setCustomFieldValues(updatedFields);
		// Set new Group Members
		List<GroupMember> groupMembers = new ArrayList<GroupMember>();
		GroupMember groupMember = new GroupMember();
		// Replace demoClient with newly created client as a group member
		String newClientKey = clientCreated.getEncodedKey();
		groupMember.setClientKey(newClientKey);
		groupMember.setCreationDate(new Date());
		groupMembers.add(groupMember);
		groupExpanded.setGroupMembers(groupMembers);

		// Test submitting Group roles with the same assignments but as new roles (no encoded key)
		List<GroupRole> currentRoles = groupExpanded.getGroupRoles();
		List<GroupRole> updateRoles = new ArrayList<GroupRole>();
		if (currentRoles != null) {
			for (GroupRole role : currentRoles) {
				GroupRole updated = new GroupRole(role.getGroupRoleNameKey(), newClientKey);
				updateRoles.add(updated);
			}
		}
		groupExpanded.setGroupRoles(updateRoles);

		// Send API request to update this group
		GroupExpanded updatedGroupExpaneded = clientService.updateGroup(groupExpanded);
		System.out.println("Group Updated. Name=" + groupExpanded.getGroup().getGroupNameWithId() + "\tName and Id="
				+ updatedGroupExpaneded.getGroup().getGroupNameWithId());
	}

}
