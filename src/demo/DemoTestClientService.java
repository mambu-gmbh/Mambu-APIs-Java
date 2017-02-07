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
import com.mambu.apisdk.services.DocumentsService;
import com.mambu.apisdk.services.OrganizationService;
import com.mambu.apisdk.util.DateUtils;
import com.mambu.apisdk.util.MambuEntityType;
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
import com.mambu.core.shared.model.Language;
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
			demoClient = DemoUtil.getDemoClient(null);
			demoGroup = DemoUtil.getDemoGroup(null);

			NEW_CLIENT_ID = testCreateJsonClient();
			testGetClient();

			ClientExpanded updatedClient = testUpdateClient();
			NEW_CLIENT_ID = testPatchClient(updatedClient.getClient()); // Available since 4.1
			testUpdateClientState(updatedClient.getClient()); // Available since 4.0
			
			testUpdateClientAssociations(updatedClient.getClient()); //Available since 4.5

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
			testPatchGroup(); // Available since 4.2. For more details see MBU-12985.
			testPatchGroupExpanded(); // Available since 4.2. For more details see MBU-12985.

			testGetGroup();
			testGetGroupDetails();

			testGetClientTypes(); // Available since 3.9
			testGetGroupsRoles();// Available since 3.9

			testGetDocuments();

			testUpdateDeleteCustomFields(); // Available since 3.8

			uploadClientProfileFiles(); // Available since 3.9
			getClientProfileFiles(); // Available since 3.9
			deleteClientProfileFiles(); // Available since 3.9

			// Test deleting newly created client
			testDeleteClient(NEW_CLIENT_ID); // Available since 4.2

			testDeleteGroupRolesThroughPatch(); // Available since 4.2. See MBU-13763
			testDeleteGroupMembersThroughPatch(); /// Available since 4.2. See MBU-13763

		} catch (MambuApiException e) {
			System.out.println("Exception caught in Demo Test Clients");
			System.out.println("Error code=" + e.getErrorCode());
			System.out.println(" Cause=" + e.getCause() + ".  Message=" + e.getMessage());
		}
	}

	private static void testUpdateClientAssociations(Client client) throws MambuApiException {

		String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
		System.out.println("\nIn " + methodName);
		
		client.setAssignedBranchKey(DemoUtil.getDemoClient().getAssignedBranchKey());
		client.setAssignedCentreKey(DemoUtil.getDemoClient().getAssignedCentreKey());
		client.setAssignedUserKey(DemoUtil.getDemoClient().getAssignedUserKey());
		
		// null fields not wanted in patch
		// and keep only the fields defining associations
		client.setFirstName(null);
		client.setLastName(null);
		client.setMiddleName(null);
		client.setEmailAddress(null);
		client.setMobilePhone1(null);
		client.setHomePhone(null);
		client.setState(null);
		client.setClientRole(null);
		client.setId(null);
		client.setPreferredLanguage(null);
		client.setBirthDate(null);

		ClientsService clientService = MambuAPIFactory.getClientService();
		boolean status = clientService.patchClient(client);
		System.out.println("Update status=" + status);

		// Get updated client details back to confirm PATCHed values
		Client updatedClient = clientService.getClient(client.getEncodedKey());
		System.out.println("\tUpdate AssignedBranchKey=" + updatedClient.getAssignedBranchKey() + "\tAssignedCentreKey="
				+ updatedClient.getAssignedCentreKey() + "\tAssignedUserKey=" + updatedClient.getAssignedUserKey());

	}

	public static void testGetClient() throws MambuApiException {

		System.out.println("\nIn testGetClient");
		ClientsService clientService = MambuAPIFactory.getClientService();

		String clientKey = demoClient.getEncodedKey();
		Client myClient = clientService.getClient(clientKey);

		System.out.println("Client Service by ID Ok, ID=" + myClient.getId());

	}

	/**
	 * Test Deleting client
	 * 
	 * @param clientId
	 *            client Id or encoded key. Must not be null
	 * 
	 *            Available since Mambu 4.2. See MBU-12684
	 * 
	 * @throws MambuApiException
	 */
	public static void testDeleteClient(String clientId) throws MambuApiException {

		System.out.println("\nIn testDeleteClient");
		ClientsService clientService = MambuAPIFactory.getClientService();

		boolean deleteStatus = clientService.deleteClient(clientId);

		System.out.println("Deleted Client ID= " + clientId + "\tStatus=" + deleteStatus);

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

	// Test PATCH GroupExpanded fields API. This method patches existing created GroupExpended.
	public static void testPatchGroup() throws MambuApiException {

		System.out.println("\n In testPatchGroup");
		String groupId = NEW_GROUP_ID;

		ClientsService clientService = MambuAPIFactory.getClientService();

		String patchFieldSuffix = "group_patch_test";

		Group group = setUpGroupForPatchingOperation(groupId, clientService, patchFieldSuffix);
		// reset the value for group id. Might be needed by other tests
		NEW_GROUP_ID = group.getId();

		// PATCH the group
		boolean patchStatus = clientService.patchGroup(group);

		System.out.println("Update group status=" + patchStatus);

		GroupExpanded groupExpanded = clientService.getGroupDetails(group.getEncodedKey());

		logGroupExpandedDetails(groupExpanded);

	}

	// Test PATCH GroupExpanded fields API. This method patches existing created GroupExpended.
	public static void testPatchGroupExpanded() throws MambuApiException {

		System.out.println("\n In testPatchGroupExtended");
		String groupId = NEW_GROUP_ID;

		ClientsService clientService = MambuAPIFactory.getClientService();

		// get a GroupExpanded
		GroupExpanded groupExpanded = clientService.getGroupDetails(groupId);

		String patchFieldSuffix = "group_expanded_patch_test";

		// get group and prepare it for PATCH
		Group group = setUpGroupForPatchingOperation(groupId, clientService, patchFieldSuffix);
		groupExpanded.setGroup(group);
		// reset the value for group id. might be needed by other tests
		NEW_GROUP_ID = group.getId();

		addTestClientAsGroupMamber(groupExpanded);

		replaceExistingRoleList(groupExpanded);

		// PATCH the group expanded
		boolean patchStatus = clientService.patchGroup(groupExpanded);

		System.out.println("Update group expanded status=" + patchStatus);

		GroupExpanded patchedGroup = clientService.getGroupDetails(groupExpanded.getEncodedKey());

		logGroupExpandedDetails(patchedGroup);
	}

	/**
	 * Replaces the list of roles from GroupExpanded received as parameter to this method with a new list containing
	 * only the test client
	 * 
	 * @param groupExpanded
	 *            The GroupExpanded to be updated with a new list of roles.
	 */
	private static void replaceExistingRoleList(GroupExpanded groupExpanded) {

		if (groupExpanded != null && groupExpanded.getGroupRoles() != null
				&& !groupExpanded.getGroupRoles().isEmpty()) {
			// replace the existing role list
			List<GroupRole> groupRoles = groupExpanded.getGroupRoles();
			GroupRole groupRole = groupRoles.get(0);
			groupRole.setClientKey(demoClient.getEncodedKey());
			// set the role(replace the existing one)
			groupExpanded.setGroupRoles(groupRoles);
		}
	}

	/**
	 * Adds the test client to the list of existing roles on the GroupExpanded passed as parameter to this method.
	 * 
	 * @param groupExpanded
	 *            The GroupExpanded to be updated with a new list of members (including the existing ones)
	 */
	private static void addTestClientAsGroupMamber(GroupExpanded groupExpanded) {

		if (groupExpanded != null && groupExpanded.getGroupMembers() != null) {
			// add a new member
			List<GroupMember> groupMembers = groupExpanded.getGroupMembers();
			GroupMember groupMember = new GroupMember();
			groupMember.setClientKey(demoClient.getEncodedKey());
			groupMember.setCreationDate(new Date());
			groupMembers.add(groupMember);
			groupExpanded.setGroupMembers(groupMembers);
		}
	}

	/**
	 * Calls ClientService to get a Group from Mambu for a given group id and change some details on it and then returns
	 * it.
	 * 
	 * @param groupId
	 *            the id of the group to be searched in Mambu
	 * @param clientService
	 *            the ClientService
	 * @param patchFieldSuffix
	 *            the suffix that will be added on some fields of the group. Needed for differentiating the patch group
	 *            against patch group expanded operation.
	 * @return a Group with changed details ready for patching
	 * @throws MambuApiException
	 */
	private static Group setUpGroupForPatchingOperation(String groupId, ClientsService clientService,
			String patchFieldSuffix) throws MambuApiException {

		Group group = clientService.getGroup(groupId);
		if (group != null) {
			String randomIndex = Integer.toString((int) (Math.random() * 1000000));
			// change the group information
			group.setId("99999_" + randomIndex);
			group.setGroupName("Patched Group " + patchFieldSuffix);
			group.setEmailAddress("test_group_patch5" + patchFieldSuffix + "@mambu.com");
			group.setPreferredLanguage(Language.ROMANIAN);
			group.setHomePhone("333-4444-5555-66");
			group.setNotes("this is a note created through patch group " + patchFieldSuffix);
			group.setMobilePhone1("777-888-9999");
		}
		return group;
	}

	/**
	 * Logs to the console the details of the group passed as parameter.
	 * 
	 * @param groupExpanded
	 *            the GroupExpanded, whose details will be printed to the console.
	 */
	private static void logGroupExpandedDetails(GroupExpanded groupExpanded) {

		Group group = groupExpanded.getGroup();
		if (group != null) {
			System.out.println("Group details:");
			System.out.println("\tGroup key: " + group.getEncodedKey());
			System.out.println("\tGroup name: " + group.getGroupName());
			System.out.println("\tGroup preferred language: " + group.getPreferredLanguage());
			System.out.println("\tGroup phone: " + group.getMobilePhone1());
			System.out.println("\tGroup notes: " + group.getNotes());
			System.out.println("\tGroup homePhone: " + group.getHomePhone());
			System.out.println("\tGroup emailAddress: " + group.getEmailAddress());
		}

		logMembersOfTheGroupExpanded(groupExpanded);

		logRolesOfTheGroupExpanded(groupExpanded);
	}

	/**
	 * Takes as parameter a GroupExpanded and logs to the console some details about its group roles if there are any
	 * roles.
	 * 
	 * @param groupExpanded
	 *            The GroupExpanded whose group roles will be printed to the console.
	 */
	private static void logRolesOfTheGroupExpanded(GroupExpanded groupExpanded) {

		List<GroupRole> roles = groupExpanded.getGroupRoles();
		if (roles != null && !roles.isEmpty()) {
			System.out.println("Group roles:");
			for (GroupRole role : roles) {
				System.out.println("\tRole`s encoded key " + role.getEncodedKey());
				System.out.println("\tRole`s name " + role.getRoleName());
			}
			System.out.println("Roles count = " + roles.size());
		}
	}

	/**
	 * Takes as parameter a GroupExpanded and logs to the console some details about its group members if there are any
	 * members.
	 * 
	 * @param groupExpanded
	 *            The GroupExpanded whose group members will be printed to the console.
	 */
	private static void logMembersOfTheGroupExpanded(GroupExpanded groupExpanded) {

		List<GroupMember> members = groupExpanded.getGroupMembers();
		if (members != null && !members.isEmpty()) {
			System.out.println("Group mambers:");
			for (GroupMember member : members) {
				System.out.println("\tMember`s encoded key " + member.getEncodedKey());
				System.out.println("\tMember`s parent key " + member.getParentKey());
			}
			System.out.println("Members count = " + members.size());
		}
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

	//
	/**
	 * Test creating new client. Save newly created client in clientCreated object for testing updates
	 * 
	 * @return the id of the newly created client
	 * @throws MambuApiException
	 */
	public static String testCreateJsonClient() throws MambuApiException {

		System.out.println("\nIn testCreateJsonClient");

		ClientsService clientService = MambuAPIFactory.getClientService();
		int randomIndex = (int) (Math.random() * 10000);
		String clientId = Integer.toString(randomIndex);
		Client clientIn = new Client(apiTestFirstNamePrefix + clientId, apiTestLastNamePrefix + clientId);
		clientIn.setId(clientId);
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
		clientIn.setPreferredLanguage(Language.ENGLISH); // MBU-9221 in 3.12

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

		createAndAddDocumentIdsForClient(clExpanded);

		// Use helper to make test custom fields which are valid for the client's role
		List<CustomFieldValue> clientCustomInformation = DemoUtil
				.makeForEntityCustomFieldValues(CustomFieldType.CLIENT_INFO, cientRole.getEncodedKey());
		// Add All custom fields
		clExpanded.setCustomFieldValues(clientCustomInformation);

		// Create in Mambu using Json API
		clientCreated = clientService.createClient(clExpanded);

		String createdClientId = clientCreated.getId();
		System.out.println("Client created, OK, ID=" + createdClientId + " Full name= "
				+ clientCreated.getClient().getFullName() + " First, Last=" + clientCreated.getClient().getFirstName());

		List<Address> addressOut = clientCreated.getAddresses();
		System.out.println("\nClient address, total=" + addressOut.size());

		return createdClientId;
	}

	/**
	 * Helper method, it creates and add other doc IDs for the client received as parameter to this method call. NOTE:
	 * the documents will be added only if the settings allow this
	 * 
	 * @param clientExpanded
	 *            the client to updated with new document IDs
	 * @throws MambuApiException
	 */
	private static void createAndAddDocumentIdsForClient(ClientExpanded clientExpanded) throws MambuApiException {

		// check if other document IDs are allowed
		boolean areOtherDocIdsAllowed = getOtherDocumentTemplateIdsEnabeled();

		if (areOtherDocIdsAllowed) {
			// create and add doc IDs
			List<IdentificationDocument> idDocs = new ArrayList<>();
			IdentificationDocument doc = new IdentificationDocument();
			doc.setDocumentId("DFG1234");
			doc.setDocumentType("Passport");
			doc.setIssuingAuthority("Vancouver");
			idDocs.add(doc);
			clientExpanded.setIdDocuments(idDocs);
		}
	}

	/**
	 * Helper method call organization API to get the value of the OtherDocumentTemplateIdsEnabeled field.
	 * 
	 * @return true if other document template IDs is enabled
	 * @throws MambuApiException
	 */
	private static boolean getOtherDocumentTemplateIdsEnabeled() throws MambuApiException {

		OrganizationService organizationService = MambuAPIFactory.getOrganizationService();

		return organizationService.getGeneralSettings().getOtherIdDocumentsEnabled();
	}

	private static final String updatedSuffix = "_updated";

	// Test update client. This method updates previously created client saved in clientCreated object
	public static ClientExpanded testUpdateClient() throws MambuApiException {

		System.out.println("\nIn testUpdateClient");
		ClientsService clientService = MambuAPIFactory.getClientService();

		ClientExpanded clientUpdated = clientCreated;
		Client client = clientUpdated.getClient();
		client.setFirstName(client.getFirstName() + updatedSuffix);
		client.setLastName(client.getLastName() + updatedSuffix);
		client.setPreferredLanguage(Language.SPANISH);

		// Test updating custom fields too
		List<CustomFieldValue> customFields = clientCreated.getCustomFieldValues();
		List<CustomFieldValue> updatedFields = new ArrayList<CustomFieldValue>();
		if (customFields != null) {
			for (CustomFieldValue value : customFields) {
				value = DemoUtil.makeNewCustomFieldValue(value);
				updatedFields.add(value);
			}
		}
		clientUpdated.setCustomFieldValues(updatedFields);
		// Test also updating client's address
		List<Address> addresses = clientUpdated.getAddresses();
		Address currentAddress = (addresses == null || addresses.size() == 0) ? new Address() : addresses.get(0);

		Address updatedAddress = new Address();
		updatedAddress.setLine1(currentAddress.getLine1() + updatedSuffix);
		updatedAddress.setLine2(currentAddress.getLine2() + updatedSuffix);
		updatedAddress.setCity(currentAddress.getCity() + updatedSuffix);
		updatedAddress.setPostcode(currentAddress.getPostcode() + updatedSuffix);
		updatedAddress.setCountry(currentAddress.getCountry() + updatedSuffix);
		updatedAddress.setLatitude(currentAddress.getLatitude());
		updatedAddress.setLongitude(currentAddress.getLongitude());

		List<Address> updatedAddresses = new ArrayList<Address>();
		updatedAddresses.add(updatedAddress);

		clientUpdated.setAddresses(updatedAddresses);

		ClientExpanded clientExpandedResult = clientService.updateClient(clientUpdated);

		System.out.println("Client Update OK, ID=" + clientExpandedResult.getClient().getId() + "\tLastName="
				+ clientExpandedResult.getClient().getLastName() + "\tFirst Name ="
				+ clientExpandedResult.getClient().getFirstName());

		return clientExpandedResult;
	}

	// Test updating client's state API
	public static void testUpdateClientState(Client client) throws MambuApiException {

		System.out.println("\nIn testUpdateClientState");
		if (client == null) {
			System.out.println("WARNING:cannot test updating state for a  null client");
			return;
		}
		String clientId = client.getId();
		ClientState currentState = client.getState();

		// Change client's state
		ClientState newState = ClientState.BLACKLISTED;

		System.out.println("Updating State from " + currentState + " to " + newState + " for client ID=" + clientId);
		ClientsService clientService = MambuAPIFactory.getClientService();
		boolean stateUpdated = clientService.patchClientState(clientId, newState);
		System.out.println("Update status=" + stateUpdated);

		// Test restoring client's state back to its previous state
		System.out
				.println("Updating State back from " + newState + " to " + currentState + " for client ID=" + clientId);
		boolean stateUpdated2 = clientService.patchClientState(clientId, currentState);
		System.out.println("Update status=" + stateUpdated2);

	}

	/**
	 * Test PATCHing Client fields
	 * 
	 * @param client
	 *            client to update
	 * @return the id of the updated client
	 * @throws MambuApiException
	 */
	public static String testPatchClient(Client client) throws MambuApiException {

		System.out.println("\nIn testPatchClient");

		client.setFirstName(client.getFirstName()); // keep the same to continue using our demo client
		client.setLastName(client.getLastName()); // keep the same to continue using our demo client
		client.setMiddleName(client.getMiddleName() + updatedSuffix);
		client.setPreferredLanguage(Language.ROMANIAN);
		String iD = client.getId() + updatedSuffix;
		// Limit the ID's value to 32 chars. Otherwise 501 error code is returned
		if (iD.length() > 32) {
			iD = iD.substring(0, 16);
		}
		client.setId(iD);
		client.setBirthDate(new Date());
		// Execute Client PATCH API
		ClientsService clientService = MambuAPIFactory.getClientService();
		boolean status = clientService.patchClient(client);
		System.out.println("Update status=" + status);

		// Get updated client details back to confirm PATCHed values
		Client updatedClient = clientService.getClient(client.getEncodedKey());
		String updatedId = updatedClient.getId();
		System.out.println("\tUpdate FirstName=" + updatedClient.getFirstName() + "\tID=" + updatedClient + "\tState="
				+ updatedClient.getState());

		// Return the ID of our test client
		return updatedId;

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
			System.out.println(
					"Group Name=" + group.getGroupName() + "\tBranchId=" + group.getAssignedBranchKey() + "\tCentreId="
							+ group.getAssignedCentreKey() + "\tCredit Officer id=" + group.getAssignedUserKey());
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

		Integer offset = 0;
		Integer limit = 50;
		// Getting Documents for a Client
		DocumentsService documentsService = MambuAPIFactory.getDocumentsService();
		List<Document> documents = documentsService.getDocuments(MambuEntityType.CLIENT, demoClient.getId(), offset,
				limit);

		// Log returned documents using DemoTestDocumentsService helper
		System.out.println("Documents returned for a Client with ID=" + demoClient.getId());
		DemoTestDocumentsService.logDocuments(documents);

		// Getting Documents for a Group
		documents = documentsService.getDocuments(MambuEntityType.GROUP, demoGroup.getId(), offset, limit);

		// Log returned documents using DemoTestDocumentsService helper
		System.out.println("Documents returned for a Group with ID=" + demoGroup.getId());
		DemoTestDocumentsService.logDocuments(documents);

	}

	// Update Custom Field values for the client and for the group and delete the first custom field
	public static void testUpdateDeleteCustomFields() throws MambuApiException {

		System.out.println("\nIn testUpdateDeleteCustomFields");

		// Delegate tests to new since 3.11 DemoTestCustomFiledValueService
		// Test fields for a Client
		DemoTestCustomFieldValueService.testUpdateDeleteEntityCustomFields(MambuEntityType.CLIENT);

		// Test fields for a Group
		DemoTestCustomFieldValueService.testUpdateDeleteEntityCustomFields(MambuEntityType.GROUP);
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
		List<CustomFieldValue> clientCustomInformation = DemoUtil
				.makeForEntityCustomFieldValues(CustomFieldType.GROUP_INFO, groupType.getEncodedKey());
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
		updatedGroup.setNotes(updatedGroup.getNotes() + updatedSuffix);

		List<Address> addresses = groupExpanded.getAddresses();
		Address currentAddress = (addresses == null || addresses.size() == 0) ? new Address() : addresses.get(0);
		List<Address> updatedAddresses = new ArrayList<Address>();
		// TODO: Mambu 3.14 returns an exception when updating Groups with an existent address if it'a a new address.
		// See MBU-11719. The workaround is use the current Address (and not to create a new one) and just to update its
		// fields. This works. See MBU-11214
		Address updatedAddress = currentAddress;
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
		String newClientKey = (clientCreated != null) ? clientCreated.getEncodedKey() : null;
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

	// tests group members deletion through patch operation
	public static void testDeleteGroupMembersThroughPatch() throws MambuApiException {

		System.out.println("\nIn testDeleteGroupThroughPatch");
		String groupId = NEW_GROUP_ID;

		ClientsService clientService = MambuAPIFactory.getClientService();

		// get a GroupExpanded
		GroupExpanded groupExpanded = clientService.getGroupDetails(groupId);
		// create an empty list of members
		List<GroupMember> groupMembers = new ArrayList<>();
		groupExpanded.setGroupMembers(groupMembers);

		// PATCH the group expanded
		boolean patchStatus = clientService.patchGroup(groupExpanded);

		System.out.println("Update group expanded status=" + patchStatus);

		GroupExpanded patchedGroup = clientService.getGroupDetails(groupExpanded.getEncodedKey());

		logGroupExpandedDetails(patchedGroup);

		if (!patchedGroup.getGroupMembers().isEmpty()) {
			throw new MambuApiException(new Exception("Members weren`t deleted!"));
		}
	}

	// tests group roles deletion through patch operation
	public static void testDeleteGroupRolesThroughPatch() throws MambuApiException {

		System.out.println("\nIn testDeleteRolesThroughPatch");
		String groupId = NEW_GROUP_ID;

		ClientsService clientService = MambuAPIFactory.getClientService();

		// get a GroupExpanded
		GroupExpanded groupExpanded = clientService.getGroupDetails(groupId);
		// create an empty list of roles
		List<GroupRole> groupRoles = new ArrayList<>();
		groupExpanded.setGroupRoles(groupRoles);

		// PATCH the group expanded
		boolean patchStatus = clientService.patchGroup(groupExpanded);

		System.out.println("Update group expanded status=" + patchStatus);

		GroupExpanded patchedGroup = clientService.getGroupDetails(groupExpanded.getEncodedKey());

		logGroupExpandedDetails(patchedGroup);

		if (!patchedGroup.getGroupRoles().isEmpty()) {
			throw new MambuApiException(new Exception("Roles weren`t deleted!"));
		}
	}

}
