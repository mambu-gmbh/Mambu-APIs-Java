/**
 * 
 */
package com.mambu.apisdk.services;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.mambu.apisdk.MambuAPIService;
import com.mambu.apisdk.exception.MambuApiException;
import com.mambu.apisdk.util.APIData;
import com.mambu.apisdk.util.GsonUtils;
import com.mambu.apisdk.util.ParamsMap;
import com.mambu.apisdk.util.RequestExecutor.ContentType;
import com.mambu.apisdk.util.RequestExecutor.Method;
import com.mambu.clients.shared.model.Client;
import com.mambu.clients.shared.model.ClientExpanded;
import com.mambu.clients.shared.model.Group;
import com.mambu.clients.shared.model.GroupExpanded;

/**
 * Service class which handles API operations like getting and creating clients and groups of clients
 * 
 * @author ipenciuc
 * 
 */
public class ClientsService {

	private MambuAPIService mambuAPIService;

	private static String CLIENTS = APIData.CLIENTS;
	private static String GROUPS = APIData.GROUPS;

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
	private static final String CREDIT_OFFICER_USER_NAME = APIData.CREDIT_OFFICER_USER_NAME;
	private static final String CLIENT_STATE = APIData.CLIENT_STATE;

	/***
	 * Create a new client service
	 * 
	 * @param mambuAPIService
	 *            the service responsible with the connection to the server
	 */
	@Inject
	public ClientsService(MambuAPIService mambuAPIService) {
		this.mambuAPIService = mambuAPIService;
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

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/" + clientId));
		String jsonResposne = mambuAPIService.executeRequest(urlString, Method.GET);

		Client clientResult = GsonUtils.createGson().fromJson(jsonResposne, Client.class);

		return clientResult;

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

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS));

		ParamsMap params = new ParamsMap();
		params.put(LAST_NAME, clientLastName);
		params.put(FIRST_NAME, clientFirstName);

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<Client>>() {}.getType();
		List<Client> clients = GsonUtils.createGson().fromJson(jsonResponse, collectionType);

		return clients;

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

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS));

		ParamsMap params = new ParamsMap();
		params.put(LAST_NAME, clientLastName);
		params.put(BIRTH_DATE, birthDay);

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<Client>>() {}.getType();
		List<Client> clients = GsonUtils.createGson().fromJson(jsonResponse, collectionType);

		return clients;

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

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS));

		ParamsMap params = new ParamsMap();

		params.addParam(CLIENT_STATE, (active ? "ACTIVE" : "INACTIVE"));

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<Client>>() {}.getType();
		List<Client> clients = GsonUtils.createGson().fromJson(jsonResponse, collectionType);

		return clients;

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
		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS));

		ParamsMap params = new ParamsMap();

		if ((offset < 0) || (limit < 1)) {
			throw new MambuApiException(new IllegalArgumentException("Offset has to be >= 0, limit has to be > 0"));
		} else {
			params.addParam(CLIENT_STATE, (active ? "ACTIVE" : "INACTIVE"));
			params.addParam(APIData.OFFSET, String.valueOf(offset));
			params.addParam(APIData.LIMIT, String.valueOf(limit));

			String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

			Type collectionType = new TypeToken<List<Client>>() {}.getType();
			List<Client> clients = GsonUtils.createGson().fromJson(jsonResponse, collectionType);

			return clients;
		}
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

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS));

		ParamsMap params = new ParamsMap();

		params.put(LAST_NAME, clientLastName);
		params.put(ID_DOCUMENT, documentId);

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<Client>>() {}.getType();
		List<Client> clients = GsonUtils.createGson().fromJson(jsonResponse, collectionType);

		return clients;

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

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/" + clientId));
		ParamsMap params = new ParamsMap();
		params.put(APIData.FULL_DETAILS, "true");

		String jsonResposne = mambuAPIService.executeRequest(urlString, params, Method.GET);

		ClientExpanded clientResult = GsonUtils.createGson().fromJson(jsonResposne, ClientExpanded.class);

		return clientResult;

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

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(GROUPS + "/" + groupId));
		String jsonResponse = mambuAPIService.executeRequest(urlString, Method.GET);

		Group groupResult = GsonUtils.createGson().fromJson(jsonResponse, Group.class);
		return groupResult;

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

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(GROUPS + "/" + groupId));
		ParamsMap params = new ParamsMap();
		params.put(APIData.FULL_DETAILS, "true");

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		GroupExpanded groupResult = GsonUtils.createGson().fromJson(jsonResponse, GroupExpanded.class);
		return groupResult;

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
		// Convert ClientExpanded object into json string using specific date
		// time format
		final String dateTimeFormat = APIData.yyyyMmddFormat;
		String jsonClient = GsonUtils.createGson(dateTimeFormat).toJson(clientDetails, ClientExpanded.class);

		ParamsMap params = new ParamsMap();
		// Add json string as JSON_OBJECT
		params.put(APIData.JSON_OBJECT, jsonClient);

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/"));

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.POST, ContentType.JSON);

		ClientExpanded clientResult = GsonUtils.createGson().fromJson(jsonResponse, ClientExpanded.class);

		return clientResult;
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
		// Convert ClientExpanded object into json string using specific date
		// time format
		final String dateTimeFormat = APIData.yyyyMmddFormat;
		String jsonClient = GsonUtils.createGson(dateTimeFormat).toJson(clientDetails, ClientExpanded.class);

		ParamsMap params = new ParamsMap();
		// Add json string as JSON_OBJECT
		params.put(APIData.JSON_OBJECT, jsonClient);

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/"));

		String jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.POST, ContentType.JSON);

		ClientExpanded clientResult = GsonUtils.createGson().fromJson(jsonResponse, ClientExpanded.class);

		return clientResult;
	}
	/***
	 * Create a new client with only it's first name and last name
	 * 
	 * @param firstName
	 * @param lastName
	 * 
	 * @return Client object returned by Mambu
	 * 
	 * @throws MambuApiException
	 */
	public Client createClient(String firstName, String lastName) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/"));
		ParamsMap params = new ParamsMap();
		params.put(FIRST_NAME, firstName);
		params.put(LAST_NAME, lastName);

		String jsonResposne = mambuAPIService.executeRequest(urlString, params, Method.POST);

		Client clientResult = GsonUtils.createGson().fromJson(jsonResposne, Client.class);
		return clientResult;
	}

	/***
	 * Create a new client with contact details
	 * 
	 * @param firstName
	 * @param lastName
	 * @param homephone
	 * @param mobilephone
	 * @param gender
	 * @param birthdate
	 * @param email
	 * @param notes
	 * 
	 * @return Client object returned by Mambu
	 * 
	 * @throws MambuApiException
	 */
	public Client createClient(String firstName, String lastName, String homephone, String mobilephone, String gender,
			String birthdate, String email, String notes) throws MambuApiException {

		// create the api call
		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/"));

		ParamsMap params = new ParamsMap();
		params.put(FIRST_NAME, firstName);
		params.put(LAST_NAME, lastName);
		params.put(HOME_PHONE, homephone); // params.put("HOME_PHONE",
											// homephone);
		params.put(MOBILE_PHONE, mobilephone);
		params.put(GENDER, gender);
		params.put(BIRTH_DATE, birthdate);
		params.put(EMAIL_ADDRESS, email);
		params.put(NOTES, notes); //

		String jsonResposne = mambuAPIService.executeRequest(urlString, params, Method.POST);
		Client clientResult = GsonUtils.createGson().fromJson(jsonResposne, Client.class);

		return clientResult;
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
	@SuppressWarnings("unchecked")
	public List<Client> getClientsByBranchOfficerState(String branchId, String creditOfficerUserName,
			String clientState, String offset, String limit) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(CLIENTS + "/"));

		ParamsMap params = new ParamsMap();
		params.addParam(BRANCH_ID, branchId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.addParam(CLIENT_STATE, clientState);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		String jsonResponse;

		jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<Client>>() {}.getType();

		List<Client> clients = (List<Client>) GsonUtils.createGson().fromJson(jsonResponse, collectionType);
		return clients;
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
	@SuppressWarnings("unchecked")
	public List<Group> getGroupsByBranchOfficer(String branchId, String creditOfficerUserName, String offset,
			String limit) throws MambuApiException {

		String urlString = new String(mambuAPIService.createUrl(GROUPS + "/"));

		ParamsMap params = new ParamsMap();
		params.addParam(BRANCH_ID, branchId);
		params.addParam(CREDIT_OFFICER_USER_NAME, creditOfficerUserName);
		params.put(APIData.OFFSET, offset);
		params.put(APIData.LIMIT, limit);

		String jsonResponse;

		jsonResponse = mambuAPIService.executeRequest(urlString, params, Method.GET);

		Type collectionType = new TypeToken<List<Group>>() {}.getType();

		List<Group> groups = (List<Group>) GsonUtils.createGson().fromJson(jsonResponse, collectionType);
		return groups;
	}
}