/**
 * 
 */
package com.mambu.apisdk.services;

import java.util.List;

import com.google.inject.Inject;
import com.mambu.accounts.shared.model.AccountHolderType;
import com.mambu.api.server.handler.core.dynamicsearch.model.JSONFilterConstraints;
import com.mambu.api.server.handler.documents.model.JSONDocument;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.services.CustomViewsService.CustomViewResultType;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.ApiDefinition;
import com.mambu.apisdk.util.ApiDefinition.ApiType;
import com.mambu.apisdk.util.MambuEntityType;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.ServiceExecutor;
import com.mambu.apisdk.util.ServiceHelper;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;
import com.mambu.clients.shared.model.GroupRoleName;
import com.mambu.core.shared.model.ClientRole;
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

	private static String BIRTH_DATE = APIData.BIRTH_DATE;
	private static String ID_DOCUMENT = APIData.ID_DOCUMENT;

	private static final String BRANCH_ID = APIData.BRANCH_ID;
	public static final String CENTRE_ID = APIData.CENTRE_ID;
	private static final String CREDIT_OFFICER_USER_NAME = APIData.CREDIT_OFFICER_USER_NAME;
	private static final String CLIENT_STATE = APIData.CLIENT_STATE;
	private static final String FOR_TYPE = APIData.FOR;

	// Our serviceExecutor
	private ServiceExecutor serviceExecutor;

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
	private final static ApiDefinition updateClient = new ApiDefinition(ApiType.POST_ENTITY, ClientExpanded.class);
	// Create Group. POST JSON /api/groups
	private final static ApiDefinition createGroup = new ApiDefinition(ApiType.CREATE_JSON_ENTITY, GroupExpanded.class);
	// Update Group. PATCH JSON /api/groups/groupId
	private final static ApiDefinition updateGroup = new ApiDefinition(ApiType.POST_ENTITY, GroupExpanded.class);
	// Get Group Role Names. GET /api/grouprolenames/
	private final static ApiDefinition getGroupRoles = new ApiDefinition(ApiType.GET_LIST, GroupRoleName.class);
	// Get Group Role Name details. GET /api/grouprolenames/groupRoleNameId
	private final static ApiDefinition getGroupRole = new ApiDefinition(ApiType.GET_ENTITY_DETAILS, GroupRoleName.class);
	// Get Client Types. GET /host/api/clienttypes?for=CLIENTS
	private final static ApiDefinition getClientTypes = new ApiDefinition(ApiType.GET_LIST, ClientRole.class);
	// Groups
	private final static ApiDefinition getGroup = new ApiDefinition(ApiType.GET_ENTITY, Group.class);
	private final static ApiDefinition getGroupDetails = new ApiDefinition(ApiType.GET_ENTITY_DETAILS,
			GroupExpanded.class);
	// Get Lists of Groups
	private final static ApiDefinition getGroupsList = new ApiDefinition(ApiType.GET_LIST, Group.class);

	// Get Documents for a Client
	private final static ApiDefinition getClientDocuments = new ApiDefinition(ApiType.GET_OWNED_ENTITIES, Client.class,
			Document.class);
	// Post Client Profile Documents. POST clients/client_id/documents/PROFILE_PICTURE or
	// clients/client_id/documents/SIGNATURE
	private final static ApiDefinition postClientProfileFile = new ApiDefinition(ApiType.POST_OWNED_ENTITY,
			Client.class, Document.class, Boolean.class);
	// Get profile picture or signature file for a Client. GET /api/clients/{ID}/documents/PROFILE_PICTURE or GET
	// /api/clients/{ID}/documents/SIGNATURE
	private final static ApiDefinition getClientProfileFile = new ApiDefinition(ApiType.GET_OWNED_ENTITY, Client.class,
			Document.class, String.class);
	// Delete profile picture or signature for a Client. DELETE api/clients/client_id/documents/PROFILE_PICTURE
	// or DELETE api/clients/client_id/documents/SIGNATURE
	private final static ApiDefinition deleteClientProfileFile = new ApiDefinition(ApiType.DELETE_OWNED_ENTITY,
			Client.class, Document.class);
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
		this.serviceExecutor = new ServiceExecutor(mambuAPIService);
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
		return serviceExecutor.execute(getClient, clientId);
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

		return serviceExecutor.execute(getClientsList, params);
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

		return serviceExecutor.execute(getClientsList, params);

	}

	/**
	 * Requests a list of clients, limited by offset/limit
	 * 
	 * @param active
	 *            True if active Clients should retrieved, false for inactive Clients
	 * @param offset
	 *            Offset to start loading Clients, has to be >= 0 if not null. If null, Mambu default will be used
	 * @param limit
	 *            Limit of Clients to load, has to be > 0 if not null. If null, Mambu default will be used
	 * 
	 * @return the list of Mambu clients
	 * 
	 * @throws MambuApiException
	 */
	public List<Client> getClients(boolean active, Integer offset, Integer limit) throws MambuApiException {

		if ((offset != null && offset < 0) || (limit != null && limit < 1)) {
			throw new MambuApiException(new IllegalArgumentException("Offset has to be >= 0, limit has to be > 0"));
		}
		ParamsMap params = new ParamsMap();
		params.addParam(CLIENT_STATE, (active ? APIData.ACTIVE : APIData.INACTIVE));
		if (offset != null) {
			params.addParam(APIData.OFFSET, String.valueOf(offset));
		}
		if (limit != null) {
			params.addParam(APIData.LIMIT, String.valueOf(limit));
		}

		return serviceExecutor.execute(getClientsList, params);

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

		return serviceExecutor.execute(getClientsList, params);

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
		return serviceExecutor.execute(getClientDetails, clientId);
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
		return serviceExecutor.execute(getGroup, groupId);
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
		return serviceExecutor.execute(getGroupDetails, groupId);
	}

	/***
	 * Create a new client (expanded) using ClientExpanded object and sending it as a JSON api. This API allows creating
	 * Client with more details, including creating custom fields.
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

		return serviceExecutor.executeJson(createClient, clientDetails);
	}

	/***
	 * Update an existent client (expanded) using ClientExpanded object and send it as a JSON api. This API allows
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
		return serviceExecutor.executeJson(updateClient, clientDetails, encodedKey);
	}

	/***
	 * Create a new group using GroupExpanded object and sending it as a JSON api. This API allows creating a new Group
	 * with group details, group members, group roles, custom fields, and group address
	 * 
	 * Available since Mambu 3.8.10. See MBU-7336
	 * 
	 * @param groupDetails
	 *            The encodedKey for the groupDetails must be null to create a new group
	 * 
	 * @return group details for the newly created group
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public GroupExpanded createGroup(GroupExpanded groupDetails) throws MambuApiException {

		// Get encodedKey and ensure it's NULL for the new group request
		String encodedKey = groupDetails.getEncodedKey();
		if (encodedKey != null) {
			throw new IllegalArgumentException("Cannot create group, the encoded key must be null");
		}
		return serviceExecutor.executeJson(createGroup, groupDetails);

	}

	/***
	 * Update an existent group using GroupExpanded object and send it as a JSON api. This API allows updating Group
	 * with new details, including modifying group details, group members, group roles, custom fields and group address
	 * 
	 * @param groupDetails
	 *            group details to be updated. The encodedKey for the groupDetails object must be NOT null to update an
	 *            existent group.
	 * 
	 * @return updated group details
	 * 
	 * @throws MambuApiException
	 * @throws IllegalArgumentException
	 */
	public GroupExpanded updateGroup(GroupExpanded groupDetails) throws MambuApiException {
		// Available since Mambu 3.10. See MBU-7337
		// POST groupJSON /api/groups/groupId

		// Verify that the encodedKey for this object is not NULL
		String encodedKey = groupDetails.getEncodedKey();
		if (encodedKey == null) {
			throw new IllegalArgumentException("Cannot update group, the encoded key for the object does not exist");
		}

		return serviceExecutor.executeJson(updateGroup, groupDetails, encodedKey);

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
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return the list of Clients matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	public List<Client> getClientsByBranchCentreOfficerState(String branchId, String centreId,
			String creditOfficerUserName, String clientState, String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam(BRANCH_ID, branchId);
		params.addParam(CENTRE_ID, centreId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.addParam(CLIENT_STATE, clientState);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		return serviceExecutor.execute(getClientsList, params);
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
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
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
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return the list of Mambu clients
	 * 
	 * @throws MambuApiException
	 */
	public List<Client> getClientsByCustomView(String customViewKey, String offset, String limit)
			throws MambuApiException {

		String branchId = null;
		String centreId = null;
		String creditOfficerName = null;
		CustomViewResultType resultType = CustomViewResultType.BASIC;

		ParamsMap params = CustomViewsService.makeParamsForGetByCustomView(customViewKey, resultType, branchId,
				centreId, creditOfficerName, offset, limit);
		return serviceExecutor.execute(getClientsList, params);

	}

	/**
	 * Get clients by specifying filter constraints
	 * 
	 * @param filterConstraints
	 *            filter constraints. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return list of clients matching filter constraint
	 * @throws MambuApiException
	 */
	public List<Client> getClients(JSONFilterConstraints filterConstraints, String offset, String limit)
			throws MambuApiException {
		// Available since Mambu 3.12. See MBU-8975 for more details
		// POST {JSONFilterConstraints} /api/clients/search?offset=0&limit=5

		ApiDefinition apiDefintition = SearchService.makeApiDefinitionforSearchByFilter(MambuEntityType.CLIENT);

		// POST Filter JSON with pagination params map
		return serviceExecutor.executeJson(apiDefintition, filterConstraints, null, null,
				ServiceHelper.makePaginationParams(offset, limit));

	}

	/**
	 * Requests a list of groups for a custom view, limited by offset/limit
	 * 
	 * @param customViewKey
	 *            the key of the Custom View to filter groups
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return the list of Mambu groups
	 * 
	 * @throws MambuApiException
	 */
	public List<Group> getGroupsByCustomView(String customViewKey, String offset, String limit)
			throws MambuApiException {
		String branchId = null;
		String centreId = null;
		String creditOfficerName = null;
		CustomViewResultType resultType = CustomViewResultType.BASIC;

		ParamsMap params = CustomViewsService.makeParamsForGetByCustomView(customViewKey, resultType, branchId,
				centreId, creditOfficerName, offset, limit);
		return serviceExecutor.execute(getGroupsList, params);

	}

	/**
	 * Get groups by specifying filter constraints
	 * 
	 * @param filterConstraints
	 *            filter constraints. Must not be null
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return list of groups matching filter constraint.
	 * @throws MambuApiException
	 */
	public List<Group> getGroups(JSONFilterConstraints filterConstraints, String offset, String limit)
			throws MambuApiException {
		// Available since Mambu 3.12. See MBU-8987 for more details
		// POST {JSONFilterConstraints} /api/groups/search?offset=0&limit=5

		ApiDefinition apiDefintition = SearchService.makeApiDefinitionforSearchByFilter(MambuEntityType.GROUP);

		// POST Filter JSON with pagination params map
		return serviceExecutor.executeJson(apiDefintition, filterConstraints, null, null,
				ServiceHelper.makePaginationParams(offset, limit));

	}

	/**
	 * Requests a list of group role names
	 * 
	 * This API doesn't accept pagination parameters (offset and limit ) and returns all group role names
	 * 
	 * @return the list of Mambu group role names
	 * 
	 * @throws MambuApiException
	 */
	public List<GroupRoleName> getGroupRoleNames() throws MambuApiException {
		// Example GET /api/grouprolenames/
		// Available since 3.9, see MBU-7351. The API returns a list of GroupRoleName

		return serviceExecutor.execute(getGroupRoles);

	}

	/**
	 * Requests details for the group role name by id
	 * 
	 * @param groupRoleNameId
	 *            group role name id or encoded key. Must be not null
	 * @return group role details
	 * 
	 * @throws MambuApiException
	 */
	public GroupRoleName getGroupRoleName(String groupRoleNameId) throws MambuApiException {
		// Example GET /api/grouprolenames/groupRoleNameId
		// Available since 3.9, see MBU-7351

		return serviceExecutor.execute(getGroupRole, groupRoleNameId);

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
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
	 * 
	 * @return the list of Groups matching these parameters
	 * 
	 * @throws MambuApiException
	 */
	public List<Group> getGroupsByBranchCentreOfficer(String branchId, String centreId, String creditOfficerUserName,
			String offset, String limit) throws MambuApiException {

		ParamsMap params = new ParamsMap();
		params.addParam(BRANCH_ID, branchId);
		params.addParam(CENTRE_ID, centreId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		return serviceExecutor.execute(getGroupsList, params);
	}

	/***
	 * Get Groups by branch id, credit officer
	 * 
	 * @param branchId
	 *            the ID of the Group's branch
	 * @param creditOfficerUserName
	 *            the username of the credit officer to whom the Groups are assigned to
	 * @param offset
	 *            pagination offset. If not null it must be an integer greater or equal to zero
	 * @param limit
	 *            pagination limit. If not null it must be an integer greater than zero
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
	 * @deprecated Starting from 3.14 use
	 *             {@link DocumentsService#getDocuments(MambuEntityType, String, Integer, Integer)}. This methods
	 *             supports pagination parameters
	 * @param clientId
	 *            the encoded key or id of the Mambu client for which attached documents are to be retrieved
	 * 
	 * @return documents attached to the entity
	 * 
	 * @throws MambuApiException
	 */
	public List<Document> getClientDocuments(String clientId) throws MambuApiException {
		return serviceExecutor.execute(getClientDocuments, clientId);
	}

	/***
	 * Get all documents for a specific Group
	 * 
	 * @deprecated Starting from 3.14 use
	 *             {@link DocumentsService#getDocuments(MambuEntityType, String, Integer, Integer)}. This methods
	 *             supports pagination parameters
	 * @param groupId
	 *            the encoded key or id of the Mambu group for which attached documents are to be retrieved
	 * 
	 * @return documents attached to the entity
	 * 
	 * @throws MambuApiException
	 */
	public List<Document> getGroupDocuments(String groupId) throws MambuApiException {
		return serviceExecutor.execute(getGroupDocuments, groupId);
	}

	/***
	 * Get client types for clients or for groups
	 * 
	 * @param clientType
	 *            the account holder type (CLIENT or GROUP). Must be not null
	 * @return client types
	 * @throws MambuApiException
	 */
	public List<ClientRole> getClientTypes(AccountHolderType clientType) throws MambuApiException {
		// Example GET /host/api/clienttypes?for=CLIENTS GET /host/api/clienttypes?for=GROUPS
		// See MBU-7061 for more details

		if (clientType == null) {
			throw new IllegalArgumentException("Client Type must not be null");
		}
		ParamsMap params = new ParamsMap();
		String clientTypeParam = (clientType == AccountHolderType.CLIENT) ? APIData.CLIENTS : APIData.GROUPS;
		params.addParam(FOR_TYPE, clientTypeParam);

		return serviceExecutor.execute(getClientTypes, params);
	}

	/***
	 * Get client types for both clients and groups
	 * 
	 * @return all client types defined in Mambu for both clients and groups
	 * @throws MambuApiException
	 */
	public List<ClientRole> getClientTypes() throws MambuApiException {
		// Example GET /host/api/clienttypes
		// See MBU-7061 for more details

		ParamsMap params = null;
		return serviceExecutor.execute(getClientTypes, params);
	}

	/***
	 * Get client profile picture API response message containing image type indicator and the base64 encoded picture
	 * file
	 * 
	 * API response message format: "data:image/jpg;base64,/9j/4AAQSkZJRgABAgAA..."
	 * 
	 * @param clientId
	 *            the encoded key or id of the Mambu Client
	 * @return API response string
	 * @throws MambuApiException
	 */
	public String getClientProfilePicture(String clientId) throws MambuApiException {
		// Example. GET /api/clients/{ID}/documents/PROFILE_PICTURE
		// See MBU-7312 for details
		final String documentType = APIData.PROFILE_PICTURE;
		String apiResponse = serviceExecutor.execute(getClientProfileFile, clientId, documentType, null);

		return apiResponse;

	}

	/***
	 * Convenience method to get get client profile picture file content only (base64 encoded)
	 * 
	 * @param clientId
	 *            the encoded key or id of the Mambu Client
	 * @return picture file as a Base64 string
	 * @throws MambuApiException
	 */
	public String getClientProfilePictureFile(String clientId) throws MambuApiException {
		// Example. GET /api/clients/{ID}/documents/PROFILE_PICTURE
		// See MBU-7312 for details
		String apiResponse = getClientProfilePicture(clientId);

		// return just the image content part (i.e. without the base64 indicator)
		return ServiceHelper.getContentForBase64EncodedMessage(apiResponse);

	}

	/***
	 * Get client signature API response message containing image type indicator and the base64 encoded signature file
	 * 
	 * API response message format: "data:image/PNG;base64,iVBORw0KGgoAAAANSUhE..."
	 * 
	 * @param clientId
	 *            the encoded key or id of the Mambu Client
	 * 
	 * @return API response string
	 * @throws MambuApiException
	 */
	public String getClientSignature(String clientId) throws MambuApiException {
		// Example. GET /api/clients/{ID}/documents/SIGNATURE
		// See MBU-7313 for details
		final String documentType = APIData.SIGNATURE;
		String apiResponse = serviceExecutor.execute(getClientProfileFile, clientId, documentType, null);

		// return api response as is
		return apiResponse;

	}

	/***
	 * Convenience method to get get client signature file content only (base64 encoded)
	 * 
	 * @param clientId
	 *            the encoded key or id of the Mambu Client
	 * 
	 * @return signature file as a Base64 string
	 * @throws MambuApiException
	 */
	public String getClientSignatureFile(String clientId) throws MambuApiException {
		// Example. GET /api/clients/{ID}/documents/SIGNATURE
		// See MBU-7313 for details
		// Response message format: "data:image/PNG;base64,iVBORw0KGgoAAAANSUhE..."
		String apiResponse = getClientSignature(clientId);

		// return just the image content part (i.e. without the base64 indicator)
		return ServiceHelper.getContentForBase64EncodedMessage(apiResponse);

	}

	/****
	 * Upload client profile picture file
	 * 
	 * @param pictureDocument
	 *            JSON document whose content is base64 encoded profile picture file
	 * 
	 * @return success or failure
	 * 
	 * @throws MambuApiException
	 */
	public boolean uploadClientProfilePicture(String clientId, JSONDocument pictureDocument) throws MambuApiException {
		// Upload client profile picture. See MBU-7312 for details
		// Example: POST JSON "{"document":{"name":"Client
		// Photo", "type":"jpeg"}, "documentContent":"base64encodedString"}" api/clients/{ID}/documents/PROFILE_PICTURE
		// Returns {"returnCode":0,"returnStatus":"SUCCESS"}.

		if (pictureDocument == null) {
			throw new IllegalArgumentException("Document cannot be null");
		}
		// Make JSON document and add it to the ParamsMap
		ParamsMap paramsMap = ServiceHelper.makeParamsForDocumentJson(pictureDocument);

		// Update ApiDefintion (we need to use JSON content)
		postClientProfileFile.setContentType(ContentType.JSON);

		// Execute with PROFILE_PICTURE as an api endpoint
		final String documentType = APIData.PROFILE_PICTURE;
		return serviceExecutor.execute(postClientProfileFile, clientId, documentType, paramsMap);
	}

	/****
	 * Upload client signature file
	 * 
	 * @param signatureDocument
	 *            JSON document whose content is base64 encoded signature file
	 * 
	 * @return success or failure
	 * 
	 * @throws MambuApiException
	 */
	public boolean uploadClientSignatureFile(String clientId, JSONDocument signatureDocument) throws MambuApiException {
		// Upload client profile signature file.See MBU-7313 for details
		// Example: POST JSON {"document":{"name":"Client Signature",
		// "type":"png"}, "documentContent":"[base64encodedString]"} api/clients/\{ID\}/documents/SIGNATURE
		// Returns {"returnCode":0,"returnStatus":"SUCCESS"}.

		if (signatureDocument == null) {
			throw new IllegalArgumentException("Document cannot be null");
		}
		// Make JSON document and add it to the ParamsMap
		ParamsMap paramsMap = ServiceHelper.makeParamsForDocumentJson(signatureDocument);

		// Update ApiDefintion (we need to use JSON content type and it's set to WWW_FORM)
		postClientProfileFile.setContentType(ContentType.JSON);

		// Execute with SIGNATURE as an api endpoint
		final String documentType = APIData.SIGNATURE;
		return serviceExecutor.execute(postClientProfileFile, clientId, documentType, paramsMap);
	}

	/***
	 * Delete client profile picture file
	 * 
	 * @param clientId
	 *            the encoded key or id of the Mambu Client
	 * @return success or failure
	 * @throws MambuApiException
	 */
	public boolean deleteClientProfilePicture(String clientId) throws MambuApiException {
		// Example. DELETE /api/clients/{ID}/documents/PROFILE_PICTURE
		// See MBU-7312 for details
		final String documentType = APIData.PROFILE_PICTURE;
		return serviceExecutor.execute(deleteClientProfileFile, clientId, documentType, null);

	}

	/***
	 * Delete client signature file
	 * 
	 * @param clientId
	 *            the encoded key or id of the Mambu Client
	 * @return a boolean indicating if deletion was successful
	 * @throws MambuApiException
	 */
	//
	public boolean deleteClientSignatureFile(String clientId) throws MambuApiException {
		// e.g. DELETE /api/clients/{ID}/documents/SIGNATURE
		// See MBU-7313 for details
		final String documentType = APIData.SIGNATURE;
		return serviceExecutor.execute(deleteClientProfileFile, clientId, documentType, null);

	}
}
