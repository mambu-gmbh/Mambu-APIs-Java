/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.List;

import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.ServiceHelper;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.docs.shared.model.Document;

/**
 * Service class which handles API operations like getting and creating clients and groups of clients
 * 
 * @author ipenciuc
 * 
 */
public class ClientsService {

	// Client search and create fields
	private static String FIRST_NAME = APIData.FIRST_NAME;
	private static String LAST_NAME = APIData.LAST_NAME;
	private static String HOME_PHONE = APIData.HOME_PHONE;
	private static String MOBILE_PHONE = APIData.MOBILE_PHONE;
	private static String GENDER = APIData.GENDER;
	private static String BIRTH_DATE = APIData.BIRTH_DATE;
	private static String EMAIL_ADDRESS = APIData.EMAIL_ADDRESS;
	private static String ID_DOCUMENT = APIData.ID_DOCUMENT;

	private static String NOTES = APIData.NOTES;

	private static final String BRANCH_ID = APIData.BRANCH_ID;
	public static final String CENTRE_ID = APIData.CENTRE_ID;
	private static final String CREDIT_OFFICER_USER_NAME = APIData.CREDIT_OFFICER_USER_NAME;
	private static final String CLIENT_STATE = APIData.CLIENT_STATE;

	// Our service helper
	private ServiceHelper serviceHelper;

	// Create API definitions for services provided by ClientService
	// Get Client Details
	private final static ApiDefinition getClient = new ApiDefinition(ApiType.GET_ENTITY, Client.class);
	private final static ApiDefinition getClientDetails = new ApiDefinition(ApiType.GET_ENTITY_DETAILS,
			ClientExpanded.class);
	// Get Lists of Clients
	private final static ApiDefinition getClientsList = new ApiDefinition(ApiType.GET_LIST, Client.class);
	// Create Client
	private final static ApiDefinition createClient = new ApiDefinition(ApiType.CREATE_JSON_ENTITY,
			ClientExpanded.class);
	// Update Client
	private final static ApiDefinition updateClient = new ApiDefinition(ApiType.UPDATE_JSON, ClientExpanded.class);

	// Groups
	private final static ApiDefinition getGroup = new ApiDefinition(ApiType.GET_ENTITY, Group.class);
	private final static ApiDefinition getGroupDetails = new ApiDefinition(ApiType.GET_ENTITY_DETAILS,
			GroupExpanded.class);
	// Get Lists of Groups
	private final static ApiDefinition getGroupsList = new ApiDefinition(ApiType.GET_LIST, Group.class);
	// Get Documents for a Client
	private final static ApiDefinition getClientDocuments = new ApiDefinition(ApiType.GET_OWNED_ENTITIES, Client.class,
			Document.class);
	// Get Documents for a group
	private final static ApiDefinition getGroupDocuments = new ApiDefinition(ApiType.GET_OWNED_ENTITIES, Group.class,
			Document.class);

	/***
	 * Create a new client service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public ClientsService(MambuAPIService mambuAPIService) {
		this.serviceHelper = new ServiceHelper(mambuAPIService);
	}

	/**
	 * Requests a client by their Mambu ID
	 * 
	 * @param clientId
	 * 
	 * @return the Mambu client model
	 * 
	 * @throws MambuApiException
	 */
	public Client getClient(String clientId) throws MambuApiException {
		return serviceHelper.execute(getClient, clientId);
	}

	/**
	 * Requests a client by their Last name and first name
	 * 
	 * @param clientLastName
	 * @param clientFirstName
	 * 
	 * @return list of Mambu clients
	 * 
	 * @throws MambuApiException
	 */

	public List<Client> getClientByFullName(String clientLastName, String clientFirstName) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.put(LAST_NAME, clientLastName);
		params.put(FIRST_NAME, clientFirstName);

		return serviceHelper.execute(getClientsList, params);
	}

	/**
	 * Requests a client by their Last name and Birth date
	 * 
	 * @param clientLastName
	 * @param birthDay
	 *            ("yyyy-MM-dd")
	 * 
	 * @return list of Mambu clients
	 * 
	 * @throws MambuApiException
	 */
	public List<Client> getClientByLastNameBirthday(String clientLastName, String birthDay) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.put(LAST_NAME, clientLastName);
		params.put(BIRTH_DATE, birthDay);

		return serviceHelper.execute(getClientsList, params);

	}

	/**
	 * Requests a list of all matching clients
	 * 
	 * @param active
	 *            True if active Clients should retrieved, false for inactive Clients
	 * 
	 * @return the list of Mambu clients
	 * 
	 * @throws MambuApiException
	 */
	public List<Client> getClients(boolean active) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam(CLIENT_STATE, (active ? "ACTIVE" : "INACTIVE"));

		return serviceHelper.execute(getClientsList, params);
	}

	/**
	 * Requests a list of clients, limited by offset/limit
	 * 
	 * @param active
	 *            True if active Clients should retrieved, false for inactive Clients
	 * @param offset
	 *            Offset to start loading Clients, has to be >= 0
	 * @param limit
	 *            Limit of Clients to load, has to be > 0
	 * 
	 * @return the list of Mambu clients
	 * 
	 * @throws MambuApiException
	 */
	public List<Client> getClients(boolean active, int offset, int limit) throws MambuApiException {

		if ((offset < 0) || (limit < 1)) {
			throw new MambuApiException(new IllegalArgumentException("Offset has to be >= 0, limit has to be > 0"));
		}
		ParamsMap params = new ParamsMap();
		params.addParam(CLIENT_STATE, (active ? "ACTIVE" : "INACTIVE"));
		params.addParam(APIData.OFFSET, String.valueOf(offset));
		params.addParam(APIData.LIMIT, String.valueOf(limit));

		return serviceHelper.execute(getClientsList, params);

	}

	/**
	 * Requests a client by their Document ID and Last name
	 * 
	 * @param clientLastName
	 * @param documentId
	 * 
	 * @return the list of Mambu clients
	 * 
	 * @throws MambuApiException
	 */

	public List<Client> getClientByLastNameDocId(String clientLastName, String documentId) throws MambuApiException {

		ParamsMap params = new ParamsMap();

		params.put(LAST_NAME, clientLastName);
		params.put(ID_DOCUMENT, documentId);

		return serviceHelper.execute(getClientsList, params);

	}

	/**
	 * Returns a client with their full details such as addresses or custom fields
	 * 
	 * @param clientId
	 *            the id of the client
	 * @return the retrieved expanded client
	 * 
	 * @throws MambuApiException
	 */
	public ClientExpanded getClientDetails(String clientId) throws MambuApiException {
		return serviceHelper.execute(getClientDetails, clientId);
	}

	/**
	 * Requests a group by it's Mambu ID
	 * 
	 * @param groupId
	 *            the id of the group
	 * 
	 * @return the retrieved group
	 * 
	 * @throws MambuApiException
	 */
	public Group getGroup(String groupId) throws MambuApiException {
		return serviceHelper.execute(getGroup, groupId);
	}

	/**
	 * Requests the details about a group
	 * 
	 * @param groupId
	 *            the id of the group
	 * 
	 * @return the retrieved expanded group
	 * 
	 * @throws MambuApiException
	 */
	public GroupExpanded getGroupDetails(String groupId) throws MambuApiException {
		return serviceHelper.execute(getGroupDetails, groupId);
	}

	/***
	 * Create a new client (expanded) using ClientExpanded object and sending it as a Json api. This API allows creating
	 * Client with more details, including creating custom fields.
	 * 
	 * Note: since Mambu3.2 this is the API that must be used for creating Clients. Non-json APIs for creating a client
	 * could be deprecated in a future
	 * 
	 * @param clientDetails
	 *            The encodedKey for the clientExpanded object must be null to create a new client
	 * 
	 * @return clientDetails for the newly created client.
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public ClientExpanded createClient(ClientExpanded clientDetails) throws MambuApiException {

		// Get encodedKey and ensure it's NULL for the new client request
		String encodedKey = clientDetails.getEncodedKey();
		if (encodedKey != null) {
			throw new IllegalArgumentException("Cannot create client, the encoded key must be null");
		}

		return serviceHelper.executeJson(createClient, clientDetails);
	}

	/***
	 * Update an existent client (expanded) using ClientExpanded object and send it as a Json api. This API allows
	 * updating Client with new details, including modifying client details, custom fields, address, contacts and
	 * document IDs
	 * 
	 * Note: Available since Mambu 3.4
	 * 
	 * @param clientDetails
	 *            client details to be updated. See MBU-3603 for full details. The encodedKey for the clientExpanded
	 *            object must be NOT null to update an existent client. Client ID and Client Assignment are not
	 *            modifiable
	 * 
	 * @return updated clientDetails
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public ClientExpanded updateClient(ClientExpanded clientDetails) throws MambuApiException {

		// Verify that the encodedKey for this object is not NULL
		String encodedKey = clientDetails.getEncodedKey();
		if (encodedKey == null) {
			throw new IllegalArgumentException("Cannot update client, encoded key for the object does not exist");
		}
		return serviceHelper.executeJson(updateClient, clientDetails, encodedKey);
	}

	/***
	 * Get Clients by branch id, centre id, credit officer, clientState
	 * 
	 * @param branchId
	 *            the ID of the Client's branch
	 * @param centreId
	 *            The ID of the centre to which the loan accounts are assigned to. If both branchId and centreId are
	 *            provided then this centre must be assigned to the branchId
	 * @param creditOfficerUserName
	 *            the username of the credit officer to whom the CLients are assigned to
	 * @param clientState
	 *            the desired state of a Client to filter on (eg: ACTIVE)
	 * 
	 * @return the list of Clients matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	// TODO: test filtering by centreId with Mambu 3.7, See MBU-5946 @ https://mambucom.jira.com/browse/MBU-5946
	public List<Client> getClientsByBranchCentreOfficerState(String branchId, String centreId,
			String creditOfficerUserName, String clientState, String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam(BRANCH_ID, branchId);
		params.addParam(CENTRE_ID, centreId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.addParam(CLIENT_STATE, clientState);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		return serviceHelper.execute(getClientsList, params);
	}

	/***
	 * Get Clients by branch id, credit officer, clientState
	 * 
	 * @param branchId
	 *            the ID of the Client's branch
	 * @param creditOfficerUserName
	 *            the username of the credit officer to whom the CLients are assigned to
	 * @param clientState
	 *            the desired state of a Client to filter on (eg: ACTIVE)
	 * 
	 * @return the list of Clients matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	public List<Client> getClientsByBranchOfficerState(String branchId, String creditOfficerUserName,
			String clientState, String offset, String limit) throws MambuApiException {
		String centreId = null;
		return getClientsByBranchCentreOfficerState(branchId, centreId, creditOfficerUserName, clientState, offset,
				limit);
	}

	/**
	 * Requests a list of clients for a custom view, limited by offset/limit
	 * 
	 * @param customViewKey
	 *            the id of the Custom View to filter clients
	 * @param offset
	 *            pagination offset. If not null the must be an integer greater or equal to zero
	 * 
	 * @param limit
	 *            pagination limit. If not null the must be an integer greater than zero
	 * 
	 * @return the list of Mambu clients
	 * 
	 * @throws MambuApiException
	 */
	public List<Client> getClientsByCustomView(String customViewKey, String offset, String limit)
			throws MambuApiException {
		return serviceHelper.getEntitiesByCustomView(getClientsList, customViewKey, offset, limit);

	}

	/**
	 * Requests a list of groups for a custom view, limited by offset/limit
	 * 
	 * @param customViewKey
	 *            the key of the Custom View to filter groups
	 * @param offset
	 *            pagination offset. If not null the must be an integer greater or equal to zero
	 * 
	 * @param limit
	 *            pagination limit. If not null the must be an integer greater than zero
	 * 
	 * @return the list of Mambu groups
	 * 
	 * @throws MambuApiException
	 */
	public List<Group> getGroupsByCustomView(String customViewKey, String offset, String limit)
			throws MambuApiException {
		return serviceHelper.getEntitiesByCustomView(getGroupsList, customViewKey, offset, limit);

	}

	/***
	 * Get Groups by branch id, credit officer
	 * 
	 * @param branchId
	 *            the ID of the Group's branch
	 * @param centreId
	 *            The ID of the centre to which the loan accounts are assigned to. If both branchId and centreId are
	 *            provided then this centre must be assigned to the branchId
	 * @param creditOfficerUserName
	 *            the username of the credit officer to whom the Groups are assigned to
	 * 
	 * @return the list of Groups matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	// TODO: test filtering by centreId with Mambu 3.7, See MBU-5946 @ https://mambucom.jira.com/browse/MBU-5946
	public List<Group> getGroupsByBranchCentreOfficer(String branchId, String centreId, String creditOfficerUserName,
			String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam(BRANCH_ID, branchId);
		params.addParam(CENTRE_ID, centreId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		return serviceHelper.execute(getGroupsList, params);
	}

	/***
	 * Get Groups by branch id, credit officer
	 * 
	 * @param branchId
	 *            the ID of the Group's branch
	 * @param creditOfficerUserName
	 *            the username of the credit officer to whom the Groups are assigned to
	 * 
	 * @return the list of Groups matching these parameters
	 * 
	 * @throws MambuApiException
	 */

	public List<Group> getGroupsByBranchOfficer(String branchId, String creditOfficerUserName, String offset,
			String limit) throws MambuApiException {
		String centreId = null;
		return getGroupsByBranchCentreOfficer(branchId, centreId, creditOfficerUserName, offset, limit);
	}

	/***
	 * Get all documents for a specific Client
	 * 
	 * @param clientId
	 *            the encoded key or id of the Mambu client for which attached documents are to be retrieved
	 * 
	 * @return documents attached to the entity
	 * 
	 * @throws MambuApiException
	 */
	public List<Document> getClientDocuments(String clientId) throws MambuApiException {
		return serviceHelper.execute(getClientDocuments, clientId);
	}

	/***
	 * Get all documents for a specific Group
	 * 
	 * @param groupId
	 *            the encoded key or id of the Mambu group for which attached documents are to be retrieved
	 * 
	 * @return documents attached to the entity
	 * 
	 * @throws MambuApiException
	 */
	public List<Document> getGroupDocuments(String groupId) throws MambuApiException {
		return serviceHelper.execute(getGroupDocuments, groupId);
	}
}
